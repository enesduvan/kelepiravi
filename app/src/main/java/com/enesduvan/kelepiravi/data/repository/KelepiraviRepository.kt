package com.enesduvan.kelepiravi.data.repository

import com.enesduvan.kelepiravi.data.local.dao.KelepiraviDao
import com.enesduvan.kelepiravi.data.local.entity.DEFAULT_USER_ID
import com.enesduvan.kelepiravi.data.local.entity.INITIAL_BALANCE
import com.enesduvan.kelepiravi.data.local.entity.UserInventoryEntity
import com.enesduvan.kelepiravi.data.market.DailyEvent
import com.enesduvan.kelepiravi.data.market.EconomyEngine
import com.enesduvan.kelepiravi.data.model.MarketItem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class KelepiraviRepository(private val dao: KelepiraviDao) {

    fun getPlayerState(): Flow<List<UserInventoryEntity>> = dao.getAllInventories()

    suspend fun initializePlayerIfNeeded() {
        if (dao.getInventoryById(DEFAULT_USER_ID) == null) {
            dao.insertInventory(
                UserInventoryEntity(
                    id = DEFAULT_USER_ID,
                    balance = INITIAL_BALANCE,
                    inventory = emptyList(),
                    currentDay = 1
                )
            )
        }
    }

    /**
     * Satın alma: purchasePrice ve purchaseDate damgalanır, dailyChangePercent sıfırlanır.
     */
    suspend fun purchaseItem(item: MarketItem): Boolean {
        initializePlayerIfNeeded()
        val player = dao.getInventoryById(DEFAULT_USER_ID) ?: return false
        val currentBalance = player.balance.toDoubleOrNull() ?: 0.0
        val itemPrice = item.salesValue.toDoubleOrNull() ?: 0.0
        if (currentBalance < itemPrice) return false

        val enrichedItem = item.copy(
            purchasePrice = item.salesValue,
            purchaseDate = LocalDate.now().toString(),
            dailyChangePercent = 0.0  // Yeni alınan ürünün henüz günlük değişimi yok
        )
        dao.updateInventory(
            player.copy(
                balance = (currentBalance - itemPrice).toString(),
                inventory = player.inventory + enrichedItem
            )
        )
        return true
    }

    /**
     * Satış: kondisyon katsayılı fiyat hesaplanır, ürün envanterden çıkar.
     */
    suspend fun sellItem(item: MarketItem): Boolean {
        val player = dao.getInventoryById(DEFAULT_USER_ID) ?: return false
        val itemInInventory = player.inventory.find { it == item } ?: return false
        val sellPrice = calculateSellPrice(itemInInventory)
        val currentBalance = player.balance.toDoubleOrNull() ?: 0.0
        dao.updateInventory(
            player.copy(
                balance = (currentBalance + sellPrice).toString(),
                inventory = player.inventory - itemInInventory
            )
        )
        return true
    }

    /**
     * Satış fiyatı = tahmini değer × kondisyon katsayısı.
     */
    fun calculateSellPrice(item: MarketItem): Double {
        val estimated = item.estimatedValue.toDoubleOrNull() ?: 0.0
        val multiplier = when {
            item.condition.contains("Kusursuz") || item.condition.contains("Temiz") -> 1.00
            item.condition.contains("Hafif")    -> 0.82
            item.condition.contains("Orta")     -> 0.65
            item.condition.contains("Kırık") || item.condition.contains("Arızalı") -> 0.40
            item.condition.contains("Bantlı") || item.condition.contains("Tamir")  -> 0.25
            else -> 0.70
        }
        return ((estimated * multiplier) * 100.0).toLong() / 100.0
    }

    /**
     * Yeni gün işlemi:
     * 1. Envanter fiyatları EconomyEngine tarafından güncellenir.
     * 2. Gün sayacı artar.
     * 3. Tetiklenen olay (varsa) döner.
     */
    suspend fun advanceDay(): DailyEvent? {
        val player = dao.getInventoryById(DEFAULT_USER_ID) ?: return null
        val (updatedInventory, event) = EconomyEngine.processNewDay(
            currentDay = player.currentDay,
            inventory = player.inventory
        )
        dao.updateInventory(
            player.copy(
                currentDay = player.currentDay + 1,
                inventory = updatedInventory
            )
        )
        return event
    }
}

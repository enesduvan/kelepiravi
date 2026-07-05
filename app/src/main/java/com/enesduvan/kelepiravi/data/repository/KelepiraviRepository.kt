package com.enesduvan.kelepiravi.data.repository

import com.enesduvan.kelepiravi.data.local.dao.KelepiraviDao
import com.enesduvan.kelepiravi.data.local.entity.DEFAULT_USER_ID
import com.enesduvan.kelepiravi.data.local.entity.INITIAL_BALANCE
import com.enesduvan.kelepiravi.data.local.entity.UserInventoryEntity
import com.enesduvan.kelepiravi.data.market.AchievementManager
import com.enesduvan.kelepiravi.data.market.DailyEvent
import com.enesduvan.kelepiravi.data.market.EconomyEngine
import com.enesduvan.kelepiravi.data.model.MarketItem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class KelepiraviRepository(private val dao: KelepiraviDao) {

    fun getPlayerState(): Flow<List<UserInventoryEntity>> = dao.getAllInventories()

    private fun getRequiredXpForLevel(level: Int): Int {
        return (level - 1) * level * 50
    }

    private fun processXpGain(player: UserInventoryEntity, xpGain: Int): UserInventoryEntity {
        var newXp = player.xp + xpGain
        var newLevel = player.level
        while (newXp >= getRequiredXpForLevel(newLevel + 1)) {
            newLevel++
        }
        return player.copy(xp = newXp, level = newLevel)
    }

    private fun processAchievements(player: UserInventoryEntity): UserInventoryEntity {
        val unlockedList = if (player.unlockedAchievements.isEmpty()) emptyList() else player.unlockedAchievements.split(",")
        val newAchievements = AchievementManager.checkAchievements(
            itemsBought = player.itemsBought,
            itemsSold = player.itemsSold,
            currentDay = player.currentDay,
            totalRepairs = 0, // could track repairs later
            unlockedIds = unlockedList
        )
        
        if (newAchievements.isEmpty()) return player
        
        var currentBalance = player.balance.toDoubleOrNull() ?: 0.0
        var totalXpReward = 0
        val newIds = mutableListOf<String>()
        
        for (ach in newAchievements) {
            currentBalance += ach.rewardMoney
            totalXpReward += ach.rewardXp
            newIds.add(ach.id)
        }
        
        val newUnlockedString = if (player.unlockedAchievements.isEmpty()) newIds.joinToString(",") else player.unlockedAchievements + "," + newIds.joinToString(",")
        val playerWithMoney = player.copy(balance = currentBalance.toString(), unlockedAchievements = newUnlockedString)
        
        return processXpGain(playerWithMoney, totalXpReward)
    }

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

    suspend fun updateInventoryItem(oldItem: MarketItem, newItem: MarketItem, cost: Double): Boolean {
        val player = dao.getInventoryById(DEFAULT_USER_ID) ?: return false
        val currentBalance = player.balance.toDoubleOrNull() ?: 0.0
        if (currentBalance < cost) return false
        
        val newInventory = player.inventory.toMutableList()
        val index = newInventory.indexOfFirst { it.itemName == oldItem.itemName && it.sellerName == oldItem.sellerName && it.purchaseDate == oldItem.purchaseDate }
        if (index != -1) {
            newInventory[index] = newItem
            val basePlayer = processXpGain(player, 15) // 15 XP for repairing
            val finalPlayer = processAchievements(basePlayer.copy(
                balance = (currentBalance - cost).toString(),
                inventory = newInventory
            ))
            dao.updateInventory(finalPlayer)
            return true
        }
        return false
    }

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
        val basePlayer = processXpGain(player, 10) // 10 XP for buying
        val finalPlayer = processAchievements(basePlayer.copy(
            balance = (currentBalance - itemPrice).toString(),
            inventory = basePlayer.inventory + enrichedItem,
            itemsBought = basePlayer.itemsBought + 1
        ))
        dao.updateInventory(finalPlayer)
        return true
    }

    suspend fun sellItem(item: MarketItem, agreedPrice: Double? = null): Boolean {
        val player = dao.getInventoryById(DEFAULT_USER_ID) ?: return false
        val itemInInventory = player.inventory.find { 
            it.itemName == item.itemName && it.sellerName == item.sellerName && it.purchaseDate == item.purchaseDate
        } ?: return false
        val sellPrice = agreedPrice ?: calculateSellPrice(itemInInventory)
        val purchasePrice = itemInInventory.purchasePrice.ifEmpty { itemInInventory.salesValue }.toDoubleOrNull() ?: 0.0
        val currentBalance = player.balance.toDoubleOrNull() ?: 0.0
        
        val profit = sellPrice - purchasePrice
        val xpGain = 10 + (profit * 0.01).coerceAtLeast(0.0).toInt() // Base 10 + 1 XP per 100 profit
        val basePlayer = processXpGain(player, xpGain)
        
        val finalPlayer = processAchievements(basePlayer.copy(
            balance = (currentBalance + sellPrice).toString(),
            inventory = basePlayer.inventory - itemInInventory,
            itemsSold = basePlayer.itemsSold + 1,
            totalProfit = basePlayer.totalProfit + profit
        ))
        
        dao.updateInventory(finalPlayer)
        return true
    }

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

    suspend fun advanceDay(): DailyEvent? {
        val player = dao.getInventoryById(DEFAULT_USER_ID) ?: return null
        val (updatedInventory, event) = EconomyEngine.processNewDay(
            currentDay = player.currentDay,
            inventory = player.inventory
        )
        
        // Günlük giriş bonusu (50 para, 20 XP)
        val currentBalance = player.balance.toDoubleOrNull() ?: 0.0
        val basePlayer = processXpGain(player, 20)
        
        val finalPlayer = processAchievements(basePlayer.copy(
            currentDay = player.currentDay + 1,
            inventory = updatedInventory,
            balance = (currentBalance + 50.0).toString()
        ))
        
        dao.updateInventory(finalPlayer)
        return event
    }
}

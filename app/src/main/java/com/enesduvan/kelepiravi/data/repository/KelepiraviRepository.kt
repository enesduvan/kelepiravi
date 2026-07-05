package com.enesduvan.kelepiravi.data.repository

import androidx.room.withTransaction
import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.data.local.AppDatabase
import com.enesduvan.kelepiravi.data.local.entity.UserInventoryEntity
import com.enesduvan.kelepiravi.data.market.AchievementManager
import com.enesduvan.kelepiravi.data.market.DailyEvent
import com.enesduvan.kelepiravi.data.market.EconomyEngine
import com.enesduvan.kelepiravi.data.market.MarketGenerator
import com.enesduvan.kelepiravi.data.model.MarketItem
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

class KelepiraviRepository(
    private val database: AppDatabase
) {
    private val dao = database.kelepiraviDao()

    fun getPlayerState(): Flow<List<UserInventoryEntity>> = dao.getAllInventories()

    private fun getRequiredXpForLevel(level: Int): Int {
        return (level - 1) * level * GameConstants.XP_LEVEL_FACTOR
    }

    private fun processXpGain(player: UserInventoryEntity, xpGain: Int): UserInventoryEntity {
        val newXp = player.xp + xpGain
        var newLevel = player.level
        while (newXp >= getRequiredXpForLevel(newLevel + 1)) {
            newLevel++
        }
        return player.copy(xp = newXp, level = newLevel)
    }

    private fun processAchievements(player: UserInventoryEntity): UserInventoryEntity {
        val unlockedList = player.unlockedAchievements
            .takeIf { it.isNotEmpty() }
            ?.split(",")
            ?: emptyList()

        val newAchievements = AchievementManager.checkAchievements(
            itemsBought = player.itemsBought,
            itemsSold = player.itemsSold,
            currentDay = player.currentDay,
            totalRepairs = 0,
            unlockedIds = unlockedList
        )

        if (newAchievements.isEmpty()) return player

        val rewardedBalance = player.balance.toDoubleOrNull().orZero() +
            newAchievements.sumOf { it.rewardMoney }
        val rewardedXp = newAchievements.sumOf { it.rewardXp }
        val newIds = newAchievements.joinToString(",") { it.id }
        val unlockedAchievements = listOf(player.unlockedAchievements, newIds)
            .filter { it.isNotEmpty() }
            .joinToString(",")

        return processXpGain(
            player.copy(
                balance = rewardedBalance.toString(),
                unlockedAchievements = unlockedAchievements
            ),
            rewardedXp
        )
    }

    suspend fun initializePlayerIfNeeded() {
        database.withTransaction {
            getPlayerOrCreate()
        }
    }

    suspend fun updateInventoryItem(oldItem: MarketItem, newItem: MarketItem, cost: Double): Boolean {
        return database.withTransaction {
            val player = getPlayerOrCreate()
            val currentBalance = player.balance.toDoubleOrNull().orZero()
            if (currentBalance < cost) return@withTransaction false

            val newInventory = player.inventory.toMutableList()
            val index = newInventory.indexOfFirst { it.isSameInventoryItem(oldItem) }
            if (index == -1) return@withTransaction false

            newInventory[index] = newItem
            val basePlayer = processXpGain(player, GameConstants.REPAIR_XP)
            val finalPlayer = processAchievements(
                basePlayer.copy(
                    balance = (currentBalance - cost).toString(),
                    inventory = newInventory
                )
            )
            dao.updateInventory(finalPlayer)
            true
        }
    }

    suspend fun purchaseItem(item: MarketItem): Boolean {
        return database.withTransaction {
            val player = getPlayerOrCreate()
            val currentBalance = player.balance.toDoubleOrNull().orZero()
            val itemPrice = item.salesValue.toDoubleOrNull().orZero()
            if (currentBalance < itemPrice) return@withTransaction false

            val enrichedItem = item.copy(
                purchasePrice = item.salesValue,
                purchaseDate = LocalDate.now().toString(),
                dailyChangePercent = 0.0
            )
            val basePlayer = processXpGain(player, GameConstants.BUY_XP)
            val finalPlayer = processAchievements(
                basePlayer.copy(
                    balance = (currentBalance - itemPrice).toString(),
                    inventory = basePlayer.inventory + enrichedItem,
                    itemsBought = basePlayer.itemsBought + 1
                )
            )
            dao.updateInventory(finalPlayer)
            true
        }
    }

    suspend fun sellItem(item: MarketItem, agreedPrice: Double? = null): Boolean {
        return database.withTransaction {
            val player = getPlayerOrCreate()
            val itemInInventory = player.inventory.find { it.isSameInventoryItem(item) }
                ?: return@withTransaction false
            val sellPrice = agreedPrice ?: calculateSellPrice(itemInInventory)
            val purchasePrice = itemInInventory.purchasePrice
                .ifEmpty { itemInInventory.salesValue }
                .toDoubleOrNull()
                .orZero()
            val currentBalance = player.balance.toDoubleOrNull().orZero()

            val profit = sellPrice - purchasePrice
            val xpGain = GameConstants.SELL_BASE_XP +
                (profit / GameConstants.PROFIT_PER_XP).coerceAtLeast(0.0).toInt()
            val basePlayer = processXpGain(player, xpGain)
            val finalPlayer = processAchievements(
                basePlayer.copy(
                    balance = (currentBalance + sellPrice).toString(),
                    inventory = basePlayer.inventory - itemInInventory,
                    itemsSold = basePlayer.itemsSold + 1,
                    totalProfit = basePlayer.totalProfit + profit
                )
            )

            dao.updateInventory(finalPlayer)
            true
        }
    }

    fun calculateSellPrice(item: MarketItem): Double {
        val estimated = item.estimatedValue.toDoubleOrNull().orZero()
        val multiplier = MarketGenerator.getConditionMultiplier(item.condition)
        return ((estimated * multiplier) * GameConstants.SELL_PRICE_ROUNDING_SCALE).toLong() /
            GameConstants.SELL_PRICE_ROUNDING_SCALE
    }

    suspend fun advanceDay(): DailyEvent? {
        return database.withTransaction {
            val player = getPlayerOrCreate()
            val (updatedInventory, event) = EconomyEngine.processNewDay(
                currentDay = player.currentDay,
                inventory = player.inventory
            )

            val currentBalance = player.balance.toDoubleOrNull().orZero()
            val basePlayer = processXpGain(player, GameConstants.DAILY_LOGIN_XP)
            val finalPlayer = processAchievements(
                basePlayer.copy(
                    currentDay = player.currentDay + 1,
                    inventory = updatedInventory,
                    balance = (currentBalance + GameConstants.DAILY_LOGIN_BONUS).toString()
                )
            )

            dao.updateInventory(finalPlayer)
            event
        }
    }

    private suspend fun getPlayerOrCreate(): UserInventoryEntity {
        val existing = dao.getInventoryById(GameConstants.DEFAULT_USER_ID)
        if (existing != null) return existing

        val created = UserInventoryEntity(
            id = GameConstants.DEFAULT_USER_ID,
            balance = GameConstants.INITIAL_BALANCE,
            inventory = emptyList(),
            currentDay = GameConstants.INITIAL_DAY
        )
        dao.insertInventory(created)
        return created
    }

    private fun MarketItem.isSameInventoryItem(other: MarketItem): Boolean {
        return itemName == other.itemName &&
            sellerName == other.sellerName &&
            purchaseDate == other.purchaseDate
    }

    private fun Double?.orZero(): Double = this ?: 0.0
}

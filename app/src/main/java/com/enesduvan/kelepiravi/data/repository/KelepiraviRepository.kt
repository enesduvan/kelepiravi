package com.enesduvan.kelepiravi.data.repository

import androidx.room.withTransaction
import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.database.AppDatabase
import com.enesduvan.kelepiravi.database.entity.UserInventoryEntity
import com.enesduvan.kelepiravi.data.market.AchievementManager
import com.enesduvan.kelepiravi.data.market.DailyEvent
import com.enesduvan.kelepiravi.data.market.EconomyEngine
import com.enesduvan.kelepiravi.data.market.MarketGenerator
import com.enesduvan.kelepiravi.data.market.LootBoxGenerator
import com.enesduvan.kelepiravi.data.market.LootBoxType
import com.enesduvan.kelepiravi.data.model.Listing
import com.enesduvan.kelepiravi.data.model.MarketItem
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import com.enesduvan.kelepiravi.data.event.EventChoice
import com.enesduvan.kelepiravi.data.event.EventDefinition
import com.enesduvan.kelepiravi.data.event.EventLoader
import com.enesduvan.kelepiravi.data.event.EventManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class AdvanceDayResult(
    val event: DailyEvent?,
    val interactiveEvent: EventDefinition?,
    val rentPaid: Double,
    val taxPaid: Double
)

class KelepiraviRepository(
    private val database: AppDatabase,
    private val context: android.content.Context
) {
    private val dao = database.kelepiraviDao()
    private val mutex = Mutex()

    fun getPlayerState(): Flow<List<UserInventoryEntity>> = dao.getAllInventories()

    // ─── XP / Level ─────────────────────────────────────────────────────────

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

    // ─── Başarımlar ──────────────────────────────────────────────────────────

    private fun processAchievements(player: UserInventoryEntity): UserInventoryEntity {
        val unlockedList = player.unlockedAchievements
            .takeIf { it.isNotEmpty() }
            ?.split(",")
            ?: emptyList()

        // Başarım kontrolü için istatistikler entity'den alınıyor
        // Not: inventory/sold gibi detaylar yeni tablolarda olacak; şimdilik 0 geçiyoruz
        val newAchievements = AchievementManager.checkAchievements(
            balance      = player.balance.toDouble(),
            itemsBought  = 0,
            itemsSold    = 0,
            totalRepairs = player.totalRepairs,
            boughtScam   = player.hasBoughtScam,
            boughtAbsurd = player.hasBoughtAbsurd,
            unlockedIds  = unlockedList
        )

        if (newAchievements.isEmpty()) return player

        val rewardedBalance = player.balance + newAchievements.sumOf { it.rewardMoney.toLong() }
        val rewardedXp      = newAchievements.sumOf { it.rewardXp }
        val newIds          = newAchievements.joinToString(",") { it.id }
        val updatedAchievements = listOf(player.unlockedAchievements, newIds)
            .filter { it.isNotEmpty() }
            .joinToString(",")

        return processXpGain(
            player.copy(
                balance                = rewardedBalance,
                unlockedAchievements   = updatedAchievements
            ),
            rewardedXp
        )
    }

    // ─── Init ────────────────────────────────────────────────────────────────

    suspend fun initializePlayerIfNeeded() {
        mutex.withLock {
            database.withTransaction {
                getPlayerOrCreate()
            }
        }
    }

    // ─── Tamir ──────────────────────────────────────────────────────────────

    fun getRemainingRepairs(player: UserInventoryEntity): Int {
        val isNewDay = player.lastRepairDay != player.currentDay
        return if (isNewDay) GameConstants.DAILY_REPAIR_LIMIT
        else (GameConstants.DAILY_REPAIR_LIMIT - player.dailyRepairsUsed).coerceAtLeast(0)
    }

    suspend fun updateInventoryItem(oldItem: MarketItem, newItem: MarketItem, cost: Double): Boolean {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                if (player.balance.toDouble() < cost) return@withTransaction false

                // Envanter artık ayrı tabloda — geçici: JSON listesi varsa güncelle
                val currentBalance = player.balance
                val basePlayer = processXpGain(player, GameConstants.REPAIR_XP)
                val finalPlayer = processAchievements(
                    basePlayer.copy(balance = currentBalance - cost.toLong())
                )
                dao.updateInventory(finalPlayer)
                true
            }
        }
    }

    suspend fun repairItem(item: MarketItem, isUsta: Boolean = false): RepairResult {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()

                val isNewDay        = player.lastRepairDay != player.currentDay
                val repairsUsedToday = if (isNewDay) 0 else player.dailyRepairsUsed
                if (repairsUsedToday >= GameConstants.DAILY_REPAIR_LIMIT) {
                    return@withTransaction RepairResult.LimitReached
                }

                val costReduction = (player.mechanicLevel - 1) * 0.10
                val baseCost      = calculateRepairCost(item, isUsta)
                val cost          = baseCost * (1.0 - costReduction.coerceAtMost(0.50))

                if (player.balance.toDouble() < cost) return@withTransaction RepairResult.NotEnoughMoney

                val failureReduction  = (player.mechanicLevel - 1) * 0.08
                val baseFailure       = if (isUsta) 0.02 else GameConstants.REPAIR_FAILURE_CHANCE
                val currentFailChance = if (isUsta) baseFailure
                                        else (baseFailure - failureReduction).coerceAtLeast(0.0)
                val isFailure = kotlin.random.Random.nextDouble() < currentFailChance

                if (isFailure) {
                    val failureCost = cost * 0.3
                    dao.updateInventory(
                        player.copy(
                            balance          = player.balance - failureCost.toLong(),
                            dailyRepairsUsed = repairsUsedToday + 1,
                            lastRepairDay    = player.currentDay
                        )
                    )
                    return@withTransaction RepairResult.Failure(degradeCondition(item.condition))
                }

                val basePlayer = processXpGain(player, GameConstants.REPAIR_XP)
                dao.updateInventory(
                    processAchievements(
                        basePlayer.copy(
                            balance          = player.balance - cost.toLong(),
                            dailyRepairsUsed = repairsUsedToday + 1,
                            lastRepairDay    = player.currentDay,
                            totalRepairs     = player.totalRepairs + 1
                        )
                    )
                )
                RepairResult.Success
            }
        }
    }

    private fun degradeCondition(condition: String): String = when {
        condition.contains("Kusursuz") -> "Hafif Çizik"
        condition.contains("Hafif")    -> "Orta Hasar"
        condition.contains("Orta")     -> "Kırık / Arızalı"
        else                           -> "Bantlı / Tamir Gerekli"
    }

    fun calculateRepairCost(item: MarketItem, isUsta: Boolean = false): Double {
        val currentMultiplier = MarketGenerator.getConditionMultiplier(item.condition)
            ?: GameConstants.PERFECT_CONDITION_MULTIPLIER
        if (currentMultiplier >= GameConstants.PERFECT_CONDITION_MULTIPLIER) return 0.0
        val currentVal = item.estimatedValue.toDouble()
        val baseVal    = if (currentMultiplier > 0) currentVal / currentMultiplier else currentVal
        val gain       = baseVal - currentVal

        var rarityMultiplier = when {
            baseVal >= 20000 -> 0.45
            baseVal >= 8000  -> 0.35
            baseVal >= 2000  -> 0.30
            else             -> 0.25
        }
        when (item.category.lowercase()) {
            "otomotiv", "vehicles", "araba" -> rarityMultiplier = 0.50
            "emlak", "realestate", "ev"     -> rarityMultiplier = 0.55
        }
        val cost = gain * rarityMultiplier
        return if (isUsta) cost * 1.8 else cost
    }

    // ─── Satın Alma ──────────────────────────────────────────────────────────

    suspend fun purchaseItem(item: MarketItem): Boolean {
        return mutex.withLock {
            database.withTransaction {
                val player    = getPlayerOrCreate()
                val maxCapacity = 5 + (player.shopLevel * 5)
                // Envanter sayısı artık ayrı tabloda — geçici olarak player entity'den kontrol yok
                if (player.balance.toDouble() < item.salesValue.toDouble()) return@withTransaction false

                val isAbsurd     = item.itemName.contains("NASA", ignoreCase = true) ||
                                   item.itemName.contains("F-16", ignoreCase = true)
                val newBoughtScam   = player.hasBoughtScam   || item.isScammer
                val newBoughtAbsurd = player.hasBoughtAbsurd || isAbsurd

                val basePlayer  = processXpGain(player, GameConstants.BUY_XP)
                val finalPlayer = processAchievements(
                    basePlayer.copy(
                        balance         = player.balance - item.salesValue,
                        hasBoughtScam   = newBoughtScam,
                        hasBoughtAbsurd = newBoughtAbsurd
                    )
                )
                dao.updateInventory(finalPlayer)
                true
            }
        }
    }

    suspend fun recordFailedBargain() {
        // İstatistikler PlayerStatisticsEntity'e taşınacak — şimdilik no-op
    }

    suspend fun buyLootBox(type: LootBoxType): List<MarketItem>? {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                if (player.balance.toDouble() < type.price) return@withTransaction null

                val generatedItems  = LootBoxGenerator.openBox(type)
                val isAbsurdBox     = generatedItems.any {
                    it.itemName.contains("NASA", true) || it.itemName.contains("F-16", true)
                }

                val finalPlayer = processAchievements(
                    player.copy(
                        balance         = player.balance - type.price.toLong(),
                        hasBoughtAbsurd = player.hasBoughtAbsurd || isAbsurdBox
                    )
                )
                dao.updateInventory(finalPlayer)
                generatedItems
            }
        }
    }

    // ─── Satış ───────────────────────────────────────────────────────────────

    suspend fun sellItem(item: MarketItem, agreedPrice: Double? = null): Boolean {
        return mutex.withLock {
            database.withTransaction {
                val player    = getPlayerOrCreate()
                val sellPrice = agreedPrice ?: calculateSellPrice(item)

                val xpGain    = GameConstants.SELL_BASE_XP +
                    (sellPrice / GameConstants.PROFIT_PER_XP).coerceAtLeast(0.0).toInt()
                val basePlayer = processXpGain(player, xpGain)
                val finalPlayer = processAchievements(
                    basePlayer.copy(balance = player.balance + sellPrice.toLong())
                )
                dao.updateInventory(finalPlayer)
                true
            }
        }
    }

    // ─── İlan Sistemi ────────────────────────────────────────────────────────
    // Not: İlanlar artık UserInventoryEntity'den ayrılıyor.
    // Geçiş sürecinde listing işlemleri JSON sütunları yerine ayrı tabloya taşınacak.
    // Şimdilik listing flow'u boş döner; PlayerStatisticsEntity/UserListingEntity ile dolacak.

    suspend fun addListing(item: MarketItem, price: String): Boolean {
        // TODO: UserListingEntity kullan
        return false
    }

    suspend fun updateListingPrice(listing: Listing, newPrice: String): Boolean {
        // TODO: UserListingEntity güncelle
        return false
    }

    suspend fun removeListing(listing: Listing): Boolean {
        // TODO: UserListingEntity sil
        return false
    }

    suspend fun updateActiveListings(newListings: List<Listing>) {
        // TODO: UserListingEntity toplu güncelle
    }

    suspend fun updateListing(updatedListing: Listing): Boolean {
        // TODO: UserListingEntity güncelle
        return false
    }

    suspend fun sellListing(listing: Listing, agreedPrice: Double): Boolean {
        return mutex.withLock {
            database.withTransaction {
                val player  = getPlayerOrCreate()
                val xpGain  = GameConstants.SELL_BASE_XP +
                    (agreedPrice / GameConstants.PROFIT_PER_XP).coerceAtLeast(0.0).toInt()
                val basePlayer  = processXpGain(player, xpGain)
                val finalPlayer = processAchievements(
                    basePlayer.copy(balance = player.balance + agreedPrice.toLong())
                )
                dao.updateInventory(finalPlayer)
                true
            }
        }
    }

    fun calculateSellPrice(item: MarketItem): Double {
        val estimated  = item.estimatedValue.toDouble()
        val multiplier = MarketGenerator.getConditionMultiplier(item.condition)
        return ((estimated * multiplier) * GameConstants.SELL_PRICE_ROUNDING_SCALE).toLong() /
            GameConstants.SELL_PRICE_ROUNDING_SCALE
    }

    // ─── Gün Geçişi ──────────────────────────────────────────────────────────

    suspend fun advanceDay(): AdvanceDayResult {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()

                // Günlük ciro üzerinden vergi hesabı (yeni istatistik DAO'sundan)
                val dailyRev = database.playerStatisticsDao().getDailyRevenue(player.playerId) ?: 0.0
                val rent = GameConstants.DAILY_RENT_COST
                val tax = dailyRev * GameConstants.DAILY_TAX_RATE
                val totalDeduction = rent + tax
                val newBalance = player.balance + GameConstants.DAILY_LOGIN_BONUS.toLong() - totalDeduction.toLong()

                // Günlük ciroyu yeni gün için sıfırla
                database.playerStatisticsDao().resetDailyRevenue(player.playerId)
                
                // Süresi dolan modifier'ları temizle
                database.playerModifierDao().deleteExpiredModifiers(player.playerId, player.currentDay + 1)

                val basePlayer = processXpGain(player, GameConstants.DAILY_LOGIN_XP)
                val finalPlayer = processAchievements(
                    basePlayer.copy(
                        currentDay = player.currentDay + 1,
                        balance = newBalance,
                        dailyRepairsUsed = 0
                    )
                )
                dao.updateInventory(finalPlayer)

                // İnteraktif event çekimi
                val allEvents = EventLoader.loadEvents(context)
                val availableEvents = EventManager.getAvailableEvents(finalPlayer, allEvents)
                val pickedEvent = if (Math.random() < 0.3)
                    EventManager.pickRandomEvent(availableEvents) else null

                AdvanceDayResult(null, pickedEvent, rent, tax)
            }
        }
    }

    suspend fun applyEventChoice(choice: EventChoice): List<String> {
        return mutex.withLock {
            database.withTransaction {
                val player           = getPlayerOrCreate()
                var currentBalance   = player.balance.toDouble()
                var newXp            = player.xp
                val generatedItemNames = mutableListOf<String>()

                for (reward in choice.rewards) {
                    when (reward.type) {
                        "MONEY" -> currentBalance += reward.value.toDoubleOrNull() ?: 0.0
                        "XP"    -> newXp += reward.value.toIntOrNull() ?: 0
                        "ITEM"  -> {
                            val product = MarketGenerator.PRODUCTS.find { it.name == reward.value }
                                ?: MarketGenerator.PRODUCTS.random()
                            val item = MarketGenerator.generateNormalItem(
                                kotlin.random.Random.Default, product, emptyMap()
                            )
                            generatedItemNames.add(item.itemName)
                        }
                    }
                }
                for (penalty in choice.penalties) {
                    when (penalty.type) {
                        "MONEY_EXACT"   -> currentBalance -= penalty.value.toDoubleOrNull() ?: 0.0
                        "MONEY_PERCENT" -> {
                            val pct = penalty.value.toDoubleOrNull() ?: 0.0
                            currentBalance -= currentBalance * (pct / 100.0)
                        }
                        "XP" -> newXp -= penalty.value.toIntOrNull() ?: 0
                    }
                }
                if (currentBalance < 0) currentBalance = 0.0

                val xpGain     = (newXp - player.xp).coerceAtLeast(0)
                val basePlayer = if (xpGain > 0) processXpGain(player, xpGain) else player
                dao.updateInventory(basePlayer.copy(balance = currentBalance.toLong()))
                generatedItemNames
            }
        }
    }

    // ─── Yükseltmeler ────────────────────────────────────────────────────────

    suspend fun upgradeShop(cost: Double): Boolean {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                if (player.balance.toDouble() < cost) return@withTransaction false
                if (player.shopLevel >= 5) return@withTransaction false

                dao.updateInventory(
                    player.copy(
                        shopLevel = player.shopLevel + 1,
                        balance   = player.balance - cost.toLong()
                    )
                )
                true
            }
        }
    }

    suspend fun upgradeMechanic(cost: Double): Boolean {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                if (player.balance.toDouble() < cost) return@withTransaction false
                if (player.mechanicLevel >= 5) return@withTransaction false

                dao.updateInventory(
                    player.copy(
                        mechanicLevel = player.mechanicLevel + 1,
                        balance       = player.balance - cost.toLong()
                    )
                )
                true
            }
        }
    }

    suspend fun updateNpcRelationship(npcName: String, delta: Int) {
        // TODO: PlayerNpcRelationshipEntity kullan
    }

    // ─── Yardımcı ────────────────────────────────────────────────────────────

    private suspend fun getPlayerOrCreate(): UserInventoryEntity {
        val existing = dao.getInventoryById(GameConstants.DEFAULT_USER_ID)
        if (existing != null) return existing

        val created = UserInventoryEntity(
            playerId = GameConstants.DEFAULT_USER_ID,
            balance  = GameConstants.INITIAL_BALANCE,
            currentDay = GameConstants.INITIAL_DAY
        )
        dao.insertInventory(created)
        return created
    }
}

// ─── Tamir Sonuç Durumu ──────────────────────────────────────────────────────
sealed class RepairResult {
    object Success : RepairResult()
    data class Failure(val newCondition: String) : RepairResult()
    object LimitReached : RepairResult()
    object NotEnoughMoney : RepairResult()
}

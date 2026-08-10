package com.enesduvan.kelepiravi.data.repository

import androidx.room.withTransaction
import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.database.AppDatabase
import com.enesduvan.kelepiravi.database.entity.UserInventoryEntity
import com.enesduvan.kelepiravi.database.entity.UserInventoryItemEntity
import com.enesduvan.kelepiravi.database.entity.UserListingEntity
import com.enesduvan.kelepiravi.database.entity.PlayerProgressEntity
import com.enesduvan.kelepiravi.database.entity.PlayerStatisticsEntity
import com.enesduvan.kelepiravi.database.entity.PlayerEventStateEntity
import com.enesduvan.kelepiravi.database.entity.PlayerNpcRelationshipEntity
import com.enesduvan.kelepiravi.data.market.AchievementManager
import com.enesduvan.kelepiravi.data.market.DailyEvent
import com.enesduvan.kelepiravi.data.market.EconomyEngine
import com.enesduvan.kelepiravi.data.market.MarketGenerator
import com.enesduvan.kelepiravi.data.market.LootBoxGenerator
import com.enesduvan.kelepiravi.data.market.LootBoxType
import com.enesduvan.kelepiravi.data.model.Listing
import com.enesduvan.kelepiravi.data.listing.ListingEngine
import com.enesduvan.kelepiravi.data.model.MarketItem
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import com.enesduvan.kelepiravi.data.event.EventChoice
import com.enesduvan.kelepiravi.data.event.EventDefinition
import com.enesduvan.kelepiravi.data.event.EventLoader
import com.enesduvan.kelepiravi.data.event.EventManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

import com.enesduvan.kelepiravi.domain.repository.IKelepiraviRepository
import com.enesduvan.kelepiravi.domain.model.RepairResult

data class AdvanceDayResult(
    val event: DailyEvent?,
    val interactiveEvent: EventDefinition?,
    val rentPaid: Double,
    val taxPaid: Double
)

class KelepiraviRepository(
    private val database: AppDatabase,
    private val context: android.content.Context
) : IKelepiraviRepository {
    private val dao = database.kelepiraviDao()
    private val inventoryDao = database.inventoryItemDao()
    private val listingDao = database.listingDao()
    private val statisticsDao = database.playerStatisticsDao()
    private val relationshipDao = database.npcRelationshipDao()
    private val mutex = Mutex()
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun getPlayerState(): Flow<List<UserInventoryEntity>> = dao.getAllInventories()

    override fun observeUserInventoryItems(): Flow<List<MarketItem>> =
        inventoryDao.observeInventory(GameConstants.DEFAULT_USER_ID).map { entities ->
            entities.map(::toMarketItem)
        }

    override fun observePlayerStatistics(): Flow<PlayerStatisticsEntity?> =
        statisticsDao.observeStatistics(GameConstants.DEFAULT_USER_ID)

    override fun observeActiveListings(): Flow<List<Listing>> =
        listingDao.observeActiveListings(GameConstants.DEFAULT_USER_ID).map { entities ->
            entities.mapNotNull { entity -> decodeListing(entity) }
        }

    override suspend fun getUserInventoryItems(): List<MarketItem> {
        return inventoryDao.getInventory(GameConstants.DEFAULT_USER_ID).map(::toMarketItem)
    }

    override suspend fun recordSuccessfulBargain(category: String, profit: Double) {
        mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                val stats = getStatisticsOrCreate(player.playerId)
                statisticsDao.upsert(
                    stats.copy(
                        successfulBargains = stats.successfulBargains + 1,
                        totalBargains = stats.totalBargains + 1,
                        totalProfit = stats.totalProfit + profit,
                        highestProfit = maxOf(stats.highestProfit, profit),
                        soldCategories = updateCategoryCount(stats.soldCategories, category)
                    )
                )
            }
        }
    }

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

    override suspend fun initializePlayerIfNeeded() {
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

    override suspend fun repairItem(item: MarketItem, isUsta: Boolean): RepairResult {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                val inventoryEntity = findOwnedItem(item)
                    ?: return@withTransaction RepairResult.NotOwned

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
                    val failedItem = item.copy(condition = degradeCondition(item.condition))
                    inventoryDao.updateItem(
                        inventoryEntity.copy(
                            condition = conditionCode(failedItem.condition),
                            itemJson = json.encodeToString(failedItem)
                        )
                    )
                    dao.updateInventory(
                        player.copy(
                            balance          = player.balance - failureCost.toLong(),
                            dailyRepairsUsed = repairsUsedToday + 1,
                            lastRepairDay    = player.currentDay
                        )
                    )
                    return@withTransaction RepairResult.Failure(failedItem.condition)
                }

                val repairedItem = item.copy(condition = improveCondition(item.condition))
                inventoryDao.updateItem(
                    inventoryEntity.copy(
                        condition = conditionCode(repairedItem.condition),
                        itemJson = json.encodeToString(repairedItem)
                    )
                )
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
                statisticsDao.incrementRepairs(player.playerId)
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

    override fun calculateRepairCost(item: MarketItem, isUsta: Boolean): Double {
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

    override suspend fun purchaseItem(item: MarketItem): Boolean {
        return mutex.withLock {
            database.withTransaction {
                val player    = getPlayerOrCreate()
                val maxCapacity = 5 + (player.shopLevel * 5)
                // Envanter sayısı artık ayrı tabloda — geçici olarak player entity'den kontrol yok
                if (item.salesValue <= 0L || inventoryDao.getInventoryCount(player.playerId) >= maxCapacity) {
                    return@withTransaction false
                }
                if (player.balance < item.salesValue) return@withTransaction false

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
                inventoryDao.insertItem(
                    UserInventoryItemEntity(
                        playerId = player.playerId,
                        itemId = item.itemName,
                        purchasePrice = item.salesValue.toDouble(),
                        condition = conditionCode(item.condition),
                        itemJson = json.encodeToString(
                            item.copy(
                                purchasePrice = item.salesValue,
                                purchaseDate = LocalDate.now().toString()
                            )
                        )
                    )
                )
                statisticsDao.incrementItemsBought(player.playerId)
                true
            }
        }
    }

    override suspend fun recordFailedBargain() {
        mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                val stats = getStatisticsOrCreate(player.playerId)
                statisticsDao.upsert(stats.copy(totalBargains = stats.totalBargains + 1))
            }
        }
    }

    override suspend fun buyLootBox(type: LootBoxType): List<MarketItem>? {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                if (type.price <= 0.0 || player.balance.toDouble() < type.price) return@withTransaction null

                val generatedItems  = LootBoxGenerator.openBox(type)
                val maxCapacity = 5 + (player.shopLevel * 5)
                if (inventoryDao.getInventoryCount(player.playerId) + generatedItems.size > maxCapacity) {
                    return@withTransaction null
                }
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
                generatedItems.forEach { generatedItem ->
                    inventoryDao.insertItem(
                        UserInventoryItemEntity(
                            playerId = player.playerId,
                            itemId = generatedItem.itemName,
                            purchasePrice = type.price / generatedItems.size.coerceAtLeast(1),
                            condition = conditionCode(generatedItem.condition),
                            itemJson = json.encodeToString(generatedItem)
                        )
                    )
                }
                generatedItems
            }
        }
    }

    // ─── Satış ───────────────────────────────────────────────────────────────

    override suspend fun sellItem(item: MarketItem, agreedPrice: Double?): Boolean {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                val inventoryEntity = findOwnedItem(item) ?: return@withTransaction false
                val sellPrice = agreedPrice ?: calculateSellPrice(item)
                if (!sellPrice.isFinite() || sellPrice <= 0.0) return@withTransaction false
                // Aktif bir ilanı varsa sil ve satışı tamamla
                val activeListing = listingDao.getActiveListings(player.playerId).find { it.itemId == inventoryEntity.id }
                if (activeListing != null) {
                    listingDao.deleteListing(activeListing.id)
                }

                val xpGain    = GameConstants.SELL_BASE_XP +
                    (sellPrice / GameConstants.PROFIT_PER_XP).coerceAtLeast(0.0).toInt()
                val basePlayer = processXpGain(player, xpGain)
                val finalPlayer = processAchievements(
                    basePlayer.copy(balance = player.balance + sellPrice.toLong())
                )
                dao.updateInventory(finalPlayer)
                inventoryDao.deleteItemForPlayer(inventoryEntity.id, player.playerId)
                val stats = getStatisticsOrCreate(player.playerId)
                statisticsDao.upsert(
                    stats.copy(
                        itemsSold = stats.itemsSold + 1,
                        totalProfit = stats.totalProfit + (sellPrice - inventoryEntity.purchasePrice),
                        dailyRevenue = stats.dailyRevenue + sellPrice,
                        highestProfit = maxOf(stats.highestProfit, sellPrice - inventoryEntity.purchasePrice)
                    )
                )
                true
            }
        }
    }

    // ─── İlan Sistemi ────────────────────────────────────────────────────────
    // Not: İlanlar artık UserInventoryEntity'den ayrılıyor.
    // Geçiş sürecinde listing işlemleri JSON sütunları yerine ayrı tabloya taşınacak.
    // Şimdilik listing flow'u boş döner; PlayerStatisticsEntity/UserListingEntity ile dolacak.

    override suspend fun addListing(item: MarketItem, price: String): Boolean {
        val askingPrice = price.toDoubleOrNull()
            ?: return false
        if (!askingPrice.isFinite() || askingPrice <= 0.0) return false
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                val inventoryEntity = findOwnedItem(item) ?: return@withTransaction false
                val maxListings = 5 + (player.shopLevel * 5)
                if (listingDao.getActiveListingCount(player.playerId) >= maxListings) return@withTransaction false
                if (listingDao.getActiveListings(player.playerId).any { it.itemId == inventoryEntity.id }) {
                    return@withTransaction false
                }

                val listingId = listingDao.insertListing(
                    UserListingEntity(
                        playerId = player.playerId,
                        itemId = inventoryEntity.id,
                        askingPrice = askingPrice,
                        listedDay = player.currentDay,
                        listingJson = json.encodeToString(
                            Listing(
                                item = item,
                                listedPrice = askingPrice.toLong(),
                                listedDay = player.currentDay
                            )
                        )
                    )
                )
                val listing = Listing(
                    id = listingId.toString(),
                    item = item,
                    listedPrice = askingPrice.toLong(),
                    listedDay = player.currentDay
                )
                listingDao.updateListing(
                    UserListingEntity(
                        id = listingId,
                        playerId = player.playerId,
                        itemId = inventoryEntity.id,
                        askingPrice = askingPrice,
                        listedDay = player.currentDay,
                        listingJson = json.encodeToString(listing)
                    )
                )
                true
            }
        }
    }

    override suspend fun updateListingPrice(listing: Listing, newPrice: String): Boolean {
        val askingPrice = newPrice.toDoubleOrNull()
            ?: return false
        if (!askingPrice.isFinite() || askingPrice <= 0.0) return false
        val listingId = listing.id.toLongOrNull() ?: return false
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                val updated = listing.copy(listedPrice = askingPrice.toLong())
                listingDao.updatePriceForPlayer(
                    id = listingId,
                    playerId = player.playerId,
                    newPrice = askingPrice,
                    listingJson = json.encodeToString(updated)
                ) > 0
            }
        }
    }

    override suspend fun removeListing(listing: Listing): Boolean {
        val listingId = listing.id.toLongOrNull() ?: return false
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                listingDao.deactivateListingForPlayer(listingId, player.playerId) > 0
            }
        }
    }

    override suspend fun updateActiveListings(newListings: List<Listing>) {
        mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                newListings.forEach { listing ->
                    val listingId = listing.id.toLongOrNull() ?: return@forEach
                    val existing = listingDao.getListingByIdForPlayer(listingId, player.playerId)
                        ?: return@forEach
                    listingDao.updateListing(
                        existing.copy(
                            askingPrice = listing.listedPrice.toDouble(),
                            listedDay = listing.listedDay,
                            listingJson = json.encodeToString(listing)
                        )
                    )
                }
            }
        }
    }

    suspend fun updateListing(updatedListing: Listing): Boolean {
        val listingId = updatedListing.id.toLongOrNull() ?: return false
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                val existing = listingDao.getListingByIdForPlayer(listingId, player.playerId)
                    ?: return@withTransaction false
                listingDao.updateListing(
                    existing.copy(
                        askingPrice = updatedListing.listedPrice.toDouble(),
                        listedDay = updatedListing.listedDay,
                        listingJson = json.encodeToString(updatedListing)
                    )
                ) > 0
            }
        }
    }

    override suspend fun sellListing(listing: Listing, agreedPrice: Double): Boolean {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                if (!agreedPrice.isFinite() || agreedPrice <= 0.0) return@withTransaction false
                val listingId = listing.id.toLongOrNull() ?: return@withTransaction false
                val storedListing = listingDao.getListingByIdForPlayer(listingId, player.playerId)
                    ?: return@withTransaction false
                if (!storedListing.isActive) return@withTransaction false
                val inventoryEntity = inventoryDao.getItemByIdForPlayer(storedListing.itemId, player.playerId)
                    ?: return@withTransaction false
                val xpGain  = GameConstants.SELL_BASE_XP +
                    (agreedPrice / GameConstants.PROFIT_PER_XP).coerceAtLeast(0.0).toInt()
                val basePlayer  = processXpGain(player, xpGain)
                val finalPlayer = processAchievements(
                    basePlayer.copy(balance = player.balance + agreedPrice.toLong())
                )
                dao.updateInventory(finalPlayer)
                inventoryDao.deleteItemForPlayer(inventoryEntity.id, player.playerId)
                listingDao.deactivateListingForPlayer(listingId, player.playerId)
                val stats = getStatisticsOrCreate(player.playerId)
                statisticsDao.upsert(
                    stats.copy(
                        itemsSold = stats.itemsSold + 1,
                        totalProfit = stats.totalProfit + (agreedPrice - inventoryEntity.purchasePrice),
                        dailyRevenue = stats.dailyRevenue + agreedPrice,
                        highestProfit = maxOf(stats.highestProfit, agreedPrice - inventoryEntity.purchasePrice)
                    )
                )
                true
            }
        }
    }

    override fun calculateSellPrice(item: MarketItem): Double {
        val estimated  = item.estimatedValue.toDouble()
        val multiplier = MarketGenerator.getConditionMultiplier(item.condition)
        return ((estimated * multiplier) * GameConstants.SELL_PRICE_ROUNDING_SCALE).toLong() /
            GameConstants.SELL_PRICE_ROUNDING_SCALE
    }

    // ─── Gün Geçişi ──────────────────────────────────────────────────────────

    override suspend fun advanceDay(): AdvanceDayResult {
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

                // Active listing day transition (offers, views, favorites generation)
                val activeEntities = listingDao.getActiveListings(player.playerId)
                if (activeEntities.isNotEmpty()) {
                    val decodedListings = activeEntities.mapNotNull { decodeListing(it) }
                    val processedListings = ListingEngine.processDay(decodedListings)
                    processedListings.forEach { updated ->
                        val entityId = updated.id.toLongOrNull()
                        if (entityId != null) {
                            val existing = activeEntities.find { it.id == entityId }
                            if (existing != null) {
                                listingDao.updateListing(
                                    existing.copy(
                                        askingPrice = updated.listedPrice.toDouble(),
                                        listedDay = updated.listedDay,
                                        listingJson = json.encodeToString(updated)
                                    )
                                )
                            }
                        }
                    }
                }

                // İnteraktif event çekimi
                val allEvents = EventLoader.loadEvents(context)
                val availableEvents = EventManager.getAvailableEvents(finalPlayer, allEvents)
                val pickedEvent = if (Math.random() < 0.3)
                    EventManager.pickRandomEvent(availableEvents) else null

                AdvanceDayResult(null, pickedEvent, rent, tax)
            }
        }
    }

    override suspend fun processListingTicks() {
        mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                val activeEntities = listingDao.getActiveListings(player.playerId)
                if (activeEntities.isEmpty()) return@withTransaction
                activeEntities.forEach { entity ->
                    val decoded = decodeListing(entity) ?: return@forEach
                    val updated = ListingEngine.processTick(decoded)
                    listingDao.updateListing(
                        entity.copy(
                            askingPrice = updated.listedPrice.toDouble(),
                            listingJson = json.encodeToString(updated)
                        )
                    )
                }
            }
        }
    }

    override suspend fun applyEventChoice(choice: EventChoice): List<MarketItem> {
        return mutex.withLock {
            database.withTransaction {
                val player           = getPlayerOrCreate()
                var currentBalance   = player.balance.toDouble()
                var newXp            = player.xp
                val generatedItems = mutableListOf<MarketItem>()

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
                            generatedItems.add(item)
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
                generatedItems.forEach { generatedItem ->
                    inventoryDao.insertItem(
                        UserInventoryItemEntity(
                            playerId = player.playerId,
                            itemId = generatedItem.itemName,
                            purchasePrice = 0.0,
                            condition = conditionCode(generatedItem.condition),
                            itemJson = json.encodeToString(generatedItem)
                        )
                    )
                }
                generatedItems
            }
        }
    }

    // ─── Yükseltmeler ────────────────────────────────────────────────────────

    override suspend fun upgradeShop(cost: Double): Boolean {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                if (!cost.isFinite() || cost <= 0.0 || player.balance.toDouble() < cost) return@withTransaction false
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

    override suspend fun upgradeMechanic(cost: Double): Boolean {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                if (!cost.isFinite() || cost <= 0.0 || player.balance.toDouble() < cost) return@withTransaction false
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

    override suspend fun updateNpcRelationship(npcName: String, delta: Int) {
        if (npcName.isBlank() || delta == 0) return
        mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                val current = relationshipDao.getScore(player.playerId, npcName) ?: 0
                relationshipDao.upsert(
                    PlayerNpcRelationshipEntity(
                        playerId = player.playerId,
                        npcId = npcName,
                        relationshipScore = (current + delta).coerceIn(-100, 100)
                    )
                )
            }
        }
    }

    // ─── Yardımcı ────────────────────────────────────────────────────────────

    private suspend fun getPlayerOrCreate(): UserInventoryEntity {
        val existing = dao.getInventoryById(GameConstants.DEFAULT_USER_ID)
        if (existing != null) {
            ensureModernPlayerRows(existing)
            return existing
        }

        val created = UserInventoryEntity(
            playerId = GameConstants.DEFAULT_USER_ID,
            balance  = GameConstants.INITIAL_BALANCE,
            currentDay = GameConstants.INITIAL_DAY
        )
        dao.insertInventory(created)
        ensureModernPlayerRows(created)
        return created
    }

    private suspend fun ensureModernPlayerRows(player: UserInventoryEntity) {
        val progressDao = database.playerProgressDao()
        if (progressDao.getPlayer(player.playerId) == null) {
            progressDao.upsert(
                PlayerProgressEntity(
                    playerId = player.playerId,
                    balance = player.balance,
                    currentDay = player.currentDay,
                    xp = player.xp,
                    level = player.level,
                    shopLevel = player.shopLevel,
                    mechanicLevel = player.mechanicLevel
                )
            )
        }
        if (statisticsDao.getStatistics(player.playerId) == null) {
            statisticsDao.upsert(PlayerStatisticsEntity(playerId = player.playerId))
        }
        if (database.playerEventStateDao().getEventState(player.playerId) == null) {
            database.playerEventStateDao().upsert(PlayerEventStateEntity(playerId = player.playerId))
        }
    }

    private suspend fun getStatisticsOrCreate(playerId: Int): PlayerStatisticsEntity {
        return statisticsDao.getStatistics(playerId)
            ?: PlayerStatisticsEntity(playerId = playerId).also { statisticsDao.upsert(it) }
    }

    private suspend fun findOwnedItem(item: MarketItem): UserInventoryItemEntity? {
        val itemId = item.id.toLongOrNull() ?: return null
        return inventoryDao.getItemByIdForPlayer(itemId, GameConstants.DEFAULT_USER_ID)
    }

    private fun toMarketItem(entity: UserInventoryItemEntity): MarketItem {
        val decoded = entity.itemJson.takeIf { it.isNotBlank() }?.let {
            runCatching { json.decodeFromString<MarketItem>(it) }.getOrNull()
        }
        if (decoded != null) {
            return decoded.copy(
                id = entity.id.toString(),
                purchasePrice = entity.purchasePrice.toLong()
            )
        }

        val product = MarketGenerator.PRODUCTS.find { it.name == entity.itemId }
            ?: MarketGenerator.PRODUCTS.first()
        return MarketItem(
            id = entity.id.toString(),
            itemName = product.name,
            category = product.category,
            condition = conditionName(entity.condition),
            sellerName = "Oyuncu",
            salesValue = product.baseMaxValue.toLong(),
            estimatedValue = product.baseMaxValue.toLong(),
            imageName = product.imageKey,
            purchasePrice = entity.purchasePrice.toLong()
        )
    }

    private fun decodeListing(entity: UserListingEntity): Listing? {
        val decoded = entity.listingJson.takeIf { it.isNotBlank() }?.let {
            runCatching { json.decodeFromString<Listing>(it) }.getOrNull()
        } ?: return null
        return decoded.copy(
            id = entity.id.toString(),
            listedPrice = entity.askingPrice.toLong(),
            listedDay = entity.listedDay
        )
    }

    private fun conditionCode(condition: String): Int = when {
        condition.contains("Kusursuz", ignoreCase = true) -> 0
        condition.contains("Hafif", ignoreCase = true) -> 1
        condition.contains("Orta", ignoreCase = true) -> 2
        condition.contains("Kırık", ignoreCase = true) -> 3
        else -> 4
    }

    private fun conditionName(code: Int): String = when (code.coerceIn(0, 4)) {
        0 -> "Kusursuz Temiz"
        1 -> "Hafif Çizik"
        2 -> "Orta Hasar"
        3 -> "Kırık / Arızalı"
        else -> "Bantlı / Tamir Gerekli"
    }

    private fun improveCondition(condition: String): String = when {
        condition.contains("Bantlı", ignoreCase = true) -> "Kırık / Arızalı"
        condition.contains("Kırık", ignoreCase = true) -> "Orta Hasar"
        condition.contains("Orta", ignoreCase = true) -> "Hafif Çizik"
        condition.contains("Hafif", ignoreCase = true) -> "Kusursuz Temiz"
        else -> condition
    }

    private fun updateCategoryCount(serialized: String, category: String): String {
        val values = mutableMapOf<String, Int>()
        serialized.split(',').forEach { entry ->
            val parts = entry.split(':', limit = 2)
            if (parts.size == 2) values[parts[0]] = parts[1].toIntOrNull() ?: 0
        }
        values[category] = (values[category] ?: 0) + 1
        return values.entries.joinToString(",") { "${it.key}:${it.value}" }
    }
}

// ─── Tamir Sonuç Durumu ──────────────────────────────────────────────────────

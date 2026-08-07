package com.enesduvan.kelepiravi.data.repository

import androidx.room.withTransaction
import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.data.local.AppDatabase
import com.enesduvan.kelepiravi.data.local.entity.UserInventoryEntity
import com.enesduvan.kelepiravi.data.market.AchievementManager
import com.enesduvan.kelepiravi.data.market.DailyEvent
import com.enesduvan.kelepiravi.data.market.EconomyEngine
import com.enesduvan.kelepiravi.data.market.MarketGenerator
import com.enesduvan.kelepiravi.data.market.LootBoxGenerator
import com.enesduvan.kelepiravi.data.market.LootBoxType
import com.enesduvan.kelepiravi.data.market.ScamType
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
            balance = player.balance.toDoubleOrNull().orZero(),
            itemsBought = player.itemsBought,
            itemsSold = player.itemsSold,
            totalRepairs = player.totalRepairs,
            boughtScam = player.hasBoughtScam,
            boughtAbsurd = player.hasBoughtAbsurd,
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
        mutex.withLock {
            database.withTransaction {
                getPlayerOrCreate()
            }
        }
    }

    // Ch6: Günlük tamir limiti kontrolü
    fun getRemainingRepairs(player: UserInventoryEntity): Int {
        val isNewDay = player.lastRepairDay != player.currentDay
        return if (isNewDay) GameConstants.DAILY_REPAIR_LIMIT
        else (GameConstants.DAILY_REPAIR_LIMIT - player.dailyRepairsUsed).coerceAtLeast(0)
    }

    suspend fun updateInventoryItem(oldItem: MarketItem, newItem: MarketItem, cost: Double): Boolean {
        return mutex.withLock {
            database.withTransaction {
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
    }

    /**
     * Ch6: Tamir işlemi — günlük limit kontrolü ve %10 başarısızlık riski.
     * @return RepairResult — başarı/başarısızlık/limit durumu
     */
    suspend fun repairItem(item: MarketItem, isUsta: Boolean = false): RepairResult {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()

            // Günlük limit kontrolü
            val isNewDay = player.lastRepairDay != player.currentDay
            val repairsUsedToday = if (isNewDay) 0 else player.dailyRepairsUsed
            if (repairsUsedToday >= GameConstants.DAILY_REPAIR_LIMIT) {
                return@withTransaction RepairResult.LimitReached
            }

            val costReduction = (player.mechanicLevel - 1) * 0.10 // Level başı %10 indirim (Level 1: %0, Level 5: %40)
            val baseCost = calculateRepairCost(item, isUsta)
            val cost = baseCost * (1.0 - costReduction.coerceAtMost(0.50)) // Maksimum %50 indirim

            val currentBalance = player.balance.toDoubleOrNull().orZero()
            if (currentBalance < cost) return@withTransaction RepairResult.NotEnoughMoney

            // Temel başarısızlık riski Çırak için %35, Usta için %2 (seviye bağımsız)
            // Usta seçilirse başarısızlık riski her halükarda çok düşüktür.
            val failureReduction = (player.mechanicLevel - 1) * 0.08
            val baseFailure = if (isUsta) 0.02 else GameConstants.REPAIR_FAILURE_CHANCE
            val currentFailureChance = if (isUsta) baseFailure else (baseFailure - failureReduction).coerceAtLeast(0.0)
            val isFailure = kotlin.random.Random.nextDouble() < currentFailureChance

            if (isFailure) {
                // Başarısız: Kondisyon 1 seviye düşer, para gider, hak gider
                val degradedCondition = degradeCondition(item.condition)
                val degradedItem = item.copy(condition = degradedCondition)

                val newInventory = player.inventory.toMutableList()
                val index = newInventory.indexOfFirst { it.isSameInventoryItem(item) }
                if (index != -1) newInventory[index] = degradedItem

                val failureCost = cost * 0.3 // Başarısız tamir maliyetinin %30'u gider
                dao.updateInventory(
                    player.copy(
                        balance = (currentBalance - failureCost).toString(),
                        inventory = newInventory,
                        dailyRepairsUsed = repairsUsedToday + 1,
                        lastRepairDay = player.currentDay
                    )
                )
                return@withTransaction RepairResult.Failure(degradedCondition)
            }

            // Başarılı tamir
            val currentMultiplier = MarketGenerator.getConditionMultiplier(item.condition)
            val currentVal = item.estimatedValue.toDoubleOrNull() ?: 0.0
            val baseVal = if (currentMultiplier > 0) currentVal / currentMultiplier else currentVal

            val repairedItem = item.copy(
                condition = "Kusursuz Temiz",
                estimatedValue = baseVal.toString()
            )

            val newInventory = player.inventory.toMutableList()
            val index = newInventory.indexOfFirst { it.isSameInventoryItem(item) }
            if (index == -1) return@withTransaction RepairResult.Failure(item.condition)
            newInventory[index] = repairedItem

            val basePlayer = processXpGain(player, GameConstants.REPAIR_XP)
            dao.updateInventory(
                processAchievements(
                    basePlayer.copy(
                        balance = (currentBalance - cost).toString(),
                        inventory = newInventory,
                        dailyRepairsUsed = repairsUsedToday + 1,
                        lastRepairDay = player.currentDay,
                        totalRepairs = player.totalRepairs + 1
                    )
                )
            )
            RepairResult.Success
        }
        }
    }

    /** Kondisyon seviyesini bir basamak düşürür */
    private fun degradeCondition(condition: String): String {
        return when {
            condition.contains("Kusursuz") -> "Hafif Çizik"
            condition.contains("Hafif") -> "Orta Hasar"
            condition.contains("Orta") -> "Kırık / Arızalı"
            else -> "Bantlı / Tamir Gerekli"
        }
    }

    fun calculateRepairCost(item: MarketItem, isUsta: Boolean = false): Double {
        val currentMultiplier = MarketGenerator.getConditionMultiplier(item.condition) ?: GameConstants.PERFECT_CONDITION_MULTIPLIER
        if (currentMultiplier >= GameConstants.PERFECT_CONDITION_MULTIPLIER) return 0.0
        val currentVal = item.estimatedValue.toDoubleOrNull() ?: 0.0
        val baseVal = if (currentMultiplier > 0) currentVal / currentMultiplier else currentVal
        val gain = baseVal - currentVal
        
        // Eşyanın temel değerine göre tamir masrafı
        var rarityMultiplier = when {
            baseVal >= 20000 -> 0.45
            baseVal >= 8000 -> 0.35
            baseVal >= 2000 -> 0.30
            else -> 0.25
        }

        // Kategoriye özel çarpanlar
        when (item.category.lowercase()) {
            "otomotiv", "vehicles", "araba" -> rarityMultiplier = 0.50
            "emlak", "realestate", "ev" -> rarityMultiplier = 0.55
        }
        
        val cost = gain * rarityMultiplier
        return if (isUsta) cost * 1.8 else cost
    }

    suspend fun purchaseItem(item: MarketItem): Boolean {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
            val maxCapacity = 5 + (player.shopLevel * 5)
            if (player.inventory.size >= maxCapacity) return@withTransaction false

            val currentBalance = player.balance.toDoubleOrNull().orZero()
            val itemPrice = item.salesValue.toDoubleOrNull().orZero()
            if (currentBalance < itemPrice) return@withTransaction false

            // Ch6: Dolandırıcıdan alıyorsak gerçek kondisyonu ortaya çıkar
            val enrichedItem = if (item.isScammer && item.hiddenCondition.isNotEmpty()) {
                val hiddenMultiplier = MarketGenerator.getConditionMultiplier(item.hiddenCondition)
                val fakeEstimated = item.estimatedValue.toDoubleOrNull() ?: 0.0
                val trueEstimated = (fakeEstimated * hiddenMultiplier).toInt().toString()
                item.copy(
                    condition = item.hiddenCondition,   // Gerçek kondisyon açıklandı
                    estimatedValue = trueEstimated,      // Gerçek değer
                    purchasePrice = item.salesValue,
                    purchaseDate = LocalDate.now().toString(),
                    dailyChangePercent = 0.0
                )
            } else {
                item.copy(
                    purchasePrice = item.salesValue,
                    purchaseDate = LocalDate.now().toString(),
                    dailyChangePercent = 0.0
                )
            }

            val isRare = enrichedItem.category == "Antika" || (enrichedItem.estimatedValue.toDoubleOrNull() ?: 0.0) >= 50000.0
            val newRareCount = if (isRare) player.rareItemsFound + 1 else player.rareItemsFound

            val isAbsurd = enrichedItem.itemName.contains("NASA", ignoreCase = true) || enrichedItem.itemName.contains("F-16", ignoreCase = true)
            val newBoughtScam = player.hasBoughtScam || item.isScammer
            val newBoughtAbsurd = player.hasBoughtAbsurd || isAbsurd

            val basePlayer = processXpGain(player, GameConstants.BUY_XP)
            val finalPlayer = processAchievements(
                basePlayer.copy(
                    balance = (currentBalance - itemPrice).toString(),
                    inventory = basePlayer.inventory + enrichedItem,
                    itemsBought = basePlayer.itemsBought + 1,
                    rareItemsFound = newRareCount,
                    hasBoughtScam = newBoughtScam,
                    hasBoughtAbsurd = newBoughtAbsurd,
                    successfulBargains = basePlayer.successfulBargains + 1,
                    totalBargains = basePlayer.totalBargains + 1
                )
            )
            dao.updateInventory(finalPlayer)
            true
        }
        }
    }

    suspend fun recordFailedBargain() {
        mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                dao.updateInventory(
                    player.copy(
                        totalBargains = player.totalBargains + 1
                    )
                )
            }
        }
    }

    suspend fun buyLootBox(type: LootBoxType): List<MarketItem>? {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
            val currentBalance = player.balance.toDoubleOrNull().orZero()
            if (currentBalance < type.price) return@withTransaction null

            val maxCapacity = 5 + (player.shopLevel * 5)
            // Kutu içinden ortalama 2-3 eşya çıkar, o yüzden +3 kontrol edelim
            if (player.inventory.size + 3 > maxCapacity) return@withTransaction null

            val generatedItems = LootBoxGenerator.openBox(type)
            val newBalance = currentBalance - type.price

            val isAbsurdBox = generatedItems.any { it.itemName.contains("NASA", true) || it.itemName.contains("F-16", true) }
            val newBoughtAbsurd = player.hasBoughtAbsurd || isAbsurdBox

            val finalPlayer = processAchievements(
                player.copy(
                    balance = newBalance.toString(),
                    inventory = player.inventory + generatedItems,
                    itemsBought = player.itemsBought + generatedItems.size,
                    hasBoughtAbsurd = newBoughtAbsurd
                )
            )
            dao.updateInventory(finalPlayer)
            generatedItems
        }
        }
    }

    suspend fun sellItem(item: MarketItem, agreedPrice: Double? = null): Boolean {
        return mutex.withLock {
            database.withTransaction {
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
            val newHighestProfit = if (profit > player.highestProfit) profit else player.highestProfit
            val xpGain = GameConstants.SELL_BASE_XP +
                (profit / GameConstants.PROFIT_PER_XP).coerceAtLeast(0.0).toInt()
            val basePlayer = processXpGain(player, xpGain)
            val updatedListings = basePlayer.activeListings.filterNot { it.item.isSameInventoryItem(itemInInventory) }
            val newSoldCategories = try {
                if (basePlayer.soldCategories.isBlank()) mutableMapOf<String, Int>()
                else Json.decodeFromString<Map<String, Int>>(basePlayer.soldCategories).toMutableMap()
            } catch (e: Exception) {
                mutableMapOf<String, Int>()
            }
            newSoldCategories[itemInInventory.category] = (newSoldCategories[itemInInventory.category] ?: 0) + 1

            val finalPlayer = processAchievements(
                basePlayer.copy(
                    balance = (currentBalance + sellPrice).toString(),
                    inventory = basePlayer.inventory - itemInInventory,
                    activeListings = updatedListings,
                    itemsSold = basePlayer.itemsSold + 1,
                    totalProfit = basePlayer.totalProfit + profit,
                    dailyRevenue = basePlayer.dailyRevenue + sellPrice, // Ch8: Günlük ciroya ekle
                    highestProfit = newHighestProfit,
                    successfulBargains = basePlayer.successfulBargains + 1,
                    totalBargains = basePlayer.totalBargains + 1,
                    soldCategories = Json.encodeToString<Map<String, Int>>(newSoldCategories)
                )
            )

            dao.updateInventory(finalPlayer)
            true
        }
        }
    }

    suspend fun addListing(item: MarketItem, price: String): Boolean {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                val itemInInventory = player.inventory.find { it.isSameInventoryItem(item) }
                    ?: return@withTransaction false
                
                val newListing = com.enesduvan.kelepiravi.data.model.Listing(
                    item = itemInInventory,
                    listedPrice = price,
                    listedDay = player.currentDay
                )
                
                val finalPlayer = player.copy(
                    inventory = player.inventory - itemInInventory,
                    activeListings = player.activeListings + newListing
                )
                dao.updateInventory(finalPlayer)
                true
            }
        }
    }

    suspend fun updateListingPrice(listing: com.enesduvan.kelepiravi.data.model.Listing, newPrice: String): Boolean {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                val index = player.activeListings.indexOfFirst { it.id == listing.id }
                if (index == -1) return@withTransaction false
                
                val updatedListings = player.activeListings.toMutableList()
                updatedListings[index] = updatedListings[index].copy(listedPrice = newPrice)
                
                val finalPlayer = player.copy(activeListings = updatedListings)
                dao.updateInventory(finalPlayer)
                true
            }
        }
    }


    suspend fun removeListing(listing: com.enesduvan.kelepiravi.data.model.Listing): Boolean {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                val listingExists = player.activeListings.any { it.id == listing.id }
                if (!listingExists) return@withTransaction false
                
                val finalPlayer = player.copy(
                    activeListings = player.activeListings.filter { it.id != listing.id },
                    inventory = player.inventory + listing.item
                )
                dao.updateInventory(finalPlayer)
                true
            }
        }
    }

    suspend fun updateActiveListings(newListings: List<com.enesduvan.kelepiravi.data.model.Listing>) {
        mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                val finalPlayer = player.copy(activeListings = newListings)
                dao.updateInventory(finalPlayer)
            }
        }
    }

    suspend fun updateListing(updatedListing: com.enesduvan.kelepiravi.data.model.Listing): Boolean {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                val index = player.activeListings.indexOfFirst { it.id == updatedListing.id }
                if (index == -1) return@withTransaction false
                
                val newListings = player.activeListings.toMutableList()
                newListings[index] = updatedListing
                
                dao.updateInventory(player.copy(activeListings = newListings))
                true
            }
        }
    }

    suspend fun sellListing(listing: com.enesduvan.kelepiravi.data.model.Listing, agreedPrice: Double): Boolean {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                val listingExists = player.activeListings.any { it.id == listing.id }
                if (!listingExists) return@withTransaction false
                
                val purchasePrice = listing.item.purchasePrice
                    .ifEmpty { listing.item.salesValue }
                    .toDoubleOrNull()
                    .orZero()
                val currentBalance = player.balance.toDoubleOrNull().orZero()

                val profit = agreedPrice - purchasePrice
                val newHighestProfit = if (profit > player.highestProfit) profit else player.highestProfit
                val xpGain = GameConstants.SELL_BASE_XP +
                    (profit / GameConstants.PROFIT_PER_XP).coerceAtLeast(0.0).toInt()
                val basePlayer = processXpGain(player, xpGain)
                val newSoldCategories = try {
                    if (basePlayer.soldCategories.isBlank()) mutableMapOf<String, Int>()
                    else Json.decodeFromString<Map<String, Int>>(basePlayer.soldCategories).toMutableMap()
                } catch (e: Exception) {
                    mutableMapOf<String, Int>()
                }
                newSoldCategories[listing.item.category] = (newSoldCategories[listing.item.category] ?: 0) + 1

                val finalPlayer = processAchievements(
                    basePlayer.copy(
                        balance = (currentBalance + agreedPrice).toString(),
                        inventory = basePlayer.inventory.filterNot { it.isSameInventoryItem(listing.item) },
                        activeListings = player.activeListings.filter { it.id != listing.id },
                        itemsSold = basePlayer.itemsSold + 1,
                        totalProfit = basePlayer.totalProfit + profit,
                        dailyRevenue = basePlayer.dailyRevenue + agreedPrice, // Ch8: Günlük ciroya ekle
                        highestProfit = newHighestProfit,
                        successfulBargains = basePlayer.successfulBargains + 1,
                        totalBargains = basePlayer.totalBargains + 1,
                        soldCategories = Json.encodeToString<Map<String, Int>>(newSoldCategories)
                    )
                )

                dao.updateInventory(finalPlayer)
                true
            }
        }
    }

    fun calculateSellPrice(item: MarketItem): Double {
        val estimated = item.estimatedValue.toDoubleOrNull().orZero()
        val multiplier = MarketGenerator.getConditionMultiplier(item.condition)
        return ((estimated * multiplier) * GameConstants.SELL_PRICE_ROUNDING_SCALE).toLong() /
            GameConstants.SELL_PRICE_ROUNDING_SCALE
    }

    suspend fun advanceDay(): AdvanceDayResult {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
            val currentTrends = runCatching {
                if (player.marketTrends.isBlank()) emptyMap<String, Double>()
                else Json.decodeFromString<Map<String, Double>>(player.marketTrends)
            }.getOrDefault(emptyMap())

            val (updatedInventory, newTrends, event) = EconomyEngine.processNewDay(
                currentDay = player.currentDay,
                inventory = player.inventory,
                currentMarketTrends = currentTrends
            )

            val currentBalance = player.balance.toDoubleOrNull().orZero()
            
            // Ch8: Kira ve Vergi Hesaplaması
            val rent = GameConstants.DAILY_RENT_COST
            val tax = player.dailyRevenue * GameConstants.DAILY_TAX_RATE
            val totalDeduction = rent + tax
            val newBalance = currentBalance + GameConstants.DAILY_LOGIN_BONUS - totalDeduction

            val currentModifiers = runCatching {
                if (player.activeModifiers.isBlank()) mutableMapOf<String, Int>()
                else Json.decodeFromString<MutableMap<String, Int>>(player.activeModifiers)
            }.getOrDefault(mutableMapOf())
            
            // Decrement and remove expired modifiers
            val it = currentModifiers.entries.iterator()
            while (it.hasNext()) {
                val entry = it.next()
                if (entry.value <= 1) {
                    it.remove()
                } else {
                    entry.setValue(entry.value - 1)
                }
            }
            val newModifiersStr = Json.encodeToString(currentModifiers)

            val basePlayer = processXpGain(player, GameConstants.DAILY_LOGIN_XP)
            val updatedListings = com.enesduvan.kelepiravi.data.listing.ListingEngine.processDay(player.activeListings)
            
            val finalPlayer = processAchievements(
                basePlayer.copy(
                    currentDay = player.currentDay + 1,
                    inventory = updatedInventory,
                    activeListings = updatedListings,
                    balance = newBalance.toString(),
                    marketTrends = Json.encodeToString(newTrends),
                    // Günlük tamir sayacı sıfırlanır (yeni gün = yeni hak)
                    dailyRepairsUsed = 0,
                    // Günlük ciro sıfırlanır
                    dailyRevenue = 0.0,
                    activeModifiers = newModifiersStr
                )
            )

            dao.updateInventory(finalPlayer)
            
            // Ch6: Rastgele interaktif event çekme (Phase 2 Event Engine)
            val allEvents = EventLoader.loadEvents(context)
            val availableEvents = EventManager.getAvailableEvents(finalPlayer, allEvents)
            // Sadece %30 ihtimalle interaktif event çıksın ki her gün oyun durmasın
            val pickedInteractiveEvent = if (Math.random() < 0.3) {
                EventManager.pickRandomEvent(availableEvents)
            } else null
            
            AdvanceDayResult(event, pickedInteractiveEvent, rent, tax)
        }
        }
    }

    suspend fun applyEventChoice(choice: EventChoice): List<String> {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                var currentBalance = player.balance.toDoubleOrNull().orZero()
                var newXp = player.xp
                val newInventory = player.inventory.toMutableList()
                val generatedItemNames = mutableListOf<String>()
                
                // Apply Rewards
                for (reward in choice.rewards) {
                    when (reward.type) {
                        "MONEY" -> currentBalance += reward.value.toDoubleOrNull().orZero()
                        "XP" -> newXp += reward.value.toIntOrNull() ?: 0
                        "ITEM" -> {
                            val product = MarketGenerator.PRODUCTS.find { it.name == reward.value } 
                                ?: MarketGenerator.PRODUCTS.random()
                            val item = MarketGenerator.generateNormalItem(kotlin.random.Random.Default, product, emptyMap())
                            val enrichedItem = item.copy(
                                purchasePrice = "0.0", 
                                purchaseDate = LocalDate.now().toString(),
                                sellerName = "Olay Hediyesi"
                            )
                            newInventory.add(enrichedItem)
                            generatedItemNames.add(enrichedItem.itemName)
                        }
                    }
                }
                
                // Apply Penalties
                for (penalty in choice.penalties) {
                    when (penalty.type) {
                        "MONEY_EXACT" -> currentBalance -= penalty.value.toDoubleOrNull().orZero()
                        "MONEY_PERCENT" -> {
                            val percent = penalty.value.toDoubleOrNull().orZero()
                            currentBalance -= currentBalance * (percent / 100.0)
                        }
                        "XP" -> newXp -= penalty.value.toIntOrNull() ?: 0
                    }
                }
                
                if (currentBalance < 0) currentBalance = 0.0
                
                // Apply Flags and Modifiers
                val currentFlags = player.eventFlags.split(",").filter { it.isNotEmpty() }.toMutableSet()
                val currentModifiers = runCatching {
                    if (player.activeModifiers.isBlank()) mutableMapOf<String, Int>()
                    else Json.decodeFromString<MutableMap<String, Int>>(player.activeModifiers)
                }.getOrDefault(mutableMapOf())

                for (flag in choice.flags) {
                    if (flag.startsWith("MODIFIER:")) {
                        val parts = flag.split(":")
                        if (parts.size >= 3) {
                            val modifierName = parts[1]
                            val duration = parts[2].toIntOrNull() ?: 0
                            if (duration > 0) {
                                currentModifiers[modifierName] = duration
                            }
                        }
                    } else {
                        currentFlags.add(flag)
                    }
                }
                val newFlagsStr = currentFlags.joinToString(",")
                val newModifiersStr = Json.encodeToString(currentModifiers)
                
                // Update player
                val xpGain = newXp - player.xp
                val basePlayer = if (xpGain > 0) processXpGain(player, xpGain) else player
                val updatedPlayer = basePlayer.copy(
                    balance = currentBalance.toString(),
                    inventory = newInventory,
                    eventFlags = newFlagsStr,
                    activeModifiers = newModifiersStr
                )
                
                dao.updateInventory(updatedPlayer)
                generatedItemNames
            }
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

    // Ch10: Yükseltmeler
    suspend fun upgradeShop(cost: Double): Boolean {
        return mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
            val currentBalance = player.balance.toDoubleOrNull().orZero()
            if (currentBalance < cost) return@withTransaction false
            if (player.shopLevel >= 5) return@withTransaction false // Maksimum seviye

            dao.updateInventory(
                player.copy(
                    shopLevel = player.shopLevel + 1,
                    balance = (currentBalance - cost).toString()
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
            val currentBalance = player.balance.toDoubleOrNull().orZero()
            if (currentBalance < cost) return@withTransaction false
            if (player.mechanicLevel >= 5) return@withTransaction false // Maksimum seviye

            dao.updateInventory(
                player.copy(
                    mechanicLevel = player.mechanicLevel + 1,
                    balance = (currentBalance - cost).toString()
                )
            )
            true
        }
        }
    }

    suspend fun updateNpcRelationship(npcName: String, delta: Int) {
        if (delta == 0) return
        mutex.withLock {
            database.withTransaction {
                val player = getPlayerOrCreate()
                val currentRels = runCatching {
                    if (player.npcRelationships.isBlank()) mutableMapOf<String, Int>()
                    else kotlinx.serialization.json.Json.decodeFromString<MutableMap<String, Int>>(player.npcRelationships)
                }.getOrDefault(mutableMapOf())

                val currentScore = currentRels[npcName] ?: 0
                // Minimum ve maksimum sınır koyabiliriz, şimdilik serbest
                currentRels[npcName] = currentScore + delta

                dao.updateInventory(
                    player.copy(
                        npcRelationships = kotlinx.serialization.json.Json.encodeToString(currentRels)
                    )
                )
            }
        }
    }

    private fun MarketItem.isSameInventoryItem(other: MarketItem): Boolean {
        return itemName == other.itemName &&
            sellerName == other.sellerName &&
            purchaseDate == other.purchaseDate
    }

    private fun Double?.orZero(): Double = this ?: 0.0
}

// Ch6: Tamir sonuç durumu
sealed class RepairResult {
    object Success : RepairResult()
    data class Failure(val newCondition: String) : RepairResult()
    object LimitReached : RepairResult()
    object NotEnoughMoney : RepairResult()
}

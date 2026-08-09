package com.enesduvan.kelepiravi.domain.repository

import com.enesduvan.kelepiravi.data.event.EventChoice
import com.enesduvan.kelepiravi.data.market.LootBoxType
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.data.model.Listing
import com.enesduvan.kelepiravi.data.repository.AdvanceDayResult
import com.enesduvan.kelepiravi.domain.model.RepairResult
import com.enesduvan.kelepiravi.database.entity.UserInventoryEntity
import com.enesduvan.kelepiravi.database.entity.PlayerStatisticsEntity
import kotlinx.coroutines.flow.Flow

interface IKelepiraviRepository {
    fun getPlayerState(): Flow<List<UserInventoryEntity>>
    suspend fun initializePlayerIfNeeded()
    fun observeUserInventoryItems(): Flow<List<MarketItem>>
    fun observePlayerStatistics(): Flow<PlayerStatisticsEntity?>
    fun observeActiveListings(): Flow<List<Listing>>
    suspend fun getUserInventoryItems(): List<MarketItem>
    suspend fun purchaseItem(item: MarketItem): Boolean
    suspend fun repairItem(item: MarketItem, isUsta: Boolean): RepairResult
    fun calculateRepairCost(item: MarketItem, isUsta: Boolean): Double
    suspend fun upgradeShop(cost: Double): Boolean
    suspend fun upgradeMechanic(cost: Double): Boolean
    suspend fun advanceDay(): AdvanceDayResult
    suspend fun applyEventChoice(choice: EventChoice): List<MarketItem>
    suspend fun buyLootBox(type: LootBoxType): List<MarketItem>?
    fun calculateSellPrice(item: MarketItem): Double
    suspend fun recordSuccessfulBargain(category: String, profit: Double)
    suspend fun recordFailedBargain()
    suspend fun addListing(item: MarketItem, price: String): Boolean
    suspend fun updateListingPrice(listing: Listing, newPrice: String): Boolean
    suspend fun removeListing(listing: Listing): Boolean
    suspend fun updateActiveListings(newListings: List<Listing>)
    suspend fun sellListing(listing: Listing, agreedPrice: Double): Boolean
    suspend fun sellItem(item: MarketItem, agreedPrice: Double? = null): Boolean
    suspend fun updateNpcRelationship(npcName: String, delta: Int)
}

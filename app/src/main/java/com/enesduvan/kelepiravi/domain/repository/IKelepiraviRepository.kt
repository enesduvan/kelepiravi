package com.enesduvan.kelepiravi.domain.repository

import com.enesduvan.kelepiravi.data.event.EventChoice
import com.enesduvan.kelepiravi.data.market.LootBoxType
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.data.repository.AdvanceDayResult
import com.enesduvan.kelepiravi.data.repository.RepairResult
import com.enesduvan.kelepiravi.database.entity.UserInventoryEntity
import kotlinx.coroutines.flow.Flow

interface IKelepiraviRepository {
    fun getPlayerState(): Flow<List<UserInventoryEntity>>
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
}

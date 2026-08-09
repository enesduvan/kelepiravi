package com.enesduvan.kelepiravi.domain.model

import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.data.model.MarketItem

/** Business-facing player snapshot. UI observes this through a ViewModel. */
data class PlayerState(
    val balance: Long = GameConstants.INITIAL_BALANCE,
    val inventory: List<MarketItem> = emptyList(),
    val currentDay: Int = 1,
    val xp: Int = 0,
    val level: Int = 1,
    val totalProfit: Double = 0.0,
    val itemsBought: Int = 0,
    val itemsSold: Int = 0,
    val unlockedAchievements: String = "",
    val marketTrends: Map<String, Double> = emptyMap(),
    val npcRelationships: Map<String, Int> = emptyMap(),
    val highestProfit: Double = 0.0,
    val rareItemsFound: Int = 0,
    val totalRepairs: Int = 0,
    val hasBoughtScam: Boolean = false,
    val hasBoughtAbsurd: Boolean = false,
    val dailyRepairsUsed: Int = 0,
    val lastRepairDay: Int = 0,
    val dailyRevenue: Double = 0.0,
    val shopLevel: Int = 1,
    val mechanicLevel: Int = 1,
    val successfulBargains: Int = 0,
    val totalBargains: Int = 0,
    val soldCategories: Map<String, Int> = emptyMap(),
    val portfolioValue: Double = 0.0,
    val totalInvestment: Double = 0.0,
    val portfolioROI: Double = 0.0,
    val activeModifiers: Map<String, Int> = emptyMap()
)

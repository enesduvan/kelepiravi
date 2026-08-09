package com.enesduvan.kelepiravi.domain.model

import com.enesduvan.kelepiravi.data.BargainConstants
import com.enesduvan.kelepiravi.data.market.Achievement
import com.enesduvan.kelepiravi.data.market.DailyEvent
import com.enesduvan.kelepiravi.data.model.MarketItem
import java.util.UUID

data class MarketUiState(
    val marketItems: List<MarketItem> = emptyList(),
    val isRefreshing: Boolean = false,
    val selectedItem: MarketItem? = null,
    val selectedCategory: String = "Tümü",
    val searchQuery: String = "",
    val isDayAdvancing: Boolean = false,
    val isLootBoxSheetOpen: Boolean = false,
    val purchasedLootBoxItems: List<MarketItem>? = null,
    val latestAchievement: Achievement? = null
)

data class BargainMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromPlayer: Boolean,
    val timestamp: String
)

data class BargainState(
    val item: MarketItem,
    val messages: List<BargainMessage> = emptyList(),
    val sellerPatience: Int = BargainConstants.STARTING_PATIENCE,
    val sellerMood: String = "Kararsız",
    val isDealClosed: Boolean = false,
    val isFailed: Boolean = false,
    val agreedPrice: Double = 0.0,
    val isScamPromptActive: Boolean = false,
    val isScamSuccess: Boolean = false,
    val isScamFailed: Boolean = false,
    val suggestedPrice: Double = 0.0,
    val lastSellerOffer: Double = 0.0,
    val lastPlayerOfferAmount: Double = 0.0,
    val npcRelationshipScore: Int = 0,
    val relationshipDelta: Int = 0
)

data class SellBargainState(
    val item: MarketItem,
    val buyerName: String = "Alıcı",
    val buyerTitle: String = "Müşteri",
    val initialOffer: Double = 0.0,
    val messages: List<BargainMessage> = emptyList(),
    val buyerPatience: Int = BargainConstants.STARTING_PATIENCE,
    val buyerMood: String = "Kararsız",
    val isDealClosed: Boolean = false,
    val isFailed: Boolean = false,
    val agreedPrice: Double = 0.0,
    val baseSellPrice: Double = 0.0,
    val lastBuyerOffer: Double = 0.0,
    val lastPlayerOfferAmount: Double = 0.0,
    val npcRelationshipScore: Int = 0,
    val relationshipDelta: Int = 0
)

data class DailySummaryState(
    val day: Int,
    val xpGained: Int,
    val bonusMoney: Double,
    val taxPaid: Double = 0.0,
    val rentPaid: Double = 0.0,
    val event: DailyEvent? = null
)

data class SellerProfileState(
    val name: String,
    val title: String,
    val rating: Double,
    val joinYear: Int,
    val totalSales: Int,
    val items: List<MarketItem>
) {
    val sellerName: String get() = name
    val sellerTitle: String get() = title
    val otherItems: List<MarketItem> get() = items
}

data class RepairResultState(
    val isSuccess: Boolean,
    val newCondition: String = "",
    val itemName: String = ""
)

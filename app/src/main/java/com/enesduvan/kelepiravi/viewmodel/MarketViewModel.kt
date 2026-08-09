package com.enesduvan.kelepiravi.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.enesduvan.kelepiravi.data.BargainConstants
import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.data.event.EventChoice
import com.enesduvan.kelepiravi.data.event.EventDefinition
import com.enesduvan.kelepiravi.data.local.SettingsManager
import com.enesduvan.kelepiravi.data.market.Achievement
import com.enesduvan.kelepiravi.data.market.AchievementManager
import com.enesduvan.kelepiravi.data.market.DailyEvent
import com.enesduvan.kelepiravi.data.market.LootBoxType
import com.enesduvan.kelepiravi.data.market.MarketGenerator
import com.enesduvan.kelepiravi.data.market.NegotiationEngine
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.data.repository.KelepiraviRepository
import com.enesduvan.kelepiravi.data.repository.RepairResult
import com.enesduvan.kelepiravi.ui.shared.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

// ─── State Modelleri ──────────────────────────────────────────────────────────

@Immutable
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

@Stable
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

@Immutable
data class BargainMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromPlayer: Boolean,
    val timestamp: String
)

@Stable
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

@Stable
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

@Immutable
data class DailySummaryState(
    val day: Int,
    val xpGained: Int,
    val bonusMoney: Double,
    val taxPaid: Double = 0.0,
    val rentPaid: Double = 0.0,
    val event: DailyEvent? = null
)

@Immutable
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

@Immutable
data class RepairResultState(
    val isSuccess: Boolean,
    val newCondition: String = "",
    val itemName: String = ""
)

// ─── MarketViewModel ──────────────────────────────────────────────────────────

class MarketViewModel(
    private val repository: KelepiraviRepository,
    private val settingsManager: SettingsManager,
    private val soundManager: SoundManager
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = repository
        .getPlayerState()
        .map { list ->
            val entity = list.firstOrNull { it.playerId == GameConstants.DEFAULT_USER_ID }
            PlayerState(
                balance          = entity?.balance ?: GameConstants.INITIAL_BALANCE,
                inventory        = emptyList(),
                currentDay       = entity?.currentDay ?: 1,
                xp               = entity?.xp ?: 0,
                level            = entity?.level ?: 1,
                shopLevel        = entity?.shopLevel ?: 1,
                mechanicLevel    = entity?.mechanicLevel ?: 1,
                unlockedAchievements = entity?.unlockedAchievements ?: "",
                totalRepairs     = entity?.totalRepairs ?: 0,
                hasBoughtScam    = entity?.hasBoughtScam ?: false,
                hasBoughtAbsurd  = entity?.hasBoughtAbsurd ?: false,
                dailyRepairsUsed = entity?.dailyRepairsUsed ?: 0,
                lastRepairDay    = entity?.lastRepairDay ?: 0
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlayerState()
        )

    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    private val _scamReveal = MutableStateFlow<MarketItem?>(null)
    val scamReveal: StateFlow<MarketItem?> = _scamReveal.asStateFlow()

    private val _flashNotification = MutableStateFlow<String?>(null)
    val flashNotification: StateFlow<String?> = _flashNotification.asStateFlow()

    private val negotiationEngine = NegotiationEngine(
        scope = viewModelScope,
        repository = repository,
        uiStateFlow = _uiState,
        scamRevealFlow = _scamReveal
    )

    val bargainState: StateFlow<BargainState?> = negotiationEngine.bargainState
    val sellBargainState: StateFlow<SellBargainState?> = negotiationEngine.sellBargainState

    private val _dailySummary = MutableStateFlow<DailySummaryState?>(null)
    val dailySummary: StateFlow<DailySummaryState?> = _dailySummary.asStateFlow()

    private val _interactiveEvent = MutableStateFlow<EventDefinition?>(null)
    val interactiveEvent: StateFlow<EventDefinition?> = _interactiveEvent.asStateFlow()

    private val _eventResult = MutableStateFlow<String?>(null)
    val eventResult: StateFlow<String?> = _eventResult.asStateFlow()

    private val _repairResult = MutableStateFlow<RepairResultState?>(null)
    val repairResult: StateFlow<RepairResultState?> = _repairResult.asStateFlow()

    private val _sellerProfile = MutableStateFlow<SellerProfileState?>(null)
    val sellerProfile: StateFlow<SellerProfileState?> = _sellerProfile.asStateFlow()

    val isHapticEnabled = settingsManager.isHapticEnabled

    init {
        refreshMarket()
        startFlashDealsTicker()
    }

    // ─── Pazar Listeleme & Filtreleme ─────────────────────────────────────────

    fun refreshMarket() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        viewModelScope.launch {
            val state = playerState.value
            val count = 10 + (state.shopLevel * 2)
            val newItems = MarketGenerator.generateItems(
                count = count,
                marketTrends = state.marketTrends,
                activeModifiers = state.activeModifiers
            )
            _uiState.value = _uiState.value.copy(
                marketItems = newItems,
                isRefreshing = false
            )
        }
    }

    fun loadMoreItems() {
        viewModelScope.launch {
            val state = playerState.value
            val moreItems = MarketGenerator.generateItems(
                count = 4,
                marketTrends = state.marketTrends,
                activeModifiers = state.activeModifiers
            )
            _uiState.value = _uiState.value.copy(
                marketItems = _uiState.value.marketItems + moreItems
            )
        }
    }

    fun filteredMarketItems(state: MarketUiState = _uiState.value): List<MarketItem> {
        val query = state.searchQuery.trim().lowercase()
        return state.marketItems.filter { item ->
            val matchesCategory = state.selectedCategory == "Tümü" || item.category.equals(state.selectedCategory, ignoreCase = true)
            val matchesQuery = query.isEmpty() || item.itemName.lowercase().contains(query) || item.sellerName.lowercase().contains(query)
            matchesCategory && matchesQuery
        }
    }

    fun updateSearchQuery(query: String) {
        onSearchQueryChanged(query)
    }

    fun selectCategory(category: String) {
        soundManager.playClickSound()
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectItem(item: MarketItem) {
        soundManager.playClickSound()
        _uiState.value = _uiState.value.copy(selectedItem = item)
    }

    fun clearSelectedItem() {
        _uiState.value = _uiState.value.copy(selectedItem = null)
    }

    // ─── Satın Alma & Scam ───────────────────────────────────────────────────

    fun purchaseItem(item: MarketItem) {
        viewModelScope.launch {
            val success = repository.purchaseItem(item)
            if (success) {
                soundManager.playCoinSound()
                _uiState.value = _uiState.value.copy(
                    marketItems = _uiState.value.marketItems.filterNot { it.itemName == item.itemName && it.sellerName == item.sellerName },
                    selectedItem = null
                )

                val newlyUnlocked = AchievementManager.checkAchievements(
                    balance = playerState.value.balance.toDouble(),
                    itemsBought = playerState.value.itemsBought + 1,
                    itemsSold = playerState.value.itemsSold,
                    totalRepairs = playerState.value.totalRepairs,
                    boughtScam = playerState.value.hasBoughtScam || item.isScammer,
                    boughtAbsurd = playerState.value.hasBoughtAbsurd,
                    unlockedIds = playerState.value.unlockedAchievements.split(",").filter { it.isNotBlank() }
                )
                if (newlyUnlocked.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(latestAchievement = newlyUnlocked.first())
                    viewModelScope.launch {
                        delay(4000)
                        _uiState.value = _uiState.value.copy(latestAchievement = null)
                    }
                }

                if (item.isScammer) {
                    _scamReveal.value = item
                }
            }
        }
    }

    fun dismissScamReveal() {
        _scamReveal.value = null
    }

    // ─── Pazarlık Motoru Delegasyonları ──────────────────────────────────────

    fun startBargain(item: MarketItem) {
        negotiationEngine.startBargain(item, playerState.value.npcRelationships)
    }

    fun closeBargain() {
        negotiationEngine.closeBargain()
    }

    fun sendOffer(offerAmount: Double) {
        soundManager.playClickSound()
        negotiationEngine.sendOffer(offerAmount)
    }

    fun sendMoneyToScammer() {
        negotiationEngine.sendMoneyToScammer()
    }

    fun cancelScamDeal() {
        negotiationEngine.cancelScamDeal()
    }

    fun buyAgreedItem() {
        soundManager.playCoinSound()
        negotiationEngine.buyAgreedItem()
    }

    fun startSellBargain(item: MarketItem) {
        negotiationEngine.startSellBargain(item, playerState.value.npcRelationships)
    }

    fun startSellBargainWithOffer(item: MarketItem, buyerName: String, offerAmount: Double) {
        negotiationEngine.startSellBargainWithOffer(item, playerState.value.npcRelationships, buyerName, offerAmount)
    }

    fun closeSellBargain() {
        negotiationEngine.closeSellBargain()
    }

    fun sendSellOffer(offerAmount: Double) {
        soundManager.playClickSound()
        negotiationEngine.sendSellOffer(offerAmount)
    }

    fun sellAgreedItem() {
        soundManager.playCoinSound()
        negotiationEngine.sellAgreedItem()
    }

    // ─── Tamir Atölyesi Delegasyonları ────────────────────────────────────────

    fun getRemainingRepairs(): Int {
        val state = playerState.value
        return if (state.lastRepairDay != state.currentDay) GameConstants.DAILY_REPAIR_LIMIT
        else (GameConstants.DAILY_REPAIR_LIMIT - state.dailyRepairsUsed).coerceAtLeast(0)
    }

    fun calculateRepairCost(item: MarketItem, isUsta: Boolean = false): Double {
        return repository.calculateRepairCost(item, isUsta)
    }

    fun repairItem(item: MarketItem, isUsta: Boolean = false) {
        viewModelScope.launch {
            when (val result = repository.repairItem(item, isUsta)) {
                is RepairResult.Success -> {
                    _repairResult.value = RepairResultState(isSuccess = true, itemName = item.itemName)
                }
                is RepairResult.Failure -> {
                    _repairResult.value = RepairResultState(isSuccess = false, newCondition = result.newCondition, itemName = item.itemName)
                }
                is RepairResult.LimitReached -> {}
                is RepairResult.NotEnoughMoney -> {}
            }
        }
    }

    fun dismissRepairResult() {
        _repairResult.value = null
    }

    // ─── Profil & Yükseltmeler Delegasyonları ──────────────────────────────────

    fun openSellerProfile(sellerName: String, sellerTitle: String) {
        val rng = Random(sellerName.hashCode())
        val isScammer = MarketGenerator.SCAMMER_SELLERS.contains(sellerName)
        val rating = if (isScammer) rng.nextDouble(1.0, 3.5) else rng.nextDouble(3.8, 5.0)
        val joinYear = rng.nextInt(2015, 2024)
        val totalSales = rng.nextInt(1, 500)
        val otherItems = List(rng.nextInt(2, 4)) { MarketGenerator.generateItemForSeller(rng, sellerName) }
        _sellerProfile.value = SellerProfileState(sellerName, sellerTitle, rating, joinYear, totalSales, otherItems)
    }

    fun closeSellerProfile() {
        _sellerProfile.value = null
    }

    fun getShopUpgradeCost(level: Int): Double = when (level) {
        1 -> 15000.0
        2 -> 50000.0
        3 -> 150000.0
        4 -> 500000.0
        else -> 0.0
    }

    fun getMechanicUpgradeCost(level: Int): Double = when (level) {
        1 -> 20000.0
        2 -> 75000.0
        3 -> 250000.0
        4 -> 1000000.0
        else -> 0.0
    }

    fun upgradeShop() {
        val level = playerState.value.shopLevel
        if (level >= 5) return
        val cost = getShopUpgradeCost(level)
        viewModelScope.launch { repository.upgradeShop(cost) }
    }

    fun upgradeMechanic() {
        val level = playerState.value.mechanicLevel
        if (level >= 5) return
        val cost = getMechanicUpgradeCost(level)
        viewModelScope.launch { repository.upgradeMechanic(cost) }
    }

    // ─── Gün İlerletme & Etkinlikler ──────────────────────────────────────────

    fun advanceDay(onDayAdvanced: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.advanceDay()
            val nextDay = playerState.value.currentDay + 1
            _dailySummary.value = DailySummaryState(
                day = nextDay,
                xpGained = GameConstants.DAILY_LOGIN_XP,
                bonusMoney = GameConstants.DAILY_LOGIN_BONUS,
                taxPaid = result.taxPaid,
                rentPaid = result.rentPaid,
                event = result.event
            )
            _interactiveEvent.value = result.interactiveEvent
            onDayAdvanced()
        }
    }

    fun dismissDailySummary() { _dailySummary.value = null }
    fun dismissInteractiveEvent() { _interactiveEvent.value = null }
    fun applyInteractiveEventChoice(choice: EventChoice) {
        viewModelScope.launch {
            val generatedItems = repository.applyEventChoice(choice)
            _interactiveEvent.value = null
            _eventResult.value = choice.outcomeText ?: "Etkinlik tamamlandı."
        }
    }
    fun dismissEventResult() { _eventResult.value = null }

    // ─── LootBox (Zamazon Kutu) ──────────────────────────────────────────────

    fun setLootBoxSheetVisible(isVisible: Boolean) {
        _uiState.value = _uiState.value.copy(isLootBoxSheetOpen = isVisible)
    }

    fun buyLootBox(type: LootBoxType) {
        viewModelScope.launch {
            val resultItems = repository.buyLootBox(type)
            if (resultItems != null) {
                soundManager.playCoinSound()
                _uiState.value = _uiState.value.copy(
                    isLootBoxSheetOpen = false,
                    purchasedLootBoxItems = resultItems
                )
            }
        }
    }

    fun dismissLootBoxReveal() {
        _uiState.value = _uiState.value.copy(purchasedLootBoxItems = null)
    }

    // ─── Flaş Fırsat Ticker ─────────────────────────────────────────────────

    private fun startFlashDealsTicker() {
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                val currentItems = _uiState.value.marketItems
                if (currentItems.isNotEmpty() && Random.nextDouble() < 0.4) {
                    val targetIndex = currentItems.indices.random()
                    val targetItem = currentItems[targetIndex]
                    val discountedPrice = (targetItem.salesValue * 0.7).toLong()
                    val updatedItem = targetItem.copy(salesValue = discountedPrice)

                    val newItems = currentItems.toMutableList()
                    newItems[targetIndex] = updatedItem

                    _uiState.value = _uiState.value.copy(marketItems = newItems)
                    _flashNotification.value = "⚡ Flaş Fırsat: ${updatedItem.itemName} %30 indirimde!"

                    delay(5_000)
                    _flashNotification.value = null
                }
            }
        }
    }
}

class MarketViewModelFactory(
    private val repository: KelepiraviRepository,
    private val settingsManager: SettingsManager,
    private val soundManager: SoundManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarketViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MarketViewModel(repository, settingsManager, soundManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

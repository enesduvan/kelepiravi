package com.enesduvan.kelepiravi.ui.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.enesduvan.kelepiravi.data.BargainConstants
import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.data.event.EventChoice
import com.enesduvan.kelepiravi.data.event.EventDefinition
import com.enesduvan.kelepiravi.data.market.DailyEvent
import com.enesduvan.kelepiravi.data.market.EconomyEngine
import com.enesduvan.kelepiravi.data.market.LootBoxType
import com.enesduvan.kelepiravi.data.market.MarketGenerator
import com.enesduvan.kelepiravi.data.market.SellerPersonality
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.data.repository.KelepiraviRepository
import com.enesduvan.kelepiravi.data.repository.RepairResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ─── State Modelleri ──────────────────────────────────────────────────────────

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
data class PlayerState(
    val balance: String = GameConstants.INITIAL_BALANCE,
    val inventory: List<MarketItem> = emptyList(),
    val currentDay: Int = 1,
    val xp: Int = 0,
    val level: Int = 1,
    val totalProfit: Double = 0.0,
    val itemsBought: Int = 0,
    val itemsSold: Int = 0,
    val unlockedAchievements: String = "",
    val marketTrends: Map<String, Double> = emptyMap(),
    val dailyRepairsUsed: Int = 0,   // Ch6
    val lastRepairDay: Int = 0,      // Ch6
    val dailyRevenue: Double = 0.0,  // Ch8: Vergi hesaplaması için günlük ciro
    val shopLevel: Int = 1,          // Ch10: Dükkan kapasitesi
    val mechanicLevel: Int = 1,      // Ch10: Usta becerisi
    // Türetilmiş ekonomi değerleri
    val portfolioValue: Double = 0.0,
    val totalInvestment: Double = 0.0,
    val portfolioROI: Double = 0.0
)

@Stable
data class MarketUiState(
    val marketItems: List<MarketItem> = emptyList(),
    val isRefreshing: Boolean = false,
    val selectedItem: MarketItem? = null,
    val selectedCategory: String = "Tümü",
    val isDayAdvancing: Boolean = false,
    val isLootBoxSheetOpen: Boolean = false, // Ch9: Zamazon Kutu seçim ekranı
    val purchasedLootBoxItems: List<MarketItem>? = null // Ch9: Kutudan çıkan eşyalar (reveal için)
)

@Immutable
data class BargainMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
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
    val suggestedPrice: Double = 0.0,
    val lastSellerOffer: Double? = null,
    val lastPlayerOfferAmount: Double? = null,  // Ch6: Tekrar teklif tespiti için
    val isScamPromptActive: Boolean = false // Ch6: Dolandırıcı ödeme onayı
)

@Immutable
data class DailySummaryState(
    val day: Int,
    val xpGained: Int,
    val bonusMoney: Double,
    val taxPaid: Double,     // Ch8: Vergi kesintisi
    val rentPaid: Double,    // Ch8: Kira kesintisi
    val event: DailyEvent?
)

@Stable
data class SellBargainState(
    val item: MarketItem,
    val buyerName: String,
    val messages: List<BargainMessage> = emptyList(),
    val buyerPatience: Int = BargainConstants.STARTING_PATIENCE,
    val buyerMood: String = "Kararsız",
    val isDealClosed: Boolean = false,
    val isFailed: Boolean = false,
    val agreedPrice: Double = 0.0,
    val baseSellPrice: Double = 0.0,
    val lastBuyerOffer: Double? = null,
    val lastPlayerOfferAmount: Double? = null  // Ch6: Tekrar teklif tespiti için
)

// Ch6: Tamir sonuç gösterimi
@Immutable
data class RepairResultState(
    val isSuccess: Boolean,
    val newCondition: String = "",
    val itemName: String = ""
)

// ─── ViewModel ───────────────────────────────────────────────────────────────

class MarketViewModel(
    private val repository: KelepiraviRepository
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = repository
        .getPlayerState()
        .map { list ->
            val entity = list.firstOrNull { it.id == GameConstants.DEFAULT_USER_ID }
            val inventory = entity?.inventory ?: emptyList()
            PlayerState(
                balance = entity?.balance ?: GameConstants.INITIAL_BALANCE,
                inventory = inventory,
                currentDay = entity?.currentDay ?: 1,
                xp = entity?.xp ?: 0,
                level = entity?.level ?: 1,
                shopLevel = entity?.shopLevel ?: 0,
                mechanicLevel = entity?.mechanicLevel ?: 0,
                totalProfit = entity?.totalProfit ?: 0.0,
                itemsBought = entity?.itemsBought ?: 0,
                itemsSold = entity?.itemsSold ?: 0,
                unlockedAchievements = entity?.unlockedAchievements ?: "",
                marketTrends = runCatching {
                    if (entity?.marketTrends.isNullOrBlank()) emptyMap()
                    else kotlinx.serialization.json.Json.decodeFromString<Map<String, Double>>(entity!!.marketTrends)
                }.getOrDefault(emptyMap()),
                dailyRepairsUsed = entity?.dailyRepairsUsed ?: 0,
                lastRepairDay = entity?.lastRepairDay ?: 0,
                dailyRevenue = entity?.dailyRevenue ?: 0.0,
                portfolioValue = EconomyEngine.calculatePortfolioValue(inventory),
                totalInvestment = EconomyEngine.calculateTotalInvestment(inventory),
                portfolioROI = EconomyEngine.calculatePortfolioROI(inventory)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlayerState()
        )

    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    /** Aktif günlük olay */
    private val _dayEvent = MutableStateFlow<DailyEvent?>(null)
    val dayEvent: StateFlow<DailyEvent?> = _dayEvent.asStateFlow()

    /** Ch6: Dolandırıcıdan alındıktan sonra reveal dialogu */
    private val _scamReveal = MutableStateFlow<MarketItem?>(null)
    val scamReveal: StateFlow<MarketItem?> = _scamReveal.asStateFlow()

    private val negotiationEngine = NegotiationEngine(viewModelScope, repository, _uiState, _scamReveal)
    val bargainState: StateFlow<BargainState?> = negotiationEngine.bargainState
    val sellBargainState: StateFlow<SellBargainState?> = negotiationEngine.sellBargainState

    private val _dailySummary = MutableStateFlow<DailySummaryState?>(null)
    val dailySummary: StateFlow<DailySummaryState?> = _dailySummary.asStateFlow()

    /** Ch6: Rastgele Event Engine olayları */
    private val _interactiveEvent = MutableStateFlow<EventDefinition?>(null)
    val interactiveEvent: StateFlow<EventDefinition?> = _interactiveEvent.asStateFlow()

    private val _eventResult = MutableStateFlow<String?>(null)
    val eventResult: StateFlow<String?> = _eventResult.asStateFlow()

    /** Ch6: Tamir sonuç dialogu */
    private val _repairResult = MutableStateFlow<RepairResultState?>(null)
    val repairResult: StateFlow<RepairResultState?> = _repairResult.asStateFlow()

    init {
        viewModelScope.launch { repository.initializePlayerIfNeeded() }
        refreshMarket()
    }

    fun refreshMarket() {
        val currentTrends = _playerStateForGenerator()?.marketTrends ?: emptyMap()
        _uiState.value = _uiState.value.copy(
            marketItems = MarketGenerator.generateItems(GameConstants.MARKET_BATCH_SIZE, currentTrends),
            isRefreshing = false,
            selectedCategory = "Tümü"
        )
    }

    fun loadMoreItems() {
        val currentTrends = _playerStateForGenerator()?.marketTrends ?: emptyMap()
        val currentItems = _uiState.value.marketItems
        val newItems = MarketGenerator.generateItems(GameConstants.MARKET_BATCH_SIZE, currentTrends)
        _uiState.value = _uiState.value.copy(
            marketItems = currentItems + newItems
        )
    }

    private fun _playerStateForGenerator(): PlayerState? = playerState.value

    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun selectItem(item: MarketItem) {
        _uiState.value = _uiState.value.copy(selectedItem = item)
    }

    fun clearSelectedItem() {
        _uiState.value = _uiState.value.copy(selectedItem = null)
    }

    fun purchaseItem(item: MarketItem) {
        viewModelScope.launch {
            val success = repository.purchaseItem(item)
            if (success) {
                _uiState.value = _uiState.value.copy(
                    marketItems = _uiState.value.marketItems.filterNot { it == item },
                    selectedItem = null
                )
                // Ch6: Dolandırıcıdan alındıysa reveal göster
                if (item.isScammer && item.hiddenCondition.isNotEmpty()) {
                    _scamReveal.value = item
                }
            }
        }
    }

    fun dismissScamReveal() {
        _scamReveal.value = null
    }

    fun setLootBoxSheetVisible(isVisible: Boolean) {
        _uiState.value = _uiState.value.copy(isLootBoxSheetOpen = isVisible)
    }

    fun buyLootBox(type: LootBoxType) {
        viewModelScope.launch {
            val resultItems = repository.buyLootBox(type)
            if (resultItems != null) {
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

    // Ch10: Yükseltmeler
    fun getShopUpgradeCost(level: Int): Double {
        return when (level) {
            1 -> 15000.0
            2 -> 50000.0
            3 -> 150000.0
            4 -> 500000.0
            else -> 0.0
        }
    }

    fun getMechanicUpgradeCost(level: Int): Double {
        return when (level) {
            1 -> 20000.0
            2 -> 75000.0
            3 -> 250000.0
            4 -> 1000000.0
            else -> 0.0
        }
    }

    fun upgradeShop() {
        val level = playerState.value.shopLevel
        if (level >= 5) return
        val cost = getShopUpgradeCost(level)
        viewModelScope.launch {
            repository.upgradeShop(cost)
        }
    }

    fun upgradeMechanic() {
        val level = playerState.value.mechanicLevel
        if (level >= 5) return
        val cost = getMechanicUpgradeCost(level)
        viewModelScope.launch {
            repository.upgradeMechanic(cost)
        }
    }

    fun sellItem(item: MarketItem) {
        viewModelScope.launch { repository.sellItem(item) }
    }

    // Ch6: Günlük kalan tamir hakkı
    fun getRemainingRepairs(): Int {
        val state = playerState.value
        return if (state.lastRepairDay != state.currentDay) GameConstants.DAILY_REPAIR_LIMIT
        else (GameConstants.DAILY_REPAIR_LIMIT - state.dailyRepairsUsed).coerceAtLeast(0)
    }

    fun calculateRepairCost(item: MarketItem): Double {
        return repository.calculateRepairCost(item)
    }

    fun repairItem(item: MarketItem) {
        viewModelScope.launch {
            when (val result = repository.repairItem(item)) {
                is RepairResult.Success -> {
                    _repairResult.value = RepairResultState(
                        isSuccess = true,
                        itemName = item.itemName
                    )
                }
                is RepairResult.Failure -> {
                    _repairResult.value = RepairResultState(
                        isSuccess = false,
                        newCondition = result.newCondition,
                        itemName = item.itemName
                    )
                }
                is RepairResult.LimitReached -> {
                    // UI'da gösterilir (kalan hak = 0)
                }
                is RepairResult.NotEnoughMoney -> {
                    // UI'da gösterilir (buton disabled)
                }
            }
        }
    }

    fun dismissRepairResult() {
        _repairResult.value = null
    }

    fun getSellPrice(item: MarketItem): Double = repository.calculateSellPrice(item)

    fun advanceDay() {
        if (_uiState.value.isDayAdvancing) return
        _uiState.value = _uiState.value.copy(isDayAdvancing = true)
        viewModelScope.launch {
            val result = repository.advanceDay()
            val nextDay = playerState.value.currentDay + 1
            _dailySummary.value = DailySummaryState(
                day = nextDay,
                xpGained = com.enesduvan.kelepiravi.data.GameConstants.DAILY_LOGIN_XP,
                bonusMoney = com.enesduvan.kelepiravi.data.GameConstants.DAILY_LOGIN_BONUS,
                taxPaid = result.taxPaid,
                rentPaid = result.rentPaid,
                event = result.event
            )
            _interactiveEvent.value = result.interactiveEvent
            refreshMarket()
            _uiState.value = _uiState.value.copy(isDayAdvancing = false)
        }
    }

    fun dismissDailySummary() {
        _dailySummary.value = null
    }

    fun dismissInteractiveEvent() {
        _interactiveEvent.value = null
    }

    fun applyInteractiveEventChoice(choice: EventChoice) {
        viewModelScope.launch {
            val generatedItems = repository.applyEventChoice(choice)
            _interactiveEvent.value = null
            
            // Sonuçları kullanıcıya göster
            val resultText = buildString {
                if (choice.rewards.isEmpty() && choice.penalties.isEmpty()) {
                    append("Hiçbir şey olmadı.")
                } else {
                    if (choice.rewards.isNotEmpty()) {
                        append("KAZANIMLAR:\n")
                        var itemIndex = 0
                        choice.rewards.forEach { r -> 
                            if (r.type == "ITEM") {
                                val itemName = generatedItems.getOrNull(itemIndex) ?: r.value
                                append("+ Eşya: $itemName\n")
                                itemIndex++
                            } else {
                                val valText = if (r.type == "MONEY_EXACT" || r.type == "MONEY_PERCENT") "₺${r.value}" else r.value
                                append("+ ${r.type.replace("XP", "Tecrübe").replace("MONEY_EXACT", "Nakit")} $valText\n")
                            } 
                        }
                    }
                    if (choice.penalties.isNotEmpty()) {
                        append("\nKAYIPLAR:\n")
                        choice.penalties.forEach { p -> 
                            val valText = if (p.type == "MONEY_EXACT" || p.type == "MONEY_PERCENT") "₺${p.value}" else p.value
                            append("- ${p.type.replace("ITEM", "Eşya").replace("XP", "Tecrübe").replace("MONEY_EXACT", "Nakit")} $valText\n") 
                        }
                    }
                }
            }
            _eventResult.value = resultText.trim()
        }
    }
    
    fun dismissEventResult() {
        _eventResult.value = null
    }

    fun filteredMarketItems(uiState: MarketUiState): List<MarketItem> {
        return if (uiState.selectedCategory == "Tümü") uiState.marketItems
        else uiState.marketItems.filter { it.category == uiState.selectedCategory }
    }

    // ─── Pazarlık (Bargain) Mantığı ───────────────────────────────────────────
    fun startBargain(item: MarketItem) = negotiationEngine.startBargain(item)
    fun closeBargain() = negotiationEngine.closeBargain()
    fun sendOffer(offerAmount: Double) = negotiationEngine.sendOffer(offerAmount)
    fun sendMoneyToScammer() = negotiationEngine.sendMoneyToScammer()
    fun cancelScamDeal() = negotiationEngine.cancelScamDeal()
    fun buyAgreedItem() = negotiationEngine.buyAgreedItem()

    // 🤝 Satış Pazarlık (Sell Bargain) Mantığı 🤝
    fun startSellBargain(item: MarketItem) = negotiationEngine.startSellBargain(item)
    fun closeSellBargain() = negotiationEngine.closeSellBargain()
    fun sendSellOffer(offerAmount: Double) = negotiationEngine.sendSellOffer(offerAmount)
    fun sellAgreedItem() = negotiationEngine.sellAgreedItem()
}

// ─── Factory ─────────────────────────────────────────────────────────────────

class MarketViewModelFactory(private val repository: KelepiraviRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarketViewModel::class.java)) {
            return MarketViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

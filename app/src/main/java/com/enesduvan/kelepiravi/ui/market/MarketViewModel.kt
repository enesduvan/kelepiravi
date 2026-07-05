package com.enesduvan.kelepiravi.ui.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.enesduvan.kelepiravi.data.BargainConstants
import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.data.market.DailyEvent
import com.enesduvan.kelepiravi.data.market.EconomyEngine
import com.enesduvan.kelepiravi.data.market.MarketGenerator
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.data.repository.KelepiraviRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ─── State Modelleri ──────────────────────────────────────────────────────────

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
    // Türetilmiş ekonomi değerleri — her güncelleme hesaplanır
    val portfolioValue: Double = 0.0,
    val totalInvestment: Double = 0.0,
    val portfolioROI: Double = 0.0
)

data class MarketUiState(
    val marketItems: List<MarketItem> = emptyList(),
    val isRefreshing: Boolean = false,
    val selectedItem: MarketItem? = null,
    val selectedCategory: String = "Tümü",
    val isDayAdvancing: Boolean = false
)

data class BargainMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isFromPlayer: Boolean,
    val timestamp: String
)

data class BargainState(
    val item: MarketItem,
    val messages: List<BargainMessage> = emptyList(),
    val sellerPatience: Int = BargainConstants.STARTING_PATIENCE, // 0 - 100
    val sellerMood: String = "Kararsız", // Mutlu, Kararsız, Gergin, Sinirli
    val isDealClosed: Boolean = false,
    val isFailed: Boolean = false,
    val agreedPrice: Double = 0.0,
    val suggestedPrice: Double = 0.0
)

data class SellBargainState(
    val item: MarketItem,
    val messages: List<BargainMessage> = emptyList(),
    val buyerPatience: Int = BargainConstants.STARTING_PATIENCE, // 0 - 100
    val buyerMood: String = "Kararsız", // Mutlu, Kararsız, Gergin, Sinirli
    val isDealClosed: Boolean = false,
    val isFailed: Boolean = false,
    val agreedPrice: Double = 0.0,
    val baseSellPrice: Double = 0.0,
    val lastBuyerOffer: Double? = null
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
                totalProfit = entity?.totalProfit ?: 0.0,
                itemsBought = entity?.itemsBought ?: 0,
                itemsSold = entity?.itemsSold ?: 0,
                unlockedAchievements = entity?.unlockedAchievements ?: "",
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

    /** Aktif günlük olay — dialog olarak gösterilir */
    private val _dayEvent = MutableStateFlow<DailyEvent?>(null)
    val dayEvent: StateFlow<DailyEvent?> = _dayEvent.asStateFlow()

    /** Aktif pazarlık seansı (Alış) */
    private val _bargainState = MutableStateFlow<BargainState?>(null)
    val bargainState: StateFlow<BargainState?> = _bargainState.asStateFlow()

    /** Aktif pazarlık seansı (Satış) */
    private val _sellBargainState = MutableStateFlow<SellBargainState?>(null)
    val sellBargainState: StateFlow<SellBargainState?> = _sellBargainState.asStateFlow()

    init {
        viewModelScope.launch { repository.initializePlayerIfNeeded() }
        refreshMarket()
    }

    fun refreshMarket() {
        _uiState.value = _uiState.value.copy(
            marketItems = MarketGenerator.generateItems(GameConstants.MARKET_BATCH_SIZE),
            isRefreshing = false,
            selectedCategory = "Tümü"
        )
    }

    fun loadMoreItems() {
        val currentItems = _uiState.value.marketItems
        val newItems = MarketGenerator.generateItems(GameConstants.MARKET_BATCH_SIZE)
        _uiState.value = _uiState.value.copy(
            marketItems = currentItems + newItems
        )
    }

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
            }
        }
    }

    fun sellItem(item: MarketItem) {
        viewModelScope.launch { repository.sellItem(item) }
    }

    fun calculateRepairCost(item: MarketItem): Double {
        val currentMultiplier = MarketGenerator.getConditionMultiplier(item.condition)
        if (currentMultiplier >= GameConstants.PERFECT_CONDITION_MULTIPLIER) return 0.0
        val currentVal = item.estimatedValue.toDoubleOrNull() ?: 0.0
        val baseVal = currentVal / currentMultiplier
        val gain = baseVal - currentVal
        return gain * GameConstants.REPAIR_COST_GAIN_RATE
    }

    fun repairItem(item: MarketItem) {
        viewModelScope.launch {
            val cost = calculateRepairCost(item)
            val currentMultiplier = MarketGenerator.getConditionMultiplier(item.condition)
            val currentVal = item.estimatedValue.toDoubleOrNull() ?: 0.0
            val baseVal = currentVal / currentMultiplier
            
            val newItem = item.copy(
                condition = "Kusursuz Temiz",
                estimatedValue = baseVal.toString()
            )
            repository.updateInventoryItem(item, newItem, cost)
        }
    }

    fun getSellPrice(item: MarketItem): Double = repository.calculateSellPrice(item)

    /**
     * Yeni gün: ekonomi motoru çalışır, fiyatlar güncellenir, pazar yenilenir.
     */
    fun advanceDay() {
        if (_uiState.value.isDayAdvancing) return
        _uiState.value = _uiState.value.copy(isDayAdvancing = true)
        viewModelScope.launch {
            val event = repository.advanceDay()
            _dayEvent.value = event
            refreshMarket()
            _uiState.value = _uiState.value.copy(isDayAdvancing = false)
        }
    }

    /** Olay dialog'u kapatıldığında */
    fun dismissDayEvent() {
        _dayEvent.value = null
    }

    fun filteredMarketItems(uiState: MarketUiState): List<MarketItem> {
        return if (uiState.selectedCategory == "Tümü") uiState.marketItems
        else uiState.marketItems.filter { it.category == uiState.selectedCategory }
    }

    // ─── Pazarlık (Bargain) Mantığı ───────────────────────────────────────────

    private fun getCurrentTime(): String {
        return java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
    }

    fun startBargain(item: MarketItem) {
        val initialPrice = item.salesValue.toDoubleOrNull() ?: 0.0
        val initialMsg = BargainMessage(
            text = "Merhaba, ${item.itemName} için fiyatım ₺${com.enesduvan.kelepiravi.ui.shared.formatBalance(initialPrice.toString())}.",
            isFromPlayer = false,
            timestamp = getCurrentTime()
        )
        _bargainState.value = BargainState(
            item = item,
            messages = listOf(initialMsg),
            suggestedPrice = initialPrice * BargainConstants.BUY_SUGGESTED_RATIO
        )
    }

    fun closeBargain() {
        _bargainState.value = null
    }

    fun sendOffer(offerAmount: Double) {
        val state = _bargainState.value ?: return
        if (state.isDealClosed || state.isFailed) return

        val playerMsg = BargainMessage(
            text = "₺${com.enesduvan.kelepiravi.ui.shared.formatBalance(offerAmount.toString())} teklif ediyorum.",
            isFromPlayer = true,
            timestamp = getCurrentTime()
        )

        val updatedMessages = state.messages.toMutableList()
        updatedMessages.add(playerMsg)

        val originalPrice = state.item.salesValue.toDoubleOrNull() ?: 0.0
        val ratio = offerAmount / originalPrice

        var newPatience = state.sellerPatience
        val sellerResponseText: String
        var isDealClosed = false
        var isFailed = false
        var agreedPrice = 0.0

        if (ratio >= BargainConstants.BUY_ACCEPT_RATIO) {
            // Çok iyi teklif, hemen kabul et
            sellerResponseText = "Harika teklif, anlaştık!"
            isDealClosed = true
            agreedPrice = offerAmount
            newPatience += BargainConstants.PATIENCE_REWARD
        } else if (ratio >= BargainConstants.BUY_MAYBE_RATIO) {
            // Fena değil, biraz pazarlık
            val chance = kotlin.random.Random.nextDouble()
            if (chance > BargainConstants.BUY_COUNTER_ACCEPT_CHANCE) {
                sellerResponseText = "Tamam abi, anlaşalım ₺${com.enesduvan.kelepiravi.ui.shared.formatBalance(offerAmount.toString())} olsun."
                isDealClosed = true
                agreedPrice = offerAmount
            } else {
                val counterOffer = (originalPrice * BargainConstants.BUY_COUNTER_RATIO).toInt().toDouble()
                sellerResponseText = "₺${com.enesduvan.kelepiravi.ui.shared.formatBalance(counterOffer.toString())} yapalım ortası olsun."
                newPatience -= BargainConstants.PATIENCE_SMALL_PENALTY
            }
        } else if (ratio >= BargainConstants.BUY_LOW_RATIO) {
            sellerResponseText = "Çok düşük ya, olmaz. Biraz daha çıkman lazım."
            newPatience -= BargainConstants.PATIENCE_MEDIUM_PENALTY
        } else {
            sellerResponseText = "Ölücülük yapma kardeşim, o fiyata vermem."
            newPatience -= BargainConstants.PATIENCE_LARGE_PENALTY
        }

        if (newPatience <= 0) {
            newPatience = 0
            isFailed = true
            updatedMessages.add(
                BargainMessage(text = "Benim seninle işim olmaz, satmıyorum!", isFromPlayer = false, timestamp = getCurrentTime())
            )
        } else {
            val sellerMsg = BargainMessage(
                text = sellerResponseText,
                isFromPlayer = false,
                timestamp = getCurrentTime()
            )
            updatedMessages.add(sellerMsg)
        }

        val mood = when {
            newPatience >= BargainConstants.MOOD_HAPPY_MIN -> "Mutlu"
            newPatience >= BargainConstants.MOOD_UNSURE_MIN -> "Kararsız"
            newPatience >= BargainConstants.MOOD_TENSE_MIN -> "Gergin"
            else -> "Sinirli"
        }

        _bargainState.value = state.copy(
            messages = updatedMessages,
            sellerPatience = newPatience,
            sellerMood = mood,
            isDealClosed = isDealClosed,
            isFailed = isFailed,
            agreedPrice = agreedPrice
        )
    }

    fun buyAgreedItem() {
        val state = _bargainState.value ?: return
        if (!state.isDealClosed) return

        val itemToBuy = state.item.copy(salesValue = state.agreedPrice.toString())
        
        viewModelScope.launch {
            val success = repository.purchaseItem(itemToBuy)
            if (success) {
                _uiState.value = _uiState.value.copy(
                    marketItems = _uiState.value.marketItems.filterNot { it.itemName == itemToBuy.itemName && it.sellerName == itemToBuy.sellerName }
                )
                closeBargain()
            }
        }
    }

    // 🤝 Satış Pazarlık (Sell Bargain) Mantığı 🤝

    fun startSellBargain(item: MarketItem) {
        val baseSellPrice = repository.calculateSellPrice(item)
        // Alıcı %10-%20 daha düşük bir fiyattan kapıyı açar
        val initialOffer = baseSellPrice * (
            BargainConstants.SELL_INITIAL_MIN_RATIO +
                kotlin.random.Random.nextDouble() * BargainConstants.SELL_INITIAL_RANGE
            )
        
        val initialMsg = BargainMessage(
            text = "Selam, ${item.itemName} için ₺${com.enesduvan.kelepiravi.ui.shared.formatBalance(initialOffer.toString())} verebilirim. Ne dersin?",
            isFromPlayer = false,
            timestamp = getCurrentTime()
        )
        _sellBargainState.value = SellBargainState(
            item = item,
            messages = listOf(initialMsg),
            baseSellPrice = baseSellPrice
        )
    }

    fun closeSellBargain() {
        _sellBargainState.value = null
    }

    fun sendSellOffer(offerAmount: Double) {
        val state = _sellBargainState.value ?: return
        if (state.isDealClosed || state.isFailed) return

        val playerMsg = BargainMessage(
            text = "₺${com.enesduvan.kelepiravi.ui.shared.formatBalance(offerAmount.toString())} olursa hemen senin.",
            isFromPlayer = true,
            timestamp = getCurrentTime()
        )

        val updatedMessages = state.messages.toMutableList()
        updatedMessages.add(playerMsg)

        val basePrice = state.baseSellPrice
        val ratio = offerAmount / basePrice

        var newPatience = state.buyerPatience
        val buyerResponseText: String
        var isDealClosed = false
        var isFailed = false
        var agreedPrice = 0.0
        var lastBuyerOffer: Double? = null

        if (ratio <= BargainConstants.SELL_ACCEPT_RATIO) {
            buyerResponseText = "Harika! Bu fiyata anlaştık."
            isDealClosed = true
            agreedPrice = offerAmount
            newPatience += BargainConstants.PATIENCE_REWARD
        } else if (ratio <= BargainConstants.SELL_COUNTER_RATIO) {
            val counterOffer = offerAmount * BargainConstants.SELL_COUNTER_DISCOUNT
            lastBuyerOffer = counterOffer
            buyerResponseText = "₺${com.enesduvan.kelepiravi.ui.shared.formatBalance(counterOffer.toString())} yaparsak el sıkışırız."
            newPatience -= BargainConstants.PATIENCE_SELL_SMALL_PENALTY
        } else if (ratio <= BargainConstants.SELL_HIGH_RATIO) {
            val counterOffer = basePrice * BargainConstants.SELL_ACCEPT_RATIO
            lastBuyerOffer = counterOffer
            buyerResponseText = "Çok istedin. En fazla ₺${com.enesduvan.kelepiravi.ui.shared.formatBalance(counterOffer.toString())} veririm."
            newPatience -= BargainConstants.PATIENCE_SELL_MEDIUM_PENALTY
        } else {
            buyerResponseText = "Hadi canım sende, piyasası o kadar değil!"
            newPatience -= BargainConstants.PATIENCE_SELL_LARGE_PENALTY
        }

        if (newPatience <= 0) {
            newPatience = 0
            isFailed = true
            updatedMessages.add(
                BargainMessage(text = "Bu fiyata olmaz, ben vazgeçtim!", isFromPlayer = false, timestamp = getCurrentTime())
            )
        } else {
            val buyerMsg = BargainMessage(
                text = buyerResponseText,
                isFromPlayer = false,
                timestamp = getCurrentTime()
            )
            updatedMessages.add(buyerMsg)
        }

        val mood = when {
            newPatience >= BargainConstants.MOOD_HAPPY_MIN -> "Mutlu"
            newPatience >= BargainConstants.MOOD_UNSURE_MIN -> "Kararsız"
            newPatience >= BargainConstants.MOOD_TENSE_MIN -> "Gergin"
            else -> "Sinirli"
        }

        _sellBargainState.value = state.copy(
            messages = updatedMessages,
            buyerPatience = newPatience,
            buyerMood = mood,
            isDealClosed = isDealClosed,
            isFailed = isFailed,
            agreedPrice = agreedPrice,
            lastBuyerOffer = lastBuyerOffer
        )
    }

    fun sellAgreedItem() {
        val state = _sellBargainState.value ?: return
        if (!state.isDealClosed) return

        viewModelScope.launch {
            val success = repository.sellItem(state.item, state.agreedPrice)
            if (success) {
                closeSellBargain()
            }
        }
    }
}

// ─── Factory ─────────────────────────────────────────────────────────────────

class MarketViewModelFactory(
    private val repository: KelepiraviRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarketViewModel::class.java)) {
            return MarketViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

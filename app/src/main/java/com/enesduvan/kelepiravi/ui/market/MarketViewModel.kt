package com.enesduvan.kelepiravi.ui.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.enesduvan.kelepiravi.data.local.entity.DEFAULT_USER_ID
import com.enesduvan.kelepiravi.data.local.entity.INITIAL_BALANCE
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
    val balance: String = INITIAL_BALANCE,
    val inventory: List<MarketItem> = emptyList(),
    val currentDay: Int = 1,
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
    val sellerPatience: Int = 100, // 0 - 100
    val sellerMood: String = "Kararsız", // Mutlu, Kararsız, Gergin, Sinirli
    val isDealClosed: Boolean = false,
    val isFailed: Boolean = false,
    val agreedPrice: Double = 0.0,
    val suggestedPrice: Double = 0.0
)

// ─── ViewModel ───────────────────────────────────────────────────────────────

class MarketViewModel(
    private val repository: KelepiraviRepository
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = repository
        .getPlayerState()
        .map { list ->
            val entity = list.firstOrNull { it.id == DEFAULT_USER_ID }
            val inventory = entity?.inventory ?: emptyList()
            PlayerState(
                balance = entity?.balance ?: INITIAL_BALANCE,
                inventory = inventory,
                currentDay = entity?.currentDay ?: 1,
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

    /** Aktif pazarlık seansı */
    private val _bargainState = MutableStateFlow<BargainState?>(null)
    val bargainState: StateFlow<BargainState?> = _bargainState.asStateFlow()

    init {
        viewModelScope.launch { repository.initializePlayerIfNeeded() }
        refreshMarket()
    }

    fun refreshMarket() {
        _uiState.value = _uiState.value.copy(
            marketItems = MarketGenerator.generateItems(12),
            isRefreshing = false,
            selectedCategory = "Tümü"
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
            suggestedPrice = initialPrice * 0.9 // İlk öneri %10 altı
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

        if (ratio >= 0.95) {
            // Çok iyi teklif, hemen kabul et
            sellerResponseText = "Harika teklif, anlaştık!"
            isDealClosed = true
            agreedPrice = offerAmount
            newPatience += 10
        } else if (ratio >= 0.85) {
            // Fena değil, biraz pazarlık
            val chance = kotlin.random.Random.nextDouble()
            if (chance > 0.4) {
                sellerResponseText = "Tamam abi, anlaşalım ₺${com.enesduvan.kelepiravi.ui.shared.formatBalance(offerAmount.toString())} olsun."
                isDealClosed = true
                agreedPrice = offerAmount
            } else {
                val counterOffer = (originalPrice * 0.90).toInt().toDouble()
                sellerResponseText = "₺${com.enesduvan.kelepiravi.ui.shared.formatBalance(counterOffer.toString())} yapalım ortası olsun."
                newPatience -= 5
            }
        } else if (ratio >= 0.70) {
            sellerResponseText = "Çok düşük ya, olmaz. Biraz daha çıkman lazım."
            newPatience -= 15
        } else {
            sellerResponseText = "Ölücülük yapma kardeşim, o fiyata vermem."
            newPatience -= 30
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
            newPatience >= 80 -> "Mutlu"
            newPatience >= 50 -> "Kararsız"
            newPatience >= 20 -> "Gergin"
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

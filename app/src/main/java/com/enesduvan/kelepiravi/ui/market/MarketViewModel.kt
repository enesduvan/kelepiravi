package com.enesduvan.kelepiravi.ui.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.enesduvan.kelepiravi.data.BargainConstants
import com.enesduvan.kelepiravi.data.GameConstants
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
    // Türetilmiş ekonomi değerleri
    val portfolioValue: Double = 0.0,
    val totalInvestment: Double = 0.0,
    val portfolioROI: Double = 0.0
)

data class MarketUiState(
    val marketItems: List<MarketItem> = emptyList(),
    val isRefreshing: Boolean = false,
    val selectedItem: MarketItem? = null,
    val selectedCategory: String = "Tümü",
    val isDayAdvancing: Boolean = false,
    val isLootBoxSheetOpen: Boolean = false, // Ch9: Zamazon Kutu seçim ekranı
    val purchasedLootBoxItems: List<MarketItem>? = null // Ch9: Kutudan çıkan eşyalar (reveal için)
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

data class DailySummaryState(
    val day: Int,
    val xpGained: Int,
    val bonusMoney: Double,
    val taxPaid: Double,     // Ch8: Vergi kesintisi
    val rentPaid: Double,    // Ch8: Kira kesintisi
    val event: DailyEvent?
)

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

    /** Aktif pazarlık seansı (Alış) */
    private val _bargainState = MutableStateFlow<BargainState?>(null)
    val bargainState: StateFlow<BargainState?> = _bargainState.asStateFlow()

    /** Aktif pazarlık seansı (Satış) */
    private val _sellBargainState = MutableStateFlow<SellBargainState?>(null)
    val sellBargainState: StateFlow<SellBargainState?> = _sellBargainState.asStateFlow()

    private val _dailySummary = MutableStateFlow<DailySummaryState?>(null)
    val dailySummary: StateFlow<DailySummaryState?> = _dailySummary.asStateFlow()

    /** Ch6: Dolandırıcıdan alındıktan sonra reveal dialogu */
    private val _scamReveal = MutableStateFlow<MarketItem?>(null)
    val scamReveal: StateFlow<MarketItem?> = _scamReveal.asStateFlow()

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

    // Ch6: Tamir için maliyet-kazanç analizi
    fun isRepairWorthIt(item: MarketItem): Boolean {
        val cost = calculateRepairCost(item)
        val currentVal = item.estimatedValue.toDoubleOrNull() ?: 0.0
        val currentMultiplier = MarketGenerator.getConditionMultiplier(item.condition)
        val baseVal = if (currentMultiplier > 0) currentVal / currentMultiplier else currentVal
        val gain = baseVal - currentVal
        // Kazanç en az maliyetin 1.5 katı olmalı ki "değer" olsun
        return gain > cost * 1.5
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
            refreshMarket()
            _uiState.value = _uiState.value.copy(isDayAdvancing = false)
        }
    }

    fun dismissDailySummary() {
        _dailySummary.value = null
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
        val personality = SellerPersonality.fromName(item.sellerName)

        // Ch6: Dolandırıcı ise profesyonel pazarlıkçı kişiliğini kullan
        val effectivePersonality = if (item.isScammer && item.scamType.isNotEmpty()) {
            try {
                val scamType = com.enesduvan.kelepiravi.data.market.ScamType.valueOf(item.scamType)
                SellerPersonality.getScammerForType(scamType)
            } catch (e: Exception) { personality }
        } else personality

        // Ch6: Dolandırıcı acele ettirme cümlesi ekle
        val openingLine = if (item.isScammer) {
            effectivePersonality.dialogs.getRushPhrase()
                ?.let { "Merhaba, ${item.itemName} için fiyatım ₺${com.enesduvan.kelepiravi.ui.shared.formatBalance(initialPrice.toString())}. $it" }
                ?: "Merhaba, ${item.itemName} için fiyatım ₺${com.enesduvan.kelepiravi.ui.shared.formatBalance(initialPrice.toString())}."
        } else {
            "Merhaba, ${item.itemName} için fiyatım ₺${com.enesduvan.kelepiravi.ui.shared.formatBalance(initialPrice.toString())}."
        }

        val initialMsg = BargainMessage(
            text = openingLine,
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
        val personality = if (state.item.isScammer && state.item.scamType.isNotEmpty()) {
            try {
                val scamType = com.enesduvan.kelepiravi.data.market.ScamType.valueOf(state.item.scamType)
                SellerPersonality.getScammerForType(scamType)
            } catch (e: Exception) { SellerPersonality.fromName(state.item.sellerName) }
        } else SellerPersonality.fromName(state.item.sellerName)
        val modifiedAcceptRatio = BargainConstants.BUY_ACCEPT_RATIO + personality.buyAcceptRatioModifier

        var newPatience = state.sellerPatience
        val sellerResponseText: String
        var isDealClosed = false
        var isFailed = false
        var agreedPrice = 0.0
        var lastSellerOffer: Double? = null

        // Ch6: Tekrar teklif tespiti
        val isRepeatOffer = state.lastPlayerOfferAmount != null &&
            kotlin.math.abs((state.lastPlayerOfferAmount) - offerAmount) < 1.0
        if (isRepeatOffer) {
            val annoyedMsg = personality.dialogs.getRepeatOfferAnnoyed()
            if (annoyedMsg != null) {
                updatedMessages.add(
                    BargainMessage(text = annoyedMsg, isFromPlayer = false, timestamp = getCurrentTime())
                )
                newPatience -= BargainConstants.PATIENCE_REPEAT_OFFER_PENALTY
                _bargainState.value = state.copy(
                    messages = updatedMessages,
                    sellerPatience = newPatience.coerceAtLeast(0),
                    isFailed = newPatience <= 0,
                    lastPlayerOfferAmount = offerAmount
                )
                return
            }
        }

        if (ratio >= modifiedAcceptRatio) {
            sellerResponseText = personality.dialogs.getBuyAccept()
            isDealClosed = true
            agreedPrice = offerAmount
            newPatience += BargainConstants.PATIENCE_REWARD
        } else if (ratio >= BargainConstants.BUY_MAYBE_RATIO + personality.buyAcceptRatioModifier) {
            val chance = kotlin.random.Random.nextDouble()
            if (chance > BargainConstants.BUY_COUNTER_ACCEPT_CHANCE) {
                sellerResponseText = "Tamam abi, anlaşalım ₺${com.enesduvan.kelepiravi.ui.shared.formatBalance(offerAmount.toString())} olsun."
                isDealClosed = true
                agreedPrice = offerAmount
            } else {
                val counterOffer = (originalPrice * BargainConstants.BUY_COUNTER_RATIO).toInt().toDouble()
                lastSellerOffer = counterOffer
                sellerResponseText = personality.dialogs.getBuyCounter() + " ₺${com.enesduvan.kelepiravi.ui.shared.formatBalance(counterOffer.toString())} yapalım ortası olsun."
                newPatience -= (BargainConstants.PATIENCE_SMALL_PENALTY * personality.patiencePenaltyMultiplier).toInt()
            }
        } else if (ratio >= BargainConstants.BUY_LOW_RATIO + personality.buyAcceptRatioModifier) {
            sellerResponseText = personality.dialogs.getBuyLow()
            newPatience -= (BargainConstants.PATIENCE_MEDIUM_PENALTY * personality.patiencePenaltyMultiplier).toInt()
        } else {
            sellerResponseText = personality.dialogs.getBuyReject()
            newPatience -= (BargainConstants.PATIENCE_LARGE_PENALTY * personality.patiencePenaltyMultiplier).toInt()
        }

        // Ch6: Satıcı acele ettiriyor mu? (Aceleci & Dolandırıcı kişilikleri)
        val shouldRush = (personality == SellerPersonality.ACELECI || state.item.isScammer) &&
            !isDealClosed && !isFailed &&
            kotlin.random.Random.nextDouble() < 0.35
        if (shouldRush) {
            personality.dialogs.getRushPhrase()?.let { rush ->
                updatedMessages.add(
                    BargainMessage(text = rush, isFromPlayer = false, timestamp = getCurrentTime())
                )
            }
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

        var finalDealClosed = isDealClosed
        var finalScamPrompt = state.isScamPromptActive

        if (finalDealClosed && state.item.isScammer) {
            finalDealClosed = false
            finalScamPrompt = true
            updatedMessages.removeLastOrNull() // Remove the previous 'buy accept' message added
            updatedMessages.add(
                BargainMessage(text = "Kardeşim ürün bu, sen parayı gönder ben kargoya vereceğim zaten.", isFromPlayer = false, timestamp = getCurrentTime())
            )
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
            isDealClosed = finalDealClosed,
            isScamPromptActive = finalScamPrompt,
            isFailed = isFailed,
            agreedPrice = agreedPrice,
            lastSellerOffer = lastSellerOffer,
            lastPlayerOfferAmount = offerAmount
        )
    }

    // Ch6: Dolandırıcıya parayı gönderme (kabul etme) eylemi
    fun sendMoneyToScammer() {
        val state = _bargainState.value ?: return
        if (!state.isScamPromptActive) return

        val itemToBuy = state.item.copy(salesValue = state.agreedPrice.toString())
        
        viewModelScope.launch {
            val success = repository.purchaseItem(itemToBuy)
            if (success) {
                _uiState.value = _uiState.value.copy(
                    marketItems = _uiState.value.marketItems.filterNot {
                        it.itemName == itemToBuy.itemName && it.sellerName == itemToBuy.sellerName
                    }
                )
                
                // Deal is closed, but it's a scam!
                _bargainState.value = state.copy(isDealClosed = true, isScamPromptActive = false)
                _scamReveal.value = itemToBuy
            }
        }
    }

    // Ch6: Dolandırıcıdan vazgeçme (reddetme) eylemi
    fun cancelScamDeal() {
        val state = _bargainState.value ?: return
        if (!state.isScamPromptActive) return

        val updatedMessages = state.messages.toMutableList()
        updatedMessages.add(BargainMessage(text = "Kardeşim elden almadan para falan yok.", isFromPlayer = true, timestamp = getCurrentTime()))
        updatedMessages.add(BargainMessage(text = "Sen bilirsin kardeşim, ucuza mal veriyoruz yaranamıyoruz.", isFromPlayer = false, timestamp = getCurrentTime()))
        
        _bargainState.value = state.copy(
            isScamPromptActive = false,
            isFailed = true,
            messages = updatedMessages
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
                    marketItems = _uiState.value.marketItems.filterNot {
                        it.itemName == itemToBuy.itemName && it.sellerName == itemToBuy.sellerName
                    }
                )
                // Ch6: Dolandırıcıdan alındıysa reveal
                if (state.item.isScammer && state.item.hiddenCondition.isNotEmpty()) {
                    _scamReveal.value = state.item
                }
                closeBargain()
            }
        }
    }

    // 🤝 Satış Pazarlık (Sell Bargain) Mantığı 🤝

    fun startSellBargain(item: MarketItem) {
        val baseSellPrice = repository.calculateSellPrice(item)
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
            buyerName = MarketGenerator.getRandomName(),
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
        val personality = SellerPersonality.fromName(state.buyerName)
        val modifiedAcceptRatio = BargainConstants.SELL_ACCEPT_RATIO + personality.sellAcceptRatioModifier

        var newPatience = state.buyerPatience
        val buyerResponseText: String
        var isDealClosed = false
        var isFailed = false
        var agreedPrice = 0.0
        var lastBuyerOffer: Double? = null

        // Ch6: Tekrar teklif tespiti
        val isRepeatOffer = state.lastPlayerOfferAmount != null &&
            kotlin.math.abs(state.lastPlayerOfferAmount - offerAmount) < 1.0
        if (isRepeatOffer) {
            val annoyedMsg = personality.dialogs.getRepeatOfferAnnoyed()
            if (annoyedMsg != null) {
                updatedMessages.add(
                    BargainMessage(text = annoyedMsg, isFromPlayer = false, timestamp = getCurrentTime())
                )
                newPatience -= BargainConstants.PATIENCE_REPEAT_OFFER_PENALTY
                _sellBargainState.value = state.copy(
                    messages = updatedMessages,
                    buyerPatience = newPatience.coerceAtLeast(0),
                    isFailed = newPatience <= 0,
                    lastPlayerOfferAmount = offerAmount
                )
                return
            }
        }

        if (ratio <= modifiedAcceptRatio) {
            buyerResponseText = personality.dialogs.getSellAccept()
            isDealClosed = true
            agreedPrice = offerAmount
            newPatience += BargainConstants.PATIENCE_REWARD
        } else if (ratio <= BargainConstants.SELL_COUNTER_RATIO + personality.sellAcceptRatioModifier) {
            val counterOffer = offerAmount * BargainConstants.SELL_COUNTER_DISCOUNT
            lastBuyerOffer = counterOffer
            buyerResponseText = personality.dialogs.getSellCounter() + " ₺${com.enesduvan.kelepiravi.ui.shared.formatBalance(counterOffer.toString())} yaparsak el sıkışırız."
            newPatience -= (BargainConstants.PATIENCE_SELL_SMALL_PENALTY * personality.patiencePenaltyMultiplier).toInt()
        } else if (ratio <= BargainConstants.SELL_HIGH_RATIO + personality.sellAcceptRatioModifier) {
            val counterOffer = basePrice * modifiedAcceptRatio
            lastBuyerOffer = counterOffer
            buyerResponseText = personality.dialogs.getSellHigh() + " En fazla ₺${com.enesduvan.kelepiravi.ui.shared.formatBalance(counterOffer.toString())} veririm."
            newPatience -= (BargainConstants.PATIENCE_SELL_MEDIUM_PENALTY * personality.patiencePenaltyMultiplier).toInt()
        } else {
            buyerResponseText = personality.dialogs.getSellReject()
            newPatience -= (BargainConstants.PATIENCE_SELL_LARGE_PENALTY * personality.patiencePenaltyMultiplier).toInt()
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
            lastBuyerOffer = lastBuyerOffer,
            lastPlayerOfferAmount = offerAmount
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

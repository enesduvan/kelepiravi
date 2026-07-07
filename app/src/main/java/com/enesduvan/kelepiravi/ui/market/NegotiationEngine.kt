package com.enesduvan.kelepiravi.ui.market

import com.enesduvan.kelepiravi.data.BargainConstants
import com.enesduvan.kelepiravi.data.market.MarketGenerator
import com.enesduvan.kelepiravi.data.market.SellerPersonality
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.data.repository.KelepiraviRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NegotiationEngine(
    private val scope: CoroutineScope,
    private val repository: KelepiraviRepository,
    private val uiStateFlow: MutableStateFlow<MarketUiState>,
    private val scamRevealFlow: MutableStateFlow<MarketItem?>
) {

    private val _bargainState = MutableStateFlow<BargainState?>(null)
    val bargainState: StateFlow<BargainState?> = _bargainState.asStateFlow()

    private val _sellBargainState = MutableStateFlow<SellBargainState?>(null)
    val sellBargainState: StateFlow<SellBargainState?> = _sellBargainState.asStateFlow()

    private fun getCurrentTime(): String {
        return java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
    }

    fun startBargain(item: MarketItem) {
        val initialPrice = item.salesValue.toDoubleOrNull() ?: 0.0
        val personality = SellerPersonality.fromName(item.sellerName)

        val effectivePersonality = if (item.isScammer && item.scamType.isNotEmpty()) {
            try {
                val scamType = com.enesduvan.kelepiravi.data.market.ScamType.valueOf(item.scamType)
                SellerPersonality.getScammerForType(scamType)
            } catch (e: Exception) { personality }
        } else personality

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

        if (state.lastSellerOffer != null && kotlin.math.abs(state.lastSellerOffer - offerAmount) < 1.0) {
            sellerResponseText = personality.dialogs.getBuyAccept()
            isDealClosed = true
            agreedPrice = offerAmount
            newPatience += BargainConstants.PATIENCE_REWARD
        }
        else if (ratio >= modifiedAcceptRatio) {
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
            updatedMessages.removeLastOrNull()
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

    fun sendMoneyToScammer() {
        val state = _bargainState.value ?: return
        if (!state.isScamPromptActive) return

        val itemToBuy = state.item.copy(salesValue = state.agreedPrice.toString())
        
        scope.launch {
            val success = repository.purchaseItem(itemToBuy)
            if (success) {
                uiStateFlow.value = uiStateFlow.value.copy(
                    marketItems = uiStateFlow.value.marketItems.filterNot {
                        it.itemName == itemToBuy.itemName && it.sellerName == itemToBuy.sellerName
                    }
                )
                
                _bargainState.value = state.copy(isDealClosed = true, isScamPromptActive = false)
                scamRevealFlow.value = itemToBuy
            }
        }
    }

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
        
        scope.launch {
            val success = repository.purchaseItem(itemToBuy)
            if (success) {
                uiStateFlow.value = uiStateFlow.value.copy(
                    marketItems = uiStateFlow.value.marketItems.filterNot {
                        it.itemName == itemToBuy.itemName && it.sellerName == itemToBuy.sellerName
                    }
                )
                if (state.item.isScammer && state.item.hiddenCondition.isNotEmpty()) {
                    scamRevealFlow.value = state.item
                }
                closeBargain()
            }
        }
    }

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

        if (state.lastBuyerOffer != null && kotlin.math.abs(state.lastBuyerOffer - offerAmount) < 1.0) {
            buyerResponseText = personality.dialogs.getSellAccept()
            isDealClosed = true
            agreedPrice = offerAmount
            newPatience += BargainConstants.PATIENCE_REWARD
        }
        else if (ratio <= modifiedAcceptRatio) {
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

        scope.launch {
            val success = repository.sellItem(state.item, state.agreedPrice)
            if (success) {
                closeSellBargain()
            }
        }
    }
}

package com.enesduvan.kelepiravi.data.market

import com.enesduvan.kelepiravi.data.BargainConstants
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.domain.repository.IKelepiraviRepository
import com.enesduvan.kelepiravi.ui.shared.formatBalance
import com.enesduvan.kelepiravi.domain.model.BargainMessage
import com.enesduvan.kelepiravi.domain.model.BargainState
import com.enesduvan.kelepiravi.domain.model.MarketUiState
import com.enesduvan.kelepiravi.domain.model.SellBargainState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

class NegotiationEngine(
    private val scope: CoroutineScope,
    private val repository: IKelepiraviRepository,
    private val uiStateFlow: MutableStateFlow<MarketUiState>,
    private val scamRevealFlow: MutableStateFlow<MarketItem?>
) {

    private val _bargainState = MutableStateFlow<BargainState?>(null)
    val bargainState: StateFlow<BargainState?> = _bargainState.asStateFlow()

    private val _sellBargainState = MutableStateFlow<SellBargainState?>(null)
    val sellBargainState: StateFlow<SellBargainState?> = _sellBargainState.asStateFlow()

    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    fun startBargain(item: MarketItem, relationships: Map<String, Int> = emptyMap()) {
        val initialPrice = item.salesValue.toDouble()
        val personality = SellerPersonality.fromName(item.sellerName)

        val effectivePersonality = if (item.isScammer && item.scamType.isNotEmpty()) {
            try {
                val scamType = ScamType.valueOf(item.scamType)
                SellerPersonality.getScammerForType(scamType)
            } catch (e: Exception) { personality }
        } else personality

        val openingLine = if (item.isScammer) {
            effectivePersonality.dialogs.getRushPhrase()
                ?.let { "Merhaba, ${item.itemName} için fiyatım ₺${formatBalance(initialPrice)}. $it" }
                ?: "Merhaba, ${item.itemName} için fiyatım ₺${formatBalance(initialPrice)}."
        } else {
            "Merhaba, ${item.itemName} için fiyatım ₺${formatBalance(initialPrice)}."
        }

        val initialMsg = BargainMessage(
            text = openingLine,
            isFromPlayer = false,
            timestamp = getCurrentTime()
        )
        val relationshipScore = relationships[item.sellerName] ?: 0
        val startingPatience = (BargainConstants.STARTING_PATIENCE + (relationshipScore * 2)).coerceIn(10, 100)

        _bargainState.value = BargainState(
            item = item,
            messages = listOf(initialMsg),
            suggestedPrice = initialPrice * BargainConstants.BUY_SUGGESTED_RATIO,
            sellerPatience = startingPatience,
            sellerMood = calculateMood(startingPatience),
            npcRelationshipScore = relationshipScore,
            relationshipDelta = 0
        )
    }

    fun closeBargain() {
        _bargainState.value = null
    }

    fun sendOffer(offerAmount: Double) {
        val state = _bargainState.value ?: return
        if (state.isDealClosed || state.isFailed) return
        if (!offerAmount.isFinite() || offerAmount <= 0.0) return

        val playerMsg = BargainMessage(
            text = "₺${formatBalance(offerAmount)} teklif ediyorum.",
            isFromPlayer = true,
            timestamp = getCurrentTime()
        )

        val updatedMessages = state.messages.toMutableList()
        updatedMessages.add(playerMsg)

        val originalPrice = state.item.salesValue.toDouble()
        val ratio = offerAmount / originalPrice
        val personality = if (state.item.isScammer && state.item.scamType.isNotEmpty()) {
            try {
                val scamType = ScamType.valueOf(state.item.scamType)
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

        val isRepeatOffer = state.lastPlayerOfferAmount > 0.0 &&
            abs(state.lastPlayerOfferAmount - offerAmount) < 1.0
        if (isRepeatOffer) {
            val annoyedMsg = personality.dialogs.getRepeatOfferAnnoyed()
            if (annoyedMsg != null) {
                updatedMessages.add(
                    BargainMessage(
                        text = annoyedMsg,
                        isFromPlayer = false,
                        timestamp = getCurrentTime()
                    )
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

        if (state.lastSellerOffer > 0.0 && abs(state.lastSellerOffer - offerAmount) < 1.0) {
            sellerResponseText = personality.dialogs.getBuyAccept()
            isDealClosed = true
            agreedPrice = kotlin.math.round(offerAmount)
            newPatience += BargainConstants.PATIENCE_REWARD
        }
        else if (ratio >= modifiedAcceptRatio) {
            sellerResponseText = personality.dialogs.getBuyAccept()
            isDealClosed = true
            agreedPrice = kotlin.math.round(offerAmount)
            newPatience += BargainConstants.PATIENCE_REWARD
        } else if (ratio >= BargainConstants.BUY_MAYBE_RATIO + personality.buyAcceptRatioModifier) {
            val chance = Random.nextDouble()
            if (chance > BargainConstants.BUY_COUNTER_ACCEPT_CHANCE) {
                agreedPrice = kotlin.math.round(offerAmount)
                sellerResponseText = "Tamam abi, anlaşalım ₺${formatBalance(agreedPrice)} olsun."
                isDealClosed = true
            } else {
                val counterOffer = kotlin.math.round(originalPrice * BargainConstants.BUY_COUNTER_RATIO)
                lastSellerOffer = counterOffer
                sellerResponseText = personality.dialogs.getBuyCounter() + " ₺${formatBalance(counterOffer)} yapalım ortası olsun."
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
            Random.nextDouble() < 0.35
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
                BargainMessage(
                    text = "Benim seninle işim olmaz, satmıyorum!",
                    isFromPlayer = false,
                    timestamp = getCurrentTime()
                )
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
                BargainMessage(
                    text = "Kardeşim ürün bu, sen parayı gönder ben kargoya vereceğim zaten.",
                    isFromPlayer = false,
                    timestamp = getCurrentTime()
                )
            )
        }

        var relationshipDelta = 0
        if (isFailed) {
            relationshipDelta = -10
        } else if (finalDealClosed) {
            relationshipDelta = if (newPatience > 60) 5 else if (newPatience < 30) -5 else 2
        }

        _bargainState.value = state.copy(
            messages = updatedMessages,
            sellerPatience = newPatience,
            sellerMood = calculateMood(newPatience),
            isDealClosed = finalDealClosed,
            isScamPromptActive = finalScamPrompt,
            isFailed = isFailed,
            agreedPrice = if (finalDealClosed || finalScamPrompt) agreedPrice else state.agreedPrice,
            lastSellerOffer = lastSellerOffer ?: state.lastSellerOffer,
            lastPlayerOfferAmount = offerAmount,
            relationshipDelta = relationshipDelta
        )

        if (isFailed) {
            scope.launch { repository.recordFailedBargain() }
        }
    }

    fun sendMoneyToScammer() {
        val state = _bargainState.value ?: return
        if (!state.isScamPromptActive) return

        val agreedLong = kotlin.math.round(state.agreedPrice).toLong()
        val itemToBuy = state.item.copy(salesValue = agreedLong)

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
        updatedMessages.add(
            BargainMessage(
                text = "Kardeşim elden almadan para falan yok.",
                isFromPlayer = true,
                timestamp = getCurrentTime()
            )
        )
        updatedMessages.add(
            BargainMessage(
                text = "Sen bilirsin kardeşim, ucuza mal veriyoruz yaranamıyoruz.",
                isFromPlayer = false,
                timestamp = getCurrentTime()
            )
        )

        _bargainState.value = state.copy(
            isScamPromptActive = false,
            isFailed = true,
            messages = updatedMessages
        )
        scope.launch { repository.recordFailedBargain() }
    }

    fun buyAgreedItem() {
        val state = _bargainState.value ?: return
        if (!state.isDealClosed) return

        val agreedLong = kotlin.math.round(state.agreedPrice).toLong()
        val itemToBuy = state.item.copy(salesValue = agreedLong)

        scope.launch {
            val success = repository.purchaseItem(itemToBuy)
            if (success) {
                uiStateFlow.value = uiStateFlow.value.copy(
                    marketItems = uiStateFlow.value.marketItems.filterNot {
                        it.id == state.item.id || (it.itemName == itemToBuy.itemName && it.sellerName == itemToBuy.sellerName)
                    }
                )
                if (state.item.isScammer && state.item.hiddenCondition.isNotEmpty()) {
                    scamRevealFlow.value = state.item
                }
                if (state.relationshipDelta != 0) {
                    repository.updateNpcRelationship(state.item.sellerName, state.relationshipDelta)
                }
                closeBargain()
            } else {
                val updatedMessages = state.messages.toMutableList()
                updatedMessages.add(
                    BargainMessage(
                        text = "Sistem Hatası: Yetersiz bakiye veya dükkan kapasitesi dolu!",
                        isFromPlayer = false,
                        timestamp = getCurrentTime()
                    )
                )
                _bargainState.value = state.copy(messages = updatedMessages)
            }
        }
    }

    fun startSellBargain(item: MarketItem, relationships: Map<String, Int> = emptyMap()) {
        val baseSellPrice = repository.calculateSellPrice(item)
        val initialOffer = baseSellPrice * (
            BargainConstants.SELL_INITIAL_MIN_RATIO +
                Random.nextDouble() * BargainConstants.SELL_INITIAL_RANGE
            )

        val initialMsg = BargainMessage(
            text = "Selam, ${item.itemName} için ₺${formatBalance(initialOffer)} verebilirim. Ne dersin?",
            isFromPlayer = false,
            timestamp = getCurrentTime()
        )
        val buyerName = MarketGenerator.getRandomName()
        val relationshipScore = relationships[buyerName] ?: 0
        val startingPatience = (BargainConstants.STARTING_PATIENCE + (relationshipScore * 2)).coerceIn(10, 100)

        _sellBargainState.value = SellBargainState(
            item = item,
            buyerName = buyerName,
            messages = listOf(initialMsg),
            baseSellPrice = baseSellPrice,
            buyerPatience = startingPatience,
            buyerMood = calculateMood(startingPatience),
            npcRelationshipScore = relationshipScore,
            relationshipDelta = 0
        )
    }

    fun startSellBargainWithOffer(item: MarketItem, relationships: Map<String, Int>, buyerName: String, offerAmount: Double) {
        val baseSellPrice = repository.calculateSellPrice(item)

        val initialMsg = BargainMessage(
            text = "Daha önce anlaştığımız gibi, ${item.itemName} için ₺${formatBalance(offerAmount)} veriyorum. Son teklifim budur.",
            isFromPlayer = false,
            timestamp = getCurrentTime()
        )
        val relationshipScore = relationships[buyerName] ?: 0
        val startingPatience = (BargainConstants.STARTING_PATIENCE + (relationshipScore * 2)).coerceIn(10, 100)

        _sellBargainState.value = SellBargainState(
            item = item,
            buyerName = buyerName,
            messages = listOf(initialMsg),
            baseSellPrice = baseSellPrice,
            buyerPatience = startingPatience,
            buyerMood = calculateMood(startingPatience),
            npcRelationshipScore = relationshipScore,
            relationshipDelta = 0
        )
    }

    fun closeSellBargain() {
        _sellBargainState.value = null
    }

    fun sendSellOffer(offerAmount: Double) {
        val state = _sellBargainState.value ?: return
        if (state.isDealClosed || state.isFailed) return
        if (!offerAmount.isFinite() || offerAmount <= 0.0) return

        val playerMsg = BargainMessage(
            text = "₺${formatBalance(offerAmount)} olursa hemen senin.",
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

        val isRepeatOffer = state.lastPlayerOfferAmount > 0.0 &&
            abs(state.lastPlayerOfferAmount - offerAmount) < 1.0
        if (isRepeatOffer) {
            val annoyedMsg = personality.dialogs.getRepeatOfferAnnoyed()
            if (annoyedMsg != null) {
                updatedMessages.add(
                    BargainMessage(
                        text = annoyedMsg,
                        isFromPlayer = false,
                        timestamp = getCurrentTime()
                    )
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

        if (state.lastBuyerOffer > 0.0 && abs(state.lastBuyerOffer - offerAmount) < 1.0) {
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
            buyerResponseText = personality.dialogs.getSellCounter() + " ₺${formatBalance(counterOffer)} yaparsak el sıkışırız."
            newPatience -= (BargainConstants.PATIENCE_SELL_SMALL_PENALTY * personality.patiencePenaltyMultiplier).toInt()
        } else if (ratio <= BargainConstants.SELL_HIGH_RATIO + personality.sellAcceptRatioModifier) {
            val counterOffer = basePrice * modifiedAcceptRatio
            lastBuyerOffer = counterOffer
            buyerResponseText = personality.dialogs.getSellHigh() + " En fazla ₺${formatBalance(counterOffer)} veririm."
            newPatience -= (BargainConstants.PATIENCE_SELL_MEDIUM_PENALTY * personality.patiencePenaltyMultiplier).toInt()
        } else {
            buyerResponseText = personality.dialogs.getSellReject()
            newPatience -= (BargainConstants.PATIENCE_SELL_LARGE_PENALTY * personality.patiencePenaltyMultiplier).toInt()
        }

        if (newPatience <= 0) {
            newPatience = 0
            isFailed = true
            updatedMessages.add(
                BargainMessage(
                    text = "Bu fiyata olmaz, ben vazgeçtim!",
                    isFromPlayer = false,
                    timestamp = getCurrentTime()
                )
            )
        } else {
            val buyerMsg = BargainMessage(
                text = buyerResponseText,
                isFromPlayer = false,
                timestamp = getCurrentTime()
            )
            updatedMessages.add(buyerMsg)
        }

        var relationshipDelta = 0
        if (isFailed) {
            relationshipDelta = -10
        } else if (isDealClosed) {
            relationshipDelta = if (newPatience > 60) 5 else if (newPatience < 30) -5 else 2
        }

        _sellBargainState.value = state.copy(
            messages = updatedMessages,
            buyerPatience = newPatience,
            buyerMood = calculateMood(newPatience),
            isDealClosed = isDealClosed,
            isFailed = isFailed,
            agreedPrice = agreedPrice,
            lastBuyerOffer = lastBuyerOffer ?: 0.0,
            lastPlayerOfferAmount = offerAmount,
            relationshipDelta = relationshipDelta
        )

        if (isFailed) {
            scope.launch { repository.recordFailedBargain() }
        }
    }

    fun sellAgreedItem() {
        val state = _sellBargainState.value ?: return
        if (!state.isDealClosed) return

        scope.launch {
            val success = repository.sellItem(state.item, state.agreedPrice)
            if (success) {
                if (state.relationshipDelta != 0) {
                    repository.updateNpcRelationship(state.buyerName, state.relationshipDelta)
                }
                closeSellBargain()
            }
        }
    }

    private fun calculateMood(patience: Int): String {
        return when {
            patience >= BargainConstants.MOOD_HAPPY_MIN -> "Mutlu"
            patience >= BargainConstants.MOOD_UNSURE_MIN -> "Kararsız"
            patience >= BargainConstants.MOOD_TENSE_MIN -> "Gergin"
            else -> "Sinirli"
        }
    }
}

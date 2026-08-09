package com.enesduvan.kelepiravi.viewmodel.bargain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.enesduvan.kelepiravi.data.market.NegotiationEngine
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.domain.repository.IKelepiraviRepository
import com.enesduvan.kelepiravi.domain.usecase.GetPlayerStateUseCase
import com.enesduvan.kelepiravi.ui.shared.SoundManager
import com.enesduvan.kelepiravi.viewmodel.BargainState
import com.enesduvan.kelepiravi.viewmodel.MarketUiState
import com.enesduvan.kelepiravi.viewmodel.PlayerState
import com.enesduvan.kelepiravi.viewmodel.SellBargainState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class BargainViewModel(
    private val repository: IKelepiraviRepository,
    private val soundManager: SoundManager,
    private val getPlayerStateUseCase: GetPlayerStateUseCase = GetPlayerStateUseCase(repository)
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = getPlayerStateUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlayerState()
        )

    private val _uiState = MutableStateFlow(MarketUiState())
    private val _scamReveal = MutableStateFlow<MarketItem?>(null)

    private val negotiationEngine = NegotiationEngine(
        scope = viewModelScope,
        repository = repository,
        uiStateFlow = _uiState,
        scamRevealFlow = _scamReveal
    )

    val bargainState: StateFlow<BargainState?> = negotiationEngine.bargainState
    val sellBargainState: StateFlow<SellBargainState?> = negotiationEngine.sellBargainState

    // ─── Alış Pazarlığı ───────────────────────────────────────────────────────

    fun openSellerProfile(sellerName: String, sellerTitle: String) {}

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

    // ─── Satış Pazarlığı ───────────────────────────────────────────────────────

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
}

class BargainViewModelFactory(
    private val repository: IKelepiraviRepository,
    private val soundManager: SoundManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BargainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BargainViewModel(repository, soundManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

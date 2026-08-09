package com.enesduvan.kelepiravi.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.enesduvan.kelepiravi.data.market.MarketGenerator
import com.enesduvan.kelepiravi.domain.repository.IKelepiraviRepository
import com.enesduvan.kelepiravi.domain.usecase.GetPlayerStateUseCase
import com.enesduvan.kelepiravi.domain.usecase.UpgradeShopUseCase
import com.enesduvan.kelepiravi.viewmodel.PlayerState
import com.enesduvan.kelepiravi.viewmodel.SellerProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class ProfileViewModel(
    private val repository: IKelepiraviRepository,
    private val getPlayerStateUseCase: GetPlayerStateUseCase = GetPlayerStateUseCase(repository),
    private val upgradeShopUseCase: UpgradeShopUseCase = UpgradeShopUseCase(repository)
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = getPlayerStateUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlayerState()
        )

    private val _sellerProfile = MutableStateFlow<SellerProfileState?>(null)
    val sellerProfile: StateFlow<SellerProfileState?> = _sellerProfile.asStateFlow()

    fun openSellerProfile(sellerName: String, sellerTitle: String) {
        val rng = Random(sellerName.hashCode())
        val isScammer = MarketGenerator.SCAMMER_SELLERS.contains(sellerName)
        
        val rating = if (isScammer) rng.nextDouble(1.0, 3.5) else rng.nextDouble(3.8, 5.0)
        val joinYear = rng.nextInt(2015, 2024)
        val totalSales = rng.nextInt(1, 500)
        
        val otherItems = List(rng.nextInt(2, 4)) {
            MarketGenerator.generateItemForSeller(rng, sellerName)
        }
        
        _sellerProfile.value = SellerProfileState(sellerName, sellerTitle, rating, joinYear, totalSales, otherItems)
    }

    fun closeSellerProfile() {
        _sellerProfile.value = null
    }

    fun getShopUpgradeCost(level: Int): Double = upgradeShopUseCase.getShopUpgradeCost(level)

    fun getMechanicUpgradeCost(level: Int): Double = upgradeShopUseCase.getMechanicUpgradeCost(level)

    fun upgradeShop() {
        viewModelScope.launch { upgradeShopUseCase.upgradeShop(playerState.value.shopLevel) }
    }

    fun upgradeMechanic() {
        viewModelScope.launch { upgradeShopUseCase.upgradeMechanic(playerState.value.mechanicLevel) }
    }
}

class ProfileViewModelFactory(
    private val repository: IKelepiraviRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

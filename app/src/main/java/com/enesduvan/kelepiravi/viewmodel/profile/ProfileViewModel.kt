package com.enesduvan.kelepiravi.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.data.market.MarketGenerator
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.data.repository.KelepiraviRepository
import com.enesduvan.kelepiravi.viewmodel.PlayerState
import com.enesduvan.kelepiravi.viewmodel.SellerProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class ProfileViewModel(
    private val repository: KelepiraviRepository
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
}

class ProfileViewModelFactory(
    private val repository: KelepiraviRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

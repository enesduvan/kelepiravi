package com.enesduvan.kelepiravi.viewmodel.repair

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.data.repository.KelepiraviRepository
import com.enesduvan.kelepiravi.data.repository.RepairResult
import com.enesduvan.kelepiravi.viewmodel.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class RepairResultState(
    val isSuccess: Boolean,
    val newCondition: String = "",
    val itemName: String = ""
)

class RepairViewModel(
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

    private val _repairResult = MutableStateFlow<RepairResultState?>(null)
    val repairResult: StateFlow<RepairResultState?> = _repairResult.asStateFlow()

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
                is RepairResult.LimitReached -> {}
                is RepairResult.NotEnoughMoney -> {}
            }
        }
    }

    fun dismissRepairResult() {
        _repairResult.value = null
    }
}

class RepairViewModelFactory(
    private val repository: KelepiraviRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RepairViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RepairViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

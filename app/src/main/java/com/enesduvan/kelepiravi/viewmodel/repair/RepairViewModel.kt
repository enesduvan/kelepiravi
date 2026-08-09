package com.enesduvan.kelepiravi.viewmodel.repair

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.domain.repository.IKelepiraviRepository
import com.enesduvan.kelepiravi.domain.model.RepairResult
import com.enesduvan.kelepiravi.domain.usecase.GetPlayerStateUseCase
import com.enesduvan.kelepiravi.domain.usecase.RepairItemUseCase
import com.enesduvan.kelepiravi.viewmodel.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class RepairResultState(
    val isSuccess: Boolean,
    val newCondition: String = "",
    val itemName: String = ""
)

class RepairViewModel(
    private val repository: IKelepiraviRepository,
    private val getPlayerStateUseCase: GetPlayerStateUseCase = GetPlayerStateUseCase(repository),
    private val repairItemUseCase: RepairItemUseCase = RepairItemUseCase(repository)
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = getPlayerStateUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlayerState()
        )

    private val _repairResult = MutableStateFlow<RepairResultState?>(null)
    val repairResult: StateFlow<RepairResultState?> = _repairResult.asStateFlow()

    fun getRemainingRepairs(): Int = repairItemUseCase.getRemainingRepairs(playerState.value)

    fun calculateRepairCost(item: MarketItem, isUsta: Boolean = false): Double = repairItemUseCase.calculateRepairCost(item, isUsta)

    fun repairItem(item: MarketItem, isUsta: Boolean = false) {
        viewModelScope.launch {
            when (val result = repairItemUseCase.repair(item, isUsta)) {
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
                is RepairResult.NotOwned -> {}
            }
        }
    }

    fun dismissRepairResult() {
        _repairResult.value = null
    }
}

class RepairViewModelFactory(
    private val repository: IKelepiraviRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RepairViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RepairViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

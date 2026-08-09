package com.enesduvan.kelepiravi.viewmodel.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.enesduvan.kelepiravi.data.event.EventChoice
import com.enesduvan.kelepiravi.data.event.EventDefinition
import com.enesduvan.kelepiravi.data.repository.KelepiraviRepository
import com.enesduvan.kelepiravi.domain.usecase.AdvanceDayUseCase
import com.enesduvan.kelepiravi.domain.usecase.GetPlayerStateUseCase
import com.enesduvan.kelepiravi.viewmodel.DailySummaryState
import com.enesduvan.kelepiravi.viewmodel.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: KelepiraviRepository,
    private val getPlayerStateUseCase: GetPlayerStateUseCase = GetPlayerStateUseCase(repository),
    private val advanceDayUseCase: AdvanceDayUseCase = AdvanceDayUseCase(repository)
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = getPlayerStateUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlayerState()
        )

    private val _dailySummary = MutableStateFlow<DailySummaryState?>(null)
    val dailySummary: StateFlow<DailySummaryState?> = _dailySummary.asStateFlow()

    private val _interactiveEvent = MutableStateFlow<EventDefinition?>(null)
    val interactiveEvent: StateFlow<EventDefinition?> = _interactiveEvent.asStateFlow()

    private val _eventResult = MutableStateFlow<String?>(null)
    val eventResult: StateFlow<String?> = _eventResult.asStateFlow()

    private val _isDayAdvancing = MutableStateFlow(false)
    val isDayAdvancing: StateFlow<Boolean> = _isDayAdvancing.asStateFlow()

    fun advanceDay(onDayAdvanced: () -> Unit = {}) {
        if (_isDayAdvancing.value) return
        _isDayAdvancing.value = true
        viewModelScope.launch {
            val (summary, result) = advanceDayUseCase.advance(playerState.value.currentDay)
            _dailySummary.value = summary
            _interactiveEvent.value = result.interactiveEvent
            onDayAdvanced()
            _isDayAdvancing.value = false
        }
    }

    fun dismissDailySummary() {
        _dailySummary.value = null
    }

    fun dismissInteractiveEvent() {
        _interactiveEvent.value = null
    }

    fun applyInteractiveEventChoice(choice: EventChoice) {
        viewModelScope.launch {
            val generatedItems = advanceDayUseCase.applyEventChoice(choice)
            _interactiveEvent.value = null
            
            val resultText = buildString {
                if (choice.outcomeText != null) {
                    append(choice.outcomeText)
                    append("\n\n")
                }
                
                if (choice.rewards.isEmpty() && choice.penalties.isEmpty()) {
                    if (choice.outcomeText == null) append("Hiçbir şey olmadı.")
                } else {
                    if (choice.rewards.isNotEmpty()) {
                        append("KAZANIMLAR:\n")
                        var itemIndex = 0
                        choice.rewards.forEach { r -> 
                            if (r.type == "ITEM") {
                                val itemName = generatedItems.getOrNull(itemIndex) ?: r.value
                                append("+ Eşya: $itemName\n")
                                itemIndex++
                            } else {
                                val valText = if (r.type == "MONEY_EXACT" || r.type == "MONEY_PERCENT") "₺${r.value}" else r.value
                                append("+ ${r.type.replace("XP", "Tecrübe").replace("MONEY_EXACT", "Nakit")} $valText\n")
                            } 
                        }
                    }
                    if (choice.penalties.isNotEmpty()) {
                        if (choice.rewards.isNotEmpty()) append("\n")
                        append("KAYIPLAR:\n")
                        choice.penalties.forEach { p -> 
                            val valText = if (p.type == "MONEY_EXACT" || p.type == "MONEY_PERCENT") "₺${p.value}" else p.value
                            append("- ${p.type.replace("ITEM", "Eşya").replace("XP", "Tecrübe").replace("MONEY_EXACT", "Nakit")} $valText\n") 
                        }
                    }
                }
            }
            _eventResult.value = resultText.trim()
        }
    }
    
    fun dismissEventResult() {
        _eventResult.value = null
    }
}

class GameViewModelFactory(
    private val repository: KelepiraviRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

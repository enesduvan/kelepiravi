package com.enesduvan.kelepiravi.viewmodel.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.data.event.EventChoice
import com.enesduvan.kelepiravi.data.event.EventDefinition
import com.enesduvan.kelepiravi.data.repository.KelepiraviRepository
import com.enesduvan.kelepiravi.viewmodel.DailySummaryState
import com.enesduvan.kelepiravi.viewmodel.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(
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
            val result = repository.advanceDay()
            val nextDay = playerState.value.currentDay + 1
            _dailySummary.value = DailySummaryState(
                day = nextDay,
                xpGained = GameConstants.DAILY_LOGIN_XP,
                bonusMoney = GameConstants.DAILY_LOGIN_BONUS,
                taxPaid = result.taxPaid,
                rentPaid = result.rentPaid,
                event = result.event
            )
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
            val generatedItems = repository.applyEventChoice(choice)
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

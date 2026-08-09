package com.enesduvan.kelepiravi.domain.usecase

import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.data.event.EventChoice
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.data.repository.AdvanceDayResult
import com.enesduvan.kelepiravi.domain.repository.IKelepiraviRepository
import com.enesduvan.kelepiravi.viewmodel.DailySummaryState

class AdvanceDayUseCase(private val repository: IKelepiraviRepository) {

    suspend fun advance(currentDay: Int): Pair<DailySummaryState, AdvanceDayResult> {
        val result = repository.advanceDay()
        val nextDay = currentDay + 1
        val summary = DailySummaryState(
            day = nextDay,
            xpGained = GameConstants.DAILY_LOGIN_XP,
            bonusMoney = GameConstants.DAILY_LOGIN_BONUS,
            taxPaid = result.taxPaid,
            rentPaid = result.rentPaid,
            event = result.event
        )
        return Pair(summary, result)
    }

    suspend fun applyEventChoice(choice: EventChoice): List<MarketItem> {
        return repository.applyEventChoice(choice)
    }
}

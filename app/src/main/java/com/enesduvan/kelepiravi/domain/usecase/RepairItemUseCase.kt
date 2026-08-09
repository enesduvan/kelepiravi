package com.enesduvan.kelepiravi.domain.usecase

import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.data.repository.RepairResult
import com.enesduvan.kelepiravi.domain.repository.IKelepiraviRepository
import com.enesduvan.kelepiravi.viewmodel.PlayerState

class RepairItemUseCase(private val repository: IKelepiraviRepository) {

    fun getRemainingRepairs(playerState: PlayerState): Int {
        return if (playerState.lastRepairDay != playerState.currentDay) GameConstants.DAILY_REPAIR_LIMIT
        else (GameConstants.DAILY_REPAIR_LIMIT - playerState.dailyRepairsUsed).coerceAtLeast(0)
    }

    fun calculateRepairCost(item: MarketItem, isUsta: Boolean): Double {
        return repository.calculateRepairCost(item, isUsta)
    }

    suspend fun repair(item: MarketItem, isUsta: Boolean): RepairResult {
        return repository.repairItem(item, isUsta)
    }
}

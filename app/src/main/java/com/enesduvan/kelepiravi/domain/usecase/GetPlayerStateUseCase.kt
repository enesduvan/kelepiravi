package com.enesduvan.kelepiravi.domain.usecase

import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.domain.repository.IKelepiraviRepository
import com.enesduvan.kelepiravi.domain.model.PlayerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetPlayerStateUseCase(private val repository: IKelepiraviRepository) {
    operator fun invoke(): Flow<PlayerState> {
        return combine(
            repository.getPlayerState(),
            repository.observeUserInventoryItems(),
            repository.observePlayerStatistics()
        ) { list, inventory, statistics ->
            val entity = list.firstOrNull { it.playerId == GameConstants.DEFAULT_USER_ID }
            val investment = inventory.sumOf { it.purchasePrice.toDouble().coerceAtLeast(0.0) }
            val value = inventory.sumOf { it.estimatedValue.toDouble().coerceAtLeast(0.0) }
            val roi = if (investment > 0.0) ((value - investment) / investment) * 100.0 else 0.0
            PlayerState(
                balance          = entity?.balance ?: GameConstants.INITIAL_BALANCE,
                inventory        = inventory,
                currentDay       = entity?.currentDay ?: 1,
                xp               = entity?.xp ?: 0,
                level            = entity?.level ?: 1,
                shopLevel        = entity?.shopLevel ?: 1,
                mechanicLevel    = entity?.mechanicLevel ?: 1,
                unlockedAchievements = entity?.unlockedAchievements ?: "",
                totalProfit      = statistics?.totalProfit ?: 0.0,
                itemsBought      = statistics?.itemsBought ?: 0,
                itemsSold        = statistics?.itemsSold ?: 0,
                highestProfit    = statistics?.highestProfit ?: 0.0,
                rareItemsFound   = statistics?.rareItemsFound ?: 0,
                totalRepairs     = statistics?.totalRepairs ?: (entity?.totalRepairs ?: 0),
                hasBoughtScam    = entity?.hasBoughtScam ?: false,
                hasBoughtAbsurd  = entity?.hasBoughtAbsurd ?: false,
                dailyRepairsUsed = entity?.dailyRepairsUsed ?: 0,
                lastRepairDay    = entity?.lastRepairDay ?: 0,
                dailyRevenue     = statistics?.dailyRevenue ?: 0.0,
                successfulBargains = statistics?.successfulBargains ?: 0,
                totalBargains    = statistics?.totalBargains ?: 0,
                soldCategories   = parseCategories(statistics?.soldCategories),
                portfolioValue   = value,
                totalInvestment  = investment,
                portfolioROI     = roi
            )
        }
    }

    private fun parseCategories(serialized: String?): Map<String, Int> =
        serialized.orEmpty().split(',').mapNotNull { entry ->
            val parts = entry.split(':', limit = 2)
            if (parts.size == 2) parts[0] to (parts[1].toIntOrNull() ?: 0) else null
        }.toMap()
}

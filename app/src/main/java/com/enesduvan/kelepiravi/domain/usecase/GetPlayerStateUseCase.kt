package com.enesduvan.kelepiravi.domain.usecase

import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.domain.repository.IKelepiraviRepository
import com.enesduvan.kelepiravi.viewmodel.PlayerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetPlayerStateUseCase(private val repository: IKelepiraviRepository) {
    operator fun invoke(): Flow<PlayerState> {
        return repository.getPlayerState().map { list ->
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
    }
}

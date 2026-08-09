package com.enesduvan.kelepiravi.domain.usecase

import com.enesduvan.kelepiravi.data.market.Achievement
import com.enesduvan.kelepiravi.data.market.AchievementManager
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.domain.repository.IKelepiraviRepository
import com.enesduvan.kelepiravi.domain.model.PlayerState

class PurchaseItemUseCase(private val repository: IKelepiraviRepository) {
    suspend operator fun invoke(item: MarketItem, playerState: PlayerState): Pair<Boolean, Achievement?> {
        val success = repository.purchaseItem(item)
        if (!success) return Pair(false, null)

        val newlyUnlocked = AchievementManager.checkAchievements(
            balance = playerState.balance.toDouble(),
            itemsBought = playerState.itemsBought + 1,
            itemsSold = playerState.itemsSold,
            totalRepairs = playerState.totalRepairs,
            boughtScam = playerState.hasBoughtScam || item.isScammer,
            boughtAbsurd = playerState.hasBoughtAbsurd,
            unlockedIds = playerState.unlockedAchievements.split(",").filter { it.isNotBlank() }
        )

        val achievement = newlyUnlocked.firstOrNull()
        return Pair(true, achievement)
    }
}

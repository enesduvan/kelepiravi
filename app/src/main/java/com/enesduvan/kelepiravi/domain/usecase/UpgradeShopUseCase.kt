package com.enesduvan.kelepiravi.domain.usecase

import com.enesduvan.kelepiravi.domain.repository.IKelepiraviRepository

class UpgradeShopUseCase(private val repository: IKelepiraviRepository) {

    fun getShopUpgradeCost(level: Int): Double = when (level) {
        1 -> 15000.0
        2 -> 50000.0
        3 -> 150000.0
        4 -> 500000.0
        else -> 0.0
    }

    fun getMechanicUpgradeCost(level: Int): Double = when (level) {
        1 -> 20000.0
        2 -> 75000.0
        3 -> 250000.0
        4 -> 1000000.0
        else -> 0.0
    }

    suspend fun upgradeShop(currentLevel: Int): Boolean {
        if (currentLevel >= 5) return false
        val cost = getShopUpgradeCost(currentLevel)
        return repository.upgradeShop(cost)
    }

    suspend fun upgradeMechanic(currentLevel: Int): Boolean {
        if (currentLevel >= 5) return false
        val cost = getMechanicUpgradeCost(currentLevel)
        return repository.upgradeMechanic(cost)
    }
}

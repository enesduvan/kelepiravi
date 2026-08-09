package com.enesduvan.kelepiravi.domain.model

sealed interface RepairResult {
    data object Success : RepairResult
    data class Failure(val newCondition: String) : RepairResult
    data object LimitReached : RepairResult
    data object NotEnoughMoney : RepairResult
    data object NotOwned : RepairResult
}

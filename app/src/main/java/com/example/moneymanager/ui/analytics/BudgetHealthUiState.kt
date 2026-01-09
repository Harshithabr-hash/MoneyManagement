package com.example.moneymanager.ui.analytics

data class BudgetHealthUiState(
    val isBudgetSet: Boolean = false,
    val status: String = "",
    val usedPercent: Int = 0,
    val remainingAmount: Double = 0.0,
    val remainingDays: Int = 0,
    val safeDailySpend: Double = 0.0
)
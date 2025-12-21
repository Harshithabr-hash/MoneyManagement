package com.example.moneymanager.ui.home

enum class WarningLevel {
    SAFE,
    WARNING,
    EXCEEDED
}

data class CategoryWarning(
    val category: String,
    val spent: Double,
    val limit: Double,
    val level: WarningLevel
)

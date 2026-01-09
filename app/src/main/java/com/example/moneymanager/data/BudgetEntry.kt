package com.example.moneymanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_table")
data class BudgetEntry(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // 🔑 NEW: identifies which user this budget belongs to
    val userId: String,

    // "Monthly" or "Weekly"
    val type: String,

    // =========================
    // PERIOD IDENTIFICATION
    // =========================
    val periodStart: Long,
    val periodEnd: Long,

    // =========================
    // BUDGET VALUES
    // =========================

    // For overall budgets (category == null)
    val overallAmount: Double? = null,

    // For category budgets (category != null)
    val category: String? = null,
    val categoryAmount: Double? = null
)

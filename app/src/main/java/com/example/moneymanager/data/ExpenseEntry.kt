package com.example.moneymanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_table")
data class ExpenseEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // 🔑 NEW: identifies which user this expense belongs to
    val userId: String,

    val amount: Double,
    val category: String,
    val date: Long,
    val paymentMethod: String,
    val note: String?,
    val type: String
)

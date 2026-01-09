package com.example.moneymanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntry)

    // =========================
    // OVERALL BUDGET
    // =========================
    @Query("""
        SELECT * FROM budget_table
        WHERE userId = :userId
        AND type = :type
        AND category IS NULL
        ORDER BY periodStart DESC
        LIMIT 1
    """)
    suspend fun getOverallBudget(
        userId: String,
        type: String
    ): BudgetEntry?

    // =========================
    // CATEGORY BUDGETS
    // =========================
    @Query("""
        SELECT * FROM budget_table
        WHERE userId = :userId
        AND type = :type
        AND category IS NOT NULL
        AND periodStart = (
            SELECT MAX(periodStart)
            FROM budget_table
            WHERE userId = :userId
            AND type = :type
        )
    """)
    suspend fun getCategoryBudgets(
        userId: String,
        type: String
    ): List<BudgetEntry>

    // =========================
    // DELETE BUDGETS BY TYPE
    // =========================
    @Query("""
        DELETE FROM budget_table
        WHERE userId = :userId
        AND type = :type
    """)
    suspend fun deleteBudgetsByType(
        userId: String,
        type: String
    )
}

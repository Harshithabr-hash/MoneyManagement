package com.example.moneymanager.data

import androidx.room.*

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntry)

    // OVERALL BUDGET
    @Query("""
        SELECT * FROM budget_table
        WHERE type = :type
        AND category IS NULL
        ORDER BY periodStart DESC
        LIMIT 1
    """)
    suspend fun getOverallBudget(type: String): BudgetEntry?

    // CATEGORY BUDGETS
    @Query("""
        SELECT * FROM budget_table
        WHERE type = :type
        AND category IS NOT NULL
        AND periodStart = (
            SELECT MAX(periodStart)
            FROM budget_table
            WHERE type = :type
        )
    """)
    suspend fun getCategoryBudgets(type: String): List<BudgetEntry>

    @Query("""
        DELETE FROM budget_table
        WHERE type = :type
    """)
    suspend fun deleteBudgetsByType(type: String)
}

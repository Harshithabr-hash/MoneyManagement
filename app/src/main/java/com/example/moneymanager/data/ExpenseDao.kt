package com.example.moneymanager.data

import androidx.room.*





@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: ExpenseEntry)

    @Query("SELECT * FROM expense_table ORDER BY date DESC")
    suspend fun getAllExpenses(): List<ExpenseEntry>

    @Query("SELECT SUM(amount) FROM expense_table WHERE type = 'Expense'")
    suspend fun getTotalExpense(): Double?

    @Query("SELECT SUM(amount) FROM expense_table WHERE type = 'Income'")
    suspend fun getTotalIncome(): Double?

    @Query("SELECT * FROM expense_table WHERE date >= :from ORDER BY date DESC")
    suspend fun getExpensesAfter(from: Long): List<ExpenseEntry>

    // ✅ KEEP ONLY THIS
    @Query("SELECT * FROM expense_table ORDER BY date DESC LIMIT :count")
    suspend fun getRecentTransactions(count: Int): List<ExpenseEntry>
    @Query("""
    SELECT SUM(amount) FROM expense_table
    WHERE type = 'Expense'
    AND date >= :from
""")
    suspend fun getTotalExpenseFrom(from: Long): Double?

}



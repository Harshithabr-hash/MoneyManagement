package com.example.moneymanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: ExpenseEntry)

    @Query("""
        SELECT * FROM expense_table 
        WHERE userId = :userId 
        ORDER BY date DESC
    """)
    suspend fun getAllExpenses(userId: String): List<ExpenseEntry>

    @Query("""
        SELECT SUM(amount) FROM expense_table 
        WHERE userId = :userId 
        AND type = 'Expense'
    """)
    suspend fun getTotalExpense(userId: String): Double?

    @Query("""
        SELECT SUM(amount) FROM expense_table 
        WHERE userId = :userId 
        AND type = 'Income'
    """)
    suspend fun getTotalIncome(userId: String): Double?

    @Query("""
        SELECT * FROM expense_table 
        WHERE userId = :userId 
        AND date >= :from 
        ORDER BY date DESC
    """)
    suspend fun getExpensesAfter(
        userId: String,
        from: Long
    ): List<ExpenseEntry>

    @Query("""
        SELECT * FROM expense_table 
        WHERE userId = :userId 
        ORDER BY date DESC 
        LIMIT :count
    """)
    suspend fun getRecentTransactions(
        userId: String,
        count: Int
    ): List<ExpenseEntry>

    @Query("""
        SELECT SUM(amount) FROM expense_table
        WHERE userId = :userId
        AND type = 'Expense'
        AND date >= :from
    """)
    suspend fun getTotalExpenseFrom(
        userId: String,
        from: Long
    ): Double?
}

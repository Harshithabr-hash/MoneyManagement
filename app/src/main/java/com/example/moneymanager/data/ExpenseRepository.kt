package com.example.moneymanager.data

class ExpenseRepository(private val dao: ExpenseDao) {

    suspend fun insertExpense(expense: ExpenseEntry) = dao.insertExpense(expense)

    suspend fun getExpenses() = dao.getAllExpenses()

    suspend fun getTotalExpense() = dao.getTotalExpense() ?: 0.0

    suspend fun getTotalIncome() = dao.getTotalIncome() ?: 0.0

    suspend fun getExpensesAfter(date: Long) = dao.getExpensesAfter(date)

    // FIXED — Correct function


    // For flexibility (optional)
    suspend fun getRecentTransactions(count: Int): List<ExpenseEntry> =
        dao.getRecentTransactions(count)
}

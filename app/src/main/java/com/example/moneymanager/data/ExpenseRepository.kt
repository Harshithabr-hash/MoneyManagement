package com.example.moneymanager.data

import com.google.firebase.auth.FirebaseAuth

class ExpenseRepository(
    private val dao: ExpenseDao,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    // Get current logged-in user's UID
    private fun currentUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User not logged in")
    }

    suspend fun insertExpense(expense: ExpenseEntry) {
        dao.insertExpense(
            expense.copy(userId = currentUserId())
        )
    }

    suspend fun getExpenses(): List<ExpenseEntry> =
        dao.getAllExpenses(currentUserId())

    suspend fun getTotalExpense(): Double =
        dao.getTotalExpense(currentUserId()) ?: 0.0

    suspend fun getTotalIncome(): Double =
        dao.getTotalIncome(currentUserId()) ?: 0.0

    suspend fun getExpensesAfter(date: Long): List<ExpenseEntry> =
        dao.getExpensesAfter(currentUserId(), date)

    suspend fun getRecentTransactions(count: Int): List<ExpenseEntry> =
        dao.getRecentTransactions(currentUserId(), count)
}

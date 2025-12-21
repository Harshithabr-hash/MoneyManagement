package com.example.moneymanager.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moneymanager.data.ExpenseRepository
import com.example.moneymanager.data.BudgetRepository

class DashboardViewModelFactory(
    private val expenseRepo: ExpenseRepository,
    private val budgetRepo: BudgetRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(expenseRepo, budgetRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.example.moneymanager.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moneymanager.data.BudgetRepository
import com.example.moneymanager.data.ExpenseRepository
import android.app.Application



class BudgetViewModelFactory(
    private val application: Application,
    private val budgetRepo: BudgetRepository,
    private val expenseRepo: ExpenseRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(
                application,
                budgetRepo,
                expenseRepo
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
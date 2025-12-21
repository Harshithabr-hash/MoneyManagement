package com.example.moneymanager.ui.addentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moneymanager.data.ExpenseRepository

class AddEntryViewModelFactory(private val repo: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddEntryViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

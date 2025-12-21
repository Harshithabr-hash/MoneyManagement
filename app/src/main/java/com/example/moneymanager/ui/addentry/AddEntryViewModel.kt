package com.example.moneymanager.ui.addentry
import com.example.moneymanager.data.ExpenseRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanager.data.ExpenseEntry
import kotlinx.coroutines.launch
import java.util.Calendar





class AddEntryViewModel(
    private val repo: ExpenseRepository
) : ViewModel() {



    fun addExpense(expense: ExpenseEntry) {
        viewModelScope.launch {
            repo.insertExpense(expense)
        }
    }
}
fun getTop3Categories(expenses: List<ExpenseEntry>): List<Pair<String, Int>> {

    val monthStart = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val monthlyExpenses = expenses.filter {
        it.type == "Expense" && it.date >= monthStart
    }

    val total = monthlyExpenses.sumOf { it.amount }
    if (total == 0.0) return emptyList()

    return monthlyExpenses
        .groupBy { it.category }
        .mapValues { it.value.sumOf { e -> e.amount } }
        .map { (category, amount) ->
            category to ((amount / total) * 100).toInt()
        }
        .sortedByDescending { it.second }
        .take(3)
}


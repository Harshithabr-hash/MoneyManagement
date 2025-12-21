package com.example.moneymanager.ui.home

import androidx.lifecycle.*
import com.example.moneymanager.data.BudgetRepository
import com.example.moneymanager.data.ExpenseRepository
import com.example.moneymanager.data.ExpenseEntry
import kotlinx.coroutines.launch
import java.time.*
import java.time.temporal.WeekFields
import java.util.*

class DashboardViewModel(
    private val expenseRepo: ExpenseRepository,
    private val budgetRepo: BudgetRepository
) : ViewModel() {

    // 🔔 Alert state (SESSION ONLY)


    val totalExpense = MutableLiveData(0.0)
    val totalIncome = MutableLiveData(0.0)

    private val _monthlyBudget = MutableLiveData<Double?>(null)
    val monthlyBudget: LiveData<Double?> = _monthlyBudget

    private val _weeklyBudget = MutableLiveData<Double?>(null)
    val weeklyBudget: LiveData<Double?> = _weeklyBudget



    val weeklySpent = MutableLiveData(0.0)
    val monthlySpent = MutableLiveData(0.0)

    val weeklyRemaining = MutableLiveData(0.0)
    val monthlyRemaining = MutableLiveData(0.0)

    private val _mostSpentCategory = MutableLiveData<Pair<String, Double>?>(null)
    val mostSpentCategory: LiveData<Pair<String, Double>?> = _mostSpentCategory

    private val _recentTransactions = MutableLiveData<List<ExpenseEntry>>(emptyList())
    val recentTransactions: LiveData<List<ExpenseEntry>> = _recentTransactions

    val categoryWarnings = MutableLiveData<Map<String, String>>(emptyMap())

    // 🔁 CALL THIS WHEN USER EDITS BUDGET


    fun loadDashboardData() {
        viewModelScope.launch {

            val allExpenses = expenseRepo.getExpenses()

            totalIncome.postValue(expenseRepo.getTotalIncome())
            totalExpense.postValue(expenseRepo.getTotalExpense())
            _recentTransactions.postValue(expenseRepo.getRecentTransactions(3))

            // ---------------- CATEGORY WARNINGS ----------------
            val categoryTotals = allExpenses
                .filter { it.type == "Expense" }
                .groupBy { it.category }
                .mapValues { it.value.sumOf { e -> e.amount } }

            val categoryBudgets = budgetRepo.getCategoryBudgets("Monthly")
            val warnings = mutableMapOf<String, String>()

            categoryBudgets.forEach { budget ->
                val category = budget.category ?: return@forEach
                val limit = budget.categoryAmount ?: return@forEach
                val spent = categoryTotals[category] ?: 0.0

                when {
                    spent > limit ->
                        warnings[category] = "🚨 Exceeded by ₹${(spent - limit).toInt()}"
                    spent >= limit * 0.9 ->
                        warnings[category] = "⚠ Near limit"
                }
            }
            categoryWarnings.postValue(warnings)

            // ---------------- LOAD BUDGETS ----------------
            val monthlyValue = budgetRepo.getOverallBudget("Monthly")?.overallAmount
            val weeklyValue = budgetRepo.getOverallBudget("Weekly")?.overallAmount

            _monthlyBudget.postValue(monthlyValue)
            _weeklyBudget.postValue(weeklyValue)

            val today = LocalDate.now()
            val zone = ZoneId.systemDefault()
            val weekFields = WeekFields.of(Locale.getDefault())

            // ---------------- WEEKLY ----------------
            val weekStart = today.with(weekFields.dayOfWeek(), 1)
                .atStartOfDay(zone).toInstant().toEpochMilli()

            val weekEnd = today.with(weekFields.dayOfWeek(), 7)
                .atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()

            val weeklyTotal = allExpenses
                .filter { it.type == "Expense" && it.date in weekStart..weekEnd }
                .sumOf { it.amount }

            weeklySpent.postValue(weeklyTotal)
            weeklyValue?.let { weeklyRemaining.postValue(it - weeklyTotal) }

            // ---------------- MONTHLY ----------------
            val monthStart = today.withDayOfMonth(1)
                .atStartOfDay(zone).toInstant().toEpochMilli()

            val monthlyTotal = allExpenses
                .filter { it.type == "Expense" && it.date >= monthStart }
                .sumOf { it.amount }

            monthlySpent.postValue(monthlyTotal)
            monthlyValue?.let { monthlyRemaining.postValue(it - monthlyTotal) }

            _mostSpentCategory.postValue(categoryTotals.maxByOrNull { it.value }?.toPair())

            // 🔔 ALERT — SHOW ONLY ONCE

        }
    }
}

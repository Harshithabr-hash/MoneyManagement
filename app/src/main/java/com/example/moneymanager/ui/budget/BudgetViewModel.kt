package com.example.moneymanager.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.example.moneymanager.data.BudgetEntry
import com.example.moneymanager.data.BudgetRepository
import kotlinx.coroutines.launch
import com.example.moneymanager.data.ExpenseRepository
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.collect
import com.example.moneymanager.data.UserPreferences






class BudgetViewModel(
    application: Application,
    private val repo: BudgetRepository,
    private val expenseRepo: ExpenseRepository
) : AndroidViewModel(application) {

    private val prefs = UserPreferences(application)

    val selectedType = MutableLiveData("Monthly")

    val overallMonthly = MutableLiveData("")
    val overallWeekly = MutableLiveData("")

    val categoryMonthly = MutableLiveData<Map<String, String>>(emptyMap())
    val categoryWeekly = MutableLiveData<Map<String, String>>(emptyMap())

    val editMode = MutableLiveData(false)
    val categoryWarnings = MutableLiveData<Map<String, String>>(emptyMap())

    init {
        viewModelScope.launch {
            prefs.budgetType.collect { savedType ->
                selectedType.postValue(savedType)
                loadBudgets()
            }
        }
    }

    // ---------------- TYPE SWITCH ----------------
    fun setBudgetType(type: String) {
        selectedType.value = type
        viewModelScope.launch {
            prefs.saveBudgetType(type)
            prefs.setBudgetChanged(true)
            loadBudgets()
        }
    }

    // ---------------- EDIT MODE ----------------
    fun toggleEdit() {
        editMode.value = !(editMode.value ?: false)
    }

    // ---------------- RESET WEEKLY ----------------
    fun resetWeeklyBudget() {
        viewModelScope.launch {
            repo.clearBudgetType("Weekly")
            overallWeekly.postValue("")
            categoryWeekly.postValue(emptyMap())
            prefs.setBudgetChanged(true)
        }
    }

    // ---------------- LOAD BUDGETS ----------------
    fun loadBudgets() {
        viewModelScope.launch {

            // Overall budgets
            repo.getOverallBudget("Monthly")?.overallAmount?.let {
                overallMonthly.postValue(it.toString())
            }
            repo.getOverallBudget("Weekly")?.overallAmount?.let {
                overallWeekly.postValue(it.toString())
            }

            // Category limits
            val monthlyMap = repo.getCategoryBudgets("Monthly")
                .associate { it.category!! to it.categoryAmount!!.toString() }

            val weeklyMap = repo.getCategoryBudgets("Weekly")
                .associate { it.category!! to it.categoryAmount!!.toString() }

            categoryMonthly.postValue(monthlyMap)
            categoryWeekly.postValue(weeklyMap)

            // Expenses
            val expenses = expenseRepo.getExpenses()
                .filter { it.type == "Expense" }

            val today = LocalDate.now()
            val zone = ZoneId.systemDefault()

            val (start, end) =
                if (selectedType.value == "Monthly") {
                    val s = today.withDayOfMonth(1)
                        .atStartOfDay(zone).toInstant().toEpochMilli()
                    val e = today.withDayOfMonth(today.lengthOfMonth())
                        .atTime(23, 59, 59)
                        .atZone(zone).toInstant().toEpochMilli()
                    s to e
                } else {
                    val wf = WeekFields.of(Locale.getDefault())
                    val s = today.with(wf.dayOfWeek(), 1)
                        .atStartOfDay(zone).toInstant().toEpochMilli()
                    val e = today.with(wf.dayOfWeek(), 7)
                        .atTime(23, 59, 59)
                        .atZone(zone).toInstant().toEpochMilli()
                    s to e
                }

            val totals = expenses
                .filter { it.date in start..end }
                .groupBy { it.category.trim() }
                .mapValues { it.value.sumOf { e -> e.amount } }

            val limits =
                if (selectedType.value == "Monthly") monthlyMap else weeklyMap

            val warnings = mutableMapOf<String, String>()

            limits.forEach { (category, limitStr) ->
                val limit = limitStr.toDoubleOrNull() ?: return@forEach
                val spent = totals[category.trim()] ?: 0.0

                when {
                    spent > limit ->
                        warnings[category] = "🚨 Limit exceeded by ₹${(spent - limit).toInt()}"
                    spent >= limit * 0.8 ->
                        warnings[category] = "⚠ Near limit"
                }
            }

            categoryWarnings.postValue(warnings)
        }
    }

    // ---------------- SAVE OVERALL ----------------
    fun saveOverall(type: String, amount: Double) {
        viewModelScope.launch {
            repo.saveOverallBudget(type, amount)
            prefs.setBudgetChanged(true)

            if (type == "Monthly")
                overallMonthly.postValue(amount.toString())
            else
                overallWeekly.postValue(amount.toString())
        }
    }

    // ---------------- SAVE CATEGORY ----------------
    fun saveCategory(type: String, category: String, amount: Double) {
        viewModelScope.launch {
            repo.saveCategoryBudget(type, category, amount)
            prefs.setBudgetChanged(true)

            if (type == "Monthly") {
                val updated = categoryMonthly.value?.toMutableMap() ?: mutableMapOf()
                updated[category] = amount.toString()
                categoryMonthly.postValue(updated)
            } else {
                val updated = categoryWeekly.value?.toMutableMap() ?: mutableMapOf()
                updated[category] = amount.toString()
                categoryWeekly.postValue(updated)
            }

            loadBudgets()
        }
    }
}




package com.example.moneymanager.ui.analytics

import androidx.lifecycle.*
import com.example.moneymanager.data.ExpenseRepository
import com.example.moneymanager.MyApp
import android.app.Application
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import java.time.temporal.WeekFields
import com.example.moneymanager.data.BudgetRepository
import java.time.temporal.ChronoUnit





class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseRepo =
        ExpenseRepository((application as MyApp).db.expenseDao())

    // Category → Amount
    private val _categorySpendings = MutableLiveData<Map<String, Double>>()
    val categorySpendings: LiveData<Map<String, Double>> = _categorySpendings

    private val _monthlyTrend = MutableLiveData<List<Pair<String, Double>>>()
    val monthlyTrend: LiveData<List<Pair<String, Double>>> = _monthlyTrend


    val selectedPeriod = MutableLiveData("Monthly")
    private val budgetRepo =
        BudgetRepository((application as MyApp).db.budgetDao())

    private val _budgetHealthState = MutableLiveData<BudgetHealthUiState>()
    val budgetHealthState: LiveData<BudgetHealthUiState> = _budgetHealthState


    private fun getPeriodRange(type: String): Pair<Long, Long> {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()


        return if (type == "Monthly") {
            val start = today.withDayOfMonth(1)
                .atStartOfDay(zone).toInstant().toEpochMilli()

            val end = today.withDayOfMonth(today.lengthOfMonth())
                .atTime(23, 59, 59)
                .atZone(zone).toInstant().toEpochMilli()

            start to end
        } else {
            val weekFields = WeekFields.of(Locale.getDefault())

            val start = today.with(weekFields.dayOfWeek(), 1)
                .atStartOfDay(zone).toInstant().toEpochMilli()

            val end = today.with(weekFields.dayOfWeek(), 7)
                .atTime(23, 59, 59)
                .atZone(zone).toInstant().toEpochMilli()

            start to end
        }
    }


    fun loadAnalytics() {
        viewModelScope.launch {
            val zone = ZoneId.systemDefault()


            val period = selectedPeriod.value ?: "Monthly"
            val (startMillis, endMillis) = getPeriodRange(period)

            val expenses = expenseRepo.getExpenses()
                .filter { it.type == "Expense" }
                .filter { it.date in startMillis..endMillis }

            // ---------------- CATEGORY TOTALS (Pie Chart) ----------------
            val categoryResult =
                expenses
                    .groupBy { it.category?.trim().takeUnless { it.isNullOrEmpty() } ?: "Others" }

                    .mapValues { entry ->
                        entry.value.sumOf { it.amount }
                    }

            _categorySpendings.postValue(categoryResult)



            // ---------------- TREND DATA ----------------
            val trendResult =
                if (period == "Monthly") {
                    // Month-wise trend
                    expenses
                        .groupBy {
                            Instant.ofEpochMilli(it.date)
                                .atZone(zone)
                                .month
                                .getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        }
                        .mapValues { it.value.sumOf { e -> e.amount } }
                        .toList()
                } else {
                    // Day-wise trend for current week
                    expenses
                        .groupBy {
                            Instant.ofEpochMilli(it.date)
                                .atZone(zone)
                                .dayOfWeek
                                .getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        }
                        .mapValues { it.value.sumOf { e -> e.amount } }
                        .toList()
                }

            _monthlyTrend.postValue(trendResult)
            // ---------------- BUDGET HEALTH ----------------
            val budget = budgetRepo.getOverallBudget(period)

            if (budget == null || budget.overallAmount == null) {
                _budgetHealthState.postValue(BudgetHealthUiState(isBudgetSet = false))
                return@launch
            }

            val totalBudget = budget.overallAmount
            val used = expenses.sumOf { it.amount }
            val usedPercent =
                if (totalBudget == 0.0) 0 else ((used / totalBudget) * 100).toInt()

// ---- Days calculation ----

            val today = LocalDate.now()

            val startDate =
                Instant.ofEpochMilli(budget.periodStart).atZone(zone).toLocalDate()
            val endDate =
                Instant.ofEpochMilli(budget.periodEnd).atZone(zone).toLocalDate()

            val totalDays =
                ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1

            val daysPassed =
                ChronoUnit.DAYS.between(startDate, today).toInt().coerceAtLeast(0) + 1

            val remainingDays =
                (totalDays - daysPassed).coerceAtLeast(1)

            val remainingAmount =
                (totalBudget - used).coerceAtLeast(0.0)

            val safeDailySpend =
                remainingAmount / remainingDays

            val status = when {
                usedPercent < 70 -> "On Track"
                usedPercent < 90 -> "At Risk"
                else -> "Over Budget"
            }

            _budgetHealthState.postValue(
                BudgetHealthUiState(
                    isBudgetSet = true,
                    status = status,
                    usedPercent = usedPercent,
                    remainingAmount = remainingAmount,
                    remainingDays = remainingDays,
                    safeDailySpend = safeDailySpend
                )
            )

        }
    }
}



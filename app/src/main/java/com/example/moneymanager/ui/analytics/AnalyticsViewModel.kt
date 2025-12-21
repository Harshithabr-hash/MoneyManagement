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




class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseRepo =
        ExpenseRepository((application as MyApp).db.expenseDao())

    // Category → Amount
    private val _categorySpendings = MutableLiveData<Map<String, Double>>()
    val categorySpendings: LiveData<Map<String, Double>> = _categorySpendings

    private val _monthlyTrend = MutableLiveData<List<Pair<String, Double>>>()
    val monthlyTrend: LiveData<List<Pair<String, Double>>> = _monthlyTrend

    val selectedPeriod = MutableLiveData("Monthly")
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

            val zone = ZoneId.systemDefault()

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
        }
    }
}



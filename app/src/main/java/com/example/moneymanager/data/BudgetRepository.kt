package com.example.moneymanager.data

import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale

class BudgetRepository(
    private val dao: BudgetDao,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private fun currentUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User not logged in")
    }

    suspend fun saveOverallBudget(
        type: String,
        amount: Double
    ) {
        val (start, end) = getPeriodRange(type)
        val userId = currentUserId()

        val existing = dao.getOverallBudget(userId, type)?.takeIf {
            it.periodStart == start && it.periodEnd == end
        }

        dao.insertBudget(
            BudgetEntry(
                id = existing?.id ?: 0,
                userId = userId,
                type = type,
                periodStart = start,
                periodEnd = end,
                overallAmount = amount
            )
        )
    }

    suspend fun saveCategoryBudget(
        type: String,
        category: String,
        amount: Double
    ) {
        val (start, end) = getPeriodRange(type)

        dao.insertBudget(
            BudgetEntry(
                userId = currentUserId(),
                type = type,
                periodStart = start,
                periodEnd = end,
                category = category,
                categoryAmount = amount
            )
        )
    }

    suspend fun getOverallBudget(type: String) =
        dao.getOverallBudget(currentUserId(), type)

    suspend fun getCategoryBudgets(type: String) =
        dao.getCategoryBudgets(currentUserId(), type)

    suspend fun clearBudgetType(type: String) =
        dao.deleteBudgetsByType(currentUserId(), type)

    // =========================
    // PERIOD CALCULATION (UNCHANGED)
    // =========================
    private fun getPeriodRange(type: String): Pair<Long, Long> {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()

        return if (type == "Monthly") {
            val start = today.withDayOfMonth(1)
                .atStartOfDay(zone).toInstant().toEpochMilli()
            val end = today.withDayOfMonth(today.lengthOfMonth())
                .atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
            start to end
        } else {
            val start = today.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1)
                .atStartOfDay(zone).toInstant().toEpochMilli()
            val end = today.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 7)
                .atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
            start to end
        }
    }
}

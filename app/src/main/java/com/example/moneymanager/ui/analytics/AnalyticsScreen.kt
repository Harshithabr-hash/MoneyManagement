@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.moneymanager.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moneymanager.R

import androidx.compose.ui.platform.LocalContext
import com.example.moneymanager.data.UserPreferences


@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = viewModel()
) {
    // ---- USER BASED CURRENCY (DataStore) ----
    val context = LocalContext.current

    val userId = com.google.firebase.auth.FirebaseAuth
        .getInstance()
        .currentUser
        ?.uid

    val userPrefs = remember { UserPreferences(context) }

    val currency by remember(userId) {
        userPrefs.userCurrency(userId ?: "")
    }.collectAsState(initial = "INR - ₹ India")

    val currencySymbol = remember(currency) {
        currency
            .substringAfter(" - ", "")
            .substringBefore(" ")
            .ifBlank { "₹" }
    }


    var isWeekly by remember { mutableStateOf(false) }

    val bg = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val budgetHealth by viewModel.budgetHealthState.observeAsState()





    // Load analytics when period changes
    LaunchedEffect(isWeekly) {
        viewModel.selectedPeriod.value = if (isWeekly) "Weekly" else "Monthly"
        viewModel.loadAnalytics()
    }

    // Observed data
    val monthlyData by viewModel.monthlyTrend.observeAsState(emptyList())
    val categoryData by viewModel.categorySpendings.observeAsState(emptyMap())

    // 🔑 SINGLE SOURCE OF TRUTH
    val hasAnalyticsData =
        categoryData.isNotEmpty() || monthlyData.isNotEmpty()

    val currentTotal = monthlyData.lastOrNull()?.second ?: 0.0
    val previousTotal = monthlyData.dropLast(1).lastOrNull()?.second ?: 0.0
    val difference = currentTotal - previousTotal

    val topCategories = remember(categoryData) {
        val total = categoryData.values.sum()
        if (total == 0.0) emptyList()
        else categoryData.entries
            .sortedByDescending { it.value }
            .take(3)
            .map {
                it.key to ((it.value / total) * 100).toInt()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Analytics",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home") }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = bg
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 24.dp)
                .background(bg)
        ) {

            // PERIOD TOGGLE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = !isWeekly,
                    onClick = { isWeekly = false },
                    label = { Text("Monthly") }
                )
                FilterChip(
                    selected = isWeekly,
                    onClick = { isWeekly = true },
                    label = { Text("Weekly") }
                )
            }

            // =====================================================
            // EMPTY STATE (ONLY THIS WHEN NO DATA)
            // =====================================================
            if (!hasAnalyticsData) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.analytics),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No analytics yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Start adding expenses to see your spending insights",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

            } else {

                // =================================================
                // ALL ANALYTICS UI (ONLY WHEN DATA EXISTS)
                // =================================================

                // ------------------ REAL BUDGET HEALTH CARD ------------------

                budgetHealth?.let { state ->

                    if (!state.isBudgetSet) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    "Budget not set",
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Set a ${if (isWeekly) "weekly" else "monthly"} budget to see insights"
                                )
                            }
                        }

                    } else {

                        val statusColor = when (state.status) {
                            "On Track" -> Color(0xFF2E7D32)
                            "At Risk" -> Color(0xFFFFA000)
                            else -> Color(0xFFD32F2F)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(Modifier.padding(16.dp)) {

                                Text(
                                    text = "Budget Health",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = state.status,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )

                                Spacer(Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = (state.usedPercent / 100f).coerceIn(0f, 1f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    color = statusColor
                                )

                                Spacer(Modifier.height(8.dp))

                                Text("${state.usedPercent}% of budget used")

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    "$currencySymbol${state.remainingAmount.toInt()} left • ${state.remainingDays} days remaining"
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    "Safe daily spend: $currencySymbol${state.safeDailySpend.toInt()}",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }


                // ------------------ REAL BUDGET HEALTH CARD ------------------






                Text(
                    text = "Category-wise Spending",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(Modifier.height(12.dp))

                CategoryPieChart(
                    categoryData = categoryData.mapValues { it.value.toFloat() }
                )

                Spacer(Modifier.height(32.dp))

                Text(
                    text = if (isWeekly)
                        "Weekly Spending Trend"
                    else
                        "Monthly Spending Trend",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(Modifier.height(12.dp))

                MonthlyBarChart(
                    monthlyData = monthlyData.map { it.first to it.second.toFloat() }
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Top Spending Categories",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                topCategories.forEachIndexed { index, (category, percent) ->

                    val barColor = when (index) {
                        0 -> Color(0xFFD32F2F)
                        1 -> Color(0xFFFFA000)
                        else -> Color(0xFF388E3C)
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(category, fontWeight = FontWeight.Medium)
                            Text("$percent%", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = percent / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = barColor,
                            trackColor = Color.LightGray
                        )

                        Spacer(Modifier.height(16.dp))
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {

                        Text(
                            "💡 Tips & Recommendations",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(10.dp))
                        Text("• Follow the 50–30–20 rule for balanced spending.")
                        Spacer(Modifier.height(6.dp))
                        Text("• Keep discretionary expenses under 20% (80–20 rule).")
                        Spacer(Modifier.height(6.dp))
                        Text("• Focus on reducing your top spending category.")
                        Spacer(Modifier.height(6.dp))
                        Text("• Review expenses weekly to stay within budget.")
                        Spacer(Modifier.height(6.dp))
                        Text("• If you exceed your budget, cut back on non-essential spending.")
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // =====================================================
            // HISTORY BUTTON (ALWAYS VISIBLE)
            // =====================================================
            Button(
                onClick = { navController.navigate("history") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.history),
                    contentDescription = "History",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "View Transaction History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

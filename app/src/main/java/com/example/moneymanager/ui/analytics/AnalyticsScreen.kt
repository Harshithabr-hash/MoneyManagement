@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.moneymanager.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moneymanager.R
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = viewModel()
) {
    var isWeekly by remember { mutableStateOf(false) }



    val bg = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    // ✅ LOAD DATA ONCE
    LaunchedEffect(isWeekly) {
        viewModel.selectedPeriod.value = if (isWeekly) "Weekly" else "Monthly"
        viewModel.loadAnalytics()
    }



    // ✅ OBSERVE STATE
    val monthlyData by viewModel.monthlyTrend.observeAsState(emptyList())
    val currentTotal = monthlyData.lastOrNull()?.second ?: 0.0
    val previousTotal = monthlyData.dropLast(1).lastOrNull()?.second ?: 0.0
    val difference = currentTotal - previousTotal

    val categoryData by viewModel.categorySpendings.observeAsState(emptyMap())


    val topCategories = remember(categoryData) {
        val total = categoryData.values.sum()
        if (total == 0.0) {
            emptyList()
        } else {
            categoryData
                .entries
                .sortedByDescending { it.value }
                .take(3)
                .map { entry ->
                    val percent = ((entry.value / total) * 100).toInt()
                    entry.key to percent
                }
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
                            contentDescription = "Back to Dashboard",
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

    )
    { innerPadding ->


        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .background(bg)
        )
        {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = !isWeekly,
                    onClick = {
                        isWeekly = false
                    },
                    label = { Text("Monthly") }
                )

                FilterChip(
                    selected = isWeekly,
                    onClick = {
                        isWeekly = true
                    },
                    label = { Text("Weekly") }
                )
            }
            if (categoryData.isEmpty()) {

                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                return@Column
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        text = if (isWeekly) "This Week’s Spending" else "This Month’s Spending",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "₹${currentTotal.toInt()}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val trendText = when {
                        difference > 0 ->
                            "₹${difference.toInt()} more than last ${if (isWeekly) "week" else "month"}"
                        difference < 0 ->
                            "₹${kotlin.math.abs(difference).toInt()} less than last ${if (isWeekly) "week" else "month"}"
                        else ->
                            "Same as last ${if (isWeekly) "week" else "month"}"
                    }

                    val trendColor = when {
                        difference > 0 -> Color.Red
                        difference < 0 -> Color(0xFF2E7D32)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Text(
                        text = trendText,
                        color = trendColor,
                        fontSize = 14.sp
                    )
                }
            }





            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Category-wise Spending",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            CategoryPieChart(
                categoryData = categoryData.mapValues { it.value.toFloat() }
            )



            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if (isWeekly) "Weekly Spending Trend" else "Monthly Spending Trend",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            MonthlyBarChart(
                monthlyData = monthlyData.map { it.first to it.second.toFloat() }
            )
            Text(
                text = "Top Spending Categories",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            topCategories.forEachIndexed { index, (category, percent) ->

                val barColor = when (index) {
                    0 -> Color(0xFFD32F2F) // Red – highest
                    1 -> Color(0xFFFFA000) // Orange
                    else -> Color(0xFF388E3C) // Green
                }

                Column(modifier = Modifier.fillMaxWidth()) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = category,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$percent%",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = percent / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = barColor,
                        trackColor = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        text = "💡 Tips & Recommendations",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

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




            Spacer(modifier = Modifier.weight(1f))

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
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "View Transaction History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

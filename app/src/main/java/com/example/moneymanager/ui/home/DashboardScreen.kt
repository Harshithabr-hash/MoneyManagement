@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.moneymanager.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.runtime.livedata.observeAsState
import com.example.moneymanager.R
import com.example.moneymanager.MyApp
import com.example.moneymanager.data.BudgetRepository
import com.example.moneymanager.data.ExpenseRepository
import com.example.moneymanager.data.ExpenseEntry
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.text.style.TextAlign
import com.example.moneymanager.ui.history.getCategoryIcon
import com.example.moneymanager.ui.analytics.CategoryPieChart
import com.example.moneymanager.ui.analytics.MonthlyBarChart
import com.example.moneymanager.ui.analytics.AnalyticsViewModel
import kotlin.text.toFloat
import androidx.compose.foundation.clickable
import com.example.moneymanager.data.UserPreferences


// ------------------------------------------------------------
// MAIN SCREEN
// ------------------------------------------------------------
@Composable
fun DashboardScreen(navController: NavController) {
    // ---- USER BASED CURRENCY ----
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





    val expenseRepo =
        ExpenseRepository((context.applicationContext as MyApp).db.expenseDao())
    val budgetRepo =
        BudgetRepository((context.applicationContext as MyApp).db.budgetDao())

    val vm: DashboardViewModel =
        viewModel(factory = DashboardViewModelFactory(expenseRepo, budgetRepo))


    LaunchedEffect(Unit) { vm.loadDashboardData() }


    val analyticsViewModel: AnalyticsViewModel = viewModel()
    LaunchedEffect(Unit) {
        analyticsViewModel.loadAnalytics()
    }
    val categoryData by analyticsViewModel.categorySpendings.observeAsState(emptyMap())
    val monthlyData by analyticsViewModel.monthlyTrend.observeAsState(emptyList())


    val totalIncome by vm.totalIncome.observeAsState(0.0)
    val totalExpense by vm.totalExpense.observeAsState(0.0)

    val weeklyBudget by vm.weeklyBudget.observeAsState(null)
    val monthlyBudget by vm.monthlyBudget.observeAsState(null)

    val weeklySpent by vm.weeklySpent.observeAsState(0.0)
    val monthlySpent by vm.monthlySpent.observeAsState(0.0)

    val weeklyRemaining by vm.weeklyRemaining.observeAsState(0.0)
    val monthlyRemaining by vm.monthlyRemaining.observeAsState(0.0)

    val mostSpent by vm.mostSpentCategory.observeAsState(null)
    val transactions by vm.recentTransactions.observeAsState(emptyList())

    // 🔴 BUDGET EXCEED LOGIC
    val isWeeklyExceeded = weeklyBudget != null && weeklySpent > weeklyBudget!!
    val isMonthlyExceeded = monthlyBudget != null && monthlySpent > monthlyBudget!!





    // 🔹 SELECTED BUDGET DISPLAY LOGIC
    val displayBudgetType: String?
    val displayBudget: Double?
    val displaySpent: Double
    val displayRemaining: Double

    when {
        monthlyBudget != null -> {
            displayBudgetType = "Monthly"
            displayBudget = monthlyBudget
            displaySpent = monthlySpent
            displayRemaining = monthlyRemaining
        }

        weeklyBudget != null -> {
            displayBudgetType = "Weekly"
            displayBudget = weeklyBudget
            displaySpent = weeklySpent
            displayRemaining = weeklyRemaining
        }

        else -> {
            displayBudgetType = null
            displayBudget = null
            displaySpent = 0.0
            displayRemaining = 0.0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dashboard",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                painterResource(R.drawable.dots),
                                "",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("Profile") }, onClick = {
                                expanded = false; navController.navigate("settings")
                            })
                            DropdownMenuItem(text = { Text("Currency") }, onClick = {
                                expanded = false; navController.navigate("settings")
                            })
                            DropdownMenuItem(text = { Text("Theme") }, onClick = {
                                expanded = false; navController.navigate("settings")
                            })
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary)
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("add_entry")
                    vm.loadDashboardData()
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(painterResource(R.drawable.add), "")
            }
        },

        bottomBar = { BottomNavigationBar(navController) }

    )
    { padding ->

        Column(Modifier.padding(padding)) {
            DashboardContent(
                modifier = Modifier,
                navController = navController,
                transactions = transactions,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                budgetType = displayBudgetType,
                budgetAmount = displayBudget,
                budgetSpent = displaySpent,
                budgetRemaining = displayRemaining,
                mostSpent = mostSpent,
                categoryData = categoryData,
                monthlyData = monthlyData,
                currencySymbol = currencySymbol



            )
        }
    }
}

    // ------------------------------------------------------------
// DASHBOARD CONTENT
// ------------------------------------------------------------
    @Composable
    fun DashboardContent(
        modifier: Modifier,
        navController: NavController,
        transactions: List<ExpenseEntry>,
        totalIncome: Double,
        totalExpense: Double,
        budgetType: String?,
        budgetAmount: Double?,
        budgetSpent: Double,
        budgetRemaining: Double,
        mostSpent: Pair<String, Double>?,
        categoryData: Map<String, Double>,
        monthlyData: List<Pair<String, Double>>,
        currencySymbol: String
    ) {

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // WELCOME CARD
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(12.dp)) {
                    val quote: String = remember {
                        moneyQuotes.random()
                    }


                    Text(
                        "Welcome Back to MoneyManager👋",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        quote,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // BALANCE CARD
            Card(Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(16.dp)) {
                Row(
                    Modifier.fillMaxSize().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(R.drawable.jar),
                        "",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "Total Balance",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "$currencySymbol${totalIncome - totalExpense}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatCard("Income", totalIncome, currencySymbol, Color(0xFFE6F7EC), Color(0xFF2E7D32))
                StatCard("Expenses", totalExpense, currencySymbol, Color(0xFFFFE6E6), Color(0xFFC62828))

            }


            Spacer(Modifier.height(18.dp))
            // 🔹 EMPTY STATE: NO BUDGET SET
            if (budgetType == null || budgetAmount == null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Icon(
                        painter = painterResource(id = R.drawable.wallet),
                        contentDescription = "Set Budget",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                navController.navigate("budget")
                            }
                    )


                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "No budget set yet",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Create a budget to track your spending",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center

                    )
                    Text(
                        text = "Tap to set budget",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                }
            }



            // 🔹 SELECTED BUDGET CARD
            if (budgetType != null && budgetAmount != null) {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("$budgetType Budget", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        val safeRemaining = maxOf(0.0, budgetAmount - budgetSpent)
                        val exceededAmount = maxOf(0.0, budgetSpent - budgetAmount)

                        val progress = (budgetSpent / budgetAmount).coerceIn(0.0, 1.0)

                        val progressColor = when {
                            progress < 0.8 -> Color(0xFF2E7D32) // Green
                            progress < 1.0 -> Color(0xFFFFA000) // Orange
                            else -> Color.Red
                        }

                        Text("Total Budget: $currencySymbol$budgetAmount")
                        Text("Spent: $currencySymbol$budgetSpent")


                        Spacer(Modifier.height(8.dp))

// 🔹 PROGRESS BAR
                        LinearProgressIndicator(
                            progress = progress.toFloat(),
                            color = progressColor,
                            trackColor = Color.LightGray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                        )

// 🔹 LOGICAL TEXT
                        Spacer(Modifier.height(6.dp))

                        if (exceededAmount > 0) {
                            Text(
                                "Exceeded by $currencySymbol$exceededAmount",
                                color = Color.Red,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                "Remaining: $currencySymbol$safeRemaining",
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                    }
                }
                Spacer(Modifier.height(16.dp))
                MostSpentCategoryCard(mostSpent, currencySymbol)

                Spacer(Modifier.height(16.dp))
                RecentTransactionsSection(navController, transactions, currencySymbol)

                Spacer(modifier = Modifier.height(24.dp))


                Text(
                    text = "Spending Overview",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

// 🔹 EMPTY STATE OR CHARTS
                if (categoryData.isEmpty() && monthlyData.isEmpty()) {

                    // EMPTY OVERVIEW MESSAGE
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Icon(
                                painter = painterResource(id = R.drawable.add),
                                contentDescription = "Add Expense",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable {
                                        navController.navigate("add_entry")
                                    }
                            )


                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "No spending overview yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Tap to add your first transaction",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )


                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Add transactions to see insights and charts here",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }

                    }

                } else {

                    Text(
                        text = "Category-wise Spending",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CategoryPieChart(
                        categoryData = categoryData.mapValues { it.value.toFloat() },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Monthly Spending Analysis",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))






                    Spacer(modifier = Modifier.height(32.dp))

                    MonthlyBarChart(
                        monthlyData = monthlyData.map { pair: Pair<String, Double> ->
                            pair.first to pair.second.toFloat()
                        }
                    )


                }
            }
        }
    }


@Composable
fun RecentTransactionsSection(
    navController: NavController,
    transactions: List<ExpenseEntry>,
    currencySymbol: String
) {


    Column(Modifier.fillMaxWidth()) {

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Transactions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { navController.navigate("history") }) {
                        Text("View All")
                    }
                }

                if (transactions.isEmpty()) {
                    Text(
                        "No recent activity",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                    return
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    transactions.take(3).forEach { tx ->

                        val amountColor =
                            if (tx.type == "Expense") Color.Red else Color(0xFF2E7D32)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )

                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                // LEFT SIDE — CATEGORY + AMOUNT
                                Row(verticalAlignment = Alignment.CenterVertically) {

                                    Icon(
                                        painter = painterResource(
                                            id = getCategoryIcon(tx.category)
                                        ),
                                        contentDescription = "",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(28.dp)
                                    )

                                    Spacer(Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            tx.category,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            (if (tx.type == "Expense") "- $currencySymbol" else "+ $currencySymbol") + tx.amount
                                            ,
                                            color = amountColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }











                                fun getCategoryIcon(category: String): Int {
                                    return when (category) {
                                        "Food & Dining", "Food", "Groceries" -> R.drawable.dining
                                        "Transportation" -> R.drawable.bus
                                        "Shopping" -> R.drawable.shopping
                                        "Bills" -> R.drawable.bills
                                        "Entertainment" -> R.drawable.net
                                        "Health" -> R.drawable.health
                                        "Travel" -> R.drawable.earth
                                        "Others" -> R.drawable.others
                                        "Salary" -> R.drawable.salary
                                        "Freelance" -> R.drawable.freelance
                                        else -> R.drawable.others
                                    }
                                }

                                fun getPaymentIcon(method: String?): Int {
                                    return when (method) {
                                        "Cash" -> R.drawable.cashier
                                        "UPI" -> R.drawable.upi
                                        "Credit Card" -> R.drawable.debit
                                        "Debit Card" -> R.drawable.debit
                                        "Net Banking" -> R.drawable.bank
                                        else -> R.drawable.bank
                                    }
                                }
                                // RIGHT SIDE — PAYMENT METHOD
                                Row(verticalAlignment = Alignment.CenterVertically) {

                                    Icon(
                                        painter = painterResource(
                                            id = getPaymentIcon(tx.paymentMethod)
                                        ),
                                        contentDescription = "",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(22.dp)
                                    )

                                    Spacer(Modifier.width(6.dp))

                                    Text(
                                        tx.paymentMethod ?: "",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

@Composable
fun StatCard(
    title: String,
    value: Double,
    currencySymbol: String,
    bg: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, color = textColor)
            Text(
                text = "$currencySymbol$value",
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun MostSpentCategoryCard(mostSpent: Pair<String, Double>?, currencySymbol: String
) {

    Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("Most Spent Category", fontWeight = FontWeight.Bold)
                    if (mostSpent != null) {
                        Text(mostSpent.first)
                        Text("$currencySymbol${mostSpent.second}")

                    } else Text("No data yet")
                }
            }
        }
@Composable
        fun BottomNavigationBar(navController: NavController) {

            var selected by remember { mutableStateOf("dashboard") }

            NavigationBar {

                NavigationBarItem(
                    selected = selected == "dashboard",
                    onClick = {
                        selected = "dashboard"
                        navController.navigate("home") {
                            launchSingleTop = true
                        }

                    },
                    icon = { Icon(painterResource(R.drawable.home), contentDescription = "") },
                    label = { Text("Dashboard") }
                )

                NavigationBarItem(
                    selected = selected == "budget",
                    onClick = {
                        selected = "budget"
                        navController.navigate("budget")
                    },
                    icon = { Icon(painterResource(R.drawable.wallet), contentDescription = "") },
                    label = { Text("Budget") }
                )

                NavigationBarItem(
                    selected = selected == "analytics",
                    onClick = {
                        selected = "analytics"
                        navController.navigate("analytics")
                    },
                    icon = { Icon(painterResource(R.drawable.analytics), contentDescription = "") },
                    label = { Text("Analytics") }
                )
            }
        }

        val moneyQuotes = listOf(
            "Save a little today, smile a lot tomorrow 😊💰",
            "Your wallet deserves better decisions 😌👛",
            "Small savings = Big future 🚀💸",
            "Don’t let your money ghost you 👻💵",
            "Every rupee saved is future freedom 💰",
            "Budget now, relax later 😎📊",
            "Your future self will thank you 🙌💼",
            "Spend smart, stress less 🧠✨",
            "Track your money like a boss 👑📈",
            "Money discipline = life discipline 🎯💼"

        )






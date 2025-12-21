@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.moneymanager.ui.addentry

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
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
import androidx.navigation.NavController
import com.example.moneymanager.R
import java.text.SimpleDateFormat
import java.util.*
import com.example.moneymanager.data.ExpenseEntry
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneymanager.data.ExpenseRepository
import com.example.moneymanager.MyApp
import com.example.moneymanager.ui.addentry.AddEntryViewModel
import com.example.moneymanager.ui.addentry.AddEntryViewModelFactory
import com.example.moneymanager.data.Categories
import android.widget.Toast
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch





@Composable
fun AddEntryScreen(navController: NavController) {

    val context = LocalContext.current

    val dao = (context.applicationContext as MyApp).db.expenseDao()
    val repo = ExpenseRepository(dao)
    val viewModel: AddEntryViewModel = viewModel(factory = AddEntryViewModelFactory(repo))

    var isExpense by remember { mutableStateOf(true) }

    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    // ⭐ Selected date — WILL DEFAULT TO TODAYS DATE
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var date by remember { mutableStateOf(sdf.format(Date(selectedDateMillis))) }

    var paymentMethod by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val categories = Categories.expense


    val incomeCategories = listOf(
        "Salary", "Freelance", "Bonus", "Investment Return",
        "Rental Income", "Gift", "Business", "Others"
    )

    val paymentMethods = listOf("Cash", "UPI", "Credit Card", "Debit Card", "Net Banking")

    var categoryExpanded by remember { mutableStateOf(false) }
    var paymentExpanded by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()


    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val cal = Calendar.getInstance()
            cal.set(year, month, day)
            selectedDateMillis = cal.timeInMillis
            date = sdf.format(Date(selectedDateMillis))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    var showBudgetAlert by remember { mutableStateOf(false) }
    var budgetAlertConsumed by rememberSaveable { mutableStateOf(false) }
    var budgetExceeded by remember { mutableStateOf(false) }





    val scope = rememberCoroutineScope()



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Entry",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },

        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            // EXPENSE / INCOME Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { isExpense = true; category = "" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isExpense) Color(0xFFD50000)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isExpense) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                ) { Text("Expense") }

                Button(
                    onClick = { isExpense = false; category = "" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isExpense) Color(0xFF00C853)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (!isExpense) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                ) { Text("Income") }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // CATEGORY DROPDOWN
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (isExpense) "Expense Category" else "Income Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    val items = if (isExpense) categories else incomeCategories

                    items.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                category = it
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // DATE FIELD
            OutlinedTextField(
                value = date,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.calendar),
                            contentDescription = "Pick Date"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isExpense) {
                ExposedDropdownMenuBox(
                    expanded = paymentExpanded,
                    onExpandedChange = { paymentExpanded = !paymentExpanded }
                ) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(paymentExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = paymentExpanded,
                        onDismissRequest = { paymentExpanded = false }
                    ) {
                        paymentMethods.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    paymentMethod = it
                                    paymentExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Short Note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ADD BUTTON
            Button(
                onClick = {

                    // 1️⃣ Ensure date is valid
                    if (selectedDateMillis == 0L) {
                        selectedDateMillis = System.currentTimeMillis()
                        date = sdf.format(Date(selectedDateMillis))
                    }

                    // 2️⃣ Prepare entry object
                    val entry = ExpenseEntry(
                        amount = amount.toDouble(),
                        category = category,
                        date = selectedDateMillis,
                        paymentMethod = paymentMethod,
                        note = note,
                        type = if (isExpense) "Expense" else "Income"
                    )

                    // 3️⃣ Income → directly add (no budget check)
                    if (!isExpense) {
                        viewModel.addExpense(entry)
                        Toast.makeText(
                            context,
                            "Income added successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        navController.popBackStack()
                        return@Button
                    }

                    // 4️⃣ Expense → check budget in coroutine
                    scope.launch {

                        val db = (context.applicationContext as MyApp).db
                        val expenseDao = db.expenseDao()
                        val budgetDao = db.budgetDao()

                        // ---- Month start (based on BudgetEntry.periodStart concept) ----
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.DAY_OF_MONTH, 1)
                        cal.set(Calendar.HOUR_OF_DAY, 0)
                        cal.set(Calendar.MINUTE, 0)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        val monthStart = cal.timeInMillis

                        val monthlySpent =
                            expenseDao.getExpensesAfter(monthStart)
                                .filter { it.type == "Expense" }
                                .sumOf { it.amount }

                        val monthlyBudget =
                            budgetDao.getOverallBudget("Monthly")?.overallAmount

                        // ---------- WEEK START ----------
                        val weekCal = Calendar.getInstance()
                        weekCal.set(Calendar.DAY_OF_WEEK, weekCal.firstDayOfWeek)
                        weekCal.set(Calendar.HOUR_OF_DAY, 0)
                        weekCal.set(Calendar.MINUTE, 0)
                        weekCal.set(Calendar.SECOND, 0)
                        weekCal.set(Calendar.MILLISECOND, 0)
                        val weekStart = weekCal.timeInMillis

                        val weeklySpent =
                            expenseDao.getExpensesAfter(weekStart)
                                .filter { it.type == "Expense" }
                                .sumOf { it.amount }

                        val weeklyBudget =
                            budgetDao.getOverallBudget("Weekly")?.overallAmount

                        val newMonthlyTotal = monthlySpent + entry.amount
                        val newWeeklyTotal = weeklySpent + entry.amount

                        val isExceeded =
                            (monthlyBudget != null && newMonthlyTotal > monthlyBudget) ||
                                    (weeklyBudget != null && newWeeklyTotal > weeklyBudget)


                        // 🚨 Budget exceeded → BLOCK expense always
                        if (isExceeded) {
                            budgetExceeded = true

                            // Show alert only once
                            if (!budgetAlertConsumed) {
                                showBudgetAlert = true
                                budgetAlertConsumed = true
                            }

                            // ❌ DO NOT add expense
                            return@launch
                        }

// ✅ Budget is OK → allow expense
                        viewModel.addExpense(entry)
                        Toast.makeText(context, "Expense added successfully", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()

                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isExpense) Color(0xFFD50000) else Color(0xFF00C853),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    if (isExpense) "Add Expense" else "Add Income",
                    fontSize = 18.sp
                )
            }
            if (budgetExceeded) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Update your budget to continue adding expenses",
                    color = Color.Red,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }


            if (showBudgetAlert) {
                AlertDialog(
                    onDismissRequest = {
                        showBudgetAlert = false

                    },
                    confirmButton = {
                        Button(onClick = {
                            showBudgetAlert = false

                        }) {
                            Text("OK")
                        }
                    },
                    title = {
                        Text(
                            "Budget Exceeded ⚠️",
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    },
                    text = {
                        Text("Budget exceeded. Please review your budget.")
                    }
                )
            }
        }
    }
}




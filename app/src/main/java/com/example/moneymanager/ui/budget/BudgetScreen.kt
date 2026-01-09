@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.moneymanager.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moneymanager.MyApp
import com.example.moneymanager.R
import com.example.moneymanager.data.BudgetRepository
import kotlinx.coroutines.launch
import com.example.moneymanager.data.ExpenseRepository
import com.example.moneymanager.ui.budget.BudgetViewModel
import android.app.Application
import com.example.moneymanager.data.Categories




@Composable
fun BudgetScreen(navController: NavController) {

    // ✅ CONTEXT FIRST
    val context = LocalContext.current

    // ✅ REPOSITORIES (ONLY ONCE)
    val expenseRepo =
        ExpenseRepository((context.applicationContext as MyApp).db.expenseDao())
    val budgetRepo =
        BudgetRepository((context.applicationContext as MyApp).db.budgetDao())

    // ✅ VIEWMODEL
    val vm: BudgetViewModel = viewModel(
        factory = BudgetViewModelFactory(
            context.applicationContext as Application,
            budgetRepo,
            expenseRepo
        )
    )
    val categories = Categories.budgetDefault








    // ---------------- STATES ----------------
    val type by vm.selectedType.observeAsState("Monthly")
    val editMode by vm.editMode.observeAsState(false)
// ✅ NOW type exists, so this is valid
    LaunchedEffect(type) {
        vm.loadBudgets()
    }
    val overallMonthly by vm.overallMonthly.observeAsState("")
    val overallWeekly by vm.overallWeekly.observeAsState("")

    val monthlyCat by vm.categoryMonthly.observeAsState(emptyMap())
    val weeklyCat by vm.categoryWeekly.observeAsState(emptyMap())

    val categoryWarnings by vm.categoryWarnings.observeAsState(emptyMap())

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ---------------- INCOME CHECK ----------------
    var totalIncome by remember { mutableStateOf(0.0) }
    var showIncomeWarning by remember { mutableStateOf(false) }
    var overallText by remember { mutableStateOf("") }


    LaunchedEffect(Unit) {
        totalIncome = expenseRepo.getTotalIncome()
    }



    // ---------------- UI ----------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Set Budget",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painterResource(R.drawable.back),
                            "",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->

        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            // ---------------- EDIT BUTTON ----------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                        .clickable { vm.toggleEdit() }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = if (editMode) "Save" else "Edit",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---------------- TYPE SELECTION ----------------
            Text("Choose Budget Type", fontWeight = FontWeight.SemiBold)
            Text(
                "Currently editing: $type budget",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BudgetTypeButton("Monthly", type == "Monthly") {
                    if (editMode) vm.setBudgetType("Monthly")


                }
                BudgetTypeButton("Weekly", type == "Weekly") {
                    if (editMode) vm.setBudgetType("Weekly")


                }
            }

            Spacer(Modifier.height(20.dp))

            // ---------------- OVERALL BUDGET ----------------
            Text("Overall Budget", fontWeight = FontWeight.Bold)


            LaunchedEffect(overallMonthly, overallWeekly, type) {
                overallText = if (type == "Monthly") overallMonthly else overallWeekly
            }

            OutlinedTextField(
                value = overallText,
                onValueChange = { overallText = it },
                enabled = editMode,
                label = { Text("Enter $type Budget (₹)") },
                modifier = Modifier.fillMaxWidth()
            )

            if (editMode) {
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        val budget = overallText.toDoubleOrNull() ?: return@Button

                        if (budget > totalIncome) {
                            showIncomeWarning = true
                        } else {
                            vm.saveOverall(type, budget)
                            scope.launch {
                                snackbar.showSnackbar("$type budget saved")
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save")
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---------------- CATEGORY LIMITS ----------------
            Text("Category Limits", fontWeight = FontWeight.Bold)

            val selectedMap =
                if (type == "Monthly") monthlyCat else weeklyCat

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(categories) { category ->

                    var amount by remember(type) { mutableStateOf("") }
                    var isEditing by remember(type) { mutableStateOf(false) }


                    LaunchedEffect(selectedMap, type) {
                        amount = selectedMap[category] ?: ""
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(category, fontWeight = FontWeight.SemiBold)
                                TextButton(onClick = {
                                    if (isEditing) {
                                        amount.toDoubleOrNull()?.let {
                                            vm.saveCategory(type, category, it)
                                        }
                                    }
                                    isEditing = !isEditing
                                }) {
                                    Text(if (isEditing) "Save" else "Edit")
                                }
                            }

                            OutlinedTextField(
                                value = amount,
                                onValueChange = { amount = it },
                                enabled = isEditing,
                                label = { Text("$type Limit (₹)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            categoryWarnings[category]?.let {
                                Text(it, color = Color.Red, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }



    // ---------------- BIG WARNING DIALOG ----------------
    if (showIncomeWarning) {
        AlertDialog(
            onDismissRequest = { showIncomeWarning = false },
            confirmButton = {
                Button(onClick = { showIncomeWarning = false }) {
                    Text("OK")
                }
            },
            title = {
                Text(
                    "Budget Warning ⚠️",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Red
                )
            },
            text = {
                Text(
                    "Your budget exceeds your total income.\n\n" +
                            "Income: ₹$totalIncome\n" +
                            "Budget: ₹$overallText\n\n" +
                            "Please reduce your budget.",
                    fontSize = 16.sp
                )
            }
        )
    }
}

@Composable
fun RowScope.BudgetTypeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(40.dp)
            .clickable { onClick() }
            .background(
                if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else Color.DarkGray
        )
    }
}

// ---------------- ICON MAPPING (UNCHANGED) ----------------
fun getCategoryIcon(category: String): Int = when (category) {
    "Food and Dining" -> R.drawable.dining
    "Shopping" -> R.drawable.shopping
    "Transportation" -> R.drawable.bus
    "Bills" -> R.drawable.bills
    "Entertainment" -> R.drawable.net
    else -> R.drawable.others
}


@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.moneymanager.ui.history

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moneymanager.MyApp
import com.example.moneymanager.data.ExpenseEntry
import com.example.moneymanager.data.ExpenseRepository
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.example.moneymanager.R


@Composable
fun HistoryScreen(navController: NavController) {

    val context = LocalContext.current
    val repo = ExpenseRepository((context.applicationContext as MyApp).db.expenseDao())

    // ⭐ Load all database transactions
    var allTransactions by remember { mutableStateOf(listOf<ExpenseEntry>()) }

    LaunchedEffect(Unit) {
        allTransactions = repo.getExpenses()
    }

    // ---------- FILTER STATES ----------
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All Categories") }
    var selectedDate by remember { mutableStateOf("All Dates") }
    var expanded by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            selectedDate = "%02d/%02d/%04d".format(day, month + 1, year)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )



    val categories = listOf(
        "All Categories", "Food & Dining", "Transportation", "Shopping", "Bills",
        "Entertainment", "Health", "Travel", "Salary", "Freelance", "Others"
    )

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val toolbarColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction History", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = toolbarColor)
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .background(backgroundColor)
        ) {

            // SEARCH FIELD
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search", color = textColor) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // DATE FILTER
            OutlinedTextField(
                value = selectedDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("Filter by Date", color = textColor) },
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pick Date")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // CATEGORY DROPDOWN
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {

                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Filter by Category", color = textColor) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                selectedDate = "All Dates"
                                searchQuery = ""
                                expanded = false
                            }

                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ⭐ Convert DB date (Long → dd/MM/yyyy)
            fun formatDate(millis: Long): String {
                return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
            }

            // ⭐ FILTERING LOGIC
            // --- inside HistoryScreen.kt, replace the old filtered = ... block with this ---
            val filtered = allTransactions.filter { txn ->

                // Convert DB millis → "dd/MM/yyyy"
                val txnDateString = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(Date(txn.date))

                val matchesCategory =
                    selectedCategory == "All Categories" || txn.category == selectedCategory

                val matchesDate =
                    selectedDate == "All Dates" || txnDateString == selectedDate

                val matchesSearch =
                    searchQuery.isEmpty() || txn.category.contains(searchQuery, ignoreCase = true)

                matchesCategory && matchesDate && matchesSearch
            }





            // ⭐ LIST DISPLAY
            if (filtered.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions found.", color = textColor.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered) { txn ->
                        HistoryItem_DB(txn)
                    }
                }
            }
        }
    }
}


// ------------------------------------------------------------
// ⭐ Show Actual ExpenseEntry Data From ROOM
// ------------------------------------------------------------
@Composable
fun HistoryItem_DB(txn: ExpenseEntry) {

    val amountColor = if (txn.type == "Expense") Color.Red else Color(0xFF2E7D32)
    val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(txn.date))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // LEFT SIDE (Category + Date + Note)
            Row(verticalAlignment = Alignment.CenterVertically) {

                Icon(
                    painterResource(id = getCategoryIcon(txn.category)),
                    contentDescription = "",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(30.dp)
                )

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(txn.category, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(formattedDate, fontSize = 12.sp, color = Color.Gray)

                    if (!txn.note.isNullOrEmpty())
                        Text(txn.note ?: "", fontSize = 12.sp, color = Color.DarkGray)
                }
            }

            // RIGHT SIDE (Amount + Payment method)
            Column(horizontalAlignment = Alignment.End) {

                Text(
                    (if (txn.type == "Expense") "- ₹" else "+ ₹") + txn.amount,
                    color = amountColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Icon(
                        painterResource(id = getPaymentIcon(txn.paymentMethod)),
                        contentDescription = "",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        txn.paymentMethod ?: "",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

fun getCategoryIcon(category: String): Int {
    return when (category) {
        "Food & Dining" -> com.example.moneymanager.R.drawable.dining
        "Transportation" -> com.example.moneymanager.R.drawable.bus
        "Shopping" -> com.example.moneymanager.R.drawable.shopping
        "Bills" -> com.example.moneymanager.R.drawable.dollars
        "Entertainment" -> com.example.moneymanager.R.drawable.net
        "Health" -> com.example.moneymanager.R.drawable.health
        "Travel" -> com.example.moneymanager.R.drawable.earth
        "Others" -> com.example.moneymanager.R.drawable.others
        "Salary" -> com.example.moneymanager.R.drawable.salary
        "Freelance" -> com.example.moneymanager.R.drawable.freelance
        else -> com.example.moneymanager.R.drawable.others
    }
}

fun getPaymentIcon(method: String?): Int {
    return when (method) {
        "Cash" -> com.example.moneymanager.R.drawable.cash
        "UPI" -> com.example.moneymanager.R.drawable.upi
        "Credit Card" -> com.example.moneymanager.R.drawable.debit
        "Debit Card" -> com.example.moneymanager.R.drawable.debit
        "Net Banking" -> com.example.moneymanager.R.drawable.bank
        else -> R.drawable.bank
    }
}

fun formatDate(millis: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = millis
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH) + 1
    val year = calendar.get(Calendar.YEAR)
    return "$day/$month/$year"
}




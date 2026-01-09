package com.example.moneymanager.ui.common

import com.example.moneymanager.R

fun getCategoryIcon(category: String): Int = when (category) {
    "Food & Dining", "Food", "Groceries" -> R.drawable.dining
    "Transportation" -> R.drawable.bus
    "Shopping" -> R.drawable.shopping
    "Bills" -> R.drawable.dollars
    "Entertainment" -> R.drawable.net
    "Health" -> R.drawable.health
    "Travel" -> R.drawable.earth
    "Salary" -> R.drawable.salary
    "Freelance" -> R.drawable.freelance
    else -> R.drawable.others
}

fun getPaymentIcon(method: String?): Int = when (method) {
    "Cash" -> R.drawable.cashier
    "UPI" -> R.drawable.upi
    "Credit Card", "Debit Card" -> R.drawable.debit
    "Net Banking" -> R.drawable.bank
    else -> R.drawable.bank
}

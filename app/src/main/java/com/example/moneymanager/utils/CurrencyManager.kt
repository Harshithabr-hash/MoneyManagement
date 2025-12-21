package com.example.moneymanager.utils

import android.content.Context

object CurrencyManager {

    private const val PREF_NAME = "app_settings"
    private const val KEY_CURRENCY = "selected_currency"

    fun saveCurrency(context: Context, currency: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CURRENCY, currency).apply()
    }

    fun getCurrency(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CURRENCY, "₹") ?: "₹"
    }
}

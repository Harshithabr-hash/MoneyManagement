package com.example.moneymanager.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.map






// DataStore instance
private const val USER_PREFS_NAME = "user_preferences"

private val Context.userDataStore by preferencesDataStore(name = "user_prefs")
private val Context.appDataStore by preferencesDataStore(name = "app_prefs")



class UserPreferences(private val context: Context) {

    // ---------------- BUDGET FLAGS ----------------
    private val BUDGET_CHANGED = booleanPreferencesKey("budget_changed")
    private val BUDGET_TYPE_KEY = stringPreferencesKey("budget_type")

    val budgetChanged: Flow<Boolean> =
        context.appDataStore.data.map { it[BUDGET_CHANGED] ?: false }

    val budgetType: Flow<String> =
        context.appDataStore.data.map { it[BUDGET_TYPE_KEY] ?: "Monthly" }

    suspend fun setBudgetChanged(value: Boolean) {
        context.appDataStore.edit { it[BUDGET_CHANGED] = value }
    }

    suspend fun saveBudgetType(type: String) {
        context.appDataStore.edit { it[BUDGET_TYPE_KEY] = type }
    }

    // ---------------- USER PROFILE ----------------
    companion object {
        val NAME = stringPreferencesKey("name")
        val EMAIL = stringPreferencesKey("email")
        val PHONE = stringPreferencesKey("phone")
        val CURRENCY = stringPreferencesKey("currency")
        val PROFILE_COMPLETED = booleanPreferencesKey("profile_completed")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    val userName = context.userDataStore.data.map { it[NAME] ?: "" }
    val userEmail = context.userDataStore.data.map { it[EMAIL] ?: "" }
    val userPhone = context.userDataStore.data.map { it[PHONE] ?: "" }

    val userCurrency = context.userDataStore.data.map {
        it[CURRENCY] ?: "INR - ₹ India"
    }

    val isProfileCompleted =
        context.userDataStore.data.map { it[PROFILE_COMPLETED] ?: false }

    val isDarkTheme =
        context.userDataStore.data.map { it[DARK_THEME] ?: false }

    suspend fun saveUserDetails(
        name: String,
        email: String,
        phone: String,
        currency: String
    ) {
        context.userDataStore.edit {
            it[NAME] = name
            it[EMAIL] = email
            it[PHONE] = phone
            it[CURRENCY] = currency
            it[PROFILE_COMPLETED] = true
        }
    }

    suspend fun saveCurrency(currency: String) {
        context.userDataStore.edit { it[CURRENCY] = currency }
    }

    suspend fun saveDarkTheme(enabled: Boolean) {
        context.userDataStore.edit { it[DARK_THEME] = enabled }
    }
}





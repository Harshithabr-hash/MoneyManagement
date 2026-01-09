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
    private fun stringKey(userId: String, name: String) =
        stringPreferencesKey("${userId}_$name")

    private fun booleanKey(userId: String, name: String) =
        booleanPreferencesKey("${userId}_$name")


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
    // ---------------- USER PROFILE ----------------
    private val DARK_THEME = booleanPreferencesKey("dark_theme")


    private fun profileKey(userId: String, name: String) =
        stringPreferencesKey("${userId}_$name")

    private fun profileBoolKey(userId: String, name: String) =
        booleanPreferencesKey("${userId}_$name")

    fun userName(userId: String): Flow<String> =
        context.userDataStore.data.map {
            it[profileKey(userId, "name")] ?: ""
        }

    fun userEmail(userId: String): Flow<String> =
        context.userDataStore.data.map {
            it[profileKey(userId, "email")] ?: ""
        }

    fun userPhone(userId: String): Flow<String> =
        context.userDataStore.data.map {
            it[profileKey(userId, "phone")] ?: ""
        }

    fun userCurrency(userId: String): Flow<String> =
        context.userDataStore.data.map {
            it[profileKey(userId, "currency")] ?: "INR - ₹ India"
        }

    fun isProfileCompleted(userId: String): Flow<Boolean> =
        context.userDataStore.data.map {
            it[profileBoolKey(userId, "profile_completed")] ?: false
        }
    val isDarkTheme =
        context.userDataStore.data.map { it[DARK_THEME] ?: false }

    suspend fun saveUserDetails(
        userId: String,
        name: String,
        email: String,
        phone: String,
        currency: String
    ) {
        context.userDataStore.edit {
            it[profileKey(userId, "name")] = name
            it[profileKey(userId, "email")] = email
            it[profileKey(userId, "phone")] = phone
            it[profileKey(userId, "currency")] = currency
            it[profileBoolKey(userId, "profile_completed")] = true
        }
    }

    suspend fun saveCurrency(userId: String, currency: String) {
        context.userDataStore.edit {
            it[profileKey(userId, "currency")] = currency
        }
    }



    suspend fun saveDarkTheme(enabled: Boolean) {
        context.userDataStore.edit { it[DARK_THEME] = enabled }
    }
}





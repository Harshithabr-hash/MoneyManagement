package com.example.moneymanager.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanager.data.UserPreferences
import kotlinx.coroutines.launch

class ThemeViewModel : ViewModel() {

    // true = dark, false = light
    var isDarkTheme = mutableStateOf(false)
        private set

    // called by UI to change theme
    fun toggleTheme(prefs: UserPreferences) {
        val newValue = !isDarkTheme.value
        isDarkTheme.value = newValue

        // save to DataStore
        viewModelScope.launch {
            prefs.saveDarkTheme(newValue)
        }
    }

    // load saved theme value when app starts
    fun loadTheme(isDark: Boolean) {
        isDarkTheme.value = isDark
    }
}

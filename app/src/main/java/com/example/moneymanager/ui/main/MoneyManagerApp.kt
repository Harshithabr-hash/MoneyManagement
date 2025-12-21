package com.example.moneymanager.ui.main

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.navigation.compose.*
import com.example.moneymanager.ui.home.DashboardScreen
import com.example.moneymanager.ui.settings.SettingsScreen
import com.example.moneymanager.viewmodel.ThemeViewModel
import com.example.moneymanager.ui.theme.MoneyManagerTheme

@Composable
fun MoneyManagerApp(themeViewModel: ThemeViewModel) {

    // Listen to theme changes
    val isDarkTheme = themeViewModel.isDarkTheme.value

    // Apply your real custom theme
    MoneyManagerTheme(darkTheme = isDarkTheme) {

        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") { DashboardScreen(navController) }
            composable("settings") { SettingsScreen(navController, themeViewModel) }
        }
    }
}

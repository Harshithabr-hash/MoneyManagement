package com.example.moneymanager.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.moneymanager.ui.login.LoginScreen
import com.example.moneymanager.ui.addentry.AddEntryScreen
import com.example.moneymanager.ui.analytics.AnalyticsScreen
import com.example.moneymanager.ui.budget.BudgetScreen
import com.example.moneymanager.ui.history.HistoryScreen
import com.example.moneymanager.ui.home.DashboardScreen
import com.example.moneymanager.ui.settings.SettingsScreen
import com.example.moneymanager.ui.splash.SplashScreen
import com.example.moneymanager.ui.splash.SplashViewModel
import com.example.moneymanager.viewmodel.ThemeViewModel
import com.example.moneymanager.ui.register.RegisterScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    themeViewModel: ThemeViewModel
) {

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        composable("splash") {
            val splashVM: SplashViewModel = viewModel()
            SplashScreen(
                navController = navController,
                viewModel = splashVM
            )
        }

        composable("login") {
            LoginScreen(navController)
        }

        // ✅ REGISTER SCREEN IS SEPARATE ROUTE
        composable("register") {
            RegisterScreen(navController)
        }

        composable("home") {
            DashboardScreen(navController)
        }

        composable("add_entry") {
            AddEntryScreen(navController)
        }

        composable("budget") {
            BudgetScreen(navController)
        }

        composable("analytics") {
            AnalyticsScreen(navController)
        }

        composable("history") {
            HistoryScreen(navController)
        }

        composable("settings") {
            SettingsScreen(
                navController = navController,
                themeViewModel = themeViewModel
            )
        }
    }
}

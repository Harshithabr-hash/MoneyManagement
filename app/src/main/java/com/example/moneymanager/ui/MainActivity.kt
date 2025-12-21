package com.example.moneymanager.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.moneymanager.navigation.NavGraph
import com.example.moneymanager.ui.theme.MoneyManagerTheme
import com.example.moneymanager.viewmodel.ThemeViewModel
import com.example.moneymanager.data.UserPreferences
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth





class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val prefs = UserPreferences(this)
            LaunchedEffect(Unit) {
                prefs.isDarkTheme.collect { saved ->
                    themeViewModel.loadTheme(saved)
                }
            }

            val themeViewModel: ThemeViewModel = viewModel()

            LaunchedEffect(Unit) {
                prefs.isDarkTheme.collect { savedValue ->
                    themeViewModel.loadTheme(savedValue)
                }
            }


            val navController = rememberNavController()
            val navBackStackEntry = navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry.value?.destination?.route

            // ❗ Disable dark theme for Splash + Login only
            val shouldUseDarkTheme =
                when (currentRoute) {
                    "splash", "login" -> false     // always light
                    else -> themeViewModel.isDarkTheme.value
                }

            MoneyManagerTheme(darkTheme = shouldUseDarkTheme) {
                Surface {
                    NavGraph(
                        navController = navController,
                        themeViewModel = themeViewModel
                    )
                }
            }
        }
    }
}

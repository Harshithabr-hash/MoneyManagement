package com.example.moneymanager.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.moneymanager.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    navController: NavController,
    showBack: Boolean = false,     // show back button only when needed
    showMenu: Boolean = false      // show 3-dot menu only when needed
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        },

        navigationIcon = {
            if (showBack) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
        },

        actions = {
            if (showMenu) {
                IconButton(onClick = { /* later menu actions */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.dots),
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                }
            }
        },

        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF3279FF),   // your blue color
            titleContentColor = Color.White,
            actionIconContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}

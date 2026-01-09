@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.moneymanager.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneymanager.R
import com.example.moneymanager.viewmodel.SettingsViewModel
import com.example.moneymanager.viewmodel.ThemeViewModel
import com.example.moneymanager.data.UserPreferences
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast




@Composable
fun SettingsScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel,
    settingsViewModel: SettingsViewModel = viewModel()
) {

    val name by settingsViewModel.name.collectAsState()
    val email by settingsViewModel.email.collectAsState()
    val phone by settingsViewModel.phone.collectAsState()
    val currency by settingsViewModel.currency.collectAsState()
    val isProfileCompleted by settingsViewModel.isProfileCompleted.collectAsState()
    val isEditMode by settingsViewModel.isEditMode.collectAsState()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }

    var currencyMenuExpanded by remember { mutableStateOf(false) }
    var showSavedMessage by remember { mutableStateOf(false) }


    val currencyList = listOf(
        "INR - ₹ India",
        "USD - $ United States",
        "EUR - € Europe",
        "GBP - £ United Kingdom",
        "JPY - ¥ Japan",
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painterResource(R.drawable.back),
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary)
            )
        }

    )
    { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ---------------- EDIT BUTTON (Below AppBar) ----------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 1.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (!isEditMode) {
                    TextButton(onClick = { settingsViewModel.enableEditMode() }) {
                        Text("Edit", fontSize = 16.sp)
                    }
                }
            }

            // ---------------- PROFILE ICON ----------------
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.size(120.dp)
            )

            Spacer(Modifier.height(12.dp))

            // ---------------- HEADER INFO ----------------
            if (isProfileCompleted && !isEditMode) {
                Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(email, fontSize = 14.sp, color = Color.Gray)
                Text(phone, fontSize = 14.sp, color = Color.Gray)
                Spacer(Modifier.height(20.dp))
            }

            // ---------------- TEXT FIELDS ----------------
            OutlinedTextField(
                value = name,
                onValueChange = { settingsViewModel.updateName(it) },
                enabled = isEditMode || !isProfileCompleted,
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { settingsViewModel.updateEmail(it) },
                enabled = isEditMode || !isProfileCompleted,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { settingsViewModel.updatePhone(it) },
                enabled = isEditMode || !isProfileCompleted,
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            // ---------------- CURRENCY DROPDOWN ----------------
            Text("Select Currency", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))

            ExposedDropdownMenuBox(
                expanded = currencyMenuExpanded,
                onExpandedChange = { currencyMenuExpanded = !currencyMenuExpanded }
            ) {
                OutlinedTextField(
                    value = currency,
                    onValueChange = {},
                    readOnly = true,
                    enabled = isEditMode || !isProfileCompleted,
                    label = { Text("Currency") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(currencyMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = currencyMenuExpanded,
                    onDismissRequest = { currencyMenuExpanded = false }
                ) {
                    currencyList.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                settingsViewModel.updateCurrency(it) // ✅ save full value
                                currencyMenuExpanded = false
                            }
                        )

                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ---------------- DARK THEME TOGGLE ----------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Dark Theme", fontSize = 18.sp)
                Switch(
                    checked = themeViewModel.isDarkTheme.value,
                    onCheckedChange = { themeViewModel.toggleTheme(prefs) }
                )
            }

            Spacer(Modifier.height(20.dp))

            // ---------------- SAVE + LOGOUT SIDE BY SIDE ----------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // SAVE (only when editing or first time setup)
                if (isEditMode || !isProfileCompleted) {
                    Button(
                        onClick = {
                            scope.launch {
                                settingsViewModel.saveUserProfile(
                                    name = name,
                                    email = email,
                                    phone = phone,
                                    currency = currency
                                )
                                settingsViewModel.disableEditMode()
                                showSavedMessage = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save", fontSize = 15.sp)
                    }



                    Spacer(Modifier.width(12.dp))
                }

                // LOGOUT BUTTON (Small + clean)
                OutlinedButton(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()   // 🔐 CLEAR SESSION

                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text("Logout", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
    if (showSavedMessage) {
        LaunchedEffect(Unit) {
            Toast
                .makeText(context, "Profile updated", Toast.LENGTH_SHORT)
                .show()
            showSavedMessage = false
        }
    }

}

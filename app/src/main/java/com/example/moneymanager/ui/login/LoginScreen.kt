package com.example.moneymanager.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moneymanager.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneymanager.viewmodel.AuthViewModel
import com.example.moneymanager.viewmodel.LoginState


@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    val loginState by authViewModel.loginState.collectAsState()
    var loginError by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Welcome Back 👋",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1B)
                )
            )

            Text(
                text = "Login to your Money Manager account",
                color = Color(0xFF777777),
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            // 📧 Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // 🔒 Password
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    loginError = "" // ✅ clear error while typing
                },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = loginError.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )

            // ✅ ERROR BELOW PASSWORD (FIXED)
            if (loginError.isNotEmpty()) {
                Text(
                    text = loginError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()   // 🔑 prevents vertical text
                        .padding(start = 4.dp, bottom = 12.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 🔘 Login Button
            Button(
                onClick = {
                    authViewModel.login(email, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Login", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "or",
                style = TextStyle(color = Color.Gray, fontSize = 18.sp),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (loginState) {

                is LoginState.Loading -> {
                    CircularProgressIndicator()
                }

                is LoginState.Success -> {
                    LaunchedEffect(Unit) {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }

                is LoginState.Error -> {
                    loginError = "Invalid email or password. Please try again."
                }

                else -> {}
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account?",
                    color = Color(0xFF555555),
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.width(6.dp))

                TextButton(onClick = { navController.navigate("register") }) {
                    Text(
                        text = "Create an Account",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
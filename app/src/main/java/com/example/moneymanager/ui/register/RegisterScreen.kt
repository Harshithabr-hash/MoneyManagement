@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.moneymanager.ui.register

import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moneymanager.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun RegisterScreen(navController: NavController) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPassError by remember { mutableStateOf("") }

    val background = MaterialTheme.colorScheme.surface
    val auth = FirebaseAuth.getInstance()





    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = background
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            // TITLE
            Text(
                "Create Your Account",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // SUBTITLE
            Text(
                "Please fill in the details below to continue",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ------- INPUT CARD -------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {

                Column(modifier = Modifier.padding(16.dp)) {

                    // FULL NAME
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // EMAIL
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = ""
                        },
                        isError = emailError.isNotEmpty(),
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    if (emailError.isNotEmpty()) {
                        Text(emailError, color = Color.Red, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // PHONE
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            phoneError = ""
                        },
                        isError = phoneError.isNotEmpty(),
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    if (phoneError.isNotEmpty()) {
                        Text(phoneError, color = Color.Red, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // PASSWORD
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = ""
                        },
                        isError = passwordError.isNotEmpty(),
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    if (passwordError.isNotEmpty()) {
                        Text(passwordError, color = Color.Red, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // CONFIRM PASSWORD
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            confirmPassError = ""
                        },
                        isError = confirmPassError.isNotEmpty(),
                        label = { Text("Confirm Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    if (confirmPassError.isNotEmpty()) {
                        Text(confirmPassError, color = Color.Red, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // REGISTER BUTTON
                    Button(
                        onClick = {

                            var valid = true

                            if (!email.contains("@") || !email.contains(".")) {
                                emailError = "Enter a valid email address"
                                valid = false
                            }

                            if (phone.length < 10) {
                                phoneError = "Enter a valid 10-digit phone number"
                                valid = false
                            }

                            if (password.length < 6) {
                                passwordError = "Password must be at least 6 characters"
                                valid = false
                            }

                            if (confirmPassword != password) {
                                confirmPassError = "Passwords do not match"
                                valid = false
                            }

                            if (valid) {
                                auth.createUserWithEmailAndPassword(
                                    email.trim(),
                                    password.trim()
                                ).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {

                                        // ✅ USER CREATED IN FIREBASE
                                        navController.navigate("home") {
                                            popUpTo("register") { inclusive = true }
                                        }
                                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnCompleteListener

                                        val userProfile = hashMapOf(
                                            "name" to name,
                                            "email" to email,
                                            "phone" to phone,
                                            "currency" to "₹"
                                        )

                                        FirebaseFirestore.getInstance()
                                            .collection("users")
                                            .document(uid)
                                            .set(userProfile)



                                    } else {
                                        // ❌ SHOW FIREBASE ERROR
                                        emailError = task.exception?.localizedMessage
                                            ?: "Registration failed"
                                    }
                                }
                            }


                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Register", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

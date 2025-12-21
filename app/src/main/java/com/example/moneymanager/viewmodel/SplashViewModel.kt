package com.example.moneymanager.ui.splash

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val _nextRoute = mutableStateOf<String?>(null)
    val nextRoute: State<String?> get() = _nextRoute

    init {
        viewModelScope.launch {
            delay(2000) // splash delay

            val currentUser = FirebaseAuth.getInstance().currentUser

            _nextRoute.value = if (currentUser != null) {
                "home"
            } else {
                "login"
            }
        }
    }
}

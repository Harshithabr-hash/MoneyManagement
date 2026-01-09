package com.example.moneymanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanager.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val uid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid


    private val userPrefs = UserPreferences(application)

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone

    private val _currency = MutableStateFlow("INR - ₹ India")
    val currency: StateFlow<String> = _currency

    private val _isProfileCompleted = MutableStateFlow(false)
    val isProfileCompleted: StateFlow<Boolean> = _isProfileCompleted

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode
    fun loadUserProfileFromFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("name") ?: ""
                    val email = doc.getString("email") ?: ""
                    val phone = doc.getString("phone") ?: ""



                    viewModelScope.launch {
                        val currency =
                            doc.getString("currency")
                                ?: userPrefs.userCurrency(uid).first()
                        userPrefs.saveUserDetails(uid, name, email, phone, currency)

                        _name.value = name
                        _email.value = email
                        _phone.value = phone
                        _currency.value = currency
                        _isProfileCompleted.value = true
                    }
                }
            }
    }


    init {
        loadUserProfileFromFirebase()
        loadUserData()
    }



    private fun loadUserData() {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
                ?: return@launch

            _name.value = userPrefs.userName(userId).first()
            _email.value = userPrefs.userEmail(userId).first()
            _phone.value = userPrefs.userPhone(userId).first()
            _currency.value = userPrefs.userCurrency(userId).first()
            _isProfileCompleted.value =
                userPrefs.isProfileCompleted(userId).first()
        }
    }





    // Update individual fields (fixes your Unresolved Reference errors)
    fun updateName(newName: String) {
        _name.value = newName
    }

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    fun updatePhone(newPhone: String) {
        _phone.value = newPhone
    }

    fun enableEditMode() {
        _isEditMode.value = true
    }

    fun disableEditMode() {
        _isEditMode.value = false
    }

    fun updateCurrency(newCurrency: String) {
        viewModelScope.launch {
            val userId = uid ?: return@launch
            userPrefs.saveCurrency(userId, newCurrency)

            _currency.value = newCurrency
        }
    }

    fun saveUserProfile(name: String, email: String, phone: String, currency: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val data = mapOf(
            "name" to name,
            "email" to email,
            "phone" to phone,
            "currency" to currency
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(data)
            .addOnSuccessListener {
                viewModelScope.launch {
                    // update local cache
                    userPrefs.saveUserDetails(uid, name, email, phone, currency)


                    _name.value = name
                    _email.value = email
                    _phone.value = phone
                    _currency.value = currency
                    _isProfileCompleted.value = true

                    disableEditMode()
                }
            }
    }

}


package com.tvbc.tvbcapps.model

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState.asStateFlow()

    suspend fun loginUser(email: String, password: String) {
        try {
            _loginState.value = AuthState.Loading
            auth.signInWithEmailAndPassword(email, password).await()
            _loginState.value = AuthState.Success
        } catch (e: Exception) {
            _loginState.value = AuthState.Error(e.message ?: "Authentication failed")
        }
    }

    suspend fun registerUser(email: String, password: String) {
        try {
            _registerState.value = AuthState.Loading
            auth.createUserWithEmailAndPassword(email, password).await()
            _registerState.value = AuthState.Success
        } catch (e: Exception) {
            _registerState.value = AuthState.Error(e.message ?: "Registration failed")
        }
    }

    fun resetStates() {
        _loginState.value = AuthState.Idle
        _registerState.value = AuthState.Idle
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun logoutUser() {
        auth.signOut()
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
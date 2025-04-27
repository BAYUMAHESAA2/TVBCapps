package com.tvbc.tvbcapps.model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState.asStateFlow()

    private val _userProfile = MutableStateFlow<UserModel?>(null)
    val userProfile: StateFlow<UserModel?> = _userProfile.asStateFlow()

    private val _isProfileUpdating = MutableStateFlow(false)
    val isProfileUpdating: StateFlow<Boolean> = _isProfileUpdating.asStateFlow()

    private val _userRole = MutableStateFlow("user")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _isUserProfileLoading = MutableStateFlow(true)
    val isUserProfileLoading: StateFlow<Boolean> = _isUserProfileLoading.asStateFlow()

    init {
        auth.currentUser?.let { fetchUserProfile(it.uid) }
    }

    suspend fun registerUser(fullName: String, email: String, password: String) {
        try {
            _registerState.value = AuthState.Loading
            val result = auth.createUserWithEmailAndPassword(email, password).await()

            result.user?.let { user ->
                try {
                    val newUser = UserModel(
                        uid = user.uid,
                        fullName = fullName,
                        email = email,
                        role = "user" // Pastikan semua user baru memiliki role "user"
                    )
                    usersCollection.document(user.uid).set(newUser).await()
                } catch (firestoreError: Exception) {
                    Log.e("AuthViewModel", "Firestore profile creation failed: ${firestoreError.message}")
                }
            }
            _registerState.value = AuthState.Success
        } catch (e: Exception) {
            _registerState.value = AuthState.Error(e.message ?: "Registration failed")
        }
    }

    suspend fun loginUser(email: String, password: String) {
        try {
            _loginState.value = AuthState.Loading
            val result = auth.signInWithEmailAndPassword(email, password).await()

            result.user?.let { user ->
                // Ambil informasi user dari Firestore, termasuk role-nya
                val userDoc = usersCollection.document(user.uid).get().await()

                if (userDoc.exists()) {
                    val userProfile = userDoc.toObject(UserModel::class.java)
                    _userProfile.value = userProfile
                    _userRole.value = userProfile?.role ?: "user"
                } else {
                    // Jika dokumen tidak ada di Firestore, buat dengan role default "user"
                    val defaultUser = UserModel(
                        uid = user.uid,
                        email = email,
                        role = "user"
                    )
                    usersCollection.document(user.uid).set(defaultUser).await()
                    _userProfile.value = defaultUser
                    _userRole.value = "user"
                }
            }

            _loginState.value = AuthState.Success
        } catch (e: Exception) {
            _loginState.value = AuthState.Error(e.message ?: "Authentication failed")
        }
    }

    private fun fetchUserProfile(userId: String) {
        _isUserProfileLoading.value = true
        usersCollection.document(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                _isUserProfileLoading.value = false
                return@addSnapshotListener
            }
            snapshot?.let {
                val userProfile = it.toObject(UserModel::class.java)
                _userProfile.value = userProfile
                _userRole.value = userProfile?.role ?: "user"
                _isUserProfileLoading.value = false
            }
        }
    }

    suspend fun updateUserProfile(fullName: String, nim: String, jurusan: String, angkatan: String) {
        val currentUser = auth.currentUser ?: return
        _isProfileUpdating.value = true

        try {
            val updates = hashMapOf<String, Any>(
                "fullName" to fullName,
                "nim" to nim,
                "jurusan" to jurusan,
                "angkatan" to angkatan
            )
            usersCollection.document(currentUser.uid).update(updates).await()
            _isProfileUpdating.value = false
        } catch (e: Exception) {
            _isProfileUpdating.value = false
        }
    }

    fun resetStates() {
        _loginState.value = AuthState.Idle
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
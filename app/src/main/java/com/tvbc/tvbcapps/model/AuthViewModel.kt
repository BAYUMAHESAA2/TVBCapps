package com.tvbc.tvbcapps.model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.tvbc.tvbcapps.util.FileUtilProfil
import id.zelory.compressor.Compressor
import kotlinx.coroutines.launch

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
                        role = "user" // Default role for new users
                    )
                    usersCollection.document(user.uid).set(newUser).await()
                } catch (firestoreError: Exception) {
                    Log.e(
                        "AuthViewModel",
                        "Firestore profile creation failed: ${firestoreError.message}"
                    )
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
                // Fetch user info from Firestore
                val userDoc = usersCollection.document(user.uid).get().await()

                if (userDoc.exists()) {
                    val userProfile = userDoc.toObject(UserModel::class.java)
                    _userProfile.value = userProfile
                    _userRole.value = userProfile?.role ?: "user"
                } else {
                    // Create a default user profile if it doesn't exist
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

    suspend fun updateUserProfile(
        fullName: String,
        nim: String,
        jurusan: String,
        angkatan: String
    ) {
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

    fun uploadProfileImage(context: Context, imageUri: Uri, callback: (Boolean, String) -> Unit) {
        val file = FileUtilProfil.getFileFromUriProfil(context, imageUri)
        if (file == null) {
            callback(false, "Tidak dapat mengakses file")
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(false, "User tidak terautentikasi")
            return
        }

        viewModelScope.launch {
            val compressedFile = try {
                Compressor.compress(context, file)
            } catch (e: Exception) {
                callback(false, "Gagal mengompresi gambar: ${e.message}")
                return@launch
            }

            val timestamp = System.currentTimeMillis()

            MediaManager.get().upload(compressedFile.absolutePath)
                .unsigned("fotoProfil")
                .option("folder", "folder/fotoProfil")
                .option("public_id", "profile_${userId}_$timestamp")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val imageUrl = resultData["url"] as? String ?: ""
                        updateProfileImageInFirestore(imageUrl) { success, message ->
                            callback(success, message)
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        callback(false, "Upload error: ${error.description}")
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                })
                .dispatch()
        }
    }

    private fun updateProfileImageInFirestore(
        imageUrl: String,
        callback: (Boolean, String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(false, "User tidak terautentikasi")
            return
        }

        firestore.collection("users").document(userId)
            .update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                fetchUserProfile(userId) // ✅ Tambahkan ini supaya data userProfile di-update ulang
                callback(true, "Foto profil berhasil diperbarui")
            }
            .addOnFailureListener { e ->
                callback(false, "Gagal memperbarui foto profil: ${e.message}")
            }
    }
}

    sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
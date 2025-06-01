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
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

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

    private val _allUsers = MutableStateFlow<List<UserModel>>(emptyList())
    val allUsers: StateFlow<List<UserModel>> = _allUsers.asStateFlow()

    private val _isLoadingAllUsers = MutableStateFlow(true)
    val isLoadingAllUsers: StateFlow<Boolean> = _isLoadingAllUsers.asStateFlow()

    // NEW: State untuk reset password
    private val _resetPasswordState = MutableStateFlow<AuthState>(AuthState.Idle)
    val resetPasswordState: StateFlow<AuthState> = _resetPasswordState.asStateFlow()

    init {
        auth.currentUser?.let { fetchUserProfile(it.uid) }
    }

    //Menangani Register
    suspend fun registerUser(fullName: String, email: String, password: String) {
        try {
            _registerState.value = AuthState.Loading
            val result = auth.createUserWithEmailAndPassword(email, password).await()

            result.user?.let { user ->
                // Kirim email verifikasi
                user.sendEmailVerification().await()

                // Simpan data ke Firestore
                val newUser = UserModel(
                    uid = user.uid,
                    fullName = fullName,
                    email = email,
                    role = "user"
                )
                usersCollection.document(user.uid).set(newUser).await()
            }

            _registerState.value = AuthState.Success
        } catch (e: Exception) {
            _registerState.value = AuthState.Error(e.message ?: "Registration failed")
        }
    }

    //Menangani Login
    suspend fun loginUser(email: String, password: String) {
        try {
            _loginState.value = AuthState.Loading
            val result = auth.signInWithEmailAndPassword(email, password).await()

            result.user?.let { user ->
                // Cek verifikasi email, kecuali untuk tvbc@gmail.com
                val isEmailVerified = user.isEmailVerified || user.email == "tvbc@gmail.com"
                if (!isEmailVerified) {
                    auth.signOut() // keluarin user yang belum verifikasi
                    _loginState.value = AuthState.Error("Silakan verifikasi email kamu terlebih dahulu.")
                    return
                }

                // Fetch user info dari Firestore
                val userDoc = usersCollection.document(user.uid).get().await()
                if (userDoc.exists()) {
                    val userProfile = userDoc.toObject(UserModel::class.java)
                    _userProfile.value = userProfile
                    _userRole.value = userProfile?.role ?: "user"
                } else {
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

    //Menangani ketika mengisi data di edit profil
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

    //Mengambil seluruh data user di firestore dengan role user
    fun fetchAllUsers() {
        _isLoadingAllUsers.value = true

        usersCollection
            .whereEqualTo("role", "user")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val usersList = mutableListOf<UserModel>()
                for (document in querySnapshot.documents) {
                    val user = document.toObject(UserModel::class.java)
                    user?.let { usersList.add(it) }
                }
                _allUsers.value = usersList
                _isLoadingAllUsers.value = false
                Log.d("AuthViewModel", "Berhasil mengambil ${usersList.size} user")
            }
            .addOnFailureListener { exception ->
                Log.e("AuthViewModel", "Error mengambil data user: ${exception.message}")
                _isLoadingAllUsers.value = false
            }
    }

    //Upload gambar ke cloud
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

    //Menghubungkan data cloud ke firestore
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

    // NEW: Function untuk reset password
    fun sendPasswordResetEmail(email: String) {
        _resetPasswordState.value = AuthState.Loading

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _resetPasswordState.value = AuthState.Success
                } else {
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthInvalidUserException -> "Email tidak terdaftar"
                        is FirebaseAuthInvalidCredentialsException -> "Format email tidak valid"
                        else -> task.exception?.message ?: "Gagal mengirim email reset password"
                    }
                    _resetPasswordState.value = AuthState.Error(errorMessage)
                }
            }
    }

    // NEW: Function untuk reset state reset password
    fun resetPasswordState() {
        _resetPasswordState.value = AuthState.Idle
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
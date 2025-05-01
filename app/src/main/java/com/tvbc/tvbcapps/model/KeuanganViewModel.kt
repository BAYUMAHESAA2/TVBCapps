package com.tvbc.tvbcapps.model

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import id.zelory.compressor.Compressor
import com.tvbc.tvbcapps.util.FileUtil
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KeuanganViewModel : ViewModel() {

    fun uploadImage(context: Context, imageUri: Uri, nominal: String, callback: (Boolean, String) -> Unit) {
        val file = FileUtil.getFileFromUri(context, imageUri)
        if (file == null) {
            callback(false, "Tidak dapat mengakses file")
            return
        }

        // Get user data first
        getUserFullName { fullName ->
            if (fullName.isEmpty()) {
                callback(false, "Gagal mendapatkan nama pengguna")
                return@getUserFullName
            }

            // Compress and upload image asynchronously
            viewModelScope.launch {
                val compressedFile = try {
                    Compressor.compress(context, file)
                } catch (e: Exception) {
                    callback(false, "Gagal mengompresi gambar: ${e.message}")
                    return@launch
                }

                // Replace spaces with underscores for filename
                val safeFullName = fullName.replace(" ", "_").lowercase()
                val timestamp = System.currentTimeMillis()
                val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

                // Upload to Cloudinary with filename based on fullName
                MediaManager.get().upload(compressedFile.absolutePath)
                    .unsigned("keuangan") // Use the same preset or create a new one for finance
                    .option("folder", "folder/keuangan") // Changed folder to keuangan
                    .option("public_id", "${safeFullName}_${currentDate.replace("/", "_")}_$timestamp")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {}

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            val imageUrl = resultData["url"] as? String ?: ""
                            saveToFirestore(imageUrl, nominal, currentDate) { success, message ->
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
    }

    private fun getUserFullName(callback: (String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            callback("")
            return
        }

        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val fullName = document.getString("fullName") ?: ""
                    callback(fullName)
                } else {
                    callback("")
                }
            }
            .addOnFailureListener {
                callback("")
            }
    }

    private fun saveToFirestore(
        imageUrl: String,
        nominal: String,
        date: String,
        callback: (Boolean, String) -> Unit
    ) {
        // Get user ID from Firebase Auth
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            callback(false, "User tidak terautentikasi")
            return
        }

        // Get user data from Firestore
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userModel = document.toObject(UserModel::class.java)

                    // Check if userModel is not null and add data to keuanganData
                    if (userModel != null) {
                        val keuanganData = hashMapOf(
                            "userId" to userId,
                            "imageUrl" to imageUrl,
                            "date" to date,
                            "nominal" to nominal,  // Added nominal field
                            "timestamp" to FieldValue.serverTimestamp(),
                            "fullName" to userModel.fullName,
                            "nim" to userModel.nim,
                            "jurusan" to userModel.jurusan,
                            "angkatan" to userModel.angkatan
                        )

                        // Save to Firestore in keuangan collection
                        FirebaseFirestore.getInstance().collection("keuangan")
                            .add(keuanganData)
                            .addOnSuccessListener {
                                callback(true, "Data berhasil disimpan")
                            }
                            .addOnFailureListener { e ->
                                callback(false, "Gagal menyimpan data: ${e.message}")
                            }
                    } else {
                        callback(false, "Data pengguna tidak lengkap")
                    }
                } else {
                    callback(false, "Data pengguna tidak ditemukan")
                }
            }
            .addOnFailureListener { e ->
                callback(false, "Gagal mengambil data pengguna: ${e.message}")
            }
    }
}
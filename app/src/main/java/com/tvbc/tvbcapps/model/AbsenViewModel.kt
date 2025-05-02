package com.tvbc.tvbcapps.model

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
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
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class AbsenViewModel : ViewModel() {
    private val totalLatihanPerBulan = 8

    private val _jumlahHadir = MutableStateFlow(0)
    val jumlahHadir: StateFlow<Int> = _jumlahHadir

    private val _jumlahTidakHadir = MutableStateFlow(totalLatihanPerBulan)
    val jumlahTidakHadir: StateFlow<Int> = _jumlahTidakHadir

    private val _selectedMonth = MutableStateFlow("")
    val selectedMonth: StateFlow<String> = _selectedMonth

    fun setSelectedMonth(month: String, year: String = getCurrentYear()) {
        _selectedMonth.value = month
        loadJumlahHadirByMonth(month, year)
    }

    fun uploadImage(context: Context, imageUri: Uri, date: String, callback: (Boolean, String) -> Unit) {
        val file = FileUtil.getFileFromUri(context, imageUri)
        if (file == null) {
            callback(false, "Tidak dapat mengakses file")
            return
        }

        // Ambil data user terlebih dahulu
        getUserFullName { fullName ->
            if (fullName.isEmpty()) {
                callback(false, "Gagal mendapatkan nama pengguna")
                return@getUserFullName
            }

            // Kompres gambar dan upload secara asinkron
            viewModelScope.launch {
                val compressedFile = try {
                    Compressor.compress(context, file)
                } catch (e: Exception) {
                    callback(false, "Gagal mengompresi gambar: ${e.message}")
                    return@launch
                }

                // Ganti spasi dengan underscore untuk nama file
                val safeFullName = fullName.replace(" ", "_").lowercase()
                val timestamp = System.currentTimeMillis()

                // Upload ke Cloudinary dengan nama file sesuai fullName
                MediaManager.get().upload(compressedFile.absolutePath)
                    .unsigned("absensi")
                    .option("folder", "folder/absensi")
                    .option("public_id", "${safeFullName}_${date.replace("/", "_")}_$timestamp")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {}

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            val imageUrl = resultData["url"] as? String ?: ""

                            val dateParts = date.split("/")
                            val month = getMonthName(dateParts[1].toInt())
                            val year = dateParts[2]

                            saveToFirestore(imageUrl, date, month, year) { success, message ->
                                if (success && _selectedMonth.value == month) {
                                    loadJumlahHadirByMonth(month, year)
                                }
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

    //Memasukkan data ke firestore absensi
    private fun saveToFirestore(
        imageUrl: String,
        date: String,
        month: String,
        year: String,
        callback: (Boolean, String) -> Unit
    ) {
        // Dapatkan user ID dari Firebase Auth
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            callback(false, "User tidak terautentikasi")
            return
        }

        // Ambil data user dari Firestore
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userModel = document.toObject(UserModel::class.java)

                    if (userModel != null) {
                        val absenData = hashMapOf(
                            "userId" to userId,
                            "imageUrl" to imageUrl,
                            "date" to date,
                            "month" to month,
                            "year" to year,
                            "timestamp" to FieldValue.serverTimestamp(),
                            "fullName" to userModel.fullName,
                            "nim" to userModel.nim,
                            "jurusan" to userModel.jurusan,
                            "angkatan" to userModel.angkatan
                        )

                        // Simpan ke Firestore
                        FirebaseFirestore.getInstance().collection("absensi")
                            .add(absenData)
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

    //Menghitung jumlah hadir
    fun loadJumlahHadir() {
        val currentMonth = getCurrentMonth()
        val currentYear = getCurrentYear()
        _selectedMonth.value = currentMonth
        loadJumlahHadirByMonth(currentMonth, currentYear)
    }

    //Menghitung jumlah hadir dari bulan
    fun loadJumlahHadirByMonth(month: String, year: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("absensi")
            .whereEqualTo("userId", userId)
            .whereEqualTo("month", month)
            .whereEqualTo("year", year)
            .get()
            .addOnSuccessListener { result ->
                val jumlah = result.size()
                _jumlahHadir.value = jumlah
                _jumlahTidakHadir.value = totalLatihanPerBulan - jumlah
            }
            .addOnFailureListener {
                _jumlahHadir.value = 0
                _jumlahTidakHadir.value = totalLatihanPerBulan
            }
    }

    private fun getMonthName(monthNumber: Int): String {
        return when (monthNumber) {
            1 -> "Januari"
            2 -> "Februari"
            3 -> "Maret"
            4 -> "April"
            5 -> "Mei"
            6 -> "Juni"
            7 -> "Juli"
            8 -> "Agustus"
            9 -> "September"
            10 -> "Oktober"
            11 -> "November"
            12 -> "Desember"
            else -> ""
        }
    }

    private fun getCurrentMonth(): String {
        val calendar = Calendar.getInstance()
        val monthNumber = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
        return getMonthName(monthNumber)
    }

    private fun getCurrentYear(): String {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR).toString()
    }

    private val _isAbsenEnabled = mutableStateOf(false)
    val isAbsenEnabled: State<Boolean> = _isAbsenEnabled

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    init {
        setupFirestoreListener()
    }

    private fun setupFirestoreListener() {
        Firebase.firestore.collection("settings").document("absen_button")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                snapshot?.getBoolean("isEnabled")?.let { enabled ->
                    _isAbsenEnabled.value = enabled
                }
            }
    }

    fun setAbsenEnabled(enabled: Boolean) {
        _isLoading.value = true
        Firebase.firestore.collection("settings").document("absen_button")
            .set(mapOf("isEnabled" to enabled))
            .addOnCompleteListener {
                _isLoading.value = false
            }
    }

}
package com.tvbc.tvbcapps.model

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tvbc.tvbcapps.util.FileUtil
import id.zelory.compressor.Compressor
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.Timestamp

class KeuanganViewModel : ViewModel() {
    // LiveData untuk total saldo
    private val _totalSaldo = MutableLiveData<String>()
    val totalSaldo: LiveData<String> = _totalSaldo

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData untuk status operasi
    private val _operationStatus = MutableLiveData<Pair<Boolean, String>>()
    val operationStatus: LiveData<Pair<Boolean, String>> = _operationStatus

    private val _listKeuangan = MutableLiveData<List<Map<String, Any>>>()
    val listKeuangan: LiveData<List<Map<String, Any>>> = _listKeuangan

    init {
        calculateTotalSaldo()
    }

    fun uploadImage(context: Context, imageUri: Uri, nominal: String, callback: (Boolean, String) -> Unit) {
        val file = FileUtil.getFileFromUri(context, imageUri)
        if (file == null) {
            callback(false, "Tidak dapat mengakses file")
            return
        }

        getUserFullName { fullName ->
            if (fullName.isEmpty()) {
                callback(false, "Gagal mendapatkan nama pengguna")
                return@getUserFullName
            }

            viewModelScope.launch {
                val compressedFile = try {
                    Compressor.compress(context, file)
                } catch (e: Exception) {
                    callback(false, "Gagal mengompresi gambar: ${e.message}")
                    return@launch
                }

                val safeFullName = fullName.replace(" ", "_").lowercase()
                val timestamp = System.currentTimeMillis()
                val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

                MediaManager.get().upload(compressedFile.absolutePath)
                    .unsigned("keuangan")
                    .option("folder", "folder/keuangan")
                    .option("public_id", "${safeFullName}_${currentDate.replace("/", "_")}_$timestamp")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {}

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            val imageUrl = resultData["url"] as? String ?: ""
                            saveToFirestore(imageUrl, nominal, currentDate) { success, message ->
                                if (success) {
                                    // Recalculate total saldo after successful upload
                                    calculateTotalSaldo()
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

    // New function to record expenses
    fun recordExpense(nominal: String, keterangan: String) {
        _isLoading.value = true

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _operationStatus.value = Pair(false, "User tidak terautentikasi")
            _isLoading.value = false
            return
        }

        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userModel = document.toObject(UserModel::class.java)

                    if (userModel != null) {
                        val cleanNominalStr = nominal.replace(Regex("[^0-9]"), "")
                        val nominalLong = cleanNominalStr.toLongOrNull()

                        if (nominalLong == null || nominalLong <= 0L) {
                            _operationStatus.value = Pair(false, "Nominal tidak valid")
                            _isLoading.value = false
                            return@addOnSuccessListener
                        }

                        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

                        val pengeluaranData = hashMapOf(
                            "userId" to userId,
                            "date" to currentDate,
                            "nominal" to -nominalLong,  // Simpan sebagai angka negatif untuk pengeluaran
                            "keterangan" to keterangan,
                            "timestamp" to FieldValue.serverTimestamp(),
                            "fullName" to userModel.fullName,
                            "type" to "pengeluaran"  // Menandai ini sebagai pengeluaran
                        )

                        FirebaseFirestore.getInstance().collection("keuangan")
                            .add(pengeluaranData)
                            .addOnSuccessListener {
                                calculateTotalSaldo()  // Recalculate total saldo after adding expense
                                _operationStatus.value = Pair(true, "Pengeluaran berhasil dicatat")
                                _isLoading.value = false
                            }
                            .addOnFailureListener { e ->
                                _operationStatus.value = Pair(false, "Gagal mencatat pengeluaran: ${e.message}")
                                _isLoading.value = false
                            }
                    } else {
                        _operationStatus.value = Pair(false, "Data pengguna tidak lengkap")
                        _isLoading.value = false
                    }
                } else {
                    _operationStatus.value = Pair(false, "Data pengguna tidak ditemukan")
                    _isLoading.value = false
                }
            }
            .addOnFailureListener { e ->
                _operationStatus.value = Pair(false, "Gagal mengambil data pengguna: ${e.message}")
                _isLoading.value = false
            }
    }

    fun calculateTotalSaldo() {
        _isLoading.value = true

        FirebaseFirestore.getInstance()
            .collection("keuangan")
            .get()
            .addOnSuccessListener { result ->
                var total = 0L

                for (document in result) {
                    // Ensure we're specifically looking for the nominal field as a number
                    val nominal = when {
                        document.contains("nominal") -> document.getLong("nominal") ?: 0L
                        else -> 0L
                    }
                    total += nominal  // This now works for both positive (income) and negative (expense) values
                }

                // Format the total with thousand separators
                val formatter = DecimalFormat("#,###")
                _totalSaldo.value = formatter.format(total)
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                Log.e("KeuanganViewModel", "Error calculating total: ${e.message}")
                _totalSaldo.value = "0"
                _isLoading.value = false
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
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            callback(false, "User tidak terautentikasi")
            return
        }

        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userModel = document.toObject(UserModel::class.java)

                    if (userModel != null) {
                        val cleanNominalStr = nominal.replace(Regex("[^0-9]"), "")
                        val nominalLong = cleanNominalStr.toLongOrNull()

                        if (nominalLong == null || nominalLong <= 0L) {
                            callback(false, "Nominal tidak valid")
                            return@addOnSuccessListener
                        }

                        val keuanganData = hashMapOf(
                            "userId" to userId,
                            "imageUrl" to imageUrl,
                            "date" to date,
                            "nominal" to nominalLong,  // simpan sebagai angka positif untuk pemasukan
                            "timestamp" to FieldValue.serverTimestamp(),
                            "fullName" to userModel.fullName,
                            "nim" to userModel.nim,
                            "jurusan" to userModel.jurusan,
                            "angkatan" to userModel.angkatan,
                            "type" to "pemasukan"  // Menandai ini sebagai pemasukan
                        )

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

    fun fetchAllKeuangan() {
        _isLoading.value = true
        FirebaseFirestore.getInstance()
            .collection("keuangan")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val dataList = result.documents.mapNotNull { document ->
                    document.data?.toMutableMap()?.apply {
                        // Konversi timestamp ke format yang bisa difilter
                        val timestamp = this["timestamp"] as? Timestamp
                        timestamp?.let {
                            val date = it.toDate()
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

                            this["date"] = dateFormat.format(date) // Format: 01/05/2025
                            this["time"] = timeFormat.format(date) // Format: 19:19:56
                            this["month"] = date.month + 1 // Bulan dalam angka (1-12)
                            this["year"] = date.year + 1900 // Tahun lengkap
                        }
                    }
                }
                _listKeuangan.value = dataList
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                Log.e("KeuanganViewModel", "Error fetching data: ${e.message}")
                _listKeuangan.value = emptyList()
                _isLoading.value = false
            }
    }

}
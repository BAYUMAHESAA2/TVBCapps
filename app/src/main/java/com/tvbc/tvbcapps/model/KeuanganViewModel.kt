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
import java.util.Calendar
import kotlin.math.abs

class KeuanganViewModel : ViewModel() {
    private val _totalSaldo = MutableLiveData<String>()
    val totalSaldo: LiveData<String> = _totalSaldo

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _operationStatus = MutableLiveData<Pair<Boolean, String>>()
    val operationStatus: LiveData<Pair<Boolean, String>> = _operationStatus

    private val _listKeuangan = MutableLiveData<List<Map<String, Any>>>()
    val listKeuangan: LiveData<List<Map<String, Any>>> = _listKeuangan

    private val _totalPemasukan = MutableLiveData<String>()
    val totalPemasukan: LiveData<String> = _totalPemasukan

    private val _totalPengeluaran = MutableLiveData<String>()
    val totalPengeluaran: LiveData<String> = _totalPengeluaran

    private val _selectedMonth = MutableLiveData<Int?>(null)
    private val _selectedType = MutableLiveData<String?>(null)

    //Agar bisa dipanggil di file lain
    init {
        calculateTotalSaldo()
        calculateIncomeExpense()
    }

    //Fungsi untuk upload bukti bayar
    fun uploadImage(
        context: Context,
        imageUri: Uri,
        nominal: String,
        callback: (Boolean, String) -> Unit
    ) {
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
                    .option(
                        "public_id",
                        "${safeFullName}_${currentDate.replace("/", "_")}_$timestamp"
                    )
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

    //Fungsi tambahan untuk filter data
    fun setMonthFilter(month: Int?) {
        _selectedMonth.value = month
        calculateIncomeExpense()
    }

    //Menghitung pengeluaran dan pemasukan berdasarkna bulan
    private fun calculateIncomeExpense() {
        _isLoading.value = true

        var query: Query = FirebaseFirestore.getInstance().collection("keuangan")

        // Tambahkan filter bulan jika ada
        _selectedMonth.value?.let { month ->
            query = query.whereEqualTo("month", month)
        }

        query.get()
            .addOnSuccessListener { result ->
                var pemasukan = 0L
                var pengeluaran = 0L

                for (document in result) {
                    val nominal = document.getLong("nominal") ?: 0L
                    when (document.getString("type")) {
                        "pemasukan" -> pemasukan += nominal
                        "pengeluaran" -> pengeluaran += abs(nominal)
                    }
                }

                _totalPemasukan.value = DecimalFormat("#,###").format(pemasukan)
                _totalPengeluaran.value = DecimalFormat("#,###").format(pengeluaran)
                _isLoading.value = false
            }
            .addOnFailureListener {
                _totalPemasukan.value = "0"
                _totalPengeluaran.value = "0"
                _isLoading.value = false
            }
    }

    //Menghitung total saldo
    fun calculateTotalSaldo() {
        _isLoading.value = true

        FirebaseFirestore.getInstance().collection("keuangan")
            .get()
            .addOnSuccessListener { result ->
                var total = 0L
                for (document in result) {
                    total += document.getLong("nominal") ?: 0L
                }
                _totalSaldo.value = DecimalFormat("#,###").format(total)
                _isLoading.value = false
            }
            .addOnFailureListener {
                _totalSaldo.value = "0"
                _isLoading.value = false
            }
    }

    //Mengambil nama
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

    //Untuk Pemasukan Keuangan
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
                    val now = Date()
                    val calendar = Calendar.getInstance().apply { time = now }

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
                            "nominal" to nominalLong,
                            "timestamp" to FieldValue.serverTimestamp(),
                            "fullName" to userModel.fullName,
                            "nim" to userModel.nim,
                            "jurusan" to userModel.jurusan,
                            "angkatan" to userModel.angkatan,
                            "type" to "pemasukan",
                            "month" to calendar.get(Calendar.MONTH) + 1, // Tambah bulan
                            "year" to calendar.get(Calendar.YEAR)        // Tambah tahun
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

    //Untuk Pengeluaran Keuangan
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
                    val now = Date()
                    val calendar = Calendar.getInstance().apply { time = now }

                    if (userModel != null) {
                        val cleanNominalStr = nominal.replace(Regex("[^0-9]"), "")
                        val nominalLong = cleanNominalStr.toLongOrNull()

                        if (nominalLong == null || nominalLong <= 0L) {
                            _operationStatus.value = Pair(false, "Nominal tidak valid")
                            _isLoading.value = false
                            return@addOnSuccessListener
                        }

                        val currentDate =
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

                        val pengeluaranData = hashMapOf(
                            "userId" to userId,
                            "date" to currentDate,
                            "nominal" to -nominalLong,
                            "keterangan" to keterangan,
                            "timestamp" to FieldValue.serverTimestamp(),
                            "fullName" to userModel.fullName,
                            "type" to "pengeluaran",
                            "month" to calendar.get(Calendar.MONTH) + 1,
                            "year" to calendar.get(Calendar.YEAR)
                        )

                        FirebaseFirestore.getInstance().collection("keuangan")
                            .add(pengeluaranData)
                            .addOnSuccessListener {
                                calculateTotalSaldo()  // Recalculate total saldo after adding expense
                                _operationStatus.value = Pair(true, "Pengeluaran berhasil dicatat")
                                _isLoading.value = false
                            }
                            .addOnFailureListener { e ->
                                _operationStatus.value =
                                    Pair(false, "Gagal mencatat pengeluaran: ${e.message}")
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

    //Mengambil seluruh data keuangan
    fun fetchAllKeuangan() {
        _isLoading.value = true

        // Mulai dengan CollectionReference
        val collectionRef = FirebaseFirestore.getInstance()
            .collection("keuangan")

        // Buat query berdasarkan filter
        var query: Query = collectionRef.orderBy("timestamp", Query.Direction.DESCENDING)

        _selectedMonth.value?.let { month ->
            query = query.whereEqualTo("month", month)
        }

        _selectedType.value?.let { type ->
            query = query.whereEqualTo("type", type)
        }

        query.get()
            .addOnSuccessListener { result ->
                val dataList = result.documents.mapNotNull { document ->
                    document.data?.toMutableMap()?.apply {
                        val timestamp = this["timestamp"] as? Timestamp
                        timestamp?.let {
                            val date = it.toDate()
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

                            this["date"] = dateFormat.format(date)
                            this["time"] = timeFormat.format(date)

                            val calendar = Calendar.getInstance().apply { time = date }
                            this["month"] = calendar.get(Calendar.MONTH) + 1
                            this["year"] = calendar.get(Calendar.YEAR)
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
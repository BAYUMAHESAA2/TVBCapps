package com.tvbc.tvbcapps.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

data class Notifikasi(
    val id: String = "",
    val judul: String = "",
    val isi: String = "",
    val tanggal: Timestamp = Timestamp.now()
)

class NotifikasiViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val notifikasiCollection = db.collection("notifikasi")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _notifikasiList = MutableStateFlow<List<Notifikasi>>(emptyList())
    val notifikasiList: StateFlow<List<Notifikasi>> = _notifikasiList

    init {
        getNotifikasi()
    }

    fun getNotifikasi() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                notifikasiCollection.orderBy("tanggal", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            // Handle error
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            val notifikasi = snapshot.toObjects(Notifikasi::class.java)
                            _notifikasiList.value = notifikasi
                        }
                    }
            } catch (e: Exception) {
                // Handle exception
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun tambahNotifikasi(judul: String, isi: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val notifikasiId = UUID.randomUUID().toString()
                val notifikasi = Notifikasi(
                    id = notifikasiId,
                    judul = judul,
                    isi = isi,
                    tanggal = Timestamp.now()
                )

                notifikasiCollection.document(notifikasiId).set(notifikasi).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Terjadi kesalahan saat menambahkan notifikasi")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
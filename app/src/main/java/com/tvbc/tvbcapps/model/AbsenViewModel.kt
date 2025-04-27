package com.tvbc.tvbcapps.model

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.tvbc.tvbcapps.database.Absen


class AbsenViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()


    fun submitAbsen(
        absen: Absen, onSuccess : () -> Unit, onError: (Exception) -> Unit
    ){

        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            database.child("absensi").child(userId)
                .push()
                .setValue(absen)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onError(it) }
        } else {
            onError(Exception("User Tidak Login. Login terlebih dahulu"))
        }
    }
}
package com.tvbc.tvbcapps.model

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.tvbc.tvbcapps.database.Absen
import com.tvbc.tvbcapps.database.CloudinaryApi
import com.tvbc.tvbcapps.util.uriToMultipart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class AbsenViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    fun submitAbsen(
        context: Context,
        absen: Absen,
        imageUri: Uri,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        // Show a loading indicator or disable the button here if needed

        val cloudinaryApi = Retrofit.Builder()
            .baseUrl("https://api.cloudinary.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CloudinaryApi::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Prepare the file and upload preset
                val imagePart = uriToMultipart(context, imageUri)
                val uploadPreset = "TVBCapps".toRequestBody("text/plain".toMediaTypeOrNull())

                // Upload to Cloudinary
                val response = cloudinaryApi.uploadImage(
                    cloudName = "dpoivpc9u",
                    file = imagePart,
                    uploadPreset = uploadPreset
                )

                // Use secure_url from response (note the corrected property name)
                val finalAbsen = absen.copy(fotoUri = response.secure_url)

                // Get current user ID
                val userId = auth.currentUser?.uid ?: throw Exception("User tidak terautentikasi")

                // Save to Firebase Realtime Database
                withContext(Dispatchers.Main) {
                    database.child("absensi")
                        .child(userId)
                        .push()
                        .setValue(finalAbsen)
                        .addOnSuccessListener {
                            onSuccess()
                        }
                        .addOnFailureListener {
                            onError(it)
                        }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }
}
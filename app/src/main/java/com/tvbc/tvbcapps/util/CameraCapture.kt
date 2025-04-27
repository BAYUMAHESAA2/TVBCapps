package com.tvbc.tvbcapps.util

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun rememberCameraCaptureLauncher(
    context: Context,
    onImageCaptured: (Uri?) -> Unit
): Pair<Uri?, () -> Unit> {
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val currentUri = photoUri
        if (success && currentUri != null) {
            onImageCaptured(currentUri)
        } else {
            onImageCaptured(null)
        }
    }

    val launchCamera: () -> Unit = {
        val photoFile = File.createTempFile("absen_", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            photoFile
        )

        photoUri = uri
        cameraLauncher.launch(uri)
    }

    return Pair(photoUri, launchCamera)
}
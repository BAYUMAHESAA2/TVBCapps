package com.tvbc.tvbcapps.ui.theme.screen

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.tvbc.tvbcapps.R
import com.tvbc.tvbcapps.model.KeuanganViewModel
import com.tvbc.tvbcapps.ui.theme.TVBCappsTheme
import com.tvbc.tvbcapps.util.rememberCameraCaptureLauncher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormKeuangan(navController: NavHostController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.shadow(6.dp),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.kembali),
                            tint = Color.Black,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.form_keuangan),
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        ScreenContentFormKeuangan(
            Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun ScreenContentFormKeuangan(
    modifier: Modifier = Modifier,
    viewModel: KeuanganViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var nominal by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var uploadStatus by remember { mutableStateOf("") }

    // Permission handling
    val permissionsToRequest = remember {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            // Permissions granted, proceed with action
            uploadStatus = ""
        } else {
            uploadStatus = "Izin diperlukan untuk menggunakan fitur ini"
        }
    }

    // Add image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
        }
    }

    val (_, launchCamera) = rememberCameraCaptureLauncher(context) {
        selectedImageUri = it
    }

    // Function to check permissions and launch image picker
    fun launchImagePicker() {
        permissionLauncher.launch(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        )
        imagePickerLauncher.launch("image/*")
    }

    // Function to check permissions and launch camera
    fun checkPermissionsAndLaunchCamera() {
        permissionLauncher.launch(permissionsToRequest)
        launchCamera()
    }

    // Function to upload to Cloudinary
    fun uploadImageToCloudinary() {
        if (selectedImageUri == null || nominal.isEmpty()) {
            uploadStatus = "Masukkan nominal dan pilih gambar terlebih dahulu"
            return
        }

        isUploading = true
        uploadStatus = "Mengunggah..."

        // Process upload to Cloudinary with viewModel
        viewModel.uploadImage(context, selectedImageUri!!, nominal) { success, message ->
            isUploading = false
            uploadStatus = if (success) "Berhasil diunggah!" else "Gagal: $message"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image for form
        Image(
            painter = painterResource(id = R.drawable.formkeuangan),
            contentDescription = "Ilustrasi Keuangan",
            modifier = Modifier
                .size(325.dp)
                .padding(vertical = 16.dp)
        )

        OutlinedTextField(
            value = nominal,
            onValueChange = { nominal = it },
            label = { Text("Masukkan nominal") },
            singleLine = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.MonetizationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                .clickable(
                    onClick = { launchImagePicker() }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = "Preview Bukti Pembayaran",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                    Text(stringResource(R.string.pilih_file), color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(stringResource(R.string.atau))

        Button(
            onClick = { checkPermissionsAndLaunchCamera() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.buka_kamera))
        }

        // Show upload status if any
        if (uploadStatus.isNotEmpty()) {
            Text(
                text = uploadStatus,
                color = if (uploadStatus.startsWith("Berhasil")) Color.Green else Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = { uploadImageToCloudinary() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF660000)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            enabled = !isUploading && selectedImageUri != null && nominal.isNotEmpty()
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(stringResource(R.string.kirim), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun FormKeuanganScreenPreview() {
    TVBCappsTheme {
        FormKeuangan(rememberNavController())
    }
}
package com.tvbc.tvbcapps.ui.theme.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import androidx.compose.material3.CircularProgressIndicator
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseAuth
import com.tvbc.tvbcapps.R
import com.tvbc.tvbcapps.database.Absen
import com.tvbc.tvbcapps.model.AbsenViewModel
import com.tvbc.tvbcapps.model.AuthViewModel
import com.tvbc.tvbcapps.ui.theme.TVBCappsTheme
import com.tvbc.tvbcapps.util.rememberCameraCaptureLauncher
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormAbsenScreen(navController: NavHostController) {
    Scaffold(
        containerColor = Color.Transparent,
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
                        text = stringResource(R.string.form_absen),
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ){
            ScreenContentAbsenForm(
                navController
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScreenContentAbsenForm(
    navController: NavHostController
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedDate by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val viewModel: AbsenViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    val userProfile by authViewModel.userProfile.collectAsState()

    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val gallerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null){
            selectedImageUri = uri
        }
    }

    val (_,launchCamera) = rememberCameraCaptureLauncher(context) {
        selectedImageUri = it
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = "$dayOfMonth/${month + 1}/$year"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = calendar.timeInMillis // Tidak bisa pilih sebelum hari ini
        }
    }

    val isFormValid by remember(selectedImageUri, selectedDate) {
        derivedStateOf {
            selectedImageUri != null && selectedDate.isNotEmpty()
        }
    }

    //kolom di gunakan untuk membatasi gambar dari top app bar dan agar gambar bisa central berada di tengah"
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gambar form
        Image(
            painter = painterResource(id = R.drawable.formabsen),
            contentDescription = "Ilustrasi Absen",
            modifier = Modifier
                .size(325.dp) // Sesuai gambar
                .padding(vertical = 16.dp)
        )

        OutlinedTextField(
            value = selectedDate,
            onValueChange = { selectedDate = it},
            readOnly = true,
            label = { Text(stringResource(R.string.tanggal)) },
            trailingIcon = {
                IconButton(
                    onClick = {datePickerDialog.show() }
                ) {
                    Icon(
                        Icons.Filled.CalendarMonth,
                        contentDescription = "Pilih Tanggal"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box (
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                .clickable { gallerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ){
            if (selectedImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = "Preview Gambar Absen",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }else{
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
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
            onClick = {
                if (cameraPermissionState.status.isGranted) {
                    launchCamera()
                } else {
                    cameraPermissionState.launchPermissionRequest()
                }
            },
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

        Button(
            onClick = {
                isLoading = true
                val currentUser = FirebaseAuth.getInstance().currentUser

                if (currentUser != null && userProfile != null) {
                    val nama = userProfile?.fullName?: "Nama Tidak Diketahui"
                    val nim = userProfile?.nim?: "NIM Tidak Diketahui"

                    selectedImageUri?.let { uri ->
                        val absenData = Absen(
                            nama = nama,
                            nim = nim,
                            tanggal = selectedDate,
                            fotoUri = uri.toString()
                        )

                        viewModel.submitAbsen(
                            context = context,
                            absen = absenData,
                            imageUri = uri,
                            onSuccess = {
                                isLoading = false
                                Toast.makeText(context, "Absen Berhasil!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            },
                            onError = { error ->
                                isLoading = false
                                Log.e("FormAbsenScreen", "Error submitting absen", error)
                                Toast.makeText(
                                    context,
                                    "Gagal Absen: ${error.message ?: "Terjadi kesalahan"}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    } ?: run {
                        isLoading = false
                        Toast.makeText(context, "Silakan pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    isLoading = false
                    Toast.makeText(context, "User belum login atau data profile kosong", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = isFormValid && !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF660000)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(stringResource(R.string.kirim), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun FormAbsenScreenPreview() {
    TVBCappsTheme {
        FormAbsenScreen(rememberNavController())
    }
}
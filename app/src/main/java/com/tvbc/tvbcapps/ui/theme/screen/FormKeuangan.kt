package com.tvbc.tvbcapps.ui.theme.screen

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.input.KeyboardCapitalization
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
import com.tvbc.tvbcapps.model.AuthViewModel
import com.tvbc.tvbcapps.model.KeuanganViewModel
import com.tvbc.tvbcapps.ui.theme.TVBCappsTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormKeuangan(navController: NavHostController, authViewModel: AuthViewModel = viewModel()) {
    val isUserLoggedIn = authViewModel.isUserLoggedIn()
    val userRole by authViewModel.userRole.collectAsState()
    val isUserProfileLoading by authViewModel.isUserProfileLoading.collectAsState()

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
                    if (isUserLoggedIn && !isUserProfileLoading) {
                        when (userRole) {
                            "admin" -> {
                                Text(
                                    text = "Form Pengeluaran",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 30.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            "user" -> {
                                Text(
                                    text = stringResource(R.string.form_keuangan),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 30.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        if (isUserLoggedIn && !isUserProfileLoading) {
            when (userRole) {
                "admin" -> {
                    FormKeuanganAdmin(Modifier.padding(innerPadding))
                }

                "user" -> {
                    FormKeuanganAnggota(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun FormKeuanganAnggota(
    modifier: Modifier = Modifier,
    viewModel: KeuanganViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var nominal by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var uploadStatus by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        uploadStatus = if (allGranted) {
            ""
        } else {
            "Izin diperlukan untuk menggunakan fitur ini"
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
            onValueChange = { input ->
                // Hanya izinkan angka
                if (input.all { it.isDigit() }) {
                    nominal = input
                }
            },
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

        Spacer(modifier = Modifier.height(16.dp))

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
                Text(
                    stringResource(R.string.kirim),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FormKeuanganAdmin(
    modifier: Modifier = Modifier,
    keuanganViewModel: KeuanganViewModel = viewModel()
) {
    var nominal by remember { mutableStateOf("") }
    var nominalError by remember { mutableStateOf(false) }

    var keteranganPengeluaran by remember { mutableStateOf("") }
    var keteranganPengeluaranError by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }

    // Observe loading state
    val isLoading by keuanganViewModel.isLoading.observeAsState(initial = false)

    // Observe operation status for showing result
    val operationStatus by keuanganViewModel.operationStatus.observeAsState()

    // Show snackbar for operation status
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Process operation status
    LaunchedEffect(operationStatus) {
        operationStatus?.let { (success, message) ->
            if (isSubmitting) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }

                if (success) {
                    // Reset form fields if success
                    nominal = ""
                    keteranganPengeluaran = ""
                    nominalError = false
                    keteranganPengeluaranError = false
                }

                isSubmitting = false
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.formkeuangan),
                contentDescription = "Ilustrasi Keuangan",
                modifier = Modifier
                    .size(325.dp)
                    .padding(vertical = 16.dp)
            )

            OutlinedTextField(
                value = nominal,
                onValueChange = {
                    nominal = it
                    nominalError = false
                },
                label = { Text("Nominal") },
                isError = nominalError,
                supportingText = { ErrorHintKeuangan(nominalError) },
                trailingIcon = {
                    if (nominalError) {
                        IconPickerKeuangan(false)
                    } else {
                        Icon(
                            imageVector = Icons.Filled.MonetizationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = keteranganPengeluaran,
                onValueChange = {
                    keteranganPengeluaran = it
                    keteranganPengeluaranError = false
                },
                label = { Text("Keterangan Pengeluaran") },
                isError = keteranganPengeluaranError,
                supportingText = { ErrorHintKeuangan(keteranganPengeluaranError) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Send
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    nominalError = ((nominal.isBlank() || nominal == "0" || (nominal.toIntOrNull()
                        ?: 0) <= 0))
                    keteranganPengeluaranError = (keteranganPengeluaran.isBlank())

                    if (!nominalError && !keteranganPengeluaranError) {
                        isSubmitting = true
                        keuanganViewModel.recordExpense(nominal, keteranganPengeluaran)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF660000)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading && !isSubmitting
            ) {
                if (isLoading || isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "Kirim",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
fun ErrorHintKeuangan(isError: Boolean){
    if (isError){
        Text("Masukkan Input yang sesuai")
    }
}

@Composable
fun IconPickerKeuangan(isError: Boolean) {
    if (isError) {
        Icon(imageVector = Icons.Filled.Warning, contentDescription = null)
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
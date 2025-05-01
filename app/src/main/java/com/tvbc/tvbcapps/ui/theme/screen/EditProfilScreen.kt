package com.tvbc.tvbcapps.ui.theme.screen

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import coil.compose.AsyncImage
import com.tvbc.tvbcapps.R
import com.tvbc.tvbcapps.component.CurvedBackground
import com.tvbc.tvbcapps.model.AuthViewModel
import com.tvbc.tvbcapps.ui.theme.TVBCappsTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfilScreen(navController: NavHostController) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.keluar),//diganti nanti jadi kembali
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Edit Profil",//ganti nanti
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF660000),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        ScreenContentEditProfil(
            modifier = Modifier.padding(innerPadding),
            navController = navController
        )
    }
}

@Composable
fun ScreenContentEditProfil(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(),
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val isUserLoggedIn = authViewModel.isUserLoggedIn()
    val userRole by authViewModel.userRole.collectAsState()
    val isUserProfileLoading by authViewModel.isUserProfileLoading.collectAsState()

    val userProfile by viewModel.userProfile.collectAsState()
    val isUpdating by viewModel.isProfileUpdating.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // State for profile data
    var fullName by remember { mutableStateOf("") }
    var nim by remember { mutableStateOf("") }
    var jurusan by remember { mutableStateOf("") }
    var angkatan by remember { mutableStateOf("") }

    // Photo upload states (moved from ProfileScreen)
    var showChangePictureDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadStatus by remember { mutableStateOf("") }


    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (!allGranted) {
            uploadStatus = "Izin diperlukan untuk menggunakan fitur ini"
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            isUploading = true
            viewModel.uploadProfileImage(context, it) { success, message ->
                isUploading = false
                uploadStatus = if (success) "Berhasil diperbarui!" else "Gagal: $message"
            }
        }
    }


    // Function to launch image picker
    fun launchImagePicker() {
        permissionLauncher.launch(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        )
        imagePickerLauncher.launch("image/*")
    }

    // Profile picture change dialog
    if (showChangePictureDialog) {
        AlertDialog(
            onDismissRequest = { showChangePictureDialog = false },
            title = { Text("Ubah Foto Profil") },
            text = { Text("Pilih sumber foto") },
            confirmButton = {
                TextButton(onClick = {
                    showChangePictureDialog = false
                    launchImagePicker()
                }) {
                    Text("Galeri")
                }
            }
        )
    }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            fullName = it.fullName
            nim = it.nim
            jurusan = it.jurusan
            angkatan = it.angkatan
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        CurvedBackground()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Profile Image Section with edit capability
            Box {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .clickable { showChangePictureDialog = true }
                ) {
                    if (userProfile?.profileImageUrl?.isNotEmpty() == true) {
                        val safeUrl = userProfile?.profileImageUrl?.replace("http://", "https://")
                        AsyncImage(
                            model = safeUrl,
                            contentDescription = "Foto Profil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            error = painterResource(id = R.drawable.logoprofil),
                            placeholder = painterResource(id = R.drawable.logoprofil)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.logoprofil),
                            contentDescription = "Foto Profil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Edit icon
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(40.dp)
                            .background(Color(0xFF660000), CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Ubah Foto",
                            tint = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(180.dp)
                            .align(Alignment.Center),
                        color = Color(0xFF660000),
                        strokeWidth = 4.dp
                    )
                }
            }

            // Upload status message
            if (uploadStatus.isNotEmpty()) {
                Text(
                    text = uploadStatus,
                    color = if (uploadStatus.startsWith("Berhasil")) Color.Green else Color.Red,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isUserLoggedIn && !isUserProfileLoading) {
                when (userRole) {
                    "admin" -> {
                        EditableTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            hint = "Nama Lengkap"
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    "user" -> {
                        // Form fields
                        EditableTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            hint = "Nama Lengkap"
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        EditableTextField(
                            value = nim,
                            onValueChange = { nim = it },
                            hint = "NIM"
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        EditableTextField(
                            value = jurusan,
                            onValueChange = { jurusan = it },
                            hint = "Jurusan"
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        EditableTextField(
                            value = angkatan,
                            onValueChange = { angkatan = it },
                            hint = "Angkatan"
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }

            Button(
                onClick = {
                    scope.launch {
                        viewModel.updateUserProfile(fullName, nim, jurusan, angkatan)
                        navController.navigateUp()
                    }
                },
                enabled = !isUpdating,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF660000)
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Simpan",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EditableTextField(value: String, onValueChange: (String) -> Unit, hint: String) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = hint,
                color = Color.Gray
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFEEEEEE),
            unfocusedContainerColor = Color(0xFFEEEEEE),
            disabledContainerColor = Color(0xFFEEEEEE),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledTextColor = Color.Black,
            cursorColor = Color.Black
        ),
        singleLine = true
    )
}

@Preview(showBackground = true)
@Composable
fun EditProfilScreenPreview() {
    TVBCappsTheme {
        EditProfilScreen(rememberNavController())
    }
}

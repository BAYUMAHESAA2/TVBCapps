package com.tvbc.tvbcapps.ui.theme.screen

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.tvbc.tvbcapps.R
import com.tvbc.tvbcapps.component.BottomNavigationBar
import com.tvbc.tvbcapps.component.TopBar
import com.tvbc.tvbcapps.model.AuthViewModel
import com.tvbc.tvbcapps.navigation.Screen
import com.tvbc.tvbcapps.ui.theme.TVBCappsTheme
import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File

@Composable
fun ProfilScreen(navController: NavHostController) {
    val viewModel: AuthViewModel = viewModel()

    // Check if user is logged in, if not redirect to landing page
    LaunchedEffect(Unit) {
        if (!viewModel.isUserLoggedIn()) {
            navController.navigate(Screen.LandingPage.route) {
                popUpTo(Screen.Profil.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopBar(navController)
        },
        bottomBar = {
            BottomNavigationBar(navController, viewModel)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CurvedBackgroundProfil()
            ScreenContentProfil(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun ScreenContentProfil(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: AuthViewModel
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current
    var showChangePictureDialog by remember { mutableStateOf(false) }

    // Permission and image handling states
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadStatus by remember { mutableStateOf("") }

    // Permission handling
    val permissionsToRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

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

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri?.let { uri ->
                isUploading = true
                viewModel.uploadProfileImage(context, uri) { success, message ->
                    isUploading = false
                    uploadStatus = if (success) "Berhasil diperbarui!" else "Gagal: $message"
                }
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

    // Function to launch camera
    fun launchCamera() {
        permissionLauncher.launch(permissionsToRequest)
        val file = File.createTempFile(
            "profile_${System.currentTimeMillis()}",
            ".jpg",
            context.externalCacheDir
        )
        selectedImageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        selectedImageUri?.let { uri ->
            cameraLauncher.launch(uri)
        } ?: run {
            // Handle case ketika uri null
            uploadStatus = "Gagal: URI gambar tidak valid"
        }
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
            },
            dismissButton = {
                TextButton(onClick = {
                    showChangePictureDialog = false
                    launchCamera()
                }) {
                    Text("Kamera")
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        CurvedBackgroundProfil()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image Section
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
                Spacer(modifier = Modifier.height(12.dp))
            }

            // User name
            Text(
                text = userProfile?.fullName ?: "Nama tidak tersedia",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Edit Profile Button
            ProfileActionButton(
                icon = Icons.Default.Person,
                text = stringResource(R.string.edit_profil),
                onClick = { navController.navigate(Screen.EditProfil.route) },
                tint = Color(0xFF660000)
            )

            Spacer(modifier = Modifier.height(15.dp))

            // About App Button
            ProfileActionButton(
                icon = Icons.Default.Info,
                text = stringResource(R.string.tentang_aplikasi),
                onClick = { navController.navigate(Screen.TentangAplikasi.route) },
                tint = Color(0xFF660000)
            )

            Spacer(modifier = Modifier.height(15.dp))

            // Logout Button
            ProfileActionButton(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                text = stringResource(R.string.keluar),
                onClick = {
                    viewModel.logoutUser()
                    navController.navigate(Screen.LandingPage.route) {
                        popUpTo(0)
                    }
                },
                tint = Color.White,
                backgroundColor = Color(0xFF660000)
            )
        }
    }
}

@Composable
fun ProfileActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    tint: Color,
    backgroundColor: Color = Color.White
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = tint
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(30.dp),
                tint = tint
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                fontSize = 20.sp,
                color = tint
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = text,
                modifier = Modifier.size(30.dp),
                tint = tint
            )
        }
    }
}

@Composable
fun CurvedBackgroundProfil() {
    val headerHeight = 160.dp
    val curveHeight = 40.dp
    val density = LocalDensity.current

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
    ) {
        val width = size.width
        val height = size.height
        val curveHeightPx = with(density) { curveHeight.toPx() }

        val backgroundColor = Color(0xFF660000)

        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(width, 0f)
            lineTo(width, height - curveHeightPx)
            quadraticTo(
                width / 2f,
                height - curveHeightPx * 2,
                0f,
                height - curveHeightPx
            )
            close()
        }
        drawPath(path, backgroundColor)
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun ProfilScreenPreview() {
    TVBCappsTheme {
        ProfilScreen(rememberNavController())
    }
}
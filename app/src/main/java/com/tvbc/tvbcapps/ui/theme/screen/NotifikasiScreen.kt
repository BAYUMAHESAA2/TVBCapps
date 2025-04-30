package com.tvbc.tvbcapps.ui.theme.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Timestamp
import com.tvbc.tvbcapps.R
import com.tvbc.tvbcapps.model.AuthViewModel
import com.tvbc.tvbcapps.model.Notifikasi
import com.tvbc.tvbcapps.model.NotifikasiViewModel
import com.tvbc.tvbcapps.ui.theme.TVBCappsTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotifikasiScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    notifikasiViewModel: NotifikasiViewModel = viewModel()
) {
    val isUserLoggedIn = authViewModel.isUserLoggedIn()
    val userRole by authViewModel.userRole.collectAsState()
    val isUserProfileLoading by authViewModel.isUserProfileLoading.collectAsState()
    val notifikasiList by notifikasiViewModel.notifikasiList.collectAsState()
    val isLoading by notifikasiViewModel.isLoading.collectAsState()

    val showDialog = remember { mutableStateOf(false) }

    // Snackbar state untuk menampilkan pesan
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        text = stringResource(R.string.notifikasi),
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
        },

        floatingActionButton = {
            if (isUserLoggedIn && !isUserProfileLoading) {
                when (userRole) {
                    "admin" -> {
                        FloatingActionButton(
                            onClick = { showDialog.value = true },
                            containerColor = Color(0xFF660000)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Tambah",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        ScreenContentNotifikasi(
            modifier = Modifier.padding(innerPadding),
            notifikasiList = notifikasiList,
            isLoading = isLoading
        )
    }

    if (showDialog.value) {
        InputDialog(
            onDismissRequest = { showDialog.value = false },
            onSubmit = { judul, isi ->
                notifikasiViewModel.tambahNotifikasi(
                    judul = judul,
                    isi = isi,
                    onSuccess = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Notifikasi berhasil ditambahkan")
                        }
                    },
                    onError = { errorMsg ->
                        scope.launch {
                            snackbarHostState.showSnackbar("Error: $errorMsg")
                        }
                    }
                )
            }
        )
    }
}

@Composable
fun ScreenContentNotifikasi(
    modifier: Modifier = Modifier,
    notifikasiList: List<Notifikasi>,
    isLoading: Boolean
) {
    if (isLoading && notifikasiList.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF660000))
        }
    } else if (notifikasiList.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Belum ada notifikasi",
                style = MaterialTheme.typography.titleLarge
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 60.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            items(notifikasiList) { notif ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Text(
                            text = notif.judul,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = notif.isi,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Justify
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatDate(notif.tanggal),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// Fungsi helper untuk memformat tanggal
fun formatDate(timestamp: Timestamp): String {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    return dateFormat.format(timestamp.toDate())
}

@Composable
fun InputDialog(
    onDismissRequest: () -> Unit,
    onSubmit: (judul: String, isi: String) -> Unit
) {
    var judul by remember { mutableStateOf("") }
    var isi by remember { mutableStateOf("") }

    var judulError by remember { mutableStateOf(false) }
    var isiError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { onDismissRequest() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Tambah Informasi",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = judul,
                    onValueChange = {
                        judul = it
                        judulError = false
                    },
                    label = { Text("Judul") },
                    trailingIcon = { IconPickerNotif(judulError) },
                    supportingText = {
                        if (judulError) {
                            Text("Judul tidak boleh kosong")
                        }
                    },
                    isError = judulError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = isi,
                    onValueChange = {
                        isi = it
                        isiError = false
                    },
                    label = { Text("Isi") },
                    trailingIcon = { IconPickerNotif(isiError) },
                    supportingText = {
                        if (isiError) {
                            Text("Isi tidak boleh kosong")
                        }
                    },
                    isError = isiError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Send
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        judulError = (judul.isBlank())
                        isiError = (isi.isBlank())

                        if (!judulError && !isiError) {
                            onSubmit(judul, isi)
                            onDismissRequest()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF660000)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        stringResource(R.string.kirim),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun IconPickerNotif(isError: Boolean) {
    if (isError) {
        Icon(imageVector = Icons.Filled.Warning, contentDescription = null)
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun NotifikasiScreenPreview() {
    TVBCappsTheme {
        NotifikasiScreen(rememberNavController())
    }
}

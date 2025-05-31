package com.tvbc.tvbcapps.ui.theme.screen

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.tvbc.tvbcapps.R
import com.tvbc.tvbcapps.component.BottomNavigationBar
import com.tvbc.tvbcapps.component.TopBar
import com.tvbc.tvbcapps.model.AbsenViewModel
import com.tvbc.tvbcapps.model.AuthViewModel
import com.tvbc.tvbcapps.ui.theme.TVBCappsTheme
import java.time.LocalDate
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AbsenScreen(navController: NavHostController, authViewModel: AuthViewModel = viewModel()) {
    val isUserLoggedIn = authViewModel.isUserLoggedIn()
    val userRole by authViewModel.userRole.collectAsState()
    val isUserProfileLoading by authViewModel.isUserProfileLoading.collectAsState()
    val viewModel: AuthViewModel = viewModel()
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopBar(navController)
        },
        bottomBar = {
            BottomNavigationBar(navController, viewModel)
        }
    ) { innerPadding ->
        if (isUserLoggedIn && !isUserProfileLoading) {
            when (userRole) {
                "admin" -> {
                    AbsenAdmin(modifier = Modifier.padding(innerPadding))
                }
                "user" -> {
                    ScreenContentAbsen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenContentAbsen(
    modifier: Modifier = Modifier,
    isPreview: Boolean = false,
    authViewModel: AuthViewModel = viewModel(),
    absenViewModel: AbsenViewModel = viewModel()
) {
    val userProfile by authViewModel.userProfile.collectAsState()
    val jumlahHadir by absenViewModel.jumlahHadir.collectAsState()
    val jumlahTidakHadir by absenViewModel.jumlahTidakHadir.collectAsState()
    val selectedMonth by absenViewModel.selectedMonth.collectAsState()

    // Konstanta untuk total kehadiran per bulan
    val totalKehadiranPerBulan = 8

    LaunchedEffect(Unit) {
        absenViewModel.loadJumlahHadir()
    }

    var expanded by remember { mutableStateOf(false) }
    val bulan = if (isPreview) {
        listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
    } else {
        listOf(
            stringResource(R.string.january),
            stringResource(R.string.february),
            stringResource(R.string.march),
            stringResource(R.string.april),
            stringResource(R.string.may),
            stringResource(R.string.june),
            stringResource(R.string.july),
            stringResource(R.string.august),
            stringResource(R.string.september),
            stringResource(R.string.october),
            stringResource(R.string.november),
            stringResource(R.string.desember)
        )
    }

    var pilihBulan by remember { mutableStateOf(selectedMonth) }

    LaunchedEffect(selectedMonth) {
        if (selectedMonth.isNotEmpty()) {
            pilihBulan = selectedMonth
        }
    }

    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR).toString() }

    val namaUser = userProfile?.fullName?.takeIf { it.isNotBlank() } ?: "Nama tidak tersedia"
    val nimUser = userProfile?.nim?.takeIf { it.isNotBlank() } ?: "NIM belum dilengkapi"

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
                .height(80.dp),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF660000)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Nama",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = "NIM",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = namaUser,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = nimUser,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = pilihBulan,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.pilih_bulan)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                bulan.forEach { bulanItem ->
                    DropdownMenuItem(
                        text = { Text(bulanItem) },
                        onClick = {
                            pilihBulan = bulanItem
                            expanded = false
                            absenViewModel.setSelectedMonth(bulanItem, currentYear)
                        }
                    )
                }
            }
        }

        RiwayatPresensiCard(hadir = jumlahHadir, tidakHadir = jumlahTidakHadir)

        Text(
            text = "Absensi bulan $pilihBulan $currentYear",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, start = 8.dp)
        )

        // Tampilkan grafik pie chart
        AbsensiPieChart(
            hadir = jumlahHadir,
            totalKehadiran = totalKehadiranPerBulan,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
fun RiwayatPresensiCard(
    hadir: Int,
    tidakHadir: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 24.dp, bottom = 12.dp)) {
        Text(
            text = stringResource(R.string.riwayat_absen),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Hadir", style = MaterialTheme.typography.bodyMedium, color = Color.Black)
                    Text(
                        hadir.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(Color(0xFF660000))
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Tidak Hadir",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    Text(
                        tidakHadir.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AbsenAdmin(
    modifier: Modifier = Modifier,
    viewModel: AbsenViewModel = viewModel()
) {
    val isEnabled = remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(false) }
    val absenList by viewModel.absenList.collectAsState()
    val context = LocalContext.current
    val currentDate = LocalDate.now()
    val currentMonth = currentDate.monthValue.toString() // Mengambil bulan sekarang sebagai string
    val currentDay = currentDate.dayOfMonth.toString() // Mengambil tanggal sekarang sebagai string

    val selectedMonth = remember { mutableStateOf(currentMonth) }
    val selectedDate = remember { mutableStateOf(currentDay) }

    val filteredList = absenList.filter { absen ->
        val parts = absen.date.split("/")
        parts.size == 3 &&
                parts[0] == selectedDate.value &&
                parts[1] == selectedMonth.value
    }

    LaunchedEffect(Unit) {
        viewModel.loadAllAbsensi()

        Firebase.firestore.collection("settings").document("absen_button")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    isEnabled.value = snapshot.getBoolean("isEnabled") == true
                }
            }
    }

    Column(modifier = modifier.padding(16.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Aktifkan Tombol Absen:", modifier = Modifier.weight(1f))
                    Switch(
                        checked = isEnabled.value,
                        onCheckedChange = { newValue ->
                            isLoading.value = true
                            Firebase.firestore.collection("settings").document("absen_button")
                                .set(mapOf("isEnabled" to newValue))
                                .addOnSuccessListener { isLoading.value = false }
                                .addOnFailureListener { isLoading.value = false }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF660000),
                            checkedTrackColor = Color(0xFFCC9999)
                        ),
                        enabled = !isLoading.value
                    )
                }
                Text(
                    text = "Status: ${if (isEnabled.value) "Aktif" else "Non-aktif"}" +
                            if (isLoading.value) " (Memperbarui...)" else "",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DropdownSelector(
                label = "Tanggal",
                options = (1..31).map { it.toString() },
                selected = selectedDate.value,
                onSelectedChange = { selectedDate.value = it }
            )
            DropdownSelector(
                label = "Bulan",
                options = (1..12).map { it.toString() },
                selected = selectedMonth.value,
                onSelectedChange = { selectedMonth.value = it }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Data Absensi:",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Jumlah yang hadir: ${filteredList.size} orang",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }


        LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
            items(filteredList) { absen ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Nama: ${absen.fullName}")
                        Text("NIM: ${absen.nim}")
                        Text("Jurusan: ${absen.jurusan}")
                        Text("Angkatan: ${absen.angkatan}")
                        Text("Tanggal: ${absen.date}")

                        absen.imageUrl.takeIf { it.isNotBlank() }?.let { url ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Bukti: ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "Lihat Bukti",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Blue,
                                    modifier = Modifier.clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selected: String,
    onSelectedChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.width(150.dp),
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onSelectedChange(option)
                    expanded = false
                })
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun AbsenScreenPreview() {
    TVBCappsTheme {
        AbsenScreen(rememberNavController())
    }
}
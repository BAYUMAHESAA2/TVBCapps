package com.tvbc.tvbcapps.ui.theme.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.tvbc.tvbcapps.R
import com.tvbc.tvbcapps.component.BottomNavigationBar
import com.tvbc.tvbcapps.component.CurvedBackground
import com.tvbc.tvbcapps.component.TopBar
import com.tvbc.tvbcapps.model.AuthViewModel
import com.tvbc.tvbcapps.model.KeuanganViewModel
import com.tvbc.tvbcapps.navigation.Screen
import com.tvbc.tvbcapps.ui.theme.TVBCappsTheme

@Composable
fun KeuanganScreen(navController: NavHostController, authViewModel: AuthViewModel = viewModel()) {
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.FormKeuangan.route) },
                containerColor = Color(0xFF660000)
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Filled.MonetizationOn,
                        contentDescription = "Bayar",
                        tint = Color.White
                    )
                }
            }
        },
    ) { innerPadding ->
        //Belum diperbaiki untuk tampilan anggota
        if (isUserLoggedIn && !isUserProfileLoading) {
            when (userRole) {
                "admin" -> {
                    KeuanganAdminScreen(modifier = Modifier.padding(innerPadding))
                }
                "user" -> {
                    ScreenContentKeuangan(
                        modifier = Modifier.padding(innerPadding),
                        navController
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenContentKeuangan(modifier: Modifier = Modifier, navController: NavHostController) {
    var expanded by remember { mutableStateOf(false) }

    val daftarBulan = listOf(
        "Januari 2025",
        "Februari 2025",
        "Maret 2025",
        "April 2025",
        "Mei 2025",
        "Juni 2025",
        "Juli 2025",
        "Agustus 2025",
        "September 2025",
        "Oktober 2025",
        "November 2025",
        "Desember 2025"
    )

    var pilihBulan by remember { mutableStateOf("Pilih Bulan") }

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        CurvedBackground()

        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(25.dp))

            CardKeuangan()

            Spacer(modifier = Modifier.height(30.dp))

            // Dropdown Bulan
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = pilihBulan,
                    onValueChange = { pilihBulan = it },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Dropdown Arrow Icon",
                        )
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    daftarBulan.forEach { bulanItem ->
                        DropdownMenuItem(
                            text = { Text(bulanItem) },
                            onClick = {
                                pilihBulan = bulanItem
                                expanded = false
                                navController.navigate(Screen.DetailKeuangan.route)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CardKeuangan(viewModel: KeuanganViewModel = viewModel(), selectedMonth: Int? = null) {
    val totalSaldo by viewModel.totalSaldo.observeAsState("0")
    val totalPemasukan by viewModel.totalPemasukan.observeAsState("0")
    val totalPengeluaran by viewModel.totalPengeluaran.observeAsState("0")
    val isLoading by viewModel.isLoading.observeAsState(false)

    LaunchedEffect(selectedMonth) {
        viewModel.setMonthFilter(selectedMonth)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                ) {
                    Row {
                        Canvas(
                            modifier = Modifier
                                .size(height = 37.dp, width = 4.dp)
                                .padding(top = 2.dp)
                        ) {
                            drawRect(color = Color(0xFF660000))
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column {
                            Text(
                                text = "Total Saldo",
                                style = MaterialTheme.typography.headlineLarge
                            )

                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .align(Alignment.CenterHorizontally),
                                    color = Color(0xFFFF8B1E)
                                )
                            } else {
                                Text(
                                    text = "Rp $totalSaldo",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFFFF8B1E),
                                    modifier = Modifier.padding(start = 5.dp)
                                )
                            }
                        }
                    }
                }

                Image(
                    painter = painterResource(R.drawable.gambarkeuangan),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(top = 8.dp)
                        .alpha(0.7f)
                )
            }
            // Bagian tambahan untuk menampilkan pemasukan dan pengeluaran
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .weight(0.6f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Pemasukan",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Rp $totalPemasukan",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF4CAF50) // Warna hijau untuk pemasukan
                    )
                }

                Column {
                    Text(
                        text = "Pengeluaran",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Rp $totalPengeluaran",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFF44336) // Warna merah untuk pengeluaran
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeuanganAdminScreen(
    modifier: Modifier = Modifier,
    viewModel: KeuanganViewModel = viewModel()
) {
    val listKeuangan by viewModel.listKeuangan.observeAsState(emptyList())
    val context = LocalContext.current
    var selectedMonth by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("") }

    // Daftar bulan untuk dropdown
    val months = listOf(
        "", "Januari", "Februari", "Maret", "April",
        "Mei", "Juni", "Juli", "Agustus",
        "September", "Oktober", "November", "Desember"
    )
    // Daftar tipe transaksi
    val types = listOf("", "pemasukan", "pengeluaran")

    LaunchedEffect(Unit) {
        viewModel.fetchAllKeuangan()
    }
    // Filter data berdasarkan bulan dan tipe
    val filteredData = listKeuangan.filter { item ->
        val monthMatches = selectedMonth.isEmpty() ||
                (item["month"] as? Int) == months.indexOf(selectedMonth)
        val typeMatches = selectedType.isEmpty() ||
                item["type"]?.toString().equals(selectedType, ignoreCase = true)
        monthMatches && typeMatches
    }

    LaunchedEffect(selectedMonth) {
        val monthIndex = if (selectedMonth.isEmpty()) null else months.indexOf(selectedMonth)
        viewModel.setMonthFilter(monthIndex)
    }

    CurvedBackground()
    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        val monthIndex = if (selectedMonth.isEmpty()) null else months.indexOf(selectedMonth)
        CardKeuangan(viewModel = viewModel, selectedMonth = monthIndex)
        Spacer(modifier = Modifier.height(16.dp))
        // Filter
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Filter Bulan
            Box(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedMonth,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        label = { Text("Filter Bulan") },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        months.forEach { month ->
                            DropdownMenuItem(
                                text = { Text(month.ifEmpty { "Semua Bulan" }) },
                                onClick = {
                                    selectedMonth = month
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            // Filter Tipe
            Box(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        label = { Text("Filter Tipe") },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        types.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.ifEmpty { "Semua Tipe" }) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        LazyColumn {
            items(filteredData) { item ->
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Nama: ${item["fullName"] ?: "-"}")
                        Text(text = "Nominal: Rp ${item["nominal"] ?: "-"}")
                        Text(text = "Tanggal: ${item["date"] ?: "-"}")

                        item["keterangan"]?.toString()?.takeIf { it.isNotBlank() }?.let { keterangan ->
                            Text(text = "Keterangan: $keterangan")
                        }

                        Text(text = "Tipe: ${item["type"] ?: "-"}")

                        item["imageUrl"]?.toString()?.takeIf { it.isNotBlank() }?.let { url ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Link Bukti Bayar: ")
                                IconButton(
                                    onClick = {
                                        val clipboardManager = androidx.core.content.ContextCompat.getSystemService(
                                            context,
                                            ClipboardManager::class.java
                                        )
                                        clipboardManager?.setPrimaryClip(
                                            ClipData.newPlainText("Image URL", url)
                                        )
                                        Toast.makeText(context, "Link disalin", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Salin link",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun KeuanganScreenPreview() {
    TVBCappsTheme {
        KeuanganScreen(rememberNavController())
    }
}
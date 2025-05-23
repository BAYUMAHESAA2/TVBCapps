package com.tvbc.tvbcapps.ui.theme.screen

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

enum class ViewMode {
    GRID, LIST, TABLE
}

@Composable
fun KeuanganScreen(navController: NavHostController, authViewModel: AuthViewModel = viewModel()) {
    val context = LocalContext.current
    val isUserLoggedIn = authViewModel.isUserLoggedIn()
    val isUserProfileLoading by authViewModel.isUserProfileLoading.collectAsState()
    val viewModel: AuthViewModel = viewModel()

    var viewMode by remember { mutableStateOf(ViewMode.TABLE) }

    fun nextViewMode(current: ViewMode): ViewMode {
        return when (current) {
            ViewMode.LIST -> ViewMode.TABLE
            ViewMode.TABLE -> ViewMode.GRID
            ViewMode.GRID -> ViewMode.LIST
        }
    }

    val shouldShowToast = navController
        .currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Boolean>("upload_success")
        ?.observeAsState()

    LaunchedEffect(shouldShowToast?.value) {
        if (shouldShowToast?.value == true) {
            Toast.makeText(context, "Data iuran berhasil dikirim", Toast.LENGTH_SHORT).show()
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("upload_success")
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopBar(
                navController = navController,
                viewMode = viewMode,
                onViewModeChange = { viewMode = nextViewMode(viewMode) },
                currentRoute = Screen.Keuangan.route
            )
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
        if (isUserLoggedIn && !isUserProfileLoading) {
            ScreenContentKeuangan(
                modifier = Modifier.padding(innerPadding),
                viewMode = viewMode
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenContentKeuangan(
    modifier: Modifier = Modifier,
    viewModel: KeuanganViewModel = viewModel(),
    viewMode: ViewMode = ViewMode.TABLE
) {
    val listKeuangan by viewModel.listKeuangan.observeAsState(emptyList())
    val context = LocalContext.current

    var selectedMonth by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("") }

    val months = listOf(
        "", "Januari", "Februari", "Maret", "April",
        "Mei", "Juni", "Juli", "Agustus",
        "September", "Oktober", "November", "Desember"
    )
    val types = listOf("", "pemasukan", "pengeluaran")

    LaunchedEffect(Unit) {
        viewModel.fetchAllKeuangan()
    }

    val filteredData = listKeuangan.filter { item ->
        val monthMatches =
            selectedMonth.isEmpty() || (item["month"] as? Int) == months.indexOf(selectedMonth)
        val typeMatches = selectedType.isEmpty() || item["type"]?.toString()
            .equals(selectedType, ignoreCase = true)
        monthMatches && typeMatches
    }

    CurvedBackground()
    Column(modifier = modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        val monthIndex = if (selectedMonth.isEmpty()) null else months.indexOf(selectedMonth)
        CardKeuangan(viewModel = viewModel, selectedMonth = monthIndex)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filter Bulan
            Box(modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)) {
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
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
            Box(modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)) {
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
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
        when (viewMode) {
            ViewMode.LIST -> ListViewContent(filteredData, context)
            ViewMode.TABLE -> TableScreenContent(filteredData, context)
            ViewMode.GRID -> GridScreenContent(filteredData)
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

@Composable
fun TableScreenContent(
    filteredData: List<Map<String, Any>>,
    context: Context,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 84.dp)
    ) {
        item {
            // Header tabel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF660000))
            ) {
                Text(
                    text = "No",
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(8.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Nama",
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Nominal",
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tanggal",
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        itemsIndexed(filteredData) { index, item ->
            // Baris data
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.LightGray)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Kolom Nomor
                    Text(
                        text = "${index + 1}",
                        modifier = Modifier
                            .weight(0.5f)
                            .padding(8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )

                    // Kolom Nama
                    Text(
                        text = item["fullName"]?.toString() ?: "-",
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Kolom Nominal
                    Text(
                        text = "Rp ${item["nominal"]?.toString() ?: "-"}",
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = when (item["type"]?.toString()) {
                            "pemasukan" -> Color(0xFF4CAF50) // Hijau untuk pemasukan
                            "pengeluaran" -> Color(0xFFF44336) // Merah untuk pengeluaran
                            else -> Color.Unspecified
                        }
                    )

                    // Kolom Tanggal
                    Text(
                        text = item["date"]?.toString() ?: "-",
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                item["keterangan"]?.toString()?.takeIf { it.isNotBlank() }?.let { keterangan ->
                    Text(
                        text = "Keterangan: $keterangan",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                item["imageUrl"]?.toString()?.takeIf { it.isNotBlank() }?.let { url ->
                    Row(
                        modifier = Modifier.padding(8.dp),
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

@Composable
fun GridScreenContent(
    filteredData: List<Map<String, Any>>,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(filteredData) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nama: ${item["fullName"] ?: "-"}", fontWeight = FontWeight.Bold)
                    Text(
                        "Nominal: Rp ${item["nominal"] ?: "-"}",
                        color = when (item["type"]?.toString()) {
                            "pemasukan" -> Color(0xFF4CAF50)
                            "pengeluaran" -> Color(0xFFF44336)
                            else -> Color.Unspecified
                        }
                    )
                    Text("Tanggal: ${item["date"] ?: "-"}")
                    Text(
                        "Tipe: ${
                            item["type"]?.toString()?.replaceFirstChar { it.uppercase() } ?: "-"
                        }")
                }
            }
        }
    }
}

@Composable
fun ListViewContent(
    filteredData: List<Map<String, Any>>,
    context: Context,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 84.dp)
    ) {
        items(filteredData) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nama: ${item["fullName"] ?: "-"}", fontWeight = FontWeight.Bold)
                    Text(
                        "Nominal: Rp ${item["nominal"] ?: "-"}",
                        color = when (item["type"]?.toString()) {
                            "pemasukan" -> Color(0xFF4CAF50)
                            "pengeluaran" -> Color(0xFFF44336)
                            else -> Color.Unspecified
                        }
                    )
                    Text("Tanggal: ${item["date"] ?: "-"}")
                    Text(
                        "Tipe: ${
                            item["type"]?.toString()?.replaceFirstChar { it.uppercase() } ?: "-"
                        }")

                    item["keterangan"]?.toString()?.takeIf { it.isNotBlank() }?.let { keterangan ->
                        Text(
                            "Keterangan: $keterangan",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    item["imageUrl"]?.toString()?.takeIf { it.isNotBlank() }?.let { url ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Bukti: ",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Lihat Bukti",
                                color = Color.Blue,
                                style = MaterialTheme.typography.bodySmall,
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

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun KeuanganScreenPreview() {
    TVBCappsTheme {
        KeuanganScreen(rememberNavController())
    }
}
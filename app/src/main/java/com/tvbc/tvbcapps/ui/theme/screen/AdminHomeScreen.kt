package com.tvbc.tvbcapps.ui.theme.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.tvbc.tvbcapps.R
import com.tvbc.tvbcapps.component.BottomNavigationBar
import com.tvbc.tvbcapps.component.TopBar
import com.tvbc.tvbcapps.model.AuthViewModel
import com.tvbc.tvbcapps.model.UserModel

@Composable
fun AdminHomeScreen(navController: NavHostController) {
    val viewModel: AuthViewModel = viewModel()

    LaunchedEffect(key1 = true) {
        viewModel.fetchAllUsers()
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
        ScreenContentAdminHome(innerPadding, viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenContentAdminHome(innerPadding: PaddingValues, viewModel: AuthViewModel) {
    val allUsers by viewModel.allUsers.collectAsState()
    val isLoading by viewModel.isLoadingAllUsers.collectAsState()

    // State untuk search dan filter
    var searchQuery by remember { mutableStateOf("") }
    var selectedJurusan by remember { mutableStateOf("Semua") }
    var selectedAngkatan by remember { mutableStateOf("Semua") }
    var isFilterExpanded by remember { mutableStateOf(false) }

    // Ekstrak jurusan dan angkatan yang unik untuk dropdown filter
    val jurusanList = remember(allUsers) {
        listOf("Semua") + allUsers.map { it.jurusan }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    val angkatanList = remember(allUsers) {
        listOf("Semua") + allUsers.map { it.angkatan }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    // Filter data berdasarkan kriteria yang dipilih
    val filteredUsers = remember(allUsers, searchQuery, selectedJurusan, selectedAngkatan) {
        allUsers.filter { user ->
            val matchesSearch = user.fullName.contains(searchQuery, ignoreCase = true) ||
                    user.nim.contains(searchQuery, ignoreCase = true)
            val matchesJurusan = selectedJurusan == "Semua" || user.jurusan == selectedJurusan
            val matchesAngkatan = selectedAngkatan == "Semua" || user.angkatan == selectedAngkatan

            matchesSearch && matchesJurusan && matchesAngkatan
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
    ) {
        Text(
            text = "Daftar Anggota",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            placeholder = { Text("Cari berdasarkan nama atau NIM") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Cari"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Hapus"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp)
        )

        // Filter Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { isFilterExpanded = !isFilterExpanded },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Filter")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isFilterExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isFilterExpanded) "Sembunyikan filter" else "Tampilkan filter"
                    )
                }
            }

            // Info jumlah data yang ditampilkan
            Text(
                text = "${filteredUsers.size} pengguna",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Filter Options (Jurusan dan Angkatan)
        AnimatedVisibility(visible = isFilterExpanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Filter Jurusan
                    Text(
                        text = "Jurusan:",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    // Dropdown untuk Jurusan
                    var isJurusanExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = isJurusanExpanded,
                        onExpandedChange = { isJurusanExpanded = it },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedJurusan,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isJurusanExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = isJurusanExpanded,
                            onDismissRequest = { isJurusanExpanded = false }
                        ) {
                            jurusanList.forEach { jurusan ->
                                DropdownMenuItem(
                                    text = { Text(jurusan) },
                                    onClick = {
                                        selectedJurusan = jurusan
                                        isJurusanExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filter Angkatan
                    Text(
                        text = "Angkatan:",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    // Dropdown untuk Angkatan
                    var isAngkatanExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = isAngkatanExpanded,
                        onExpandedChange = { isAngkatanExpanded = it },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedAngkatan,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isAngkatanExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = isAngkatanExpanded,
                            onDismissRequest = { isAngkatanExpanded = false }
                        ) {
                            angkatanList.forEach { angkatan ->
                                DropdownMenuItem(
                                    text = { Text(angkatan) },
                                    onClick = {
                                        selectedAngkatan = angkatan
                                        isAngkatanExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Reset Filter Button
                    OutlinedButton(
                        onClick = {
                            selectedJurusan = "Semua"
                            selectedAngkatan = "Semua"
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Filter"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset Filter")
                    }
                }
            }
        }

        // Content - User List
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (filteredUsers.isNotEmpty()) {
            LazyColumn {
                items(filteredUsers) { user ->
                    UserCard(user = user)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tidak ada pengguna yang ditemukan",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (searchQuery.isNotEmpty() || selectedJurusan != "Semua" || selectedAngkatan != "Semua") {
                        OutlinedButton(
                            onClick = {
                                searchQuery = ""
                                selectedJurusan = "Semua"
                                selectedAngkatan = "Semua"
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Reset Pencarian & Filter")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserCard(user: UserModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (user.profileImageUrl.isNotEmpty()) {
                    val safeUrl = user.profileImageUrl.replace("http://", "https://")
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
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (user.nim.isNotEmpty()) {
                    Text(
                        text = "NIM: ${user.nim}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (user.jurusan.isNotEmpty()) {
                    Text(
                        text = "Jurusan: ${user.jurusan}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (user.angkatan.isNotEmpty()) {
                    Text(
                        text = "Angkatan: ${user.angkatan}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
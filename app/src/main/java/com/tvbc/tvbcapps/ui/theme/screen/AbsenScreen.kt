package com.tvbc.tvbcapps.ui.theme.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.tvbc.tvbcapps.R
import com.tvbc.tvbcapps.component.BottomNavigationBar
import com.tvbc.tvbcapps.component.TopBar
import com.tvbc.tvbcapps.model.AuthViewModel
import com.tvbc.tvbcapps.ui.theme.TVBCappsTheme

@Composable
fun AbsenScreen(navController: NavHostController) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopBar(navController)
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
            ScreenContentAbsen(
                modifier = Modifier.padding(innerPadding)
            )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenContentAbsen(
    modifier: Modifier = Modifier,
    isPreview: Boolean = false,
    viewModel: AuthViewModel = viewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()

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

    var pilihBulan by remember { mutableStateOf(bulan[0]) }

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Card(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
                .height(80.dp),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4A0000)
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
                        text = userProfile?.fullName ?: "Nama tidak tersedia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = userProfile?.nim ?: "NIM tidak tersedia",
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
                        }
                    )
                }
            }
        }
        RiwayatPresensiCard(hadir = 6, tidakHadir = 2)
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
                    Text(hadir.toString(), style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(Color(0xFF4A0000))
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tidak Hadir", style = MaterialTheme.typography.bodyMedium, color = Color.Black)
                    Text(tidakHadir.toString(), style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun AbsenScreenPreview() {
    TVBCappsTheme {
        AbsenScreen(rememberNavController())
    }
}
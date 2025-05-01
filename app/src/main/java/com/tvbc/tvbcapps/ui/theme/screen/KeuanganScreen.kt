package com.tvbc.tvbcapps.ui.theme.screen

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
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
import com.tvbc.tvbcapps.navigation.Screen
import com.tvbc.tvbcapps.ui.theme.TVBCappsTheme


@Composable
fun KeuanganScreen(navController: NavHostController) {
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
                onClick = { navController.navigate(Screen.FormKeuangan.route)},
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
        }
    ) { innerPadding ->
        ScreenContentKeuangan(
            modifier = Modifier.padding(innerPadding),
            navController
        )
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
fun CardKeuangan() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
        ) {

            // Teks
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
            ) {
                // Teks Total Saldo
                Row {
                    Canvas(
                        modifier = Modifier
                            .size(height = 37.dp, width = 4.dp)
                            .padding(top = 2.dp)
                    ) {
                        drawRect(
                            color = Color(0xFF660000)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Text(
                            "Total Saldo",
                            style = MaterialTheme.typography.headlineLarge
                        )

                        Text(
                            "Rp430.000,00",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFF8B1E),
                            modifier = Modifier
                                .padding(start = 5.dp)
                        )
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
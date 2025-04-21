package com.tvbc.tvbcapps.ui.theme.screen

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.tvbc.tvbcapps.R
import com.tvbc.tvbcapps.component.BottomNavigationBar
import com.tvbc.tvbcapps.component.CurvedBackground
import com.tvbc.tvbcapps.component.TopBar
import com.tvbc.tvbcapps.navigation.Screen
import com.tvbc.tvbcapps.ui.theme.TVBCappsTheme

@Composable
fun MainScreen(navController: NavHostController) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopBar(navController)
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        ScreenContent(
            modifier = Modifier.padding(innerPadding),
            navController
        )
    }
}

@Composable
fun ScreenContent(modifier: Modifier = Modifier, navController: NavHostController) {
    Box(
        modifier = modifier
            .fillMaxSize()

    ) {
        CurvedBackground()
        LazyRow(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(2) { index ->
                Card(
                    modifier = Modifier
                        .width(300.dp)
                        .height(180.dp),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Top
                        ) {
                            Text(
                                text = stringResource(id = R.string.teks_card_latihan),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(80.dp))
                           Button(
                               onClick = {
                                   navController.navigate(Screen.FormAbsen.route)
                               },
                               colors = ButtonDefaults.buttonColors(
                                   containerColor = Color(0xFF660000), // Warna marun sesuai gambar
                                   contentColor = Color.White // Warna teks putih
                               ),

                               shape = RoundedCornerShape(8.dp),

                           ) {
                               Text(
                                   text = "Absen ${index + 1}",
                                   style = MaterialTheme.typography.bodyLarge,
                               )
                           }
                        }
                        Image(
                            painter = painterResource(id = R.drawable.logovollycard),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }

            }
        }
        // Rekap Keuangan
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 200.dp)
        ) {
            Text(
                text = stringResource(id = R.string.teks_rekap_keuangan),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.Black
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(150.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Ini adalah card info tambahan",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black
                    )
                }
            }
            Text(
                text = "data",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
                color = Color.Black
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(3) { index ->
                    Card(
                        modifier = Modifier
                            .width(200.dp)
                            .height(120.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Card ${index + 1}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Black
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
fun MainScreenPreview() {
    TVBCappsTheme {
        MainScreen(rememberNavController())
    }
}
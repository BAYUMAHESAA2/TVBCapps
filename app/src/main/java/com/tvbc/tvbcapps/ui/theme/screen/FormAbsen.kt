package com.tvbc.tvbcapps.ui.theme.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.tvbc.tvbcapps.R
import com.tvbc.tvbcapps.ui.theme.TVBCappsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormAbsenScreen(navController: NavHostController) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.shadow(6.dp),
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
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
                        text = stringResource(R.string.form_absen),
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
        }
    ) { innerPadding ->
        ScreenContentAbsenForm(
            Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun ScreenContentAbsenForm(
    modifier: Modifier = Modifier
){
    //kolom di gunakan untuk membatasi gambar dari top app bar dan agar gambar bisa central berada di tengah"
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gambar form
        Image(
            painter = painterResource(id = R.drawable.formabsen),
            contentDescription = "Ilustrasi Absen",
            modifier = Modifier
                .size(325.dp) // Sesuai gambar
                .padding(vertical = 16.dp)
        )
    }

    }


@Preview(showBackground = true)
@Composable
fun FormAbsenScreenPreview() {
    TVBCappsTheme {
        FormAbsenScreen(rememberNavController())
    }
}
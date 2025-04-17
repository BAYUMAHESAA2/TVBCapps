package com.tvbc.tvbcapps.ui.theme.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.tvbc.tvbcapps.component.CurvedBackground
import com.tvbc.tvbcapps.ui.theme.TVBCappsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfilScreen(navController: NavHostController) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.keluar),//diganti nanti jadi kembali
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Edit Profil",//ganti nanti
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF660000),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScreenContentEditProfil()
        }
    }
}

@Composable
fun ScreenContentEditProfil() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        CurvedBackground()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            Image(
                painter = painterResource(id = R.drawable.logoprofil),
                contentDescription = "Foto Profil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.White, CircleShape)
            )

            Spacer(modifier = Modifier.height(30.dp))

            SimpleTextField(hint = "Nama Lengkap")
            Spacer(modifier = Modifier.height(16.dp))

            SimpleTextField(hint = "NIM")
            Spacer(modifier = Modifier.height(16.dp))

            SimpleTextField(hint = "Jurusan")
            Spacer(modifier = Modifier.height(16.dp))

            SimpleTextField(hint = "Angkatan")
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { /* Save action */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF660000)
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "Simpan",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Composable
fun SimpleTextField(hint: String) {
    // Mendeklarasikan state untuk menyimpan teks yang dimasukkan oleh pengguna
    val textState = remember { mutableStateOf("") }

    TextField(
        value = textState.value,  // Mengikat nilai teks dengan state
        onValueChange = { textState.value = it },  // Mengubah nilai state ketika teks berubah
        placeholder = {
            Text(
                text = hint,
                color = Color.Gray
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = TextFieldDefaults.colors(
            // Mengatur warna latar belakang ketika TextField dalam fokus dan tidak fokus
            focusedContainerColor = Color(0xFFEEEEEE),
            unfocusedContainerColor = Color(0xFFEEEEEE),
            disabledContainerColor = Color(0xFFEEEEEE),

            // Menghilangkan indikator garis bawah saat TextField difokuskan atau tidak
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,

            // Mengatur warna teks saat TextField dalam fokus, tidak fokus, atau dinonaktifkan
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledTextColor = Color.Black,

            // Mengatur warna kursor
            cursorColor = Color.Black
        ),
        // Membatasi TextField untuk hanya menampilkan satu baris teks
        singleLine = true
    )
}

@Preview(showBackground = true)
@Composable
fun EditProfilScreenPreview() {
    TVBCappsTheme {
        EditProfilScreen(rememberNavController())
    }
}

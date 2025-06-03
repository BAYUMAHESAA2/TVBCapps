package com.tvbc.tvbcapps.ui.theme.screen

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.tvbc.tvbcapps.ui.theme.TVBCappsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TentangAplikasiScreen(navController: NavHostController) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.shadow(6.dp),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Tentang Aplikasi",
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

        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                modifier = Modifier
                    .height(120.dp)
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                Column (
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(
                        "Butuh bantuan? Kirim pesan",
                        modifier = Modifier
                            .padding(bottom = 5.dp)
                    )
                    Button(
                        onClick = {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf("gryder1907@gmail.com"))
                                putExtra(Intent.EXTRA_SUBJECT, "Permintaan Bantuan dari Aplikasi TVBC")
                                putExtra(Intent.EXTRA_TEXT, "Halo tim TVBC,\n\nSaya butuh bantuan terkait...")
                            }

                            try {
                                context.startActivity(emailIntent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(Color(0xFF660000)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Kirim pesan"
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "© 2025 JS Team.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier
                            .padding(5.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        TentangAplikasiContent(
            Modifier.padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TentangAplikasiContent(modifier: Modifier = Modifier) {
    var cari by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Judul
        Text(
            "Kami disini membantu Anda dengan apa pun dan segalanya di TVBC App",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus imperdiet, nulla et dictum interdum, nisi lorem egestas odio, vitae scelerisque enim ligula venenatis dolor.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier
                .padding(vertical = 13.dp)
        )

        OutlinedTextField(
            value = cari,
            onValueChange = { cari = it },
            placeholder = { Text("Cari") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, shape = CircleShape, clip = false),
            shape = CircleShape,
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.White,
                cursorColor = Color.Gray
            )
        )
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun TentangAplikasiPreview() {
    TVBCappsTheme {
        TentangAplikasiScreen(rememberNavController())
    }
}

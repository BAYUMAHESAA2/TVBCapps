package com.tvbc.tvbcapps.ui.theme.screen

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.tvbc.tvbcapps.ui.theme.TVBCappsTheme


@Composable
fun KeuanganScreen(navController: NavHostController) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopBar(navController)
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        ScreenContentKeuangan(
            modifier = Modifier.padding(innerPadding)
        )

    }
}

@Composable
fun ScreenContentKeuangan(
    modifier: Modifier = Modifier
){
    CurvedBackground()

    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 25.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.total_saldo),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Rp430.000,00",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Image(
                painter = painterResource(id = R.drawable.gambarkeuangan),
                contentDescription = "Ilustrasi Keuangan",
                modifier = Modifier
                    .size(90.dp),
                contentScale = ContentScale.Fit
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
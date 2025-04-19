package com.tvbc.tvbcapps.ui.theme.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.tvbc.tvbcapps.component.BottomNavigationBar
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
        ScreenContent(
            modifier = Modifier.padding(innerPadding)
        )

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
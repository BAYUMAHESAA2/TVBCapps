package com.tvbc.tvbcapps.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tvbc.tvbcapps.ui.theme.screen.AbsenScreen
import com.tvbc.tvbcapps.ui.theme.screen.EditProfilScreen
import com.tvbc.tvbcapps.ui.theme.screen.KeuanganScreen
import com.tvbc.tvbcapps.ui.theme.screen.MainScreen
import com.tvbc.tvbcapps.ui.theme.screen.NotifikasiScreen
import com.tvbc.tvbcapps.ui.theme.screen.ProfilScreen

@Composable
fun SetupNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            MainScreen(navController)
        }
        composable(route = Screen.Profil.route) {
            ProfilScreen(navController)
        }
        composable(route = Screen.EditProfil.route) {
            EditProfilScreen(navController)
        }
        composable(route = Screen.Notifikasi.route) {
            NotifikasiScreen(navController)
        }
        composable(route =  Screen.Absen.route) {
            AbsenScreen(navController)
        }
        composable(route = Screen.Keuangan.route) {
            KeuanganScreen(navController)
        }
    }

}
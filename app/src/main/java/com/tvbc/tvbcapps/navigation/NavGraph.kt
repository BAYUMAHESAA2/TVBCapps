package com.tvbc.tvbcapps.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tvbc.tvbcapps.model.AuthViewModel
import com.tvbc.tvbcapps.ui.theme.screen.AbsenScreen
import com.tvbc.tvbcapps.ui.theme.screen.AdminHomeScreen
import com.tvbc.tvbcapps.ui.theme.screen.EditProfilScreen
import com.tvbc.tvbcapps.ui.theme.screen.FormAbsenScreen
import com.tvbc.tvbcapps.ui.theme.screen.KeuanganScreen
import com.tvbc.tvbcapps.ui.theme.screen.LandingPageScreen
import com.tvbc.tvbcapps.ui.theme.screen.LoginScreen
import com.tvbc.tvbcapps.ui.theme.screen.MainScreen
import com.tvbc.tvbcapps.ui.theme.screen.NotifikasiScreen
import com.tvbc.tvbcapps.ui.theme.screen.ProfilScreen
import com.tvbc.tvbcapps.ui.theme.screen.RegisterScreen
import com.tvbc.tvbcapps.ui.theme.screen.TentangAplikasiScreen
import com.tvbc.tvbcapps.ui.theme.screen.DetailKeuanganScreen
import com.tvbc.tvbcapps.ui.theme.screen.FormKeuangan

@Composable
fun SetupNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    // State untuk menyimpan status login dan role
    val isUserLoggedIn = authViewModel.isUserLoggedIn()
    val userRole by authViewModel.userRole.collectAsState()
    val isUserProfileLoading by authViewModel.isUserProfileLoading.collectAsState()

    // Perhatikan perubahannya di sini
    LaunchedEffect(isUserLoggedIn, userRole, isUserProfileLoading) {
        if (isUserLoggedIn && !isUserProfileLoading) {
            when (userRole) {
                "admin" -> {
                    navController.navigate(Screen.AdminHomeScreen.route) {
                        popUpTo(Screen.LandingPage.route) { inclusive = true }
                    }
                }
                "user" -> {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.LandingPage.route) { inclusive = true }
                    }
                }
                else -> {
                    // mencegah error apabila role tidak terdeteksi
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.LandingPage.route
    ) {
        composable(route = Screen.LandingPage.route) {
            LandingPageScreen(navController)
        }
        composable(route = Screen.Register.route) {
            RegisterScreen(navController)
        }
        composable(route = Screen.Login.route) {
            LoginScreen(navController)
        }
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
        composable(route = Screen.Absen.route) {
            AbsenScreen(navController)
        }
        composable(route = Screen.Keuangan.route) {
            KeuanganScreen(navController)
        }
        composable(route = Screen.TentangAplikasi.route) {
            TentangAplikasiScreen(navController)
        }
        composable(route = Screen.FormAbsen.route) {
            FormAbsenScreen(navController)
        }
        composable(route = Screen.DetailKeuangan.route) {
            DetailKeuanganScreen(navController)
        }
        composable(route = Screen.FormKeuangan.route) {
            FormKeuangan(navController)
        }
        composable(route = Screen.AdminHomeScreen.route) {
            AdminHomeScreen(navController)
        }
    }
}
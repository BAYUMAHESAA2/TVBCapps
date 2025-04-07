package com.tvbc.tvbcapps.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tvbc.tvbcapps.ui.theme.screen.MainScreen
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
    }

}
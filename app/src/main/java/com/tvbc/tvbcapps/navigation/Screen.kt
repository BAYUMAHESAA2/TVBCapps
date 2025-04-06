package com.tvbc.tvbcapps.navigation

sealed class Screen(val route: String) {
    data object Home: Screen("mainScreen")
}
package com.tvbc.tvbcapps.navigation

sealed class Screen(val route: String) {
    data object LandingPage: Screen("landingPageScreen")
    data object Home: Screen("mainScreen")
    data object Profil: Screen("profilScreen")
    data object EditProfil: Screen("editProfilScreen")
    data object Notifikasi: Screen("notifikasiScreen")
    data object Absen: Screen("absenScreen")
    data object Keuangan: Screen("keuanganScreen")
    data object FormAbsen: Screen("formAbsen")
}
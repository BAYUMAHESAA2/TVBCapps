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
    data object Register: Screen("registerScreen")
    data object Login: Screen("loginScreen")
    data object TentangAplikasi: Screen("tentangAplikasiScreen")
    data object DetailKeuangan: Screen("detailKeuangan")
    data object FormKeuangan: Screen("formKeuangan")
}
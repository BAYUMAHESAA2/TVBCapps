package com.tvbc.tvbcapps.component

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.tvbc.tvbcapps.R
import com.tvbc.tvbcapps.navigation.Screen
import com.tvbc.tvbcapps.model.AuthViewModel

@Composable
fun BottomNavigationBar(navController: NavHostController, authViewModel: AuthViewModel) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val userProfile by authViewModel.userProfile.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()

    val navItems = buildNavItems(userRole)

    NavigationBar(
        modifier = Modifier
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        containerColor = Color.White,
        contentColor = Color.Black
    ) {
        navItems.forEach { item ->
            val selected = currentDestination?.route == item.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) navController.navigate(item.route)
                },
                icon = {
                    // Special handling for the Profile tab
                    if (item.label == "Profil" && userProfile?.profileImageUrl?.isNotEmpty() == true) {
                        val safeUrl = userProfile?.profileImageUrl?.replace("http://", "https://")
                        AsyncImage(
                            model = safeUrl,
                            contentDescription = "Foto Profil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            error = painterResource(id = R.drawable.logoprofil),
                            placeholder = painterResource(id = R.drawable.logoprofil)
                        )
                    } else {
                        // Normal icon handling for other tabs or when profile image is not available
                        Icon(
                            painter = painterResource(id = if (selected) item.activeIconRes else item.iconRes),
                            contentDescription = item.label,
                            modifier = Modifier.size(32.dp),
                            tint = Color.Unspecified
                        )
                    }
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = Color.Unspecified,
                    selectedTextColor = Color(0xFF660000),
                    unselectedIconColor = Color.Unspecified,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

// Kelas untuk navigasi berdasarkan role
data class RoleBasedNavItem(
    val label: String,
    val route: String,
    val iconRes: Int,
    val activeIconRes: Int
)

// Fungsi untuk membuat navigasi berdasarkan role pengguna
fun buildNavItems(role: String?): List<RoleBasedNavItem> {
    return listOf(
        // Beranda - route berbeda berdasarkan role
        RoleBasedNavItem(
            "Beranda",
            if (role == "admin") Screen.AdminHomeScreen.route else Screen.Home.route,
            R.drawable.logohome,
            R.drawable.logohomeaktif
        ),
        // Item navigasi lainnya tetap sama
        RoleBasedNavItem(
            "Absen",
            Screen.Absen.route,
            R.drawable.logoabsen,
            R.drawable.logoabsenaktif
        ),
        RoleBasedNavItem(
            "Keuangan",
            Screen.Keuangan.route,
            R.drawable.logokeuangan,
            R.drawable.logokeuanganaktif
        ),
        RoleBasedNavItem(
            "Profil",
            Screen.Profil.route,
            R.drawable.logoprofil,
            R.drawable.logoprofil
        )
    )
}
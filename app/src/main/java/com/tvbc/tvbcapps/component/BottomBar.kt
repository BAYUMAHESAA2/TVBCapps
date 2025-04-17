package com.tvbc.tvbcapps.component

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.tvbc.tvbcapps.R
import com.tvbc.tvbcapps.navigation.Screen

data class NavItem(
    val screen: Screen,
    val label: String,
    val iconRes: Int,
    val activeIconRes: Int
)

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination

    val navItems = listOf(
        NavItem(Screen.Home, "Beranda", R.drawable.logohome, R.drawable.logohomeaktif),
        NavItem(Screen.EditProfil, "Absen", R.drawable.logoabsen, R.drawable.logoabsen),
        NavItem(Screen.Home, "Keuangan", R.drawable.logokeuangan, R.drawable.logokeuangan),
        NavItem(Screen.Profil, "Profil", R.drawable.logoprofil, R.drawable.logoprofil)
    )

    NavigationBar(
        modifier = Modifier
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        containerColor = Color.White,
        contentColor = Color.Black
    ) {
        navItems.forEach { item ->
            val selected = currentDestination?.route == item.screen.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) navController.navigate(item.screen.route)
                },
                icon = {
                    Icon(
                        painter = painterResource(id = if (selected) item.activeIconRes else item.iconRes),
                        contentDescription = item.label,
                        modifier = Modifier.size(32.dp),
                        tint = Color.Unspecified
                    )
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

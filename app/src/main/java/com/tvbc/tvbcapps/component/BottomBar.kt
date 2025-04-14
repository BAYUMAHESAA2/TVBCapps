package com.tvbc.tvbcapps.component

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tvbc.tvbcapps.R
import com.tvbc.tvbcapps.navigation.Screen

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar(
        modifier = Modifier
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        containerColor = Color.White,
        contentColor = Color.Black
    ) {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Screen.Home.route) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.logohome),
                    contentDescription = "Beranda",
                    modifier = Modifier.size(32.dp),
                    tint = Color.Unspecified
                )
            },
            label = { Text("Beranda") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent,
                selectedIconColor = Color.Unspecified,
                selectedTextColor = Color.Black,
                unselectedIconColor = Color.Unspecified,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* Navigasi ke halaman latihan */ },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.logoabsen),
                    contentDescription = "Absen",
                    modifier = Modifier.size(32.dp),
                    tint = Color.Unspecified
                )
            },
            label = { Text("Absen") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent,
                selectedIconColor = Color.Unspecified,
                selectedTextColor = Color.Black,
                unselectedIconColor = Color.Unspecified,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* Navigasi ke halaman profil */ },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.logokeuangan),
                    contentDescription = "Keuangan",
                    modifier = Modifier.size(32.dp),
                    tint = Color.Unspecified
                )
            },
            label = { Text("Keuangan") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent,
                selectedIconColor = Color.Unspecified,
                selectedTextColor = Color.Black,
                unselectedIconColor = Color.Unspecified,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* Navigasi ke halaman profil */ },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.logotimeline),
                    contentDescription = "Timeline",
                    modifier = Modifier.size(32.dp),
                    tint = Color.Unspecified
                )
            },
            label = { Text("Timeline") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent,
                selectedIconColor = Color.Unspecified,
                selectedTextColor = Color.Black,
                unselectedIconColor = Color.Unspecified,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            selected = true,
            onClick = { navController.navigate(Screen.Profil.route) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.logoprofil),
                    contentDescription = "Profil",
                    modifier = Modifier.size(32.dp),
                    tint = Color.Unspecified
                )
            },
            label = { Text("Profil") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent,
                selectedIconColor = Color.Unspecified,
                selectedTextColor = Color.Black,
                unselectedIconColor = Color.Unspecified,
                unselectedTextColor = Color.Gray
            )
        )
    }
}
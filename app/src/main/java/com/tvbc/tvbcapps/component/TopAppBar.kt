package com.tvbc.tvbcapps.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tvbc.tvbcapps.R
import com.tvbc.tvbcapps.navigation.Screen
import com.tvbc.tvbcapps.ui.theme.screen.ViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavHostController,
    viewMode: ViewMode? = null,
    onViewModeChange: (() -> Unit)? = null,
    currentRoute: String? = null
) {
    TopAppBar(
        title = {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = MaterialTheme.typography.headlineSmall.fontSize)) {
                        append("TVBC")
                    }
                    withStyle(style = SpanStyle(fontSize = MaterialTheme.typography.bodySmall.fontSize)) {
                        append("apps")
                    }
                },
                color = Color.White
            )
        },
        actions = {
            if (currentRoute == Screen.Keuangan.route && viewMode != null && onViewModeChange != null) {
                IconButton(onClick = onViewModeChange) {
                    val iconRes = when (viewMode) {
                        ViewMode.LIST -> R.drawable.view_list
                        ViewMode.TABLE -> R.drawable.table_view
                        ViewMode.GRID -> R.drawable.grid_view
                    }
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = "Change View Mode",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            IconButton(onClick = {
                navController.navigate(Screen.Notifikasi.route)
            }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifikasi",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF660000)
        )
    )
}
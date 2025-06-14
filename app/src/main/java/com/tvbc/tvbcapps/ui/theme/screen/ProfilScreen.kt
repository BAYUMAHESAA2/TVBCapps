package com.tvbc.tvbcapps.ui.theme.screen

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.tvbc.tvbcapps.R
import com.tvbc.tvbcapps.component.BottomNavigationBar
import com.tvbc.tvbcapps.component.TopBar
import com.tvbc.tvbcapps.model.AuthViewModel
import com.tvbc.tvbcapps.navigation.Screen
import com.tvbc.tvbcapps.ui.theme.TVBCappsTheme

@Composable
fun ProfilScreen(navController: NavHostController) {
    val viewModel: AuthViewModel = viewModel()

    LaunchedEffect(Unit) {
        if (!viewModel.isUserLoggedIn()) {
            navController.navigate(Screen.LandingPage.route) {
                popUpTo(Screen.Profil.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopBar(navController)
        },
        bottomBar = {
            BottomNavigationBar(navController, viewModel)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CurvedBackgroundProfil()
            ScreenContentProfil(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun ScreenContentProfil(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: AuthViewModel
) {
    val userProfile by viewModel.userProfile.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        CurvedBackgroundProfil()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image Section (now just display, no edit functionality)
            Box {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                ) {
                    if (userProfile?.profileImageUrl?.isNotEmpty() == true) {
                        val safeUrl = userProfile?.profileImageUrl?.replace("http://", "https://")
                        AsyncImage(
                            model = safeUrl,
                            contentDescription = "Foto Profil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            error = painterResource(id = R.drawable.logoprofil),
                            placeholder = painterResource(id = R.drawable.logoprofil)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.logoprofil),
                            contentDescription = "Foto Profil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User name
            Text(
                text = userProfile?.fullName ?: "Nama tidak tersedia",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Edit Profile Button
            ProfileActionButton(
                icon = Icons.Default.Person,
                text = stringResource(R.string.edit_profil),
                onClick = { navController.navigate(Screen.EditProfil.route) },
                tint = Color(0xFF660000)
            )

            Spacer(modifier = Modifier.height(15.dp))

            Spacer(modifier = Modifier.height(15.dp))

            // Logout Button
            ProfileActionButton(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                text = stringResource(R.string.keluar),
                onClick = {
                    viewModel.logoutUser()
                    navController.navigate(Screen.LandingPage.route) {
                        popUpTo(0)
                    }
                },
                tint = Color.White,
                backgroundColor = Color(0xFF660000)
            )
        }
    }
}

@Composable
fun ProfileActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    tint: Color,
    backgroundColor: Color = Color.White
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = tint
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(30.dp),
                tint = tint
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                fontSize = 20.sp,
                color = tint
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = text,
                modifier = Modifier.size(30.dp),
                tint = tint
            )
        }
    }
}

@Composable
fun CurvedBackgroundProfil() {
    val headerHeight = 160.dp
    val curveHeight = 40.dp
    val density = LocalDensity.current

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
    ) {
        val width = size.width
        val height = size.height
        val curveHeightPx = with(density) { curveHeight.toPx() }

        val backgroundColor = Color(0xFF660000)

        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(width, 0f)
            lineTo(width, height - curveHeightPx)
            quadraticTo(
                width / 2f,
                height - curveHeightPx * 2,
                0f,
                height - curveHeightPx
            )
            close()
        }
        drawPath(path, backgroundColor)
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun ProfilScreenPreview() {
    TVBCappsTheme {
        ProfilScreen(rememberNavController())
    }
}
package com.tvbc.tvbcapps.ui.theme.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tvbc.tvbcapps.R
import com.tvbc.tvbcapps.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val showText = remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (showText.value) 0.6f else 1f,
        animationSpec = tween(durationMillis = 500),
        label = ""
    )

    LaunchedEffect(Unit) {
        delay(2500L)
        showText.value = true
        delay(1000L)
        navController.navigate(Screen.LandingPage.route) {
            popUpTo(Screen.SplashScreen.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF3A0000)),
        contentAlignment = Alignment.Center
    ) {
        if (showText.value) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                    // Hapus horizontalArrangement untuk menghilangkan spasi
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logovolly),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(150.dp)
                            .graphicsLayer {
                                rotationZ = rotationAngle
                                scaleX = scale
                                scaleY = scale
                            }
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("TVBC")
                            }
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            ) {
                                append("Apps")
                            }
                        },
                        color = Color.White,
                        modifier = Modifier.offset(x = (-30).dp)
                    )
                }
            }
        } else {
            Image(
                painter = painterResource(id = R.drawable.logovolly),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(250.dp)
                    .graphicsLayer {
                        rotationZ = rotationAngle
                        scaleX = scale
                        scaleY = scale
                    }
            )
        }
    }
}




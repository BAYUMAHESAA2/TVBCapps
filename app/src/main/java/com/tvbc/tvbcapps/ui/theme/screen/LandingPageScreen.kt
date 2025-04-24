package com.tvbc.tvbcapps.ui.theme.screen

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.tvbc.tvbcapps.R
import com.tvbc.tvbcapps.navigation.Screen
import com.tvbc.tvbcapps.ui.theme.TVBCappsTheme

@Composable
fun LandingPageScreen(navController : NavHostController){
    Scaffold{ innerPadding ->
        LandingPageContent(
            modifier = Modifier.padding(innerPadding),
            navController
        )
    }
}

@Composable
fun LandingPageContent(modifier: Modifier = Modifier, navController: NavHostController){
    CurvedBackgroundLanding()
    Column (
        modifier = modifier
            .fillMaxSize()
            .padding(top = 65.dp)
            .padding(start = 16.dp, end = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        Image(
            painter = painterResource(R.drawable.landingpage),
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            contentDescription = "Gambar Landing Page"
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            "Welcome",
            fontSize = MaterialTheme.typography.displayLarge.fontSize,
            color = Color(0xFF660000),
            fontWeight = FontWeight.Bold
        )

        Text(
            "We Came to Learn",
            fontSize = MaterialTheme.typography.titleLarge.fontSize,
            color = Color(0xFF646464)
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(width = if (index == 0) 24.dp else 17.dp, height = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (index == 0) Color(0xFF000000) else Color(0xFF660000))
                )
                Spacer(modifier = Modifier.width(3.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {navController.navigate(Screen.Register.route)},
                shape = CircleShape,
                modifier = Modifier
                    .size(60.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF660000))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun CurvedBackgroundLanding() {
    val headerHeight = 450.dp
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
fun LandingPageScreenPreview() {
    TVBCappsTheme {
        LandingPageScreen(rememberNavController())
    }
}
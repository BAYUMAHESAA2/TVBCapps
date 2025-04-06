package com.tvbc.tvbcapps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tvbc.tvbcapps.navigation.SetupNavGraph
import com.tvbc.tvbcapps.ui.theme.TVBCappsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TVBCappsTheme {
                SetupNavGraph()
            }
        }
    }
}
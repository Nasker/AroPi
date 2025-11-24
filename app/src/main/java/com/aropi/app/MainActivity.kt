package com.aropi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.aropi.app.ui.MainScreen
import com.aropi.app.ui.SplashScreen
import com.aropi.app.ui.theme.AropiTheme
import kotlinx.coroutines.delay

/**
 * Main activity for the AAC app.
 * Shows splash screen for 2 seconds, then main app.
 * Uses Jetpack Compose for a modern, declarative UI.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AropiTheme {
                var showSplash by remember { mutableStateOf(true) }

                // Show splash for 2 seconds
                LaunchedEffect(Unit) {
                    delay(2000)
                    showSplash = false
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showSplash) {
                        SplashScreen()
                    } else {
                        MainScreen()
                    }
                }
            }
        }
    }
}
package com.aropi.app

import android.os.Bundle
import android.util.Log
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
        Log.d("AroPi", "MainActivity onCreate started")
        
        setContent {
            Log.d("AroPi", "setContent called")
            
            AropiTheme {
                Log.d("AroPi", "AropiTheme started")
                
                var showSplash by remember { mutableStateOf(true) }
                Log.d("AroPi", "showSplash state created: $showSplash")

                // Show splash for 2 seconds
                LaunchedEffect(Unit) {
                    Log.d("AroPi", "LaunchedEffect started, waiting 2 seconds...")
                    delay(2000)
                    Log.d("AroPi", "Delay finished, hiding splash")
                    showSplash = false
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showSplash) {
                        Log.d("AroPi", "Showing SplashScreen")
                        SplashScreen()
                    } else {
                        Log.d("AroPi", "Showing MainScreen")
                        MainScreen()
                    }
                }
            }
        }
        
        Log.d("AroPi", "MainActivity onCreate finished")
    }
}
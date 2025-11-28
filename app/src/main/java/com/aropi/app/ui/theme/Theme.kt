package com.aropi.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Kid-friendly light theme - warm, cozy, playful colors
private val LightColorScheme = lightColorScheme(
    // Warm coral/peach primary - friendly and inviting
    primary = Color(0xFF4CAF50),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFCF5C7),
    onPrimaryContainer = Color(0xFF4CAF50),
    
    // Soft sky blue secondary - calming and cheerful
    secondary = Color(0xFF4ECDC4),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFCF5C7),
    onSecondaryContainer = Color(0xFF1A5551),
    
    // Sunny yellow tertiary - bright and happy
    tertiary = Color(0xFFFFD93D),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFFFCF5C7),
    onTertiaryContainer = Color(0xFF6B5E00),
    
    // Gentle error colors
    error = Color(0xFFE74C3C),
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFF8B1A1A),
    
    // Soft cream background - easy on the eyes
    background = Color(0xFFFFFDF7),
    onBackground = Color(0xFF4CAF50),
    
    // Clean white surfaces with warmth
    surface = Color(0xFFFFFFFE),
    onSurface = Color(0xFF4CAF50),
    
    // Soft lavender variant - gentle contrast
    surfaceVariant = Color(0xFFF5F0FF),
    onSurfaceVariant = Color(0xFF5A5A5A),
    
    // Soft outline
    outline = Color(0xFFBDBDBD)
)

// Dark theme colors
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99)
)

@Composable
fun AropiTheme(
    darkTheme: Boolean = false, // Always use light theme for kids
    content: @Composable () -> Unit
) {
    // Always use light color scheme for kid-friendly experience
    val colorScheme = LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primaryContainer.toArgb()
            // Always use light status bar icons for better visibility
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

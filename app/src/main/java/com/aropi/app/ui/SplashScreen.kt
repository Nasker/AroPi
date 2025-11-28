package com.aropi.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/**
 * Splash screen that displays "AroPi" with kid-friendly font and colors.
 * Shows for 2 seconds before transitioning to main app.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun SplashScreen() {
    val fredokaGoogleFont = remember { GoogleFont("Fredoka One") }
    val fredokaProvider: GoogleFont.Provider = remember {
        GoogleFont.Provider(
            providerAuthority = "com.google.android.gms.fonts",
            providerPackage = "com.google.android.gms",
            certificates = R.array.com_google_android_gms_fonts_certs
        )
    }
    val fredokaFontFamily = remember {
        FontFamily(
            Font(googleFont = fredokaGoogleFont, fontProvider = fredokaProvider)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AroPi",
            style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = fredokaFontFamily,
                fontSize = 72.sp
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center
        )
    }
}

package com.aropi.app.model

import java.util.Locale

/**
 * Application settings data model.
 */
data class AppSettings(
    val language: AppLanguage = AppLanguage.SPANISH,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val autoSpeak: Boolean = true,
    val showLabels: Boolean = true
)

/**
 * Supported languages in the app.
 */
enum class AppLanguage(val displayName: String, val locale: Locale) {
    SPANISH("Español", Locale("es", "ES")),
    CATALAN("Català", Locale("ca", "ES"));
    
    companion object {
        fun fromLocale(locale: Locale): AppLanguage {
            return values().find { it.locale.language == locale.language } ?: SPANISH
        }
    }
}

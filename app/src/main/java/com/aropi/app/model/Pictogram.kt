package com.aropi.app.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

/**
 * Represents a pictogram with an icon and multilingual labels.
 * Used to build phrases in the AAC app.
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Pictogram(
    val id: String,
    val labels: Map<AppLanguage, String>,  // Language-to-label mapping
    val iconRes: Int,
    val grammarType: String = ""
) {
    val color: PictogramColor
        get() = when (grammarType) {
            "pronoun" -> PictogramColor.YELLOW
            "verb" -> PictogramColor.GREEN
            "adjective" -> PictogramColor.BLUE
            "noun" -> PictogramColor.ORANGE
            "shortcut" -> PictogramColor.PURPLE
            else -> PictogramColor.UNKNOWN
        }

    /**
     * Get the label for the specified language.
     * Falls back to Spanish if the requested language is not available.
     */
    fun getLabel(language: AppLanguage): String {
        return labels[language] ?: labels[AppLanguage.SPANISH] ?: ""
    }
}

package com.aropi.app.model

import kotlinx.serialization.Serializable

/**
 * Represents a pictogram with an icon and multilingual labels.
 * Used to build phrases in the AAC app.
 */
@Serializable
data class Pictogram(
    val id: String,
    val labels: Map<AppLanguage, String>,  // Language-to-label mapping
    val iconRes: Int,
    val grammarType: String = ""
) {
    /**
     * Get the label for the specified language.
     * Falls back to Spanish if the requested language is not available.
     */
    fun getLabel(language: AppLanguage): String {
        return labels[language] ?: labels[AppLanguage.SPANISH] ?: ""
    }
}

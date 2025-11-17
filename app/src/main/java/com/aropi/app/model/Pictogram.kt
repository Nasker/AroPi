package com.aropi.app.model

/**
 * Represents a pictogram with an icon and bilingual labels.
 * Used to build phrases in the AAC app.
 */
data class Pictogram(
    val id: String,
    val labelEs: String,  // Spanish label
    val labelCa: String,  // Catalan label
    val iconRes: Int
) {
    /**
     * Get the label for the specified language.
     */
    fun getLabel(language: AppLanguage): String {
        return when (language) {
            AppLanguage.SPANISH -> labelEs
            AppLanguage.CATALAN -> labelCa
        }
    }
}

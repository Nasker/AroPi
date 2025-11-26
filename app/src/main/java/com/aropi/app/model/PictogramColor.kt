package com.aropi.app.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

/**
 * Represents the background color of a pictogram, determined by its grammar type.
 */
@Serializable
enum class PictogramColor(val color: Color) {
    YELLOW(Color(0xFFF4FA14)),   // For pronouns/subjects
    GREEN(Color(0xFF4EFF58)),    // For verbs/actions
    BLUE(Color(0xFF4C8EFF)),     // For adverbs/modifiers/adjectives
    ORANGE(Color(0xFFFF9E19)),  // For nouns
    PURPLE(Color(0xFFCE93D8)),  // For shortcuts to phrases
    UNKNOWN(Color(0xFFE0E0E0))         // Default/fallback color
}

package com.aropi.app.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

/**
 * Represents the background color of a pictogram, determined by its grammar type.
 */
@Serializable
enum class PictogramColor(val color: Color) {
    YELLOW(Color(0xFFFFF000)),   // For pronouns/subjects - brighter yellow
    GREEN(Color(0xFF00FF40)),    // For verbs/actions - more vibrant green
    BLUE(Color(0xFF03A9F4)),     // For adverbs/modifiers/adjectives - richer blue
    ORANGE(Color(0xFFFF9800)),   // For nouns - more saturated orange
    PURPLE(Color(0xFF9153FF)),   // For shortcuts to phrases - brighter purple
    UNKNOWN(Color(0xFFE0E0E0))   // Default/fallback color
}

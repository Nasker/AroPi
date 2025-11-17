package com.aropi.app.model

/**
 * Represents a pictogram with an icon and label.
 * Used to build phrases in the AAC app.
 */
data class Pictogram(
    val id: String,
    val label: String,
    val iconRes: Int
)

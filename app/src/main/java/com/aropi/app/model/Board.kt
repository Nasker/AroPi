package com.aropi.app.model

import kotlinx.serialization.Serializable

/**
 * Represents a pictogram board - a custom layout of selected pictograms.
 * Users can create multiple boards for different contexts (home, school, food, etc.)
 */
@Serializable
data class Board(
    val id: String,
    val name: String,
    val pictogramIds: List<String>,  // References to pictograms in catalog
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
) {
    companion object {
        const val DEFAULT_BOARD_ID = "default"
        
        /**
         * Create a default board with all pictograms from catalog
         */
        fun createDefault(catalog: PictogramCatalog): Board {
            val allPictogramIds = catalog.categories.values.flatten().map { it.id }
            return Board(
                id = DEFAULT_BOARD_ID,
                name = "Tots els pictogrames",
                pictogramIds = allPictogramIds
            )
        }
    }
}

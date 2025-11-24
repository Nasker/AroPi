package com.aropi.app.model

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Represents a catalog of pictograms organized by categories.
 * Each category (e.g., subjects, actions, places) contains a list of related pictograms.
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class PictogramCatalog(
    val categories: Map<String, List<Pictogram>>
) {
    /**
     * Get all pictograms from all categories as a flat list.
     */
    fun getAllPictograms(): List<Pictogram> {
        return categories.values.flatten()
    }
    
    /**
     * Get pictograms for a specific category.
     */
    fun getPictogramsByCategory(category: String): List<Pictogram> {
        return categories[category] ?: emptyList()
    }
    
    /**
     * Get all category names.
     */
    fun getCategoryNames(): List<String> {
        return categories.keys.toList()
    }
    
    /**
     * Find a pictogram by its ID across all categories.
     */
    fun findPictogramById(id: String): Pictogram? {
        return getAllPictograms().find { it.id == id }
    }
    
    companion object {
        private const val CATALOG_FILENAME = "pictogram_catalog.json"
        private val json = Json { 
            prettyPrint = true
            ignoreUnknownKeys = true
        }
        
        /**
         * Save the catalog to internal storage.
         */
        fun save(context: Context, catalog: PictogramCatalog) {
            val file = File(context.filesDir, CATALOG_FILENAME)
            val jsonString = json.encodeToString(catalog)
            file.writeText(jsonString)
        }
        
        /**
         * Load the catalog from internal storage.
         * Falls back to loading from assets if file doesn't exist.
         */
        fun load(context: Context): PictogramCatalog {
            val file = File(context.filesDir, CATALOG_FILENAME)
            
            return if (file.exists()) {
                val jsonString = file.readText()
                json.decodeFromString<PictogramCatalog>(jsonString)
            } else {
                loadFromAssets(context)
            }
        }
        
        /**
         * Load the catalog from assets (default data).
         */
        fun loadFromAssets(context: Context): PictogramCatalog {
            return try {
                val jsonString = context.assets.open(CATALOG_FILENAME).bufferedReader().use { it.readText() }
                json.decodeFromString<PictogramCatalog>(jsonString)
            } catch (e: Exception) {
                // Return empty catalog if file doesn't exist
                PictogramCatalog(emptyMap())
            }
        }
    }
}

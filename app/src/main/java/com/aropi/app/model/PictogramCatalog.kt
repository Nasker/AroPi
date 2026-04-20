package com.aropi.app.model

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.aropi.app.logic.bundle.BundleManager
import com.aropi.app.logic.bundle.BundlePictogramRepository
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
        private const val TAG = "PictogramCatalog"

        /**
         * Custom overlay persisted in internal storage. Only contains
         * user-added or OBF-imported pictograms (the offline bundle
         * is the source of truth for everything else and is read-only).
         */
        private const val CUSTOM_OVERLAY_FILENAME = "pictogram_custom.json"

        /** Legacy catalog file from pre-bundle versions — migrated then deleted. */
        private const val LEGACY_CATALOG_FILENAME = "pictogram_catalog.json"

        private val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

        /**
         * Persist the catalog.
         *
         * Only the *custom* slice of [catalog] is written: pictograms
         * whose id matches one from the offline bundle are dropped
         * because they are immutable and reloaded from SQLite on next
         * read. Empty categories are pruned.
         */
        fun save(context: Context, catalog: PictogramCatalog) {
            val bundleIds = try {
                loadBundlePictograms(context).map { it.id }.toSet()
            } catch (e: Exception) {
                Log.w(TAG, "Cannot read bundle ids; saving full catalog as custom", e)
                emptySet()
            }

            val customOnly = catalog.categories
                .mapValues { (_, pictos) -> pictos.filter { it.id !in bundleIds } }
                .filterValues { it.isNotEmpty() }

            val file = File(context.filesDir, CUSTOM_OVERLAY_FILENAME)
            file.writeText(json.encodeToString(PictogramCatalog(customOnly)))

            // Remove the legacy catalog file if present; the bundle now
            // supplies the default pictograms.
            File(context.filesDir, LEGACY_CATALOG_FILENAME).takeIf { it.exists() }?.delete()
        }

        /**
         * Load the merged catalog = bundle pictograms ∪ custom overlay.
         *
         * On the very first call after upgrading from a pre-bundle
         * build, any pictogram in the legacy catalog that isn't in the
         * bundle is migrated into the custom overlay so the user
         * doesn't lose their additions.
         */
        fun load(context: Context): PictogramCatalog {
            val bundle = try {
                loadBundlePictograms(context)
            } catch (e: Exception) {
                Log.w(TAG, "Bundle unavailable; loading from overlay + legacy only", e)
                emptyList()
            }
            val bundleIds = bundle.map { it.id }.toSet()

            val custom = loadCustomOverlay(context)
                ?: migrateLegacyCatalog(context, bundleIds)
                ?: PictogramCatalog(emptyMap())

            return mergeBundleWithCustom(bundle, custom)
        }

        /**
         * Load the catalog from assets. Retained for backwards
         * compatibility; now simply returns the bundle pictograms
         * grouped by category. Returns an empty catalog if the bundle
         * has not been extracted yet.
         */
        fun loadFromAssets(context: Context): PictogramCatalog {
            return try {
                PictogramCatalog(
                    BundlePictogramRepository(BundleManager.get(context))
                        .getGroupedByCategory()
                )
            } catch (e: Exception) {
                Log.w(TAG, "loadFromAssets failed", e)
                PictogramCatalog(emptyMap())
            }
        }

        private fun loadBundlePictograms(context: Context): List<Pictogram> {
            val manager = BundleManager.get(context)
            manager.ensureInitialized()
            return BundlePictogramRepository(manager).getAllPictograms()
        }

        private fun loadCustomOverlay(context: Context): PictogramCatalog? {
            val file = File(context.filesDir, CUSTOM_OVERLAY_FILENAME)
            if (!file.exists()) return null
            return try {
                json.decodeFromString<PictogramCatalog>(file.readText())
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse custom overlay", e)
                null
            }
        }

        private fun migrateLegacyCatalog(
            context: Context,
            bundleIds: Set<String>
        ): PictogramCatalog? {
            val legacy = File(context.filesDir, LEGACY_CATALOG_FILENAME)
            if (!legacy.exists()) return null
            return try {
                val full = json.decodeFromString<PictogramCatalog>(legacy.readText())
                val customOnly = full.categories
                    .mapValues { (_, list) -> list.filter { it.id !in bundleIds } }
                    .filterValues { it.isNotEmpty() }
                val overlay = PictogramCatalog(customOnly)
                File(context.filesDir, CUSTOM_OVERLAY_FILENAME)
                    .writeText(json.encodeToString(overlay))
                legacy.delete()
                Log.i(TAG, "Migrated legacy catalog to custom overlay (${customOnly.size} categories)")
                overlay
            } catch (e: Exception) {
                Log.w(TAG, "Legacy catalog migration failed", e)
                null
            }
        }

        private fun mergeBundleWithCustom(
            bundle: List<Pictogram>,
            custom: PictogramCatalog
        ): PictogramCatalog {
            val merged = linkedMapOf<String, MutableList<Pictogram>>()

            // Group bundle pictograms by grammar-type-derived category.
            for (p in bundle) {
                val cat = when (p.grammarType) {
                    "pronoun" -> BundlePictogramRepository.CAT_SUBJECTS
                    "verb" -> BundlePictogramRepository.CAT_ACTIONS
                    "noun" -> BundlePictogramRepository.CAT_OBJECTS
                    "adjective", "adverb" -> BundlePictogramRepository.CAT_MODIFIERS
                    else -> BundlePictogramRepository.CAT_MODIFIERS
                }
                merged.getOrPut(cat) { mutableListOf() }.add(p)
            }

            // Append custom pictograms under their stored category, skipping
            // any that collide with a bundle id (bundle wins — it's the
            // grammatically-supported version).
            val bundleIds = bundle.map { it.id }.toSet()
            for ((category, pictos) in custom.categories) {
                val target = merged.getOrPut(category) { mutableListOf() }
                pictos.forEach { p ->
                    if (p.id !in bundleIds) target.add(p)
                }
            }

            return PictogramCatalog(merged.mapValues { it.value.toList() })
        }
    }
}

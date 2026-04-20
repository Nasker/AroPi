package com.aropi.app.logic.obf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.aropi.app.R
import com.aropi.app.model.AppLanguage
import com.aropi.app.model.Board
import com.aropi.app.model.Pictogram
import com.aropi.app.model.obf.*
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipFile

/**
 * Imports Open Board Format (OBF/OBZ) files to AroPi boards.
 */
class OBFImporter(private val context: Context) {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    private val imagesDir: File
        get() = File(context.filesDir, "imported_images").apply {
            if (!exists()) mkdirs()
        }
    
    /**
     * Import from OBF file (single board JSON).
     */
    fun importFromOBF(obfFile: File): Pair<Board, List<Pictogram>> {
        return try {
            val jsonString = obfFile.readText()
            val obfBoard = json.decodeFromString<OBFBoard>(jsonString)
            convertFromOBFBoard(obfBoard)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse OBF file: ${e.message}", e)
        }
    }
    
    /**
     * Import from OBZ file (ZIP package).
     */
    fun importFromOBZ(obzFile: File): Pair<Board, List<Pictogram>> {
        return try {
            ZipFile(obzFile).use { zip ->
                // Read manifest
                val manifestEntry = zip.getEntry("manifest.json")
                val manifest = if (manifestEntry != null) {
                    val manifestJson = zip.getInputStream(manifestEntry).bufferedReader().use { it.readText() }
                    json.decodeFromString<OBZManifest>(manifestJson)
                } else {
                    null
                }
            
            // Find the board file
            val boardEntry = if (manifest != null) {
                // Try to get path from boards mapping first
                val rootBoardPath = manifest.paths.boards?.get(manifest.root)
                    ?: manifest.root  // Fallback: use root as direct filename
                zip.getEntry(rootBoardPath)
            } else {
                // Fallback: find first .obf file
                zip.entries().asSequence().firstOrNull { it.name.endsWith(".obf") }
            }
            
            if (boardEntry == null) {
                throw IllegalArgumentException("No board file found in OBZ package")
            }
            
            // Parse board
            val boardJson = zip.getInputStream(boardEntry).bufferedReader().use { it.readText() }
            val obfBoard = json.decodeFromString<OBFBoard>(boardJson)
            
            // Extract images from ZIP
            val imageMap = mutableMapOf<String, File>()
            zip.entries().asSequence()
                .filter { it.name.startsWith("images/") && !it.isDirectory }
                .forEach { entry ->
                    // Extract image ID from filename (e.g., "images/image_1_4241_xxx.png" -> "1_4241_xxx")
                    val filename = entry.name.substringAfterLast("/")
                    val imageId = filename.substringBeforeLast(".")
                        .removePrefix("image_")  // Remove "image_" prefix if present
                    
                    val extension = filename.substringAfterLast(".", "png")
                    val imageFile = File(imagesDir, "${UUID.randomUUID()}.$extension")
                    zip.getInputStream(entry).use { input ->
                        FileOutputStream(imageFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    imageMap[imageId] = imageFile
                }
            
                // Convert with extracted images
                return convertFromOBFBoard(obfBoard, imageMap)
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to import OBZ file: ${e.message}", e)
        }
    }
    
    /**
     * Convert OBF Board to AroPi Board and Pictograms.
     */
    private fun convertFromOBFBoard(
        obfBoard: OBFBoard,
        imageMap: Map<String, File> = emptyMap()
    ): Pair<Board, List<Pictogram>> {
        val pictograms = mutableListOf<Pictogram>()
        val pictogramIds = mutableListOf<String>()
        
        // Sort buttons by grid order
        val orderedButtons = getOrderedButtons(obfBoard)
        
        orderedButtons.forEach { button ->
            val pictogramId = button.imageId ?: "pic_${button.id}"
            
            // Extract labels from translations
            val labels = extractLabels(button)
            
            // Get grammar type (from extension or infer from color)
            val grammarType = button.extAropiGrammarType 
                ?: inferGrammarTypeFromColor(button.backgroundColor)
            
            // Get image path
            val customImagePath = getImagePath(button, obfBoard.images, imageMap)
            
            // Use a placeholder icon resource (will be overridden by custom image if available)
            val iconRes = R.drawable.ic_launcher_foreground
            
            val pictogram = Pictogram(
                id = pictogramId,
                labels = labels,
                iconRes = iconRes,
                grammarType = grammarType,
                customImagePath = customImagePath
            )
            
            pictograms.add(pictogram)
            pictogramIds.add(pictogramId)
        }
        
        // Create AroPi board
        val board = Board(
            id = obfBoard.id,
            name = obfBoard.name ?: "Imported Board",
            pictogramIds = pictogramIds,
            createdAt = System.currentTimeMillis(),
            lastModified = System.currentTimeMillis()
        )
        
        return Pair(board, pictograms)
    }
    
    /**
     * Get buttons in grid order.
     */
    private fun getOrderedButtons(obfBoard: OBFBoard): List<OBFButton> {
        val buttonMap = obfBoard.buttons.associateBy { it.id }
        val orderedButtons = mutableListOf<OBFButton>()
        
        obfBoard.grid.order.forEach { row ->
            row.forEach { buttonId ->
                if (buttonId != null) {
                    buttonMap[buttonId]?.let { orderedButtons.add(it) }
                }
            }
        }
        
        return orderedButtons
    }
    
    /**
     * Extract multilingual labels from button.
     */
    private fun extractLabels(button: OBFButton): Map<AppLanguage, String> {
        val labels = mutableMapOf<AppLanguage, String>()
        
        // Default label
        labels[AppLanguage.SPANISH] = button.label
        
        // Extract from translations
        button.translations?.forEach { (locale, translation) ->
            val language = when (locale) {
                "es", "es-ES" -> AppLanguage.SPANISH
                "ca", "ca-ES" -> AppLanguage.CATALAN
                "en", "en-US", "en-GB" -> AppLanguage.ENGLISH
                else -> null
            }
            
            if (language != null && translation.label != null) {
                labels[language] = translation.label
            }
        }
        
        // Ensure at least Spanish exists
        if (!labels.containsKey(AppLanguage.SPANISH)) {
            labels[AppLanguage.SPANISH] = button.label
        }
        
        return labels
    }
    
    /**
     * Infer grammar type from Fitzgerald color scheme.
     */
    private fun inferGrammarTypeFromColor(backgroundColor: String?): String {
        if (backgroundColor == null) return ""
        
        return when {
            backgroundColor.contains("255, 255, 0") || 
            backgroundColor.contains("#ffff00", ignoreCase = true) -> "pronoun"
            
            backgroundColor.contains("0, 255, 0") || 
            backgroundColor.contains("#00ff00", ignoreCase = true) -> "verb"
            
            backgroundColor.contains("255, 165, 0") || 
            backgroundColor.contains("255, 140, 0") ||
            backgroundColor.contains("#ffa500", ignoreCase = true) -> "noun"
            
            backgroundColor.contains("0, 0, 255") || 
            backgroundColor.contains("#0000ff", ignoreCase = true) -> "adjective"
            
            backgroundColor.contains("128, 0, 128") || 
            backgroundColor.contains("purple", ignoreCase = true) -> "shortcut"
            
            else -> ""
        }
    }
    
    /**
     * Get image path for a button.
     */
    private fun getImagePath(
        button: OBFButton,
        images: List<OBFImage>,
        imageMap: Map<String, File>
    ): String? {
        val imageId = button.imageId ?: return null
        
        // Check if image was extracted from ZIP
        imageMap[imageId]?.let { return it.absolutePath }
        
        // Find image in OBF images list
        val obfImage = images.find { it.id == imageId } ?: return null
        
        // Extract from data URL
        if (obfImage.data != null) {
            return saveImageFromDataUrl(obfImage.data, imageId)
        }
        
        // TODO: Could download from URL if needed
        // if (obfImage.url != null) { ... }
        
        return null
    }
    
    /**
     * Save image from base64 data URL to file.
     */
    private fun saveImageFromDataUrl(dataUrl: String, imageId: String): String? {
        return try {
            val base64Data = dataUrl.substringAfter("base64,")
            val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            
            val imageFile = File(imagesDir, "${imageId}_${UUID.randomUUID()}.png")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            imageFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

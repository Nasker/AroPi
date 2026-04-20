package com.aropi.app.logic.obf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.aropi.app.model.AppLanguage
import com.aropi.app.model.Board
import com.aropi.app.model.Pictogram
import com.aropi.app.model.PictogramCatalog
import com.aropi.app.model.obf.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.ceil
import kotlin.math.sqrt

/**
 * Exports AroPi boards to Open Board Format (OBF/OBZ).
 */
class OBFExporter(private val context: Context) {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = false
    }
    
    /**
     * Export a board to OBF format (single board JSON file).
     */
    fun exportToOBF(
        board: Board,
        pictograms: List<Pictogram>,
        outputFile: File
    ) {
        val obfBoard = convertToOBFBoard(board, pictograms)
        val jsonString = json.encodeToString(obfBoard)
        outputFile.writeText(jsonString)
    }
    
    /**
     * Export a board to OBZ format (ZIP package with board + images).
     */
    fun exportToOBZ(
        board: Board,
        pictograms: List<Pictogram>,
        outputFile: File
    ) {
        val obfBoard = convertToOBFBoard(board, pictograms)
        
        ZipOutputStream(FileOutputStream(outputFile)).use { zip ->
            // Add manifest.json
            val manifest = OBZManifest(
                format = "open-board-0.1",
                root = board.id,
                paths = OBZPaths(
                    boards = mapOf(board.id to "boards/${board.id}.obf")
                )
            )
            zip.putNextEntry(ZipEntry("manifest.json"))
            zip.write(json.encodeToString(manifest).toByteArray())
            zip.closeEntry()
            
            // Add board file
            zip.putNextEntry(ZipEntry("boards/${board.id}.obf"))
            zip.write(json.encodeToString(obfBoard).toByteArray())
            zip.closeEntry()
            
            // Add images
            obfBoard.images.forEach { image ->
                if (image.data != null) {
                    // Extract base64 data and save as file
                    val imageData = extractBase64Data(image.data)
                    if (imageData != null) {
                        zip.putNextEntry(ZipEntry("images/${image.id}.png"))
                        zip.write(imageData)
                        zip.closeEntry()
                    }
                }
            }
        }
    }
    
    /**
     * Convert AroPi Board to OBF Board structure.
     */
    private fun convertToOBFBoard(
        board: Board,
        pictograms: List<Pictogram>
    ): OBFBoard {
        val buttons = mutableListOf<OBFButton>()
        val images = mutableListOf<OBFImage>()
        
        pictograms.forEachIndexed { index, pictogram ->
            val buttonId = index + 1
            
            // Create button
            val button = OBFButton(
                id = buttonId,
                label = pictogram.getLabel(AppLanguage.SPANISH),
                vocalization = pictogram.getLabel(AppLanguage.SPANISH),
                imageId = pictogram.id,
                backgroundColor = getColorForGrammarType(pictogram.grammarType),
                borderColor = "rgb(170, 170, 170)",
                extAropiGrammarType = pictogram.grammarType,
                translations = buildTranslations(pictogram)
            )
            buttons.add(button)
            
            // Create image
            val imageData = getImageData(pictogram)
            if (imageData != null) {
                val image = OBFImage(
                    id = pictogram.id,
                    data = imageData,
                    contentType = "image/png",
                    license = OBFLicense(type = "private")
                )
                images.add(image)
            }
        }
        
        // Create grid layout (simple grid, 6 columns)
        val grid = createGridLayout(pictograms.size)
        
        return OBFBoard(
            id = board.id,
            name = board.name,
            locale = "es",
            defaultLayout = "landscape",
            license = OBFLicense(
                type = "private",
                authorName = "AroPi User"
            ),
            buttons = buttons,
            grid = grid,
            images = images,
            extAropiGrammarLayout = true
        )
    }
    
    /**
     * Build translations map for multiple languages.
     */
    private fun buildTranslations(pictogram: Pictogram): Map<String, OBFTranslation> {
        val translations = mutableMapOf<String, OBFTranslation>()
        
        pictogram.labels.forEach { (language, label) ->
            val locale = when (language) {
                AppLanguage.SPANISH -> "es"
                AppLanguage.CATALAN -> "ca"
                AppLanguage.ENGLISH -> "en"
            }
            translations[locale] = OBFTranslation(
                label = label,
                vocalization = label
            )
        }
        
        return translations
    }
    
    /**
     * Get Fitzgerald color for grammar type.
     */
    private fun getColorForGrammarType(grammarType: String): String {
        return when (grammarType) {
            "pronoun" -> "rgb(255, 255, 0)"     // Yellow
            "verb" -> "rgb(0, 255, 0)"          // Green
            "noun" -> "rgb(255, 165, 0)"        // Orange
            "adjective" -> "rgb(0, 0, 255)"     // Blue
            "shortcut" -> "rgb(128, 0, 128)"    // Purple
            else -> "rgb(255, 255, 255)"        // White
        }
    }
    
    /**
     * Create a simple grid layout (6 columns, auto rows).
     */
    private fun createGridLayout(buttonCount: Int): OBFGrid {
        val columns = 6
        val rows = ceil(buttonCount.toDouble() / columns).toInt().coerceAtLeast(1)
        
        val order = mutableListOf<List<Int?>>()
        var buttonId = 1
        
        for (row in 0 until rows) {
            val rowList = mutableListOf<Int?>()
            for (col in 0 until columns) {
                if (buttonId <= buttonCount) {
                    rowList.add(buttonId)
                    buttonId++
                } else {
                    rowList.add(null)
                }
            }
            order.add(rowList)
        }
        
        return OBFGrid(
            rows = rows,
            columns = columns,
            order = order
        )
    }
    
    /**
     * Get image data as base64 data URL.
     */
    private fun getImageData(pictogram: Pictogram): String? {
        return try {
            // Try custom image first
            if (pictogram.customImagePath != null) {
                val file = File(pictogram.customImagePath)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    return bitmapToBase64DataUrl(bitmap)
                }
            }
            
            // Fall back to drawable resource
            val bitmap = BitmapFactory.decodeResource(context.resources, pictogram.iconRes)
            bitmapToBase64DataUrl(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Convert bitmap to base64 data URL.
     */
    private fun bitmapToBase64DataUrl(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
        return "data:image/png;base64,$base64"
    }
    
    /**
     * Extract base64 data from data URL.
     */
    private fun extractBase64Data(dataUrl: String): ByteArray? {
        return try {
            val base64Data = dataUrl.substringAfter("base64,")
            Base64.decode(base64Data, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

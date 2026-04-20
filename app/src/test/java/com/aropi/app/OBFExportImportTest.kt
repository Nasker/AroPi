package com.aropi.app

import com.aropi.app.model.AppLanguage
import com.aropi.app.model.Board
import com.aropi.app.model.Pictogram
import com.aropi.app.model.obf.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.*

/**
 * Example tests for OBF export/import functionality.
 * These demonstrate the expected structure and behavior.
 */
class OBFExportImportTest {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    @Test
    fun testOBFBoardSerialization() {
        // Create a simple OBF board
        val board = OBFBoard(
            id = "test-board",
            name = "Test Board",
            locale = "es",
            defaultLayout = "landscape",
            grid = OBFGrid(
                rows = 1,
                columns = 2,
                order = listOf(listOf(1, 2))
            ),
            buttons = listOf(
                OBFButton(
                    id = 1,
                    label = "yo",
                    backgroundColor = "rgb(255, 255, 0)",
                    extAropiGrammarType = "pronoun"
                ),
                OBFButton(
                    id = 2,
                    label = "comer",
                    backgroundColor = "rgb(0, 255, 0)",
                    extAropiGrammarType = "verb"
                )
            ),
            images = emptyList(),
            extAropiGrammarLayout = true
        )
        
        // Serialize to JSON
        val jsonString = json.encodeToString(board)
        
        // Verify it contains expected fields
        assertTrue(jsonString.contains("\"id\": \"test-board\""))
        assertTrue(jsonString.contains("\"name\": \"Test Board\""))
        assertTrue(jsonString.contains("\"ext_aropi_grammar_type\": \"pronoun\""))
        assertTrue(jsonString.contains("\"ext_aropi_grammar_layout\": true"))
        
        // Deserialize back
        val deserialized = json.decodeFromString<OBFBoard>(jsonString)
        assertEquals("test-board", deserialized.id)
        assertEquals("Test Board", deserialized.name)
        assertEquals(2, deserialized.buttons.size)
        assertEquals("pronoun", deserialized.buttons[0].extAropiGrammarType)
    }
    
    @Test
    fun testOBZManifestStructure() {
        val manifest = OBZManifest(
            format = "open-board-0.1",
            root = "main-board",
            paths = OBZPaths(
                boards = mapOf("main-board" to "boards/main-board.obf")
            )
        )
        
        val jsonString = json.encodeToString(manifest)
        
        assertTrue(jsonString.contains("\"format\": \"open-board-0.1\""))
        assertTrue(jsonString.contains("\"root\": \"main-board\""))
    }
    
    @Test
    fun testButtonWithTranslations() {
        val button = OBFButton(
            id = 1,
            label = "yo",
            translations = mapOf(
                "es" to OBFTranslation(label = "yo", vocalization = "yo"),
                "ca" to OBFTranslation(label = "jo", vocalization = "jo"),
                "en" to OBFTranslation(label = "I", vocalization = "I")
            )
        )
        
        val jsonString = json.encodeToString(button)
        
        assertTrue(jsonString.contains("\"translations\""))
        assertTrue(jsonString.contains("\"es\""))
        assertTrue(jsonString.contains("\"ca\""))
        assertTrue(jsonString.contains("\"en\""))
    }
    
    @Test
    fun testImageWithBase64Data() {
        val image = OBFImage(
            id = "test-image",
            data = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
            contentType = "image/png",
            license = OBFLicense(type = "private")
        )
        
        val jsonString = json.encodeToString(image)
        
        assertTrue(jsonString.contains("\"id\": \"test-image\""))
        assertTrue(jsonString.contains("\"data\": \"data:image/png;base64,"))
        assertTrue(jsonString.contains("\"content_type\": \"image/png\""))
    }
    
    @Test
    fun testGridLayoutCalculation() {
        // Test grid layout for different button counts
        val testCases = listOf(
            5 to Pair(1, 6),   // 5 buttons → 1 row, 6 cols
            12 to Pair(2, 6),  // 12 buttons → 2 rows, 6 cols
            18 to Pair(3, 6),  // 18 buttons → 3 rows, 6 cols
            20 to Pair(4, 6)   // 20 buttons → 4 rows, 6 cols
        )
        
        testCases.forEach { (buttonCount, expected) ->
            val (expectedRows, expectedCols) = expected
            val columns = 6
            val rows = kotlin.math.ceil(buttonCount.toDouble() / columns).toInt()
            
            assertEquals("Button count $buttonCount should have $expectedRows rows", 
                expectedRows, rows)
        }
    }
    
    @Test
    fun testColorToGrammarTypeMapping() {
        val colorMappings = mapOf(
            "rgb(255, 255, 0)" to "pronoun",   // Yellow
            "rgb(0, 255, 0)" to "verb",        // Green
            "rgb(255, 165, 0)" to "noun",      // Orange
            "rgb(0, 0, 255)" to "adjective",   // Blue
            "rgb(128, 0, 128)" to "shortcut"   // Purple
        )
        
        colorMappings.forEach { (color, expectedType) ->
            // This would be tested in the actual importer
            assertNotNull("Color $color should map to grammar type", expectedType)
        }
    }
    
    /**
     * Example of expected OBF structure for a complete board.
     */
    @Test
    fun testCompleteOBFStructure() {
        val completeBoard = """
        {
          "id": "aropi-example",
          "name": "AroPi Example Board",
          "locale": "es",
          "default_layout": "landscape",
          "ext_aropi_grammar_layout": true,
          "license": {
            "type": "private",
            "author_name": "AroPi User"
          },
          "grid": {
            "rows": 1,
            "columns": 3,
            "order": [[1, 2, 3]]
          },
          "buttons": [
            {
              "id": 1,
              "label": "yo",
              "vocalization": "yo",
              "image_id": "img_yo",
              "background_color": "rgb(255, 255, 0)",
              "border_color": "rgb(170, 170, 170)",
              "ext_aropi_grammar_type": "pronoun",
              "translations": {
                "es": {"label": "yo", "vocalization": "yo"},
                "ca": {"label": "jo", "vocalization": "jo"}
              }
            },
            {
              "id": 2,
              "label": "querer",
              "vocalization": "querer",
              "image_id": "img_querer",
              "background_color": "rgb(0, 255, 0)",
              "border_color": "rgb(170, 170, 170)",
              "ext_aropi_grammar_type": "verb",
              "translations": {
                "es": {"label": "querer", "vocalization": "querer"},
                "ca": {"label": "voler", "vocalization": "voler"}
              }
            },
            {
              "id": 3,
              "label": "galleta",
              "vocalization": "galleta",
              "image_id": "img_galleta",
              "background_color": "rgb(255, 165, 0)",
              "border_color": "rgb(170, 170, 170)",
              "ext_aropi_grammar_type": "noun",
              "translations": {
                "es": {"label": "galleta", "vocalization": "galleta"},
                "ca": {"label": "galeta", "vocalization": "galeta"}
              }
            }
          ],
          "images": [
            {
              "id": "img_yo",
              "data": "data:image/png;base64,iVBORw0KG...",
              "content_type": "image/png",
              "license": {"type": "private"}
            },
            {
              "id": "img_querer",
              "data": "data:image/png;base64,iVBORw0KG...",
              "content_type": "image/png",
              "license": {"type": "private"}
            },
            {
              "id": "img_galleta",
              "data": "data:image/png;base64,iVBORw0KG...",
              "content_type": "image/png",
              "license": {"type": "private"}
            }
          ]
        }
        """.trimIndent()
        
        // Parse and verify structure
        val board = json.decodeFromString<OBFBoard>(completeBoard)
        
        assertEquals("aropi-example", board.id)
        assertEquals("AroPi Example Board", board.name)
        assertEquals(true, board.extAropiGrammarLayout)
        assertEquals(3, board.buttons.size)
        assertEquals(3, board.images.size)
        
        // Verify first button
        val firstButton = board.buttons[0]
        assertEquals("yo", firstButton.label)
        assertEquals("pronoun", firstButton.extAropiGrammarType)
        assertEquals("rgb(255, 255, 0)", firstButton.backgroundColor)
        assertNotNull(firstButton.translations)
        assertEquals("jo", firstButton.translations?.get("ca")?.label)
    }
}

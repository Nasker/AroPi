package com.aropi.app

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aropi.app.logic.obf.ImportResult
import com.aropi.app.logic.obf.OBFManager
import com.aropi.app.model.PictogramCatalog
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Instrumented test for OBF/OBZ import functionality.
 * Tests with the real communikate-20.obz file from assets.
 */
@RunWith(AndroidJUnit4::class)
class OBFImportTest {
    
    private lateinit var context: Context
    private lateinit var obfManager: OBFManager
    
    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        obfManager = OBFManager(context)
    }
    
    @Test
    fun testImportCommunikate20() {
        // Copy the OBZ file from assets to cache
        val obzFile = File(context.cacheDir, "test_communikate.obz")
        context.assets.open("communikate-20.obz").use { input ->
            obzFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        // Import the file
        val result = obfManager.importBoard(obzFile)
        
        // Verify it succeeded
        assertTrue("Import should succeed", result is ImportResult.Success)
        
        if (result is ImportResult.Success) {
            val (board, pictograms) = result
            
            // Verify board properties
            assertNotNull("Board should not be null", board)
            assertEquals("Board ID should match", "1_235", board.id)
            assertEquals("Board name should match", "CommuniKate Top Page", board.name)
            
            // Verify pictograms were imported
            assertTrue("Should have imported pictograms", pictograms.isNotEmpty())
            assertTrue("Should have at least 10 pictograms", pictograms.size >= 10)
            
            // Verify first pictogram has required fields
            val firstPictogram = pictograms.first()
            assertNotNull("Pictogram ID should not be null", firstPictogram.id)
            assertNotNull("Pictogram labels should not be null", firstPictogram.labels)
            assertTrue("Pictogram should have at least one label", firstPictogram.labels.isNotEmpty())
            
            println("✓ Successfully imported board: ${board.name}")
            println("✓ Imported ${pictograms.size} pictograms")
            println("✓ First pictogram: ${firstPictogram.labels.values.first()}")
        }
        
        // Cleanup
        obzFile.delete()
    }
    
    @Test
    fun testSaveImportedBoard() {
        // Copy the OBZ file from assets to cache
        val obzFile = File(context.cacheDir, "test_communikate2.obz")
        context.assets.open("communikate-20.obz").use { input ->
            obzFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        // Import the file
        val result = obfManager.importBoard(obzFile)
        assertTrue("Import should succeed", result is ImportResult.Success)
        
        if (result is ImportResult.Success) {
            val (board, pictograms) = result
            val catalog = PictogramCatalog.load(context)
            
            // Save the imported board
            val updatedCatalog = obfManager.saveImportedBoard(board, pictograms, catalog)
            
            // Verify catalog was updated
            assertNotNull("Updated catalog should not be null", updatedCatalog)
            
            // Verify imported category exists
            val importedPictograms = updatedCatalog.getPictogramsByCategory("imported")
            assertTrue("Should have imported pictograms in catalog", importedPictograms.isNotEmpty())
            
            println("✓ Successfully saved ${pictograms.size} pictograms to catalog")
        }
        
        // Cleanup
        obzFile.delete()
    }
}

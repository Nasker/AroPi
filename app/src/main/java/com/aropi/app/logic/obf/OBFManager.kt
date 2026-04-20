package com.aropi.app.logic.obf

import android.content.Context
import com.aropi.app.logic.BoardManager
import com.aropi.app.model.Board
import com.aropi.app.model.Pictogram
import com.aropi.app.model.PictogramCatalog
import java.io.File

/**
 * Manages OBF/OBZ import and export operations.
 */
class OBFManager(private val context: Context) {
    
    private val exporter = OBFExporter(context)
    private val importer = OBFImporter(context)
    private val boardManager = BoardManager(context)
    
    /**
     * Export a board to OBF format.
     * 
     * @param board The board to export
     * @param catalog The pictogram catalog to resolve pictograms
     * @param outputFile The output file (should end with .obf)
     */
    fun exportBoardToOBF(
        board: Board,
        catalog: PictogramCatalog,
        outputFile: File
    ) {
        val pictograms = boardManager.getBoardPictograms(board, catalog)
        exporter.exportToOBF(board, pictograms, outputFile)
    }
    
    /**
     * Export a board to OBZ format (recommended for sharing).
     * 
     * @param board The board to export
     * @param catalog The pictogram catalog to resolve pictograms
     * @param outputFile The output file (should end with .obz)
     */
    fun exportBoardToOBZ(
        board: Board,
        catalog: PictogramCatalog,
        outputFile: File
    ) {
        val pictograms = boardManager.getBoardPictograms(board, catalog)
        exporter.exportToOBZ(board, pictograms, outputFile)
    }
    
    /**
     * Import a board from OBF file.
     * 
     * @param obfFile The OBF file to import
     * @return Pair of imported Board and its Pictograms
     */
    fun importBoardFromOBF(obfFile: File): ImportResult {
        return try {
            val (board, pictograms) = importer.importFromOBF(obfFile)
            ImportResult.Success(board, pictograms)
        } catch (e: Exception) {
            ImportResult.Error(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Import a board from OBZ file.
     * 
     * @param obzFile The OBZ file to import
     * @return Pair of imported Board and its Pictograms
     */
    fun importBoardFromOBZ(obzFile: File): ImportResult {
        return try {
            val (board, pictograms) = importer.importFromOBZ(obzFile)
            ImportResult.Success(board, pictograms)
        } catch (e: Exception) {
            ImportResult.Error(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Import a board from file (auto-detect format).
     * 
     * @param file The file to import (.obf or .obz)
     * @return Import result
     */
    fun importBoard(file: File): ImportResult {
        return when (file.extension.lowercase()) {
            "obf" -> importBoardFromOBF(file)
            "obz" -> importBoardFromOBZ(file)
            else -> ImportResult.Error("Unsupported file format: ${file.extension}")
        }
    }
    
    /**
     * Save imported board and pictograms to the app.
     * 
     * @param board The board to save
     * @param pictograms The pictograms to add to catalog
     * @param catalog The current pictogram catalog
     * @return Updated catalog with new pictograms
     */
    fun saveImportedBoard(
        board: Board,
        pictograms: List<Pictogram>,
        catalog: PictogramCatalog
    ): PictogramCatalog {
        // Save the board
        boardManager.saveBoard(board)
        
        // Add pictograms to catalog under "imported" category
        val currentCategories = catalog.categories.toMutableMap()
        val importedCategory = currentCategories.getOrDefault("imported", emptyList()).toMutableList()
        
        // Add new pictograms (avoid duplicates by ID)
        val existingIds = catalog.getAllPictograms().map { it.id }.toSet()
        val newPictograms = pictograms.filter { it.id !in existingIds }
        importedCategory.addAll(newPictograms)
        
        currentCategories["imported"] = importedCategory
        
        val updatedCatalog = PictogramCatalog(currentCategories)
        PictogramCatalog.save(context, updatedCatalog)
        
        return updatedCatalog
    }
}

/**
 * Result of an import operation.
 */
sealed class ImportResult {
    data class Success(val board: Board, val pictograms: List<Pictogram>) : ImportResult()
    data class Error(val message: String) : ImportResult()
}

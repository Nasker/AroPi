package com.aropi.app.logic

import android.content.Context
import com.aropi.app.model.Board
import com.aropi.app.model.Pictogram
import com.aropi.app.model.PictogramCatalog
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Manages pictogram boards - loading, saving, and listing user-created boards.
 */
class BoardManager(private val context: Context) {
    
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    private val boardsDir: File
        get() = File(context.filesDir, "boards").apply {
            if (!exists()) mkdirs()
        }
    
    private val prefsFile: File
        get() = File(context.filesDir, "board_prefs.json")
    
    /**
     * Get list of all boards
     */
    fun listBoards(): List<Board> {
        val boards = mutableListOf<Board>()
        
        boardsDir.listFiles()?.forEach { file ->
            if (file.extension == "json") {
                try {
                    val jsonString = file.readText()
                    val board = json.decodeFromString<Board>(jsonString)
                    boards.add(board)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        // Sort by last modified, most recent first
        return boards.sortedByDescending { it.lastModified }
    }
    
    /**
     * Load a specific board by ID
     */
    fun loadBoard(boardId: String): Board? {
        val file = File(boardsDir, "$boardId.json")
        return if (file.exists()) {
            try {
                val jsonString = file.readText()
                json.decodeFromString<Board>(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }
    
    /**
     * Save a board
     */
    fun saveBoard(board: Board) {
        val file = File(boardsDir, "${board.id}.json")
        val jsonString = json.encodeToString(board)
        file.writeText(jsonString)
    }
    
    /**
     * Delete a board
     */
    fun deleteBoard(boardId: String): Boolean {
        val file = File(boardsDir, "$boardId.json")
        return file.delete()
    }
    
    /**
     * Get the active board ID
     */
    fun getActiveBoardId(): String {
        return if (prefsFile.exists()) {
            try {
                prefsFile.readText()
            } catch (e: Exception) {
                Board.DEFAULT_BOARD_ID
            }
        } else {
            Board.DEFAULT_BOARD_ID
        }
    }
    
    /**
     * Set the active board ID
     */
    fun setActiveBoardId(boardId: String) {
        prefsFile.writeText(boardId)
    }
    
    /**
     * Get pictograms for a board by resolving IDs from catalog
     */
    fun getBoardPictograms(board: Board, catalog: PictogramCatalog): List<Pictogram> {
        val allPictograms = catalog.categories.values.flatten()
        val pictogramMap = allPictograms.associateBy { it.id }
        
        return board.pictogramIds.mapNotNull { id ->
            pictogramMap[id]
        }
    }
    
    /**
     * Initialize with default board if no boards exist
     */
    fun ensureDefaultBoard(catalog: PictogramCatalog) {
        if (listBoards().isEmpty()) {
            val defaultBoard = Board.createDefault(catalog)
            saveBoard(defaultBoard)
            setActiveBoardId(Board.DEFAULT_BOARD_ID)
        }
    }
}

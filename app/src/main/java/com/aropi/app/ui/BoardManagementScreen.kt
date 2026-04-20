package com.aropi.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.aropi.app.logic.BoardManager
import com.aropi.app.logic.obf.ImportResult
import com.aropi.app.logic.obf.OBFManager
import com.aropi.app.model.Board
import com.aropi.app.model.PictogramCatalog
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen for managing pictogram boards - list, create, edit, delete.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardManagementScreen(
    onNavigateBack: () -> Unit,
    onCreateBoard: () -> Unit,
    onEditBoard: (Board) -> Unit
) {
    val context = LocalContext.current
    val boardManager = remember { BoardManager(context) }
    val obfManager = remember { OBFManager(context) }
    var boards by remember { mutableStateOf(boardManager.listBoards()) }
    var activeBoardId by remember { mutableStateOf(boardManager.getActiveBoardId()) }
    var showDeleteDialog by remember { mutableStateOf<Board?>(null) }
    var showExportDialog by remember { mutableStateOf<Board?>(null) }
    var showImportMenu by remember { mutableStateOf(false) }
    var importMessage by remember { mutableStateOf<String?>(null) }
    var catalog by remember { mutableStateOf(PictogramCatalog.load(context)) }
    
    // Refresh boards list
    fun refreshBoards() {
        boards = boardManager.listBoards()
        activeBoardId = boardManager.getActiveBoardId()
        catalog = PictogramCatalog.load(context)
    }
    
    // File picker for importing OBF/OBZ files
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // Copy file to temp location
                val inputStream = context.contentResolver.openInputStream(uri)
                val tempFile = File(context.cacheDir, "import_temp.obz")
                inputStream?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                // Import the file
                when (val result = obfManager.importBoard(tempFile)) {
                    is ImportResult.Success -> {
                        val updatedCatalog = obfManager.saveImportedBoard(
                            result.board,
                            result.pictograms,
                            catalog
                        )
                        catalog = updatedCatalog
                        refreshBoards()
                        importMessage = "Tauler importat: ${result.board.name}"
                    }
                    is ImportResult.Error -> {
                        importMessage = "Error: ${result.message}"
                    }
                }
                
                tempFile.delete()
            } catch (e: Exception) {
                importMessage = "Error important: ${e.message}"
            }
        }
    }
    
    // Export launcher for saving OBZ files
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        uri?.let { outputUri ->
            showExportDialog?.let { board ->
                try {
                    // Export to temp file
                    val tempFile = File(context.cacheDir, "${board.id}.obz")
                    obfManager.exportBoardToOBZ(board, catalog, tempFile)
                    
                    // Copy to selected location
                    context.contentResolver.openOutputStream(outputUri)?.use { output ->
                        tempFile.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                    
                    tempFile.delete()
                    importMessage = "Tauler exportat correctament"
                } catch (e: Exception) {
                    importMessage = "Error exportant: ${e.message}"
                }
            }
        }
        showExportDialog = null
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Taulers") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Tornar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { showImportMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Més opcions"
                        )
                    }
                    DropdownMenu(
                        expanded = showImportMenu,
                        onDismissRequest = { showImportMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Importar tauler (OBF/OBZ)") },
                            onClick = {
                                showImportMenu = false
                                importLauncher.launch("*/*")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateBoard,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear tauler"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Import/Export message
            importMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = { importMessage = null }) {
                            Text("OK")
                        }
                    }
                }
            }
            if (boards.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "No hi ha taulers",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Crea el teu primer tauler",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(boards) { board ->
                        BoardListItem(
                            board = board,
                            isActive = board.id == activeBoardId,
                            onSetActive = {
                                boardManager.setActiveBoardId(board.id)
                                refreshBoards()
                            },
                            onEdit = { onEditBoard(board) },
                            onDelete = { showDeleteDialog = board },
                            onExport = { showExportDialog = board }
                        )
                    }
                }
            }
        }
    }
    
    // Export dialog
    showExportDialog?.let { board ->
        AlertDialog(
            onDismissRequest = { showExportDialog = null },
            title = { Text("Exportar tauler") },
            text = { Text("Exportar \"${board.name}\" en format OBZ (Open Board Format)?\n\nAquest format és compatible amb altres aplicacions AAC com AsTeRICS Grid, CoughDrop, etc.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        exportLauncher.launch("${board.name}.obz")
                    }
                ) {
                    Text("Exportar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = null }) {
                    Text("Cancel·lar")
                }
            }
        )
    }
    
    // Delete confirmation dialog
    showDeleteDialog?.let { board ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar tauler") },
            text = { Text("Estàs segur que vols eliminar \"${board.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        boardManager.deleteBoard(board.id)
                        // If deleting active board, switch to first available
                        if (board.id == activeBoardId) {
                            val remaining = boardManager.listBoards()
                            if (remaining.isNotEmpty()) {
                                boardManager.setActiveBoardId(remaining.first().id)
                            }
                        }
                        refreshBoards()
                        showDeleteDialog = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel·lar")
                }
            }
        )
    }
}

@Composable
private fun BoardListItem(
    board: Board,
    isActive: Boolean,
    onSetActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = board.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${board.pictogramIds.size} pictogrames",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Modificat: ${dateFormat.format(Date(board.lastModified))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onExport) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Exportar"
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar"
                        )
                    }
                    if (board.id != Board.DEFAULT_BOARD_ID) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            if (!isActive) {
                Button(
                    onClick = onSetActive,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Utilitzar aquest tauler")
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Tauler actiu",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

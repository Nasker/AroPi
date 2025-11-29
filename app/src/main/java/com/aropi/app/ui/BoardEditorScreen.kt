package com.aropi.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aropi.app.logic.BoardManager
import com.aropi.app.model.AppLanguage
import com.aropi.app.model.Board
import com.aropi.app.model.Pictogram
import com.aropi.app.model.PictogramCatalog
import com.aropi.app.ui.components.PictogramCard

/**
 * Screen for creating or editing a pictogram board.
 * Allows user to select pictograms from catalog to add to board.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardEditorScreen(
    existingBoard: Board? = null,
    onNavigateBack: () -> Unit,
    onBoardSaved: () -> Unit
) {
    val context = LocalContext.current
    val boardManager = remember { BoardManager(context) }
    val catalog = remember { PictogramCatalog.load(context) }
    val allPictograms = remember { catalog.categories.values.flatten() }
    
    var boardName by remember { mutableStateOf(existingBoard?.name ?: "") }
    var selectedPictogramIds by remember { 
        mutableStateOf(existingBoard?.pictogramIds?.toSet() ?: emptySet()) 
    }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val isEditing = existingBoard != null
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Tauler" else "Nou Tauler") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Tornar"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            when {
                                boardName.isBlank() -> {
                                    showError = true
                                    errorMessage = "Si us plau, introdueix un nom per al tauler"
                                }
                                selectedPictogramIds.isEmpty() -> {
                                    showError = true
                                    errorMessage = "Si us plau, selecciona almenys un pictograma"
                                }
                                else -> {
                                    val board = Board(
                                        id = existingBoard?.id ?: "board_${System.currentTimeMillis()}",
                                        name = boardName,
                                        pictogramIds = selectedPictogramIds.toList(),
                                        createdAt = existingBoard?.createdAt ?: System.currentTimeMillis(),
                                        lastModified = System.currentTimeMillis()
                                    )
                                    boardManager.saveBoard(board)
                                    onBoardSaved()
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Desar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Board name input
            OutlinedTextField(
                value = boardName,
                onValueChange = { boardName = it },
                label = { Text("Nom del tauler") },
                placeholder = { Text("Ex: Casa, Escola, Menjar...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Selection counter
            Text(
                "${selectedPictogramIds.size} pictogrames seleccionats",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Error message
            if (showError) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Pictogram selection grid
            Text(
                "Selecciona pictogrames",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(12),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(allPictograms) { pictogram ->
                    val isSelected = selectedPictogramIds.contains(pictogram.id)
                    
                    Box(
                        modifier = Modifier.aspectRatio(1f)
                    ) {
                        PictogramCard(
                            pictogram = pictogram,
                            currentLanguage = AppLanguage.CATALAN,
                            showLabel = true,
                            size = null,
                            onClick = {
                                selectedPictogramIds = if (isSelected) {
                                    selectedPictogramIds - pictogram.id
                                } else {
                                    selectedPictogramIds + pictogram.id
                                }
                            }
                        )
                        
                        // Selection indicator overlay
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxSize(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        3.dp,
                                        MaterialTheme.colorScheme.primary
                                    )
                                ) {}
                            }
                        }
                    }
                }
            }
        }
    }
}

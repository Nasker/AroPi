package com.aropi.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aropi.app.model.AppLanguage
import com.aropi.app.model.Pictogram
import com.aropi.app.model.PictogramCatalog

/**
 * Screen for managing pictograms - view, edit, and delete.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePictogramsScreen(
    onNavigateBack: () -> Unit,
    onAddPictogram: () -> Unit,
    onEditPictogram: (Pictogram, String) -> Unit, // pictogram and category
    onCatalogChanged: () -> Unit = {}
) {
    val context = LocalContext.current
    var catalogVersion by remember { mutableStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pictogramToDelete by remember { mutableStateOf<Pair<Pictogram, String>?>(null) }
    
    val catalog = remember(catalogVersion) {
        PictogramCatalog.load(context)
    }
    
    // Flatten catalog with category info
    val pictogramsWithCategory = remember(catalog) {
        catalog.categories.flatMap { (category, pictograms) ->
            pictograms.map { pictogram -> Triple(pictogram, category, catalog) }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Pictogrames") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Tornar"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAddPictogram) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Afegir"
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pictogramsWithCategory) { (pictogram, category, _) ->
                PictogramGridItem(
                    pictogram = pictogram,
                    category = category,
                    onEdit = { onEditPictogram(pictogram, category) },
                    onDelete = {
                        pictogramToDelete = Pair(pictogram, category)
                        showDeleteDialog = true
                    }
                )
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog && pictogramToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Pictograma") },
            text = { 
                Text("Estàs segur que vols eliminar '${pictogramToDelete!!.first.getLabel(AppLanguage.CATALAN)}'?") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val (pictogram, category) = pictogramToDelete!!
                        val catalog = PictogramCatalog.load(context)
                        val updatedCategories = catalog.categories.toMutableMap()
                        val categoryPictograms = updatedCategories[category]?.toMutableList() ?: mutableListOf()
                        categoryPictograms.remove(pictogram)
                        updatedCategories[category] = categoryPictograms
                        
                        val updatedCatalog = PictogramCatalog(updatedCategories)
                        PictogramCatalog.save(context, updatedCatalog)
                        
                        catalogVersion++
                        onCatalogChanged() // Notify parent
                        showDeleteDialog = false
                        pictogramToDelete = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel·lar")
                }
            }
        )
    }
}

@Composable
private fun PictogramGridItem(
    pictogram: Pictogram,
    category: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = pictogram.color.color
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Pictogram card as background
            com.aropi.app.ui.components.PictogramCard(
                pictogram = pictogram,
                currentLanguage = AppLanguage.CATALAN,
                showLabel = true,
                size = null,
                onClick = null
            )
            
            // Action buttons overlay at top
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

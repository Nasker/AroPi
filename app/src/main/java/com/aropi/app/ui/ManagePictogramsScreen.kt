package com.aropi.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pictogramsWithCategory) { (pictogram, category, _) ->
                PictogramListItem(
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
private fun PictogramListItem(
    pictogram: Pictogram,
    category: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = pictogram.color.color.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = pictogram.getLabel(AppLanguage.CATALAN),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ES: ${pictogram.getLabel(AppLanguage.SPANISH)} | EN: ${pictogram.getLabel(AppLanguage.ENGLISH)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = pictogram.grammarType,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
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
}

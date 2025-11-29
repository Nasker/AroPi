package com.aropi.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import androidx.compose.foundation.Image
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.aropi.app.R
import com.aropi.app.logic.PictogramImageManager
import com.aropi.app.model.AppLanguage
import com.aropi.app.model.Pictogram
import com.aropi.app.model.PictogramCatalog

/**
 * Screen for adding or editing a pictogram in the catalog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPictogramScreen(
    onNavigateBack: () -> Unit,
    onPictogramAdded: () -> Unit,
    existingPictogram: Pictogram? = null,
    existingCategory: String? = null
) {
    val context = LocalContext.current
    val imageManager = remember { PictogramImageManager(context) }
    val isEditing = existingPictogram != null
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var labelEnglish by remember { mutableStateOf(existingPictogram?.labels?.get(AppLanguage.ENGLISH) ?: "") }
    var labelSpanish by remember { mutableStateOf(existingPictogram?.labels?.get(AppLanguage.SPANISH) ?: "") }
    var labelCatalan by remember { mutableStateOf(existingPictogram?.labels?.get(AppLanguage.CATALAN) ?: "") }
    // Grammar type: store English value, display Catalan label
    var selectedGrammarType by remember { mutableStateOf(existingPictogram?.grammarType ?: "noun") }
    var selectedCategory by remember { mutableStateOf(existingCategory ?: "subjects") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    
    // Create temp file for camera
    val tempImageFile = remember {
        File(context.cacheDir, "temp_pictogram_${System.currentTimeMillis()}.jpg")
    }
    val tempImageUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempImageFile
        )
    }
    
    // Crop launcher - receives cropped image
    val cropLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            selectedImageUri = result.uriContent
        }
    }
    
    // Gallery launcher - chains to crop
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val cropOptions = CropImageContractOptions(it, CropImageOptions(
                fixAspectRatio = true,
                aspectRatioX = 1,
                aspectRatioY = 1
            ))
            cropLauncher.launch(cropOptions)
        }
    }
    
    // Camera launcher - chains to crop
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val cropOptions = CropImageContractOptions(tempImageUri, CropImageOptions(
                fixAspectRatio = true,
                aspectRatioX = 1,
                aspectRatioY = 1
            ))
            cropLauncher.launch(cropOptions)
        }
    }
    
    // Grammar types: English values (for data) and Catalan labels (for UI)
    val grammarTypeValues = listOf("pronoun", "verb", "noun", "adjective", "shortcut")
    val grammarTypeLabels = listOf("pronom", "verb", "nom", "adjectiu", "drecera")
    
    // Categories: English values (for data) and Catalan labels (for UI)
    val categoryValues = listOf("subjects", "actions", "objects", "descriptors", "shortcuts")
    val categoryLabels = listOf("subjectes", "accions", "objectes", "descriptors", "dreceres")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Pictograma" else "Afegir Pictograma") },
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
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image picker
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { showImageSourceDialog = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUri),
                            contentDescription = "Selected image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Select image",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Toca per seleccionar imatge",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Labels section
            Text(
                "Etiquetes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            OutlinedTextField(
                value = labelCatalan,
                onValueChange = { labelCatalan = it },
                label = { Text("Català") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = labelSpanish,
                onValueChange = { labelSpanish = it },
                label = { Text("Español") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = labelEnglish,
                onValueChange = { labelEnglish = it },
                label = { Text("English") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Grammar type selector
            Text(
                "Tipus gramatical",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                grammarTypeValues.forEachIndexed { index, value ->
                    FilterChip(
                        selected = selectedGrammarType == value,
                        onClick = { selectedGrammarType = value },
                        label = { Text(grammarTypeLabels[index]) }
                    )
                }
            }
            
            // Category selector
            Text(
                "Categoria",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryValues.take(3).forEachIndexed { index, value ->
                    FilterChip(
                        selected = selectedCategory == value,
                        onClick = { selectedCategory = value },
                        label = { Text(categoryLabels[index]) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryValues.drop(3).forEachIndexed { index, value ->
                    FilterChip(
                        selected = selectedCategory == value,
                        onClick = { selectedCategory = value },
                        label = { Text(categoryLabels[index + 3]) }
                    )
                }
            }
            
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Save button
            Button(
                onClick = {
                    // Validate
                    when {
                        !isEditing && selectedImageUri == null -> {
                            showError = true
                            errorMessage = "Si us plau, selecciona una imatge"
                        }
                        labelCatalan.isBlank() && labelSpanish.isBlank() && labelEnglish.isBlank() -> {
                            showError = true
                            errorMessage = "Si us plau, afegeix almenys una etiqueta"
                        }
                        else -> {
                            // Save image if new one selected
                            val savedFilename = if (selectedImageUri != null) {
                                imageManager.saveImage(selectedImageUri!!)
                            } else null
                            
                            if (selectedImageUri == null || savedFilename != null) {
                                // Create/update pictogram
                                val labels = mutableMapOf<AppLanguage, String>()
                                if (labelEnglish.isNotBlank()) labels[AppLanguage.ENGLISH] = labelEnglish
                                if (labelSpanish.isNotBlank()) labels[AppLanguage.SPANISH] = labelSpanish
                                if (labelCatalan.isNotBlank()) labels[AppLanguage.CATALAN] = labelCatalan
                                
                                // Use new image if provided, otherwise keep existing
                                val imagePath = savedFilename ?: existingPictogram?.customImagePath
                                
                                val pictogram = Pictogram(
                                    id = existingPictogram?.id ?: "custom_${System.currentTimeMillis()}",
                                    labels = labels,
                                    iconRes = R.drawable.ic_pictogram_placeholder,
                                    grammarType = selectedGrammarType,
                                    customImagePath = imagePath
                                )
                                
                                // Load catalog, update pictogram, save
                                val catalog = PictogramCatalog.load(context)
                                val updatedCategories = catalog.categories.toMutableMap()
                                
                                if (isEditing && existingCategory != null) {
                                    // Remove from old category if category changed
                                    if (existingCategory != selectedCategory) {
                                        val oldCategoryPictograms = updatedCategories[existingCategory]?.toMutableList() ?: mutableListOf()
                                        oldCategoryPictograms.removeAll { it.id == existingPictogram!!.id }
                                        updatedCategories[existingCategory] = oldCategoryPictograms
                                    } else {
                                        // Update in same category
                                        val categoryPictograms = updatedCategories[selectedCategory]?.toMutableList() ?: mutableListOf()
                                        val index = categoryPictograms.indexOfFirst { it.id == existingPictogram!!.id }
                                        if (index >= 0) {
                                            categoryPictograms[index] = pictogram
                                        }
                                        updatedCategories[selectedCategory] = categoryPictograms
                                    }
                                }
                                
                                // Add to new/same category
                                if (!isEditing || existingCategory != selectedCategory) {
                                    val categoryPictograms = updatedCategories[selectedCategory]?.toMutableList() ?: mutableListOf()
                                    categoryPictograms.add(pictogram)
                                    updatedCategories[selectedCategory] = categoryPictograms
                                }
                                
                                val updatedCatalog = PictogramCatalog(updatedCategories)
                                PictogramCatalog.save(context, updatedCatalog)
                                
                                onPictogramAdded()
                            } else {
                                showError = true
                                errorMessage = "Error al guardar la imatge"
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Guardar Pictograma",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    
    // Image source selection dialog
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Selecciona la font de la imatge") },
            text = { Text("Tria d'on vols obtenir la imatge") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImageSourceDialog = false
                        cameraLauncher.launch(tempImageUri)
                    }
                ) {
                    Text("Càmera")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImageSourceDialog = false
                        imagePickerLauncher.launch("image/*")
                    }
                ) {
                    Text("Galeria")
                }
            }
        )
    }
}

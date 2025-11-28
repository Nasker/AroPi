package com.aropi.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import com.aropi.app.logic.PhraseManager
import com.aropi.app.logic.SettingsManager
import com.aropi.app.logic.TTSManager
import com.aropi.app.logic.composer.MockComposer
import com.aropi.app.logic.composer.PhraseComposer
import com.aropi.app.model.AppLanguage
import com.aropi.app.model.Pictogram
import com.aropi.app.model.PictogramCatalog
import com.aropi.app.R

/**
 * Main screen of the AAC app.
 * Displays pictogram grid and phrase bar with modern Material 3 design.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun MainScreen(
    phraseManager: PhraseManager = remember { PhraseManager() },
    composer: PhraseComposer = remember { MockComposer() }
) {
    val context = LocalContext.current
    val ttsManager = remember { TTSManager(context) }
    val settingsManager = remember { SettingsManager(context) }
    
    val pictograms by phraseManager.sequence.collectAsState()
    val settings by settingsManager.settings.collectAsState()
    
    var showSettings by remember { mutableStateOf(false) }
    var showManagePictograms by remember { mutableStateOf(false) }
    var showAddPictogram by remember { mutableStateOf(false) }
    var editingPictogram by remember { mutableStateOf<Pair<Pictogram, String>?>(null) }
    var catalogVersion by remember { mutableStateOf(0) }
    
    // Load pictogram catalog from JSON file
    val pictogramCatalog = remember(catalogVersion) {
        PictogramCatalog.load(context)
    }
    
    val fredokaGoogleFont = remember { GoogleFont("Fredoka One") }
    val fredokaProvider = remember {
        GoogleFont.Provider(
            providerAuthority = "com.google.android.gms.fonts",
            providerPackage = "com.google.android.gms",
            certificates = R.array.com_google_android_gms_fonts_certs
        )
    }
    val fredokaFontFamily = remember {
        FontFamily(
            Font(googleFont = fredokaGoogleFont, fontProvider = fredokaProvider)
        )
    }
    
    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
        }
    }
    
    if (editingPictogram != null) {
        AddPictogramScreen(
            onNavigateBack = { 
                editingPictogram = null
                showManagePictograms = true
            },
            onPictogramAdded = {
                editingPictogram = null
                showManagePictograms = true
                catalogVersion++
            },
            existingPictogram = editingPictogram!!.first,
            existingCategory = editingPictogram!!.second
        )
    } else if (showAddPictogram) {
        AddPictogramScreen(
            onNavigateBack = { 
                showAddPictogram = false
                showManagePictograms = true
            },
            onPictogramAdded = {
                showAddPictogram = false
                showManagePictograms = true
                catalogVersion++
            }
        )
    } else if (showManagePictograms) {
        ManagePictogramsScreen(
            onNavigateBack = { 
                showManagePictograms = false
                showSettings = true
            },
            onAddPictogram = { showAddPictogram = true },
            onEditPictogram = { pictogram, category ->
                editingPictogram = Pair(pictogram, category)
            },
            onCatalogChanged = {
                catalogVersion++ // Trigger catalog reload in main screen
            }
        )
    } else if (showSettings) {
        SettingsScreen(
            settingsManager = settingsManager,
            onNavigateBack = { showSettings = false },
            onManagePictograms = { showManagePictograms = true }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ){
                        Text(
                            "AroPi",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = fredokaFontFamily
                            ),
                                textAlign = TextAlign.Right,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                            }
                    },
                    actions = {
                        IconButton(onClick = { showSettings = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configuració"
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
        ) {
            // Pictogram grid takes most of the space
            PictogramGrid(
                pictograms = pictogramCatalog.getAllPictograms(),
                currentLanguage = settings.language,
                showLabels = settings.showLabels,
                gridColumns = settings.gridColumns,
                onPictogramClick = { pictogram ->
                    phraseManager.add(pictogram)
                    // Speak the word immediately when tapped if auto-speak is enabled
                    if (settings.autoSpeak) {
                        ttsManager.speak(
                            pictogram.getLabel(settings.language),
                            settings.language.locale,
                            settings.speechRate,
                            settings.speechPitch,
                            settings.volumeBoost
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            )
            
            // Phrase bar at the bottom
            PhraseBar(
                pictograms = pictograms,
                currentLanguage = settings.language,
                onClear = { phraseManager.clear() },
                onSpeak = {
                    val sentence = composer.compose(pictograms, settings.language)
                    ttsManager.speak(
                        sentence,
                        settings.language.locale,
                        settings.speechRate,
                        settings.speechPitch,
                        settings.volumeBoost
                    )
                },
                onRemovePictogram = { index ->
                    phraseManager.remove(index)
                }
            )
        }
        }
    }
}

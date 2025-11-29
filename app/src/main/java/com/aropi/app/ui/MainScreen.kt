package com.aropi.app.ui

import android.util.Log
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
import com.aropi.app.logic.BoardManager
import com.aropi.app.logic.PhraseManager
import com.aropi.app.logic.SettingsManager
import com.aropi.app.logic.TTSManager
import com.aropi.app.logic.composer.MockComposer
import com.aropi.app.logic.composer.PhraseComposer
import com.aropi.app.model.AppLanguage
import com.aropi.app.model.Board
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
    Log.d("AroPi", "MainScreen composable started")
    
    val context = LocalContext.current
    Log.d("AroPi", "Context obtained")
    
    val ttsManager = remember { 
        Log.d("AroPi", "Creating TTSManager")
        TTSManager(context)
    }
    Log.d("AroPi", "TTSManager created")
    
    val settingsManager = remember { 
        Log.d("AroPi", "Creating SettingsManager")
        SettingsManager(context)
    }
    Log.d("AroPi", "SettingsManager created")
    
    val pictograms by phraseManager.sequence.collectAsState()
    Log.d("AroPi", "Pictograms state collected")
    
    val settings by settingsManager.settings.collectAsState()
    Log.d("AroPi", "Settings state collected")
    
    var showSettings by remember { mutableStateOf(false) }
    var showManagePictograms by remember { mutableStateOf(false) }
    var showAddPictogram by remember { mutableStateOf(false) }
    var showBoardManagement by remember { mutableStateOf(false) }
    var showBoardEditor by remember { mutableStateOf(false) }
    var editingPictogram by remember { mutableStateOf<Pair<Pictogram, String>?>(null) }
    var editingBoard by remember { mutableStateOf<Board?>(null) }
    var catalogVersion by remember { mutableStateOf(0) }
    var boardVersion by remember { mutableStateOf(0) }
    Log.d("AroPi", "Navigation states initialized")
    
    // Load pictogram catalog and board manager
    val pictogramCatalog = remember(catalogVersion) {
        Log.d("AroPi", "Loading pictogram catalog (version: $catalogVersion)...")
        try {
            val catalog = PictogramCatalog.load(context)
            Log.d("AroPi", "Pictogram catalog loaded with ${catalog.getAllPictograms().size} pictograms")
            catalog
        } catch (e: Exception) {
            Log.e("AroPi", "ERROR loading catalog: ${e.message}", e)
            throw e
        }
    }
    
    val boardManager = remember { BoardManager(context) }
    
    // Ensure default board exists
    LaunchedEffect(Unit) {
        boardManager.ensureDefaultBoard(pictogramCatalog)
    }
    
    // Load active board pictograms
    val activeBoardPictograms = remember(catalogVersion, boardVersion) {
        Log.d("AroPi", "Loading active board pictograms...")
        val activeBoardId = boardManager.getActiveBoardId()
        val activeBoard = boardManager.loadBoard(activeBoardId)
        if (activeBoard != null) {
            val pictograms = boardManager.getBoardPictograms(activeBoard, pictogramCatalog)
            Log.d("AroPi", "Active board '${activeBoard.name}' loaded with ${pictograms.size} pictograms")
            pictograms
        } else {
            Log.w("AroPi", "No active board found, using all pictograms")
            pictogramCatalog.getAllPictograms()
        }
    }
    
    val activeBoard = remember(boardVersion) {
        boardManager.loadBoard(boardManager.getActiveBoardId())
    }
    
    Log.d("AroPi", "Loading Google Fonts...")
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
    Log.d("AroPi", "Google Fonts loaded")
    
    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
        }
    }
    
    if (editingPictogram != null) {
        Log.d("AroPi", "Rendering AddPictogramScreen (edit mode)")
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
        Log.d("AroPi", "Rendering AddPictogramScreen (add mode)")
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
        Log.d("AroPi", "Rendering ManagePictogramsScreen")
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
    } else if (showBoardEditor) {
        Log.d("AroPi", "Rendering BoardEditorScreen")
        BoardEditorScreen(
            existingBoard = editingBoard,
            onNavigateBack = { 
                showBoardEditor = false
                editingBoard = null
                showBoardManagement = true
            },
            onBoardSaved = {
                showBoardEditor = false
                editingBoard = null
                showBoardManagement = true
                boardVersion++
            }
        )
    } else if (showBoardManagement) {
        Log.d("AroPi", "Rendering BoardManagementScreen")
        BoardManagementScreen(
            onNavigateBack = { 
                showBoardManagement = false
                showSettings = true
            },
            onCreateBoard = { 
                editingBoard = null
                showBoardEditor = true
            },
            onEditBoard = { board ->
                editingBoard = board
                showBoardEditor = true
            }
        )
    } else if (showSettings) {
        Log.d("AroPi", "Rendering SettingsScreen")
        SettingsScreen(
            settingsManager = settingsManager,
            onNavigateBack = { 
                showSettings = false
                boardVersion++ // Refresh board in case it changed
            },
            onManagePictograms = { showManagePictograms = true },
            onManageBoards = { showBoardManagement = true }
        )
    } else {
        Log.d("AroPi", "Rendering main Scaffold with grid and phrase bar")
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "AroPi",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = fredokaFontFamily
                                ),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            activeBoard?.let {
                                Text(
                                    it.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
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
                pictograms = activeBoardPictograms,
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

package com.aropi.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aropi.app.logic.PhraseManager
import com.aropi.app.logic.SettingsManager
import com.aropi.app.logic.TTSManager
import com.aropi.app.logic.composer.MockComposer
import com.aropi.app.logic.composer.PhraseComposer
import com.aropi.app.model.Pictogram
import com.aropi.app.R

/**
 * Main screen of the AAC app.
 * Displays pictogram grid and phrase bar with modern Material 3 design.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    
    // Sample pictograms for PoC with bilingual labels
    val availablePictograms = remember {
        listOf(
            Pictogram("yo", "yo", "jo", R.drawable.ic_pictogram_placeholder),
            Pictogram("querer", "querer", "voler", R.drawable.ic_pictogram_placeholder),
            Pictogram("galleta", "galleta", "galeta", R.drawable.ic_pictogram_placeholder),
            Pictogram("niña", "niña", "nena", R.drawable.ic_pictogram_placeholder),
            Pictogram("comer", "comer", "menjar", R.drawable.ic_pictogram_placeholder),
            Pictogram("manzana", "manzana", "poma", R.drawable.ic_pictogram_placeholder)
        )
    }
    
    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
        }
    }
    
    if (showSettings) {
        SettingsScreen(
            settingsManager = settingsManager,
            onNavigateBack = { showSettings = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "AroPi",
                            style = MaterialTheme.typography.headlineMedium
                        ) 
                    },
                    actions = {
                        IconButton(onClick = { showSettings = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configuración"
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
                pictograms = availablePictograms,
                currentLanguage = settings.language,
                showLabels = settings.showLabels,
                onPictogramClick = { pictogram ->
                    phraseManager.add(pictogram)
                    // Speak the word immediately when tapped if auto-speak is enabled
                    if (settings.autoSpeak) {
                        ttsManager.speak(
                            pictogram.getLabel(settings.language),
                            settings.language.locale,
                            settings.speechRate,
                            settings.speechPitch
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
                        settings.speechPitch
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

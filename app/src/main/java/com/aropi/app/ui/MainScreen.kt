package com.aropi.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aropi.app.logic.PhraseManager
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
    
    val pictograms by phraseManager.sequence.collectAsState()
    
    // Sample pictograms for PoC
    val availablePictograms = remember {
        listOf(
            Pictogram("yo", "yo", R.drawable.ic_pictogram_placeholder),
            Pictogram("querer", "querer", R.drawable.ic_pictogram_placeholder),
            Pictogram("galleta", "galleta", R.drawable.ic_pictogram_placeholder),
            Pictogram("niña", "niña", R.drawable.ic_pictogram_placeholder),
            Pictogram("comer", "comer", R.drawable.ic_pictogram_placeholder),
            Pictogram("manzana", "manzana", R.drawable.ic_pictogram_placeholder)
        )
    }
    
    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "AroPi App",
                        style = MaterialTheme.typography.headlineMedium
                    ) 
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
                onPictogramClick = { pictogram ->
                    phraseManager.add(pictogram)
                    // Speak the word immediately when tapped
                    ttsManager.speak(pictogram.label, TTSManager.SPANISH)
                },
                modifier = Modifier.weight(1f)
            )
            
            // Phrase bar at the bottom
            PhraseBar(
                pictograms = pictograms,
                onClear = { phraseManager.clear() },
                onSpeak = {
                    val sentence = composer.compose(pictograms)
                    ttsManager.speak(sentence, TTSManager.SPANISH)
                },
                onRemovePictogram = { index ->
                    phraseManager.remove(index)
                }
            )
        }
    }
}

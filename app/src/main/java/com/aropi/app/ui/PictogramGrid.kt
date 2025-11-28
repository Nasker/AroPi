package com.aropi.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aropi.app.model.AppLanguage
import com.aropi.app.model.Pictogram
import com.aropi.app.ui.components.PictogramCard

/**
 * Displays a grid of pictograms organized by grammar type in columns.
 * Layout: 2 cols (pronouns) + 3 cols (verbs) + 4 cols (nouns) + 2 cols (adjectives) + 1 col (shortcuts) = 12 total
 */
@Composable
fun PictogramGrid(
    pictograms: List<Pictogram>,
    currentLanguage: AppLanguage,
    showLabels: Boolean,
    gridColumns: Int,
    onPictogramClick: (Pictogram) -> Unit,
    modifier: Modifier = Modifier
) {
    // Group pictograms by grammar type
    val pronouns = pictograms.filter { it.grammarType == "pronoun" }
    val verbs = pictograms.filter { it.grammarType == "verb" }
    val nouns = pictograms.filter { it.grammarType == "noun" }
    val adjectives = pictograms.filter { it.grammarType == "adjective" }
    val shortcuts = pictograms.filter { it.grammarType == "shortcut" }
    
    // Column widths: pronoun=2, verb=3, noun=4, adjective=2, shortcut=1
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Pronouns - 2 columns
        if (pronouns.isNotEmpty()) {
            GrammarTypeColumn(
                pictograms = pronouns,
                columns = 2,
                currentLanguage = currentLanguage,
                showLabels = showLabels,
                onPictogramClick = onPictogramClick,
                modifier = Modifier.weight(2f)
            )
        }
        
        // Verbs - 3 columns
        if (verbs.isNotEmpty()) {
            GrammarTypeColumn(
                pictograms = verbs,
                columns = 3,
                currentLanguage = currentLanguage,
                showLabels = showLabels,
                onPictogramClick = onPictogramClick,
                modifier = Modifier.weight(3f)
            )
        }
        
        // Nouns - 4 columns
        if (nouns.isNotEmpty()) {
            GrammarTypeColumn(
                pictograms = nouns,
                columns = 4,
                currentLanguage = currentLanguage,
                showLabels = showLabels,
                onPictogramClick = onPictogramClick,
                modifier = Modifier.weight(4f)
            )
        }
        
        // Adjectives - 2 columns
        if (adjectives.isNotEmpty()) {
            GrammarTypeColumn(
                pictograms = adjectives,
                columns = 2,
                currentLanguage = currentLanguage,
                showLabels = showLabels,
                onPictogramClick = onPictogramClick,
                modifier = Modifier.weight(2f)
            )
        }
        
        // Shortcuts - 1 column
        if (shortcuts.isNotEmpty()) {
            GrammarTypeColumn(
                pictograms = shortcuts,
                columns = 1,
                currentLanguage = currentLanguage,
                showLabels = showLabels,
                onPictogramClick = onPictogramClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * A column section for a specific grammar type with its own grid.
 */
@Composable
private fun GrammarTypeColumn(
    pictograms: List<Pictogram>,
    columns: Int,
    currentLanguage: AppLanguage,
    showLabels: Boolean,
    onPictogramClick: (Pictogram) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        items(pictograms) { pictogram ->
            PictogramCard(
                pictogram = pictogram,
                currentLanguage = currentLanguage,
                showLabel = showLabels,
                size = null,
                onClick = { onPictogramClick(pictogram) }
            )
        }
    }
}


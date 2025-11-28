package com.aropi.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aropi.app.model.AppLanguage
import com.aropi.app.model.Pictogram
import com.aropi.app.ui.components.PictogramCard

/**
 * Displays a grid of pictograms that users can tap to build phrases.
 * Uses large, child-friendly buttons with clear spacing.
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
    val clampedColumns = gridColumns.coerceIn(1, 16)
    val horizontalSpacing = when {
        clampedColumns >= 12 -> 6.dp
        clampedColumns >= 8 -> 8.dp
        else -> 12.dp
    }
    val verticalSpacing = when {
        clampedColumns >= 12 -> 6.dp
        clampedColumns >= 8 -> 8.dp
        else -> 12.dp
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(clampedColumns),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        modifier = modifier
    ) {
        items(pictograms) { pictogram ->
            PictogramCard(
                pictogram = pictogram,
                currentLanguage = currentLanguage,
                showLabel = showLabels,
                size = null,  // Let grid control size
                onClick = { onPictogramClick(pictogram) }
            )
        }
    }
}


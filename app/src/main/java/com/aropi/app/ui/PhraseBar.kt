package com.aropi.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aropi.app.model.AppLanguage
import com.aropi.app.model.Pictogram
import com.aropi.app.ui.components.PictogramCard

/**
 * Displays the current phrase sequence with Clear and Speak buttons.
 * Shows selected pictograms in a horizontal scrollable row.
 */
@Composable
fun PhraseBar(
    pictograms: List<Pictogram>,
    currentLanguage: AppLanguage,
    onClear: () -> Unit,
    onSpeak: () -> Unit,
    onRemovePictogram: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClear,
                enabled = pictograms.isNotEmpty(),
                modifier = Modifier
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Borra frase",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            }

            LazyRow(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 80.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pictograms.isEmpty()) {
                    item {
                        Text(
                            text = "Toca pictogrames per fer una frase",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                } else {
                    itemsIndexed(pictograms) { index, pictogram ->
                        Box {
                            PictogramCard(
                                pictogram = pictogram,
                                currentLanguage = currentLanguage,
                                showLabel = true,
                                size = 80.dp
                            )
                            
                            // Remove button overlay
                            IconButton(
                                onClick = { onRemovePictogram(index) },
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            IconButton(
                onClick = onSpeak,
                enabled = pictograms.isNotEmpty(),
                modifier = Modifier
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Parla",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}


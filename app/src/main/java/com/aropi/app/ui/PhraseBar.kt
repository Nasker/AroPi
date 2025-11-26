package com.aropi.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aropi.app.model.AppLanguage
import com.aropi.app.model.Pictogram

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
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Pictogram sequence
            LazyRow(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pictograms.isEmpty()) {
                    item {
                        Text(
                            text = "Toca pictogrames per fer una frase",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    itemsIndexed(pictograms) { index, pictogram ->
                        PhraseBarPictogram(
                            pictogram = pictogram,
                            currentLanguage = currentLanguage,
                            onRemove = { onRemovePictogram(index) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Clear button
                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier.weight(1f),
                    enabled = pictograms.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Borra frase"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Borra", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                
                // Speak button
                Button(
                    onClick = onSpeak,
                    modifier = Modifier.weight(1f),
                    enabled = pictograms.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("🔊 Parla", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

@Composable
private fun PhraseBarPictogram(
    pictogram: Pictogram,
    currentLanguage: AppLanguage,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .width(70.dp)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = pictogram.iconRes),
                    contentDescription = pictogram.getLabel(currentLanguage),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = pictogram.getLabel(currentLanguage),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1
                )
            }
        }
        
        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

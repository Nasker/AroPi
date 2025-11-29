package com.aropi.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aropi.app.logic.PictogramImageManager
import com.aropi.app.model.AppLanguage
import com.aropi.app.model.Pictogram

/**
 * Reusable pictogram card component with consistent styling.
 * Displays a pictogram icon with optional label in a colored card.
 * 
 * @param pictogram The pictogram to display
 * @param currentLanguage Language for the label
 * @param showLabel Whether to show the text label
 * @param size Size of the card (null for grid, explicit size for phrase bar)
 * @param onClick Optional click handler
 * @param modifier Optional modifier
 */
@Composable
fun PictogramCard(
    pictogram: Pictogram,
    currentLanguage: AppLanguage,
    showLabel: Boolean = true,
    size: androidx.compose.ui.unit.Dp? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageManager = PictogramImageManager(context)
    
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }
    
    val sizeModifier = if (size != null) {
        Modifier.size(size)
    } else {
        Modifier.aspectRatio(1f)
    }
    
    Card(
        modifier = modifier
            .then(sizeModifier)
            .then(clickModifier),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = pictogram.color.color
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon takes most space, centered
            if (pictogram.customImagePath != null && imageManager.imageExists(pictogram.customImagePath)) {
                // Load custom image from internal storage
                AsyncImage(
                    model = imageManager.getImageFile(pictogram.customImagePath),
                    contentDescription = pictogram.getLabel(currentLanguage),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = if (showLabel) 4.dp else 0.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    fallback = painterResource(id = pictogram.iconRes)
                )
            } else {
                // Use default drawable resource
                Image(
                    painter = painterResource(id = pictogram.iconRes),
                    contentDescription = pictogram.getLabel(currentLanguage),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = if (showLabel) 4.dp else 0.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
            
            // Label at bottom
            if (showLabel) {
                Text(
                    text = pictogram.getLabel(currentLanguage).uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onTertiary,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }
}

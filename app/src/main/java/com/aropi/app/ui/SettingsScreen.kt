package com.aropi.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aropi.app.logic.SettingsManager
import com.aropi.app.model.AppLanguage
import com.aropi.app.model.AppSettings
import kotlin.math.roundToInt

/**
 * Settings screen for configuring app preferences.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    onNavigateBack: () -> Unit
) {
    val settings by settingsManager.settings.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuració")},
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Language Section
            SettingsSection(title = "Idioma") {
                LanguageSelector(
                    currentLanguage = settings.language,
                    onLanguageChange = { settingsManager.updateLanguage(it) }
                )
            }
            
            // Speech Settings Section
            SettingsSection(title = "Veu") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Auto-speak toggle
                    SettingSwitch(
                        title = "Reproduïr al tocar",
                        description = "Reprodueix la paraula al seleccionar un pictograma",
                        checked = settings.autoSpeak,
                        onCheckedChange = { settingsManager.updateAutoSpeak(it) }
                    )
                    
                    // Speech rate slider
                    SettingSlider(
                        title = "Velocitat de veu",
                        value = settings.speechRate,
                        valueRange = 0.5f..2.0f,
                        onValueChange = { settingsManager.updateSpeechRate(it) },
                        valueLabel = { String.format("%.1fx", it) }
                    )
                    
                    // Speech pitch slider
                    SettingSlider(
                        title = "Tó de veu",
                        value = settings.speechPitch,
                        valueRange = 0.5f..2.0f,
                        onValueChange = { settingsManager.updateSpeechPitch(it) },
                        valueLabel = { String.format("%.1fx", it) }
                    )
                    
                    // Volume boost toggle
                    SettingSwitch(
                        title = "Volum Extra",
                        description = "Puja el volum automàticament",
                        checked = settings.volumeBoost,
                        onCheckedChange = { settingsManager.updateVolumeBoost(it) }
                    )
                }
            }
            
            // Display Settings Section
            SettingsSection(title = "Visualització") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingSwitch(
                        title = "Mostrar etiquetes",
                        description = "Mostra el text sota els pictogrames",
                        checked = settings.showLabels,
                        onCheckedChange = { settingsManager.updateShowLabels(it) }
                    )
                    SettingSlider(
                        title = "Tamany de la quadrícula",
                        value = settings.gridColumns.toFloat(),
                        valueRange = 2f..16f,
                        onValueChange = { value ->
                            val rounded = value.roundToInt().coerceIn(2, 16)
                            settingsManager.updateGridColumns(rounded)
                        },
                        valueLabel = { "${it.roundToInt()} columnes" },
                        steps = 13
                    )
                }
            }
            
            // App Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "AroPi",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Versió 1.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Creat amb amor per tu Aroa 💝",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun LanguageSelector(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppLanguage.values().forEach { language ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentLanguage == language,
                    onClick = { onLanguageChange(language) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = language.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingSlider(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    valueLabel: (Float) -> String,
    steps: Int = 5
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = valueLabel(value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}

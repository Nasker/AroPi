package com.aropi.app.logic

import android.content.Context
import android.content.SharedPreferences
import com.aropi.app.model.AppLanguage
import com.aropi.app.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages application settings persistence using SharedPreferences.
 */
class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    
    companion object {
        private const val PREFS_NAME = "aropi_settings"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_SPEECH_RATE = "speech_rate"
        private const val KEY_SPEECH_PITCH = "speech_pitch"
        private const val KEY_AUTO_SPEAK = "auto_speak"
        private const val KEY_SHOW_LABELS = "show_labels"
        private const val KEY_VOLUME_BOOST = "volume_boost"
        private const val KEY_GRID_COLUMNS = "grid_columns"
    }
    
    private fun loadSettings(): AppSettings {
        val languageOrdinal = prefs.getInt(KEY_LANGUAGE, AppLanguage.SPANISH.ordinal)
        val language = AppLanguage.values().getOrNull(languageOrdinal) ?: AppLanguage.SPANISH
        
        return AppSettings(
            language = language,
            speechRate = prefs.getFloat(KEY_SPEECH_RATE, 1.0f),
            speechPitch = prefs.getFloat(KEY_SPEECH_PITCH, 1.0f),
            autoSpeak = prefs.getBoolean(KEY_AUTO_SPEAK, true),
            showLabels = prefs.getBoolean(KEY_SHOW_LABELS, true),
            volumeBoost = prefs.getBoolean(KEY_VOLUME_BOOST, false),
            gridColumns = prefs.getInt(KEY_GRID_COLUMNS, 4)
        )
    }
    
    fun updateSettings(settings: AppSettings) {
        prefs.edit().apply {
            putInt(KEY_LANGUAGE, settings.language.ordinal)
            putFloat(KEY_SPEECH_RATE, settings.speechRate)
            putFloat(KEY_SPEECH_PITCH, settings.speechPitch)
            putBoolean(KEY_AUTO_SPEAK, settings.autoSpeak)
            putBoolean(KEY_SHOW_LABELS, settings.showLabels)
            putBoolean(KEY_VOLUME_BOOST, settings.volumeBoost)
            putInt(KEY_GRID_COLUMNS, settings.gridColumns)
            apply()
        }
        _settings.value = settings
    }
    
    fun updateLanguage(language: AppLanguage) {
        updateSettings(_settings.value.copy(language = language))
    }
    
    fun updateSpeechRate(rate: Float) {
        updateSettings(_settings.value.copy(speechRate = rate))
    }
    
    fun updateSpeechPitch(pitch: Float) {
        updateSettings(_settings.value.copy(speechPitch = pitch))
    }
    
    fun updateAutoSpeak(enabled: Boolean) {
        updateSettings(_settings.value.copy(autoSpeak = enabled))
    }
    
    fun updateShowLabels(enabled: Boolean) {
        updateSettings(_settings.value.copy(showLabels = enabled))
    }
    
    fun updateVolumeBoost(enabled: Boolean) {
        updateSettings(_settings.value.copy(volumeBoost = enabled))
    }
    
    fun updateGridColumns(columns: Int) {
        updateSettings(_settings.value.copy(gridColumns = columns))
    }
}

package com.aropi.app.logic

import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/**
 * Manages Text-to-Speech functionality for Catalan and Spanish.
 * Handles initialization and speaking with low latency.
 */
class TTSManager(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var originalVolume: Int = 0
    
    companion object {
        private const val TAG = "TTSManager"
        val CATALAN = Locale("ca", "ES")
        val SPANISH = Locale("es", "ES")
    }
    
    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                Log.d(TAG, "TTS initialized successfully")
            } else {
                Log.e(TAG, "TTS initialization failed")
            }
        }
        
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d(TAG, "TTS started: $utteranceId")
            }
            
            override fun onDone(utteranceId: String?) {
                Log.d(TAG, "TTS completed: $utteranceId")
                // Restore original volume after speaking
                restoreVolume()
            }
            
            override fun onError(utteranceId: String?) {
                Log.e(TAG, "TTS error: $utteranceId")
                // Restore original volume on error
                restoreVolume()
            }
        })
    }
    
    fun speak(text: String, locale: Locale = SPANISH, rate: Float = 1.0f, pitch: Float = 1.0f, volumeBoost: Boolean = false) {
        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized yet")
            return
        }
        
        // Boost volume if requested
        if (volumeBoost) {
            boostVolume()
        }
        
        tts?.apply {
            language = locale
            setSpeechRate(rate)
            setPitch(pitch)
            speak(text, TextToSpeech.QUEUE_FLUSH, null, "AAC_UTTERANCE")
        }
    }
    
    private fun boostVolume() {
        try {
            // Save current volume
            originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            
            // Get max volume
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            
            // Set to 90% of max (not 100% to avoid distortion)
            val boostedVolume = (maxVolume * 0.9).toInt()
            
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                boostedVolume,
                0  // No UI flags
            )
            
            Log.d(TAG, "Volume boosted from $originalVolume to $boostedVolume")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to boost volume", e)
        }
    }
    
    private fun restoreVolume() {
        try {
            if (originalVolume > 0) {
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    originalVolume,
                    0  // No UI flags
                )
                Log.d(TAG, "Volume restored to $originalVolume")
                originalVolume = 0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore volume", e)
        }
    }
    
    fun stop() {
        tts?.stop()
    }
    
    fun shutdown() {
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}

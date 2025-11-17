package com.example.myfirstapplication.logic

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/**
 * Manages Text-to-Speech functionality for Catalan and Spanish.
 * Handles initialization and speaking with low latency.
 */
class TTSManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    
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
            }
            
            override fun onError(utteranceId: String?) {
                Log.e(TAG, "TTS error: $utteranceId")
            }
        })
    }
    
    fun speak(text: String, locale: Locale = SPANISH) {
        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized yet")
            return
        }
        
        tts?.apply {
            language = locale
            speak(text, TextToSpeech.QUEUE_FLUSH, null, "AAC_UTTERANCE")
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

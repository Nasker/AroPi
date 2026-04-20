package com.aropi.app.logic.composer

import android.content.Context
import android.util.Log
import com.aropi.app.logic.bundle.AropiNetwork
import com.aropi.app.logic.bundle.ComposeRequest
import com.aropi.app.model.AppLanguage
import com.aropi.app.model.Pictogram
import kotlinx.coroutines.runBlocking

/**
 * Online fallback that calls the backend's `POST /compose` endpoint.
 *
 * Intended to be wrapped around [OnDeviceBundleComposer]: if the local
 * bundle doesn't know how to conjugate a new subject+verb combination
 * (e.g. the user just imported a custom pronoun), this composer asks
 * the backend to produce the sentence.
 *
 * Network I/O is performed via `runBlocking` to keep the [PhraseComposer]
 * interface synchronous. Call sites already execute inside a coroutine
 * / IO dispatcher (TTS button handler is driven off the UI thread for
 * speech anyway), but callers should not invoke [compose] on the main
 * thread. If the call fails, [fallback] is used.
 */
class OnlineComposer(
    private val context: Context,
    private val fallback: PhraseComposer
) : PhraseComposer {

    override fun compose(pictograms: List<Pictogram>, language: AppLanguage): String {
        if (pictograms.isEmpty()) return ""
        val lang = when (language) {
            AppLanguage.CATALAN -> "ca"
            AppLanguage.SPANISH -> "es"
            AppLanguage.ENGLISH -> return fallback.compose(pictograms, language)
        }

        return try {
            val api = AropiNetwork.api(context)
            val resp = runBlocking {
                api.compose(ComposeRequest(pictos = pictograms.map { it.id }, language = lang))
            }
            resp.output.takeIf { it.isNotBlank() }
                ?: fallback.compose(pictograms, language)
        } catch (e: Exception) {
            Log.w(TAG, "Online /compose failed; using fallback", e)
            fallback.compose(pictograms, language)
        }
    }

    companion object {
        private const val TAG = "OnlineComposer"
    }
}

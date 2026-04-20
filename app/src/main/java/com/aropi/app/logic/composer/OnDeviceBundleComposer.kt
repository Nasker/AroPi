package com.aropi.app.logic.composer

import android.util.Log
import com.aropi.app.logic.bundle.BundlePictogramRepository
import com.aropi.app.model.AppLanguage
import com.aropi.app.model.Pictogram

/**
 * On-device grammatical composer.
 *
 * Mirrors `project/python/composer.py` in the backend, and the Kotlin
 * reference in ARCHITECTURE.md §5.3:
 *
 * 1. The first two pictos are the subject + verb. Their conjugated head
 *    is looked up in `pictogram_phrases.db` keyed by their English IDs
 *    (e.g. `"I eat"`) and the target language.
 * 2. Remaining pictos are translated via the `word_<lang>` column in
 *    `pictos.db`.
 * 3. Head and translated tail are concatenated, capitalised, and
 *    terminated with a period.
 *
 * A [fallback] composer is used when a picto is not found in the bundle
 * (e.g. user-added custom pictograms), or when fewer than two pictos
 * are supplied.
 */
class OnDeviceBundleComposer(
    private val repo: BundlePictogramRepository,
    private val fallback: PhraseComposer = RuleBasedComposer()
) : PhraseComposer {

    override fun compose(pictograms: List<Pictogram>, language: AppLanguage): String {
        if (pictograms.size < 2) return fallback.compose(pictograms, language)

        val subject = pictograms[0]
        val verb = pictograms[1]
        val head = "${subject.id} ${verb.id}"

        val headOut = repo.lookupHead(head, language)
        if (headOut == null) {
            Log.d(TAG, "Head not found in bundle: '$head' ($language); falling back")
            return fallback.compose(pictograms, language)
        }

        val tail = pictograms.drop(2)
        if (tail.isEmpty()) return finalise(headOut)

        val translations = tail.map { p ->
            repo.translate(p.id, language)
                ?: p.getLabel(language).ifBlank { p.id }
        }

        val base = headOut.trimEnd('.', '!', '?', ' ')
        return finalise("$base ${translations.joinToString(" ")}")
    }

    private fun finalise(text: String): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return trimmed
        val punctuated = if (trimmed.last() in PUNCTUATION) trimmed else "$trimmed."
        return punctuated.replaceFirstChar { it.uppercase() }
    }

    companion object {
        private const val TAG = "OnDeviceBundleComposer"
        private val PUNCTUATION = setOf('.', '!', '?')
    }
}

package com.aropi.app.logic.bundle

import com.aropi.app.model.AppLanguage
import com.aropi.app.model.Pictogram

/**
 * Read-only accessor for the data shipped in the offline bundle.
 *
 * - Produces app-level [Pictogram] models from the `pictos` table in
 *   `pictos.db`, with the picto's English `word` used as the stable id
 *   (e.g. "I", "eat", "water").
 * - Looks up pre-computed subject+verb conjugations in `phrases` from
 *   `pictogram_phrases.db`.
 *
 * This mirrors what the backend serves at `/pictos` and `/compose` but
 * runs entirely on-device against the local SQLite files.
 */
class BundlePictogramRepository(private val bundle: BundleManager) {

    /** All pictograms in the bundle, as app-level models. */
    fun getAllPictograms(): List<Pictogram> {
        val db = bundle.pictosDb()
        val list = mutableListOf<Pictogram>()
        db.rawQuery(
            "SELECT word, grammar_type, word_ca, word_es FROM pictos ORDER BY word",
            emptyArray()
        ).use { c ->
            while (c.moveToNext()) {
                val word = c.getString(0) ?: continue
                val grammar = c.getString(1) ?: ""
                val wordCa = if (c.isNull(2)) null else c.getString(2)
                val wordEs = if (c.isNull(3)) null else c.getString(3)

                val labels = buildMap<AppLanguage, String> {
                    put(AppLanguage.ENGLISH, word)
                    wordEs?.takeIf { it.isNotBlank() }?.let { put(AppLanguage.SPANISH, it) }
                    wordCa?.takeIf { it.isNotBlank() }?.let { put(AppLanguage.CATALAN, it) }
                }

                val imagePath = bundle.imageFileForWord(word)?.absolutePath
                list += Pictogram(
                    id = word,
                    labels = labels,
                    iconRes = 0, // unused when customImagePath is set
                    grammarType = grammar,
                    customImagePath = imagePath
                )
            }
        }
        return list
    }

    /**
     * Group pictograms into the category names the app UI already
     * uses (`subjects`, `actions`, `objects`, `modifiers`).
     */
    fun getGroupedByCategory(): Map<String, List<Pictogram>> {
        val all = getAllPictograms()
        val out = linkedMapOf(
            CAT_SUBJECTS to mutableListOf<Pictogram>(),
            CAT_ACTIONS to mutableListOf(),
            CAT_OBJECTS to mutableListOf(),
            CAT_MODIFIERS to mutableListOf()
        )
        for (p in all) {
            val key = when (p.grammarType) {
                "pronoun" -> CAT_SUBJECTS
                "verb" -> CAT_ACTIONS
                "noun" -> CAT_OBJECTS
                "adjective", "adverb" -> CAT_MODIFIERS
                else -> CAT_MODIFIERS
            }
            out.getValue(key).add(p)
        }
        return out.mapValues { it.value.toList() }
    }

    /**
     * Look up the conjugated head for `"$subjectId $verbId"` in the
     * target language, or null if not in the DB.
     */
    fun lookupHead(head: String, language: AppLanguage): String? {
        val col = languageCode(language) ?: return null
        return bundle.phrasesDb().rawQuery(
            "SELECT output FROM phrases WHERE pictos = ? AND language = ? LIMIT 1",
            arrayOf(head, col)
        ).use { c -> if (c.moveToFirst()) c.getString(0) else null }
    }

    /**
     * Translate a single picto into [language] by looking up its
     * word_<lang> column in `pictos`. Returns null if the row is
     * missing or the translation column is NULL/empty.
     */
    fun translate(pictoId: String, language: AppLanguage): String? {
        val lang = languageCode(language) ?: return null
        val column = "word_$lang"
        return bundle.pictosDb().rawQuery(
            "SELECT $column FROM pictos WHERE word = ? LIMIT 1",
            arrayOf(pictoId)
        ).use { c ->
            if (!c.moveToFirst() || c.isNull(0)) null
            else c.getString(0).takeIf { it.isNotBlank() }
        }
    }

    private fun languageCode(language: AppLanguage): String? = when (language) {
        AppLanguage.CATALAN -> "ca"
        AppLanguage.SPANISH -> "es"
        AppLanguage.ENGLISH -> null // no conjugation DB for English yet
    }

    companion object {
        const val CAT_SUBJECTS = "subjects"
        const val CAT_ACTIONS = "actions"
        const val CAT_OBJECTS = "objects"
        const val CAT_MODIFIERS = "modifiers"
    }
}

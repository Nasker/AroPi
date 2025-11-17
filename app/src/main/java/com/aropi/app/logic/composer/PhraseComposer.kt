package com.aropi.app.logic.composer

import com.aropi.app.model.AppLanguage
import com.aropi.app.model.Pictogram

/**
 * Interface for composing natural language sentences from pictogram sequences.
 * This abstraction allows for different implementations (mock, rule-based, LLM).
 */
interface PhraseComposer {
    fun compose(pictograms: List<Pictogram>, language: AppLanguage = AppLanguage.SPANISH): String
}

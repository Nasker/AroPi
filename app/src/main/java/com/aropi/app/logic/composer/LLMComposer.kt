package com.aropi.app.logic.composer

import com.aropi.app.model.AppLanguage
import com.aropi.app.model.Pictogram

/**
 * Placeholder for future LLM-based composition.
 * Currently not implemented - will integrate with backend LLM service later.
 */
class LLMComposer : PhraseComposer {
    override fun compose(pictograms: List<Pictogram>, language: AppLanguage): String {
        // TODO: Implement LLM integration
        // The language parameter will be passed to the LLM to generate appropriate responses
        throw NotImplementedError("LLM composer not yet implemented")
    }
}

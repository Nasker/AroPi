package com.aropi.app.logic.composer

import com.aropi.app.model.Pictogram

/**
 * Placeholder for future LLM-based composition.
 * Currently not implemented - will integrate with backend LLM service later.
 */
class LLMComposer : PhraseComposer {
    override fun compose(pictograms: List<Pictogram>): String {
        // TODO: Implement LLM integration
        throw NotImplementedError("LLM composer not yet implemented")
    }
}

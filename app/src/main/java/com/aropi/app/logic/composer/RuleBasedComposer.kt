package com.aropi.app.logic.composer

import com.aropi.app.model.Pictogram

/**
 * Simple rule-based composer that concatenates pictogram labels.
 * Used as a fallback when no mock phrase is available.
 */
class RuleBasedComposer : PhraseComposer {
    override fun compose(pictograms: List<Pictogram>): String {
        return pictograms.joinToString(" ") { it.label }
    }
}

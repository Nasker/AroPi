package com.aropi.app.logic.composer

import com.aropi.app.model.Pictogram

/**
 * Mock composer with predefined natural sentences for known sequences.
 * Falls back to RuleBasedComposer for unknown sequences.
 */
class MockComposer(private val fallback: PhraseComposer = RuleBasedComposer()) : PhraseComposer {
    
    private val mockPhrases = mapOf(
        // Spanish examples
        listOf("yo", "querer", "galleta") to "Yo quiero una galleta",
        listOf("yo", "querer", "comer", "galleta") to "Yo quiero comer una galleta",
        listOf("niña", "comer", "manzana") to "La niña come una manzana",
        listOf("yo", "comer", "manzana") to "Yo como una manzana",
        
        // Catalan examples
        listOf("jo", "voler", "galeta") to "Jo vull una galeta",
        listOf("nena", "menjar", "poma") to "La nena menja una poma"
    )
    
    override fun compose(pictograms: List<Pictogram>): String {
        if (pictograms.isEmpty()) return ""
        
        val key = pictograms.map { it.id }
        return mockPhrases[key] ?: fallback.compose(pictograms)
    }
}

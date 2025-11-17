package com.example.myfirstapplication.logic.composer

import com.example.myfirstapplication.model.Pictogram

/**
 * Interface for composing natural language sentences from pictogram sequences.
 * This abstraction allows for different implementations (mock, rule-based, LLM).
 */
interface PhraseComposer {
    fun compose(pictograms: List<Pictogram>): String
}

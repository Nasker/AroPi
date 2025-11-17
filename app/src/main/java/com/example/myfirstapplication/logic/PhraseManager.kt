package com.example.myfirstapplication.logic

import com.example.myfirstapplication.model.Pictogram
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the current phrase sequence.
 * Provides methods to add, remove, and clear pictograms.
 */
class PhraseManager {
    private val _sequence = MutableStateFlow<List<Pictogram>>(emptyList())
    val sequence: StateFlow<List<Pictogram>> = _sequence.asStateFlow()

    fun add(pictogram: Pictogram) {
        _sequence.value = _sequence.value + pictogram
    }

    fun remove(index: Int) {
        if (index in _sequence.value.indices) {
            _sequence.value = _sequence.value.toMutableList().apply {
                removeAt(index)
            }
        }
    }

    fun clear() {
        _sequence.value = emptyList()
    }

    fun getSequence(): List<Pictogram> = _sequence.value
}

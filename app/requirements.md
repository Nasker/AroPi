Functional Requirements
FR1 — Pictogram Grid

The user must see a grid of pictograms.

Tapping a pictogram adds it to the current phrase sequence.

FR2 — Phrase Bar

The app displays the selected pictograms in order.

Clear button empties the sequence.

Speak button triggers sentence generation & TTS.

FR3 — Sentence Generation

App must use PhraseComposer interface.

Default implementation = MockComposer.

Fall back to RuleBasedComposer if not in mock list.

FR4 — Text to Speech

Must use Android TTS engine.

Must support Catalan and Spanish.

Latency must be < 250–500 ms after tapping “Speak”.

FR5 — Modularity

Codebase structured so LLM integration only requires replacing the composer.

FR6 — Offline Operation

Must work fully offline since PoC does not use backend.

Technical Requirements
TR1 — Kotlin

Entire app in Kotlin.

TR2 — Jetpack Compose

Modern UI toolkit.

TR3 — Resource Management

Pictograms stored locally as drawable PNGs.

TR4 — Architecture

Follow structure in plan.md.

Ensure clear module boundaries.

TR5 — No external dependencies

Keep PoC lightweight for fast iteration.

TR6 — State Management

Use Compose state (ViewModel optional but encouraged).

TR7 — Compatibility

Minimum Android: API 26+

Target: API 34

Non-Functional Requirements
NFR1 — Usability

Child-friendly buttons, large spacing, clear icons.

NFR2 — Latency

Phrase building interaction must be instant.

Speaking must feel responsive.

NFR3 — Extensibility

Easy to add categories later (Animals, Actions, Food).

NFR4 — Maintainability

The composer service must remain isolated and testable.

NFR5 — Privacy

All data stays local.
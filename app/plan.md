📄 plan.md
🧩 Project Goal

Build a Kotlin + Jetpack Compose AAC app (PoC) to help a child construct and speak simple sentences using pictograms.
The first version does not use an LLM, but must be fully modular so an LLM service can plug in later without rewriting the architecture.

🏗️ Architecture Overview
app/
├─ ui/
│   ├─ PictogramGrid.kt
│   ├─ PhraseBar.kt
│   └─ MainScreen.kt
│
├─ model/
│   └─ Pictogram.kt
│
├─ logic/
│   ├─ PhraseManager.kt
│   ├─ composer/
│   │     ├─ PhraseComposer.kt
│   │     ├─ MockComposer.kt
│   │     ├─ RuleBasedComposer.kt
│   │     └─ LLMComposer.kt (future)
│   └─ TTSManager.kt
│
├─ data/
│   └─ (future) LocalPhraseCache.kt
│
└─ MainActivity.kt

🧱 Core Components
1. Pictogram Model

A simple model with:

id

label

iconRes

2. Pictogram Grid UI

Shows the pictograms, lets user tap to select.

3. Phrase Bar UI

Displays selected pictograms; includes “Speak” and “Clear”.

4. PhraseManager

Stores the current sequence, exposes methods to modify it.

5. PhraseComposer (Interface)

Defines:
fun compose(pictos: List<Pictogram>): String

6. MockComposer

Returns natural sentences for a few known sequences.

7. RuleBasedComposer

Concatenates labels → fallback.

8. TTSManager

Wraps Android TTS in Catalan/Spanish.

9. LLMComposer (future)

Empty placeholder now.

🚀 PoC Flow

User taps pictograms

PhraseManager updates

User taps Speak

composer.compose()

Android TTS speaks sentence

✔️ Acceptance Criteria

Pictogram grid works

Phrase bar works

Speak + Clear functional

MockComposer + RuleBasedComposer implemented

App speaks Catalan/Spanish

Architecture matches plan


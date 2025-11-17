🟦 Phase 1 — Project Setup
1. Create project in Android Studio

Kotlin

Jetpack Compose template

Minimum SDK 26

2. Create folder structure 
    ui/
   model/
   logic/composer/
   logic/
   data/

3. Add sample pictogram drawables

    yo.png
    
    querer.png
    
    galleta.png
    
    niña.png
    
    comer.png
    
    manzana.png
(keep it minimal for PoC, and create just mocks I'll provide the pictures)

🟩 Phase 2 — Core Models & Logic
4. Create Pictogram.kt

id: String

label: String

iconRes: Int

5. Implement PhraseManager

List<Pictogram>

add

remove

clear

getSequence

6. Define PhraseComposer interface
   interface PhraseComposer {
   fun compose(pictos: List<Pictogram>): String
   }

7. Implement MockComposer

dictionary for 3–5 example sequences

fallback to RuleBased

8. Implement RuleBasedComposer

simply concatenates labels

9. Create placeholder LLMComposer

empty implementation for now

🟧 Phase 3 — Jetpack Compose UI
10. Build PictogramGrid composable

LazyVerticalGrid (columns = 3 or 4)

OnClick → PhraseManager.add

11. Build PhraseBar composable

Row of selected pictograms

“Clear” button

“Speak” button

12. Build MainScreen composable

Column(PictogramGrid, PhraseBar)

13. Wire Compose state

Use remember or a ViewModel to store PhraseManager state.

🟪 Phase 4 — TTS Integration
14. Implement TTSManager

init TextToSpeech

speak(text: String, Locale)

15. Add Speak button logic

On tap:

Retrieve pictogram list

composer.compose(list)

tts.speak(sentence)

🟥 Phase 5 — Testing & Polishing
16. Test mock sequences

Ensure known sequences produce natural output

17. Test fallback behavior

Ensure new sequences produce rule-based phrases

18. Run on emulator + physical tablet
19. Check Catalan + Spanish voices
20. Clean UI spacing, basic styling
    🟫 Phase 6 — (Optional) Future LLM Preparation
21. Add config for selecting composer

Mock / RuleBased / LLM (later)

22. Add LocalPhraseCache placeholder

SQLite or JSON
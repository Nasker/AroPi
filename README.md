# AroPi - AAC Communication App

> 💝 **Created with love for my daughter Aroa**

**AroPi** is an Augmentative and Alternative Communication (AAC) Android application designed to help non-verbal individuals communicate using pictograms. Users select pictograms to build phrases, which are then converted into natural sentences and spoken aloud using Text-to-Speech.

## 🎯 Purpose

This app aims to provide an accessible, offline-first communication tool for individuals with speech difficulties, particularly targeting Spanish and Catalan speakers. The interface is designed to be child-friendly with large touch targets, clear visuals, and responsive interactions.

## ✨ Features

- **📱 Pictogram-Based Communication**: Tap pictograms to build phrases visually
- **🗣️ Text-to-Speech**: Natural sentence generation with Android TTS (Spanish & Catalan)
- **🧠 Smart Composition**: 
  - MockComposer for predefined natural sentences
  - RuleBasedComposer as fallback for unknown sequences
  - Architecture ready for future LLM integration
- **🎨 Modern UI**: Built with Jetpack Compose and Material 3 design
- **📴 Offline-First**: Works completely offline, no internet required
- **♿ Accessibility**: Large buttons, high contrast, child-friendly design

## 🏗️ Architecture

```
app/
├── ui/                          # Jetpack Compose UI
│   ├── MainScreen.kt           # Main composable screen
│   ├── PictogramGrid.kt        # Grid of selectable pictograms
│   ├── PhraseBar.kt            # Phrase display with Speak/Clear buttons
│   └── theme/                  # Material 3 theming
│
├── model/
│   └── Pictogram.kt            # Data model for pictograms
│
├── logic/
│   ├── PhraseManager.kt        # Manages phrase sequence state
│   ├── TTSManager.kt           # Text-to-Speech for Catalan/Spanish
│   └── composer/               # Sentence composition strategies
│       ├── PhraseComposer.kt   # Interface
│       ├── MockComposer.kt     # Predefined natural sentences
│       ├── RuleBasedComposer.kt # Fallback concatenation
│       └── LLMComposer.kt      # Placeholder for future LLM
│
└── MainActivity.kt             # Compose entry point
```

## 🚀 Getting Started

### Prerequisites

- **Android Studio** (Hedgehog or later)
- **JDK** 17 or higher
- **Android SDK** with API 26+ (minimum) and API 34 (target)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Nasker/AroPi.git
   cd AroPi
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Android Studio will automatically sync Gradle files
   - Wait for dependencies to download

4. **Run the app**
   - Connect an Android device (API 26+) or start an emulator
   - Click the "Run" button or press `Shift + F10`

### Building APK

```bash
./gradlew assembleDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

## 📖 Usage

1. **Select Pictograms**: Tap pictograms from the grid to add them to your phrase
2. **Build Phrase**: Selected pictograms appear in the phrase bar at the bottom
3. **Remove Items**: Tap the X on any pictogram in the phrase bar to remove it
4. **Clear All**: Use the Clear button to empty the entire phrase
5. **Speak**: Tap the Speak button to generate and hear the sentence

### Example Sequences

- `yo` + `querer` + `galleta` → "Yo quiero una galleta" (I want a cookie)
- `niña` + `comer` + `manzana` → "La niña come una manzana" (The girl eats an apple)

## 🛠️ Technical Stack

- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose with Material 3
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 34 (Android 14)
- **Architecture**: Clean separation with reactive state (StateFlow)
- **Build System**: Gradle with Kotlin DSL

## 📝 Adding Pictograms

See [PICTOGRAM_GUIDE.md](app/PICTOGRAM_GUIDE.md) for detailed instructions on:
- Adding new pictogram images
- Expanding vocabulary
- Creating natural sentence mappings
- Organizing pictograms by categories

### Quick Example

1. Add PNG file to `app/src/main/res/drawable/` (e.g., `agua.png`)
2. Update `MainScreen.kt`:
   ```kotlin
   Pictogram("agua", "agua", R.drawable.agua)
   ```
3. Add sentence mappings in `MockComposer.kt`:
   ```kotlin
   listOf("yo", "querer", "agua") to "Yo quiero agua"
   ```

## 🌐 Language Support

Currently supports:
- **Spanish** (es-ES) - Default
- **Catalan** (ca-ES)

To switch language, modify `TTSManager.kt`:
```kotlin
private val locale = Locale("ca", "ES")  // For Catalan
```

## 🔮 Future Enhancements

- [ ] LLM integration for dynamic sentence generation
- [ ] Pictogram categories (People, Actions, Food, etc.)
- [ ] User settings (language, voice speed, theme)
- [ ] Phrase history and favorites
- [ ] Custom pictogram upload
- [ ] Multi-user profiles
- [ ] Real pictogram library (ARASAAC integration)

## 📚 Documentation

- [Requirements](app/requirements.md) - Functional and technical requirements
- [Implementation Summary](app/IMPLEMENTATION_SUMMARY.md) - Detailed implementation notes
- [Pictogram Guide](app/PICTOGRAM_GUIDE.md) - How to add and manage pictograms
- [Project Plan](app/plan.md) - Development roadmap

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

### Development Guidelines

- Follow Kotlin coding conventions
- Use Jetpack Compose for UI components
- Maintain clean architecture separation
- Write descriptive commit messages
- Test on multiple Android versions

## 📄 License

This project is licensed under the terms specified in the [LICENSE](LICENSE) file.

## 🙏 Acknowledgments

- **ARASAAC** - Free AAC pictogram resources
- **Jetpack Compose** - Modern Android UI toolkit
- **Material Design 3** - Design system and components

## 📧 Contact

For questions or feedback, please open an issue on GitHub.

---

**Note**: This is a Proof of Concept (PoC) application. Current implementation uses placeholder icons. For production use, integrate real pictogram libraries like ARASAAC.

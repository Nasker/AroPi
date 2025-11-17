# Adding Real Pictograms Guide

## Quick Start

### 1. Add Your Pictogram Images

Place your PNG files in the drawable folder:
```
app/src/main/res/drawable/
├── yo.png
├── querer.png
├── galleta.png
├── nina.png
├── comer.png
├── manzana.png
```

**Recommended specs:**
- Format: PNG with transparency
- Size: 512x512px (will be scaled down)
- Style: Clear, simple, child-friendly icons
- Naming: Use lowercase, no spaces (use underscore if needed)

### 2. Update MainScreen.kt

Replace the pictogram list (around line 30):

```kotlin
val availablePictograms = remember {
    listOf(
        Pictogram("yo", "yo", R.drawable.yo),
        Pictogram("querer", "querer", R.drawable.querer),
        Pictogram("galleta", "galleta", R.drawable.galleta),
        Pictogram("niña", "niña", R.drawable.nina),
        Pictogram("comer", "comer", R.drawable.comer),
        Pictogram("manzana", "manzana", R.drawable.manzana)
    )
}
```

### 3. Add Natural Sentences to MockComposer

Edit `logic/composer/MockComposer.kt` to add more natural sentence mappings:

```kotlin
private val mockPhrases = mapOf(
    // Spanish examples
    listOf("yo", "querer", "galleta") to "Yo quiero una galleta",
    listOf("yo", "querer", "comer", "galleta") to "Yo quiero comer una galleta",
    listOf("niña", "comer", "manzana") to "La niña come una manzana",
    listOf("yo", "comer", "manzana") to "Yo como una manzana",
    
    // Add more mappings here
    listOf("yo", "querer", "agua") to "Yo quiero agua",
    listOf("yo", "necesitar", "ayuda") to "Yo necesito ayuda",
    
    // Catalan examples
    listOf("jo", "voler", "galeta") to "Jo vull una galeta",
    listOf("nena", "menjar", "poma") to "La nena menja una poma"
)
```

## Expanding the Vocabulary

### Adding More Pictograms

1. **Add the image file** to `res/drawable/`
2. **Add to the list** in `MainScreen.kt`
3. **Add sentence mappings** in `MockComposer.kt`

Example - Adding "agua" (water):

```kotlin
// 1. Add agua.png to res/drawable/

// 2. In MainScreen.kt, add to list:
Pictogram("agua", "agua", R.drawable.agua),

// 3. In MockComposer.kt, add mappings:
listOf("yo", "querer", "agua") to "Yo quiero agua",
listOf("yo", "beber", "agua") to "Yo bebo agua",
```

### Organizing by Categories (Future Enhancement)

You can group pictograms by category:

```kotlin
data class PictogramCategory(
    val name: String,
    val pictograms: List<Pictogram>
)

val categories = listOf(
    PictogramCategory("Personas", listOf(
        Pictogram("yo", "yo", R.drawable.yo),
        Pictogram("niña", "niña", R.drawable.nina),
        // ...
    )),
    PictogramCategory("Acciones", listOf(
        Pictogram("querer", "querer", R.drawable.querer),
        Pictogram("comer", "comer", R.drawable.comer),
        // ...
    )),
    PictogramCategory("Comida", listOf(
        Pictogram("galleta", "galleta", R.drawable.galleta),
        Pictogram("manzana", "manzana", R.drawable.manzana),
        // ...
    ))
)
```

## Finding Pictogram Resources

### Free Pictogram Libraries

1. **ARASAAC** (https://arasaac.org/)
   - Free AAC pictograms
   - Multiple languages including Spanish/Catalan
   - CC BY-NC-SA license

2. **Sclera Pictograms** (https://www.sclera.be/)
   - Free pictograms for AAC
   - Simple, clear designs
   - CC BY-NC license

3. **Mulberry Symbols** (https://mulberrysymbols.org/)
   - Free symbol set
   - Good for AAC applications

### Converting to PNG

If you get SVG files, convert to PNG:
```bash
# Using ImageMagick
convert input.svg -resize 512x512 output.png

# Or use online tools like:
# - https://cloudconvert.com/svg-to-png
# - https://www.adobe.com/express/feature/image/convert/svg-to-png
```

## Testing Your Pictograms

1. **Build and run** the app
2. **Tap pictograms** to add them to the phrase
3. **Tap Speak** to hear the sentence
4. **Check**:
   - Images display clearly
   - Labels are readable
   - Touch targets are large enough
   - Sentences sound natural

## Tips for Child-Friendly Design

- **High contrast**: Make sure pictograms are visible
- **Simple designs**: Avoid complex details
- **Consistent style**: Use pictograms from the same set
- **Large touch targets**: Current cards are 1:1 aspect ratio
- **Clear labels**: Use simple, common words

## Language Support

### Adding Catalan Pictograms

```kotlin
// In MainScreen.kt, you can create separate lists:
val spanishPictograms = listOf(...)
val catalanPictograms = listOf(...)

// Switch based on user preference
val currentPictograms = if (useCatalan) catalanPictograms else spanishPictograms
```

### Bilingual Support

You can support both languages simultaneously:

```kotlin
data class Pictogram(
    val id: String,
    val labelEs: String,  // Spanish label
    val labelCa: String,  // Catalan label
    val iconRes: Int
)

// Display based on current language
Text(text = if (useCatalan) pictogram.labelCa else pictogram.labelEs)
```

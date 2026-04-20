# OBF/OBZ Quick Start Guide

## For Users

### Exporting a Board

1. Open AroPi
2. Go to **Settings** → **Gestionar Taulers** (Manage Boards)
3. Find the board you want to export
4. Tap the **download icon** (⬇️) on the board
5. Confirm the export dialog
6. Choose where to save the `.obz` file
7. Share the file with other AAC apps or users!

**What you can do with exported boards:**
- Import into AsTeRICS Grid
- Import into CoughDrop
- Import into CBoard
- Share with other AroPi users
- Backup your boards

### Importing a Board

1. Get an `.obf` or `.obz` file (from another app or user)
2. Open AroPi
3. Go to **Settings** → **Gestionar Taulers** (Manage Boards)
4. Tap the **menu icon** (⋮) in the top right
5. Select **"Importar tauler (OBF/OBZ)"**
6. Choose the file to import
7. The board will appear in your boards list!

**What gets imported:**
- All pictograms with images
- Board name and layout
- Multilingual labels (if available)
- Colors and styling

**Note:** Imported pictograms are added to the "imported" category in your catalog.

---

## For Developers

### Basic Usage

```kotlin
val context: Context = ...
val obfManager = OBFManager(context)
val boardManager = BoardManager(context)
val catalog = PictogramCatalog.load(context)

// Export a board
val board = boardManager.loadBoard("my-board-id")
val outputFile = File(context.cacheDir, "export.obz")
obfManager.exportBoardToOBZ(board!!, catalog, outputFile)

// Import a board
val inputFile = File("/path/to/board.obz")
when (val result = obfManager.importBoard(inputFile)) {
    is ImportResult.Success -> {
        val (importedBoard, pictograms) = result
        val updatedCatalog = obfManager.saveImportedBoard(
            importedBoard, 
            pictograms, 
            catalog
        )
        println("Imported: ${importedBoard.name}")
    }
    is ImportResult.Error -> {
        println("Error: ${result.message}")
    }
}
```

### Export Only

```kotlin
val exporter = OBFExporter(context)
val board: Board = ...
val pictograms: List<Pictogram> = ...

// Export to OBF (JSON only)
exporter.exportToOBF(board, pictograms, File("board.obf"))

// Export to OBZ (ZIP with images - recommended)
exporter.exportToOBZ(board, pictograms, File("board.obz"))
```

### Import Only

```kotlin
val importer = OBFImporter(context)

// Import OBF
val (board, pictograms) = importer.importFromOBF(File("board.obf"))

// Import OBZ
val (board, pictograms) = importer.importFromOBZ(File("board.obz"))
```

### Custom Extensions

AroPi preserves grammar type information using OBF extensions:

```kotlin
// Export preserves grammar types
val button = OBFButton(
    id = 1,
    label = "yo",
    backgroundColor = "rgb(255, 255, 0)",
    extAropiGrammarType = "pronoun"  // Custom extension
)

// Import reads grammar types
val grammarType = button.extAropiGrammarType 
    ?: inferGrammarTypeFromColor(button.backgroundColor)
```

---

## Testing

### Test Export

1. Create a test board with a few pictograms
2. Export to OBZ
3. Verify the file structure:
   ```
   board.obz (ZIP file)
   ├── manifest.json
   ├── boards/
   │   └── board-id.obf
   └── images/
       ├── image1.png
       └── image2.png
   ```

### Test Import

Download a sample OBZ file:
```bash
# CommuniKate sample boards
wget https://www.openboardformat.org/examples/communikate-20.obz

# Import into AroPi via UI
```

### Round-trip Test

```bash
# 1. Export from AroPi
# 2. Import into AsTeRICS Grid
# 3. Export from AsTeRICS Grid
# 4. Import back into AroPi
# 5. Verify data integrity
```

---

## Troubleshooting

### "Error important: No board file found"

**Cause:** Invalid OBZ structure
**Solution:** 
- Ensure the file is a valid ZIP
- Check it contains `manifest.json`
- Verify board files exist in `boards/` folder

### "Error: Unknown error"

**Cause:** Corrupted file or parsing error
**Solution:**
- Check Android logs: `adb logcat | grep OBF`
- Verify JSON is valid (for .obf files)
- Try a different file

### Images not showing after import

**Cause:** Image extraction failed
**Solution:**
- Check storage permissions
- Verify images exist in OBZ
- Check `imported_images/` directory in app data

### Export creates empty file

**Cause:** Board has no pictograms or permission issue
**Solution:**
- Ensure board has pictograms
- Check storage permissions
- Verify output directory is writable

---

## File Format Examples

### Minimal OBF

```json
{
  "id": "simple-board",
  "name": "Simple Board",
  "grid": {
    "rows": 1,
    "columns": 2,
    "order": [[1, 2]]
  },
  "buttons": [
    {"id": 1, "label": "yes"},
    {"id": 2, "label": "no"}
  ]
}
```

### AroPi OBF with Extensions

```json
{
  "id": "aropi-board",
  "name": "AroPi Board",
  "ext_aropi_grammar_layout": true,
  "grid": {
    "rows": 1,
    "columns": 2,
    "order": [[1, 2]]
  },
  "buttons": [
    {
      "id": 1,
      "label": "yo",
      "background_color": "rgb(255, 255, 0)",
      "ext_aropi_grammar_type": "pronoun",
      "translations": {
        "es": {"label": "yo"},
        "ca": {"label": "jo"}
      }
    }
  ]
}
```

---

## Resources

- **Full Documentation**: See `OBF_IMPLEMENTATION.md`
- **OBF Spec**: https://www.openboardformat.org/
- **Sample Files**: https://www.openboardformat.org/examples
- **AsTeRICS Grid**: https://grid.asterics.eu/

---

## Next Steps

1. ✅ Export your first board
2. ✅ Test import with a sample OBZ
3. ✅ Share boards with other AAC users
4. 🔄 Contribute improvements to the implementation
5. 📝 Report issues or suggestions

Happy communicating! 🗣️

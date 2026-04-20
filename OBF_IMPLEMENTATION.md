# OBF/OBZ Import/Export Implementation

## Overview

AroPi now supports importing and exporting boards in the **Open Board Format (OBF/OBZ)**, enabling interoperability with other AAC applications like AsTeRICS Grid, CoughDrop, CBoard, and more.

## What is OBF/OBZ?

- **OBF** (Open Board Format): A JSON-based format for a single communication board
- **OBZ** (Open Board ZIP): A ZIP package containing board(s) + images + manifest
- **Specification**: https://www.openboardformat.org/

## Architecture

### Translation Layer Approach

We use a **translation layer** instead of native OBF storage:

```
AroPi Internal Format (Simple)
       ↕ Translation Layer
OBF/OBZ Format (Complex)
```

**Benefits:**
- Keep AroPi's simple, efficient internal format
- Provide compatibility with OBF ecosystem
- No breaking changes to existing data
- Easy to maintain and extend

### File Structure

```
app/src/main/java/com/aropi/app/
├── model/obf/
│   └── OBFModels.kt          # OBF data structures
│
└── logic/obf/
    ├── OBFExporter.kt        # AroPi → OBF conversion
    ├── OBFImporter.kt        # OBF → AroPi conversion
    └── OBFManager.kt         # High-level API
```

## Features

### Export (AroPi → OBF/OBZ)

**What gets exported:**
- Board name and ID
- All pictograms with images
- Multilingual labels (Spanish, Catalan, English)
- Grammar types (via custom extension `ext_aropi_grammar_type`)
- Fitzgerald color coding
- Images as base64 data URLs or separate files in OBZ

**Format:**
```kotlin
// Export to OBF (single JSON file)
obfManager.exportBoardToOBF(board, catalog, outputFile)

// Export to OBZ (recommended - includes images)
obfManager.exportBoardToOBZ(board, catalog, outputFile)
```

**Grid Layout:**
- Uses simple 6-column grid
- Auto-calculates rows based on pictogram count
- Preserves button order

### Import (OBF/OBZ → AroPi)

**What gets imported:**
- Board structure and name
- Buttons → Pictograms
- Images (extracted and saved to internal storage)
- Multilingual labels
- Grammar types (from custom extension or inferred from colors)

**Format:**
```kotlin
// Auto-detect format (.obf or .obz)
val result = obfManager.importBoard(file)

when (result) {
    is ImportResult.Success -> {
        val (board, pictograms) = result
        // Save to AroPi
        obfManager.saveImportedBoard(board, pictograms, catalog)
    }
    is ImportResult.Error -> {
        // Handle error
    }
}
```

**Grammar Type Inference:**
If the imported board doesn't have AroPi's custom grammar extension, we infer from Fitzgerald colors:
- Yellow → pronoun
- Green → verb
- Orange → noun
- Blue → adjective
- Purple → shortcut

## UI Integration

### Board Management Screen

**Export:**
1. Tap the download icon on any board
2. Confirm export dialog
3. Choose save location
4. Board is exported as `.obz` file

**Import:**
1. Tap the menu (⋮) in top bar
2. Select "Importar tauler (OBF/OBZ)"
3. Choose `.obf` or `.obz` file
4. Board and pictograms are imported
5. Pictograms added to "imported" category

## Custom Extensions

AroPi uses OBF's extension mechanism to preserve additional data:

```json
{
  "ext_aropi_grammar_layout": true,
  "buttons": [
    {
      "id": 1,
      "label": "yo",
      "ext_aropi_grammar_type": "pronoun",
      ...
    }
  ]
}
```

These extensions are:
- **Optional**: Other apps can ignore them
- **Preserved**: Round-trip AroPi → OBF → AroPi maintains data
- **Standards-compliant**: Following OBF extension guidelines

## Compatibility

### Tested With
- ✅ AsTeRICS Grid (import/export)
- ✅ CoughDrop (import/export)
- ✅ CBoard (import/export)

### Known Limitations

**Export:**
- AroPi's grammar-based column layout → simple 6-column grid
- No board navigation (single board export only)
- No sounds (AroPi doesn't use sounds currently)

**Import:**
- Multi-board OBZ packages: only root board imported
- Board navigation links: ignored
- Sounds: ignored
- Complex grid layouts: flattened to list

## Example OBF Output

```json
{
  "id": "my-board",
  "name": "Mi Tablero",
  "locale": "es",
  "default_layout": "landscape",
  "ext_aropi_grammar_layout": true,
  "grid": {
    "rows": 2,
    "columns": 6,
    "order": [[1, 2, 3, 4, 5, 6], [7, 8, null, null, null, null]]
  },
  "buttons": [
    {
      "id": 1,
      "label": "yo",
      "vocalization": "yo",
      "image_id": "pronoun_yo",
      "background_color": "rgb(255, 255, 0)",
      "border_color": "rgb(170, 170, 170)",
      "ext_aropi_grammar_type": "pronoun",
      "translations": {
        "es": {"label": "yo", "vocalization": "yo"},
        "ca": {"label": "jo", "vocalization": "jo"},
        "en": {"label": "I", "vocalization": "I"}
      }
    }
  ],
  "images": [
    {
      "id": "pronoun_yo",
      "data": "data:image/png;base64,iVBORw0KG...",
      "content_type": "image/png",
      "license": {"type": "private"}
    }
  ]
}
```

## Testing

### Manual Testing

1. **Export Test:**
   ```
   - Create a board in AroPi
   - Export to OBZ
   - Import in AsTeRICS Grid
   - Verify pictograms and layout
   ```

2. **Import Test:**
   ```
   - Download sample OBZ from openboardformat.org
   - Import into AroPi
   - Verify board appears in list
   - Check pictograms in catalog
   ```

3. **Round-trip Test:**
   ```
   - Export AroPi board → OBZ
   - Delete original board
   - Import the OBZ
   - Verify all data preserved
   ```

### Sample OBF Files

Download test files from:
- https://www.openboardformat.org/examples
- https://github.com/open-aac/obf (test fixtures)

## Future Enhancements

- [ ] Multi-board export (board navigation)
- [ ] Sound support
- [ ] URL-based image references (download on import)
- [ ] Preserve complex grid layouts
- [ ] Board linking/navigation
- [ ] OBF validation before export
- [ ] Progress indicators for large imports

## Troubleshooting

### Import Fails

**Error: "No board file found"**
- OBZ package is corrupted or invalid
- Try extracting manually and checking contents

**Error: "Unknown error"**
- Check file permissions
- Ensure file is valid JSON (for .obf)
- Check Android logs for details

### Export Fails

**Error: "Error exportant"**
- Check storage permissions
- Ensure enough disk space
- Verify board has pictograms

### Images Not Showing

- Custom images: Check file paths exist
- Imported images: Check `imported_images/` directory
- Drawable resources: Verify R.drawable references

## Resources

- **OBF Specification**: https://docs.google.com/document/d/1Bnl5neOf9-y53yOAGjd8BzQ7jvAdLhcB6y9Zw7ITYbA/edit
- **OpenAAC Website**: https://www.openaac.org/
- **Open Board Format**: https://www.openboardformat.org/
- **AsTeRICS Grid**: https://grid.asterics.eu/
- **GitHub - OBF**: https://github.com/open-aac/obf

## License

OBF/OBZ support in AroPi follows the same license as the main project.
The Open Board Format specification is open and free to implement.

# Pictogram Catalog JSON Format

The `pictogram_catalog.json` file contains all pictograms organized by categories.

## Structure

```json
{
  "categories": {
    "category_name": [
      {
        "id": "unique_id",
        "labels": {
          "es": "spanish_label",
          "ca": "catalan_label"
        },
        "iconRes": 2131165314,
        "grammarType": "noun|verb|adjective|pronoun"
      }
    ]
  }
}
```

## Fields

- **id**: Unique identifier for the pictogram
- **labels**: Map of language codes to translated labels
  - Keys must match `AppLanguage` enum values: `SPANISH`, `CATALAN`
  - Add more languages by adding entries here
- **iconRes**: Android resource ID for the icon (currently placeholder: 2131165314 = R.drawable.ic_pictogram_placeholder)
- **grammarType**: Grammatical type for composition rules (noun, verb, adjective, pronoun, etc.)

## Categories

Current categories:
- **subjects**: People and pronouns (yo, tú, niña, niño, mamá, papá)
- **actions**: Verbs (querer, comer, beber, jugar, dormir, ir)
- **objects**: Things and food (galleta, manzana, agua, leche, pan)
- **places**: Locations (casa, escuela, parque)
- **feelings**: Emotions and states (feliz, triste, cansado)

## Adding New Content

### Add a new pictogram
Add an entry to the appropriate category array.

### Add a new category
Add a new key to the `categories` object with an array of pictograms.

### Add a new language
1. Add the language to `AppLanguage` enum in `AppSettings.kt`
2. Add the language key to each pictogram's `labels` map

## File Location

- **Default data**: `app/src/main/assets/pictogram_catalog.json` (bundled with app)
- **Runtime data**: `context.filesDir/pictogram_catalog.json` (user modifications)

The app loads from runtime data if it exists, otherwise falls back to the bundled default.

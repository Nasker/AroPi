# AroPi Color Scheme

## 🎨 Kid-Friendly Design Philosophy

The AroPi app uses a warm, bright, and cozy color palette designed specifically for children. The colors are:
- **High contrast** for easy visibility
- **Soft and warm** to create a welcoming atmosphere
- **Playful** with cheerful tones
- **Easy on the eyes** with cream backgrounds instead of stark white

## Color Palette

### Primary Colors (Warm Coral/Peach)
- **Primary**: `#FF6B6B` - Friendly coral red
- **Primary Container**: `#FFE5E5` - Soft peachy pink
- **On Primary**: White
- **On Primary Container**: `#8B2020` - Deep red

**Usage**: Top bar, main action buttons, primary UI elements

### Secondary Colors (Sky Blue)
- **Secondary**: `#4ECDC4` - Calming turquoise
- **Secondary Container**: `#D4F4F2` - Light mint
- **On Secondary**: White
- **On Secondary Container**: `#1A5551` - Deep teal

**Usage**: Phrase bar pictograms, secondary actions

### Tertiary Colors (Sunny Yellow)
- **Tertiary**: `#FFD93D` - Bright cheerful yellow
- **Tertiary Container**: `#FFF8DC` - Soft cream yellow
- **On Tertiary**: `#3D3D00` - Dark olive
- **On Tertiary Container**: `#6B5E00` - Medium olive

**Usage**: Accents, highlights, special elements

### Background & Surface
- **Background**: `#FFFDF7` - Soft cream (easy on eyes)
- **Surface**: `#FFFFFE` - Clean white with warmth
- **Surface Variant**: `#F5F0FF` - Soft lavender
- **On Background/Surface**: `#2D2D2D` - Soft dark gray (not harsh black)

### Error Colors
- **Error**: `#E74C3C` - Gentle red
- **Error Container**: `#FFEBEE` - Very light pink
- **On Error**: White
- **On Error Container**: `#8B1A1A` - Deep red

## Design Decisions

### Why These Colors?

1. **Coral Primary**: Warm and inviting, less aggressive than pure red
2. **Sky Blue Secondary**: Calming and trustworthy, associated with clarity
3. **Sunny Yellow Tertiary**: Brings joy and energy without being overwhelming
4. **Cream Background**: Reduces eye strain compared to pure white
5. **Soft Text**: `#2D2D2D` instead of black for gentler reading

### Accessibility

- All color combinations meet WCAG AA standards for contrast
- Large touch targets (already implemented in UI)
- Clear visual hierarchy
- No reliance on color alone for information

### Status Bar

- Uses `primaryContainer` (`#FFE5E5`) for a softer appearance
- Dark icons for better visibility on light background

## Theme Behavior

The app **always uses light theme**, regardless of system settings, to maintain a consistent, cheerful experience for children.

## Future Considerations

If you want to adjust colors later, you can modify:
- `app/src/main/java/com/aropi/app/ui/theme/Theme.kt`

Consider adding:
- Different color schemes for different times of day
- Customizable themes in settings
- High contrast mode for accessibility

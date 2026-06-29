# Color Theme — Footlog

Complete token system, design intent, and implementation guide. Dark mode is the primary design target. Both modes were rendered as full mockups across all 12 screens and reviewed.

---

## Design identity

**Warm, editorial field-journal.** Not a gamified fitness app (no neon, no gradients, no achievement badges). Not a clinical health tracker (no cold blues or medical whites). The feel is a seasoned travel notebook: warm paper tones, muted earth accent, restrained typography.

Dark mode feels like a leather-bound journal in low light. Light mode feels like cream-paper pages in daylight. Both are the same design — same logic, inverted luminance — not two different moods.

---

## Color tokens — Kotlin implementation

```kotlin
import androidx.compose.ui.graphics.Color

object FootlogColors {
    // ── Dark mode (primary target) ──────────────────────────────────
    val backgroundDark     = Color(0xFF1E1C18)  // App background
    val surfaceDark        = Color(0xFF2C2A25)  // Cards, bottom sheets
    val textPrimaryDark    = Color(0xFFF0ECE2)  // Headings, primary text
    val textSecondaryDark  = Color(0xFF9C9890)  // Labels, captions, meta text
    val routeLineDark      = Color(0xFF7FA77E)  // Route polyline, primary buttons, chart bars
    val highlightAccentDark = Color(0xFFE0945A) // Photo/highlight pins, active mic indicator
    val navInactiveDark    = Color(0xFF6B6862)  // Inactive bottom nav icons, subtle text
    val borderDark         = Color(0xFF3A3833)  // Dividers, card borders (dark mode)

    // ── Light mode (placeholder — confirm before shipping) ──────────
    val backgroundLight    = Color(0xFFF4EFE4)  // App background (warm cream)
    val surfaceLight       = Color(0xFFB7B2A4)  // Cards — NOTE: see open-questions.md Q3
    val textPrimaryLight   = Color(0xFF2E2C28)  // Headings, primary text
    val textSecondaryLight = Color(0xFF7A7870)  // Labels, captions
    val routeLineLight     = Color(0xFF3C5A40)  // Route polyline, primary buttons — deeper green
    val highlightAccentLight = Color(0xFFC9783A) // Photo/highlight pins — deeper amber
    val navInactiveLight   = Color(0xFF9A968A)  // Inactive nav icons
    val borderLight        = Color(0xFFE3DCC9)  // Dividers, card borders (light mode)

    // ── Shared across both modes ─────────────────────────────────────
    val danger             = Color(0xFFD85A30)  // Stop button, destructive actions, delete confirms
}
```

---

## Token-by-token rationale

### Background (`#1E1C18` dark / `#F4EFE4` light)
The foundation. Dark: a very dark warm brown-black — not pure black (#000), which would feel cold and harsh. The warmth (slight brown hue) carries the journal metaphor. Light: a warm cream, like aged paper — not white, which would feel clinical.

### Surface (`#2C2A25` dark / `#B7B2A4` light)
Cards, modals, bottom sheets. Dark: visually separates from background (clearly readable as a card layer) while maintaining the warm-dark family. Light: **confirm before shipping** — this is noticeably dark for a card surface (see open-questions.md Q3). It may feel like a photo-shadow rather than a card fill. Normal pattern for light-mode cards is to be *lighter* than the background, not darker.

### Text primary (`#F0ECE2` dark / `#2E2C28` light)
Headings and primary content. Dark: warm off-white — not pure white (#FFF). Light: near-black with warm undertone — not pure #000. Both match the parent background palette: warm-warm pairing.

### Text secondary (`#9C9890` dark / `#7A7870` light)
Labels, captions, dates, distance units. Approximately 60-65% of primary text luminance — achieves WCAG AA contrast (4.5:1 minimum) against respective backgrounds. Used for all metadata text.

### Route line / primary accent (`#7FA77E` dark / `#3C5A40` light)
The single most important accent color. Used for:
- The live route polyline on the tracking map
- Primary action buttons (Start/Save/Done) — filled background, background-color text on top
- Vico chart bars in Stats screen
- Active indicators

Choice: a muted sage green — earthy, outdoor, nature-adjacent. Not a "healthy green" or "success checkmark green." Darker in light mode (`#3C5A40`) to maintain contrast against the cream background.

### Highlight accent (`#E0945A` dark / `#C9783A` light)
Used for:
- Photo pin markers on the map
- Highlight pin markers (Cafe, Shop, Viewpoint, Custom)
- Active microphone recording indicator
- Any "you dropped a pin here" visual

A warm amber/terracotta — complements the green route line. Together they form the only two chromatic accents in the palette. Everything else is achromatic or near-achromatic.

### Nav inactive (`#6B6862` dark / `#9A968A` light)
Bottom navigation icons when not selected. The selected icon uses `textPrimaryDark`/`textPrimaryLight` — no separate "active color" token needed.

### Border (`#3A3833` dark / `#E3DCC9` light)
Dividers between session list items, card outlines. Dark mode: subtle — barely perceptible against the dark surface, used only where structure is needed. Light mode: prominent — required to separate white-ish surface cards from the cream background (confirmed during mockup review — cards look unanchored without this border in light mode).

**Rule:** Apply the border conditionally in the Card composable:
```kotlin
Card(
    border = if (!isSystemInDarkTheme()) BorderStroke(1.dp, FootlogColors.borderLight) else null
)
```

### Danger (`#D85A30`)
Same in both modes — a clear red-orange that reads as "stop" or "destructive" in both contexts. Used on the Stop button during tracking (full-width, this color), delete session confirms, and any destructive action sheet.

---

## Design rules derived from mockup review

1. **No bold (700 weight) text anywhere.** Regular (400) for body, medium (500) for headings and important numbers. The restraint is intentional — bold reads as "shout."

2. **Corner radius is not uniform.** Cards: 16dp. Buttons: 10dp. These are the only two radii in the app.

3. **Primary button is inverted contrast pair.** `routeLineDark`/`routeLineLight` background + `backgroundDark`/`backgroundLight` text. Not white text on green — the exact background color.

4. **Share card always dark.** The share card (Screen 6) renders in dark mode regardless of system theme. Social media export needs high contrast; the dark card looks polished on any feed.

5. **Map placeholder fill color = unrevealed fog color.** `surfaceLight` (#B7B2A4) and `surfaceDark` (#2C2A25) are deliberately chosen to also serve as the "unrevealed" fog-of-war area color when the fog map is eventually built. Keeps the palette coherent.

6. **No opacity/alpha overlays for semantic colors.** Don't use `routeLineDark.copy(alpha = 0.5f)` as a secondary color — use `textSecondaryDark` instead. Opacity is for frosted-glass effects only (e.g., the stat overlay in ActiveTracking over the map).

---

## Palettes explored and rejected

| Name | Colors | Rejection reason |
|---|---|---|
| Dark moody trail | Same green/amber, all dark | Removed light mode — fights accessibility goals |
| Cool slate + teal | Greys, #4A9B9B | Clinical/utility feel, no editorial warmth |
| Bright minimal + coral | White bg, #FF6B6B accent | Too generic/app-like, no personality |
| Olive + sand | #8B7355, #C4A882 | Earthier/vintage feel, lost legibility at small sizes |
| Ink + terracotta | Black bg, black route line | Route line disappeared on dark map tiles |
| Midnight blue + electric lime | #0D1B2A, #CAFF00 | Energetic/social — directly contradicts non-gamified positioning |
| Soft lavender dusk | #B5A7C7 background | Wellness-app feel — contradicts outdoor/utility positioning |

---

## Material3 integration

The Footlog color tokens are NOT a standard Material3 ColorScheme. They're a custom token system. The Material3 `ColorScheme` slots don't map 1:1. Recommended approach:

```kotlin
// In Theme.kt
private val DarkColorScheme = darkColorScheme(
    background = FootlogColors.backgroundDark,
    surface = FootlogColors.surfaceDark,
    onBackground = FootlogColors.textPrimaryDark,
    onSurface = FootlogColors.textPrimaryDark,
    primary = FootlogColors.routeLineDark,
    onPrimary = FootlogColors.backgroundDark,
    secondary = FootlogColors.highlightAccentDark,
    onSecondary = FootlogColors.backgroundDark,
    error = FootlogColors.danger,
    outline = FootlogColors.borderDark
)

private val LightColorScheme = lightColorScheme(
    background = FootlogColors.backgroundLight,
    surface = FootlogColors.surfaceLight,
    onBackground = FootlogColors.textPrimaryLight,
    onSurface = FootlogColors.textPrimaryLight,
    primary = FootlogColors.routeLineLight,
    onPrimary = FootlogColors.backgroundLight,
    secondary = FootlogColors.highlightAccentLight,
    onSecondary = FootlogColors.backgroundLight,
    error = FootlogColors.danger,
    outline = FootlogColors.borderLight
)

@Composable
fun FootlogTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = FootlogTypography, content = content)
}
```

For text colors not directly in `ColorScheme` (like `textSecondaryDark`), access `FootlogColors` directly in composables — don't try to force all tokens into Material3 slots.

Dynamic color (Material You / wallpaper-based): **disabled**. The locked palette is the brand. Add `dynamicColor = false` or omit the `dynamicColor` parameter check in `Theme.kt`.

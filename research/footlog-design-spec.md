# Footlog — design spec for build

Walk/run/cycle tracker with session journaling, point-tagged highlights, and a lifetime exploration map. Native Android, Kotlin, Jetpack Compose, Material3.

This spec is build-ready: color tokens, nav graph, then full composable-level structure per screen. No Figma intermediate — build directly from this.

## Color tokens

Define as a Compose `ColorScheme` extension or custom token object, not hardcoded hex in composables. Both dark and light modes are confirmed — both were rendered as full mockups across all 12 screens and reviewed; layout, spacing, and component patterns held up consistently in both. Light mode needed a thin border (#E3DCC9) on white surface cards for legibility against the cream background; dark mode doesn't need this since its surface/background contrast is already sufficient — apply that border conditionally in the `Card` composable based on theme.

```kotlin
object FootlogColors {
    // dark (primary target)
    val backgroundDark = Color(0xFF1E1C18)
    val surfaceDark = Color(0xFF2C2A25)
    val textPrimaryDark = Color(0xFFF0ECE2)
    val textSecondaryDark = Color(0xFF9C9890)
    val routeLineDark = Color(0xFF7FA77E)
    val highlightAccentDark = Color(0xFFE0945A)
    val navInactiveDark = Color(0xFF6B6862)
    val borderDark = Color(0xFF3A3833)

    // light
    val backgroundLight = Color(0xFFF4EFE4)
    val surfaceLight = Color(0xFFB7B2A4)
    val textPrimaryLight = Color(0xFF2E2C28)
    val textSecondaryLight = Color(0xFF7A7870)
    val routeLineLight = Color(0xFF3C5A40)
    val highlightAccentLight = Color(0xFFC9783A)
    val navInactiveLight = Color(0xFF9A968A)
    val borderLight = Color(0xFFE3DCC9)

    // shared across modes
    val danger = Color(0xFFD85A30) // stop button, destructive actions
}
```

Primary action button: filled, route-line color background, background-color text on top (inverted contrast pair).
Card corner radius: 16dp. Button corner radius: 10dp. Standard padding: 16-20dp.
Typography: single sans family, two weights only (regular 400, medium 500). No bold/700 anywhere.

## Navigation graph

Bottom nav, 4 destinations: **Home, History, Routes, Stats**. Settings is a top-bar icon on Home, not a tab.

```
NavHost
├── Home (bottom nav)
├── History (bottom nav)
├── Routes (bottom nav)
├── Stats (bottom nav)
├── ActiveTracking (full screen, no bottom nav, launched from Home)
│   ├── NoteWriting (full screen overlay/modal, returns to ActiveTracking)
│   └── HighlightTagSheet (bottom sheet, inline on ActiveTracking)
├── SessionSummary (full screen, no bottom nav, after stopping a session)
├── ShareCard (full screen, no bottom nav, from SessionSummary)
├── SessionDetail (full screen, no bottom nav, from History or Home recent list)
│   ├── NoteView (full screen, from SessionDetail)
│   └── HighlightDetail (full screen, from tapping a pin in SessionDetail)
└── Onboarding (first-launch only, before Home)
```

Pass `sessionId` as nav arg into SessionDetail, ShareCard, NoteView. Pass nothing extra into ActiveTracking — it creates a new session on launch.

## Screen 1 — Onboarding

First-launch only, 3-step horizontal pager, skippable after permissions granted.

```
Scaffold
└── HorizontalPager (3 pages)
    ├── Page 1: app intro illustration + short tagline, "Next" button
    ├── Page 2: GPS permission request
    │   - explanation text: why location is needed
    │   - "Allow location access" button → triggers system permission dialog
    ├── Page 3: camera + notification permission request
    │   - same pattern, "Get started" button → navigates to Home, marks onboarding complete in DataStore
└── Row of 3 dot indicators, bottom center
```

Store onboarding-complete flag in DataStore/SharedPreferences, check on app launch to skip straight to Home on subsequent opens.

## Screen 2 — Home

```
Scaffold(topBar, bottomBar)
├── TopAppBar
│   ├── Greeting text, left ("Good morning" or similar, optional streak text below)
│   └── Settings icon button, right
├── Column (scrollable, LazyColumn recommended)
│   ├── Row — 3 equal-weight Cards: Walk / Run / Cycle
│   │   - each: icon above, label below, filled route-line-color background
│   │   - onClick → navigate to ActiveTracking, pass activityType arg
│   ├── Spacer(24dp)
│   ├── Text "Recent sessions" (section header, medium weight)
│   ├── LazyRow — session cards (empty state if list is empty: text "No walks yet — start one above")
│   │   - each card: small map thumbnail, date, activity icon, distance
│   │   - onClick → navigate to SessionDetail(sessionId)
│   ├── Spacer(24dp)
│   ├── Text "Your explored map" (section header)
│   └── Card (large, tappable)
│       - if no sessions yet: empty state — simple text "Your map fills in as you walk" + small illustration, no fog logic needed yet
│       - if sessions exist: fog map thumbnail placeholder (actual rendering TBD separately)
│       - "View full map" text link, bottom-right of card
└── NavigationBar (Home active)
```

## Screen 3 — Active tracking

```
Box (full screen, no Scaffold bottom bar)
├── MapView (fills most of screen) — live GPS trail as route-line-colored polyline
├── Column, top, over map, semi-transparent surface background
│   └── Row — 3 stat blocks: Distance | Time | Pace
│       - large number, small label beneath each
├── Column, bottom-right, floating over map
│   ├── FAB — camera icon → opens system camera or gallery picker, on success creates a PhotoPin at current GPS coordinate
│   └── FAB — flag/pin icon → opens HighlightTagSheet (bottom sheet)
├── FAB, bottom-left, floating over map
│   └── Note icon → navigate to NoteWriting
└── Button, bottom center, full width minus margins, danger color
    └── "Stop" — on click, navigate to SessionSummary(sessionId)
```

HighlightTagSheet (ModalBottomSheet, triggered from flag FAB):
```
ModalBottomSheet
├── Category chips row: Cafe, Shop, Viewpoint, Custom
├── Emoji picker (simple grid or text field accepting emoji input)
├── TextField — name input
├── TextField — optional short note
└── Button "Add to route"
```

## Screen 4 — Note writing

```
Scaffold
├── TopAppBar — back arrow, label "Note for [session name], [distance so far]"
├── Column (centered, generous vertical spacing)
│   ├── IconButton, large (80dp), circular, primary color — mic icon
│   │   - tap to start/stop voice recording, pulsing ring animation while active
│   ├── Text "or type instead" (text button, toggles to TextField below)
│   └── TextField (multiline, shown if typing mode selected, hidden otherwise)
└── Button, bottom, full width — "Save", disabled until input exists
    - on click, attach note to current session, navigate back to ActiveTracking
```

## Screen 5 — Session summary

```
Scaffold
├── TopAppBar — no back arrow (this is a forced step after stopping), title "Walk complete"
├── Column
│   ├── Card — map snapshot of completed route, static image or simplified MapView
│   ├── TextField — editable session title, prefilled with auto-generated name (e.g. "[activityType] near [area]")
│   ├── Row — 3 stat chips: Distance | Duration | Pace
│   ├── Row — Switch + label "Save as favorite route"
│   └── Row, 2 buttons
│       ├── OutlinedButton "Done" → navigate to Home, clear back stack to Home
│       └── Button (filled) "Share" → navigate to ShareCard(sessionId)
```

## Screen 6 — Share card

```
Scaffold
├── TopAppBar — close icon (X), right-aligned, no title
├── Column
│   ├── Card, 9:16 aspect ratio — share image preview
│   │   - dark background, route line in accent color, distance/duration/date overlaid as large text bottom-left
│   └── Row — 4 IconButton + label pairs: WhatsApp, Instagram, Save, Copy link
│       - each triggers Android Intent.ACTION_SEND with the rendered image, or system share sheet
```

Render the share card as a Compose composable captured to bitmap (use `graphicsLayer` + `Canvas.toBitmap` or `ImageBitmap` capture) rather than a separate image-generation step.

## Screen 7 — History

```
Scaffold(topBar, bottomBar)
├── TopAppBar — title "History"
├── LazyColumn
│   ├── grouped by date — sticky header per group ("This week", "Last week", or actual dates)
│   └── per session: Row
│       - small square map thumbnail, left
│       - Column: activity icon + name, distance + date (secondary text)
│       - chevron icon, right
│       - onClick → navigate to SessionDetail(sessionId)
└── NavigationBar (History active)
```

## Screen 8 — Session detail

```
Scaffold
├── TopAppBar — back arrow, title = session name
├── Column
│   ├── MapView (static/read-only) — route polyline + pin markers for photos/highlights
│   │   - onClick on a pin → navigate to HighlightDetail(pinId)
│   ├── Row — stat chips: Distance | Duration | Pace
│   └── OutlinedButton, full width — "View note" with note icon
│       - hidden if session has no note
│       - onClick → navigate to NoteView(sessionId)
```

## Screen 9 — Note view

```
Scaffold
├── TopAppBar — back arrow, label "Note from [session name], [date]"
├── Column (centered, generous padding)
│   ├── if voice note: voice-memo-style player — play button, waveform graphic placeholder, duration label
│   └── Text — transcribed/typed note content, serif or larger line-height for readability
```

## Screen 10 — Highlight detail

```
Scaffold
├── TopAppBar — back arrow
├── Column
│   ├── Image, full width, the tagged photo (if present; placeholder icon if none)
│   ├── Row — emoji + name (e.g. "☕ Marine Drive Coffee")
│   ├── AssistChip — category label (e.g. "Cafe")
│   ├── Text — personal note, italic style
│   └── OutlinedButton, full width — "Open in maps" with nav icon
│       - onClick → Intent to open coordinates in default maps app
```

## Screen 11 — Routes

```
Scaffold(topBar, bottomBar)
├── TopAppBar — title "Your routes"
├── LazyColumn — empty state if none favorited: "Save a walk as a favorite to see it here"
│   └── per route: Card, Row
│       - route thumbnail map, left
│       - Column: route name, distance, "last walked [date]"
│       - star icon, filled, right (favorite indicator)
│       - onClick → navigate to SessionDetail(mostRecentSessionId for that route)
└── NavigationBar (Routes active)
```

## Screen 12 — Stats

```
Scaffold(topBar, bottomBar)
├── TopAppBar — title "Stats"
├── Column
│   ├── SegmentedButton/TabRow — "This week" | "All time" toggle
│   ├── Card — bar chart, 7 days, distance per day, single accent color bars
│   │   (use a Compose charting lib, e.g. Vico, or hand-rolled Canvas bars for simplicity)
│   └── Grid (2x2) — stat cards: Total distance | Total sessions | Current streak | Areas explored
│       - each: large number, small label beneath
└── NavigationBar (Stats active)
```

## Data model (minimum fields, for reference — full schema is a separate task)

```kotlin
data class Session(
    val id: String,
    val activityType: String, // walk, run, cycle
    val startTime: Long,
    val endTime: Long,
    val distanceMeters: Float,
    val title: String,
    val isFavoriteRoute: Boolean,
    val routePoints: List<LatLng>,
    val noteId: String?
)

data class Note(
    val id: String,
    val sessionId: String,
    val type: String, // voice, text
    val content: String, // text or file path to audio
    val createdAt: Long
)

data class Highlight(
    val id: String,
    val sessionId: String,
    val lat: Double,
    val lng: Double,
    val category: String, // cafe, shop, viewpoint, custom
    val emoji: String,
    val name: String,
    val note: String?,
    val photoPath: String?
)
```

## Build order recommendation

1. Color tokens + nav graph scaffold (empty screens, just routing working)
2. Home + ActiveTracking + SessionSummary (the core record loop)
3. History + SessionDetail (review loop)
4. NoteWriting + NoteView + HighlightTagSheet + HighlightDetail (annotation layer)
5. Routes + Stats (secondary views)
6. ShareCard + Onboarding (polish layer)

Fog-of-war map rendering is intentionally excluded from this spec — treat as a separate engineering task once core tracking is stable.

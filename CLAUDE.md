# Footlog — Android App

Privacy-first walk/run/cycle tracker with session journaling, point-tagged highlights (cafes, shops, custom spots), and a lifetime fog-of-war exploration map. Native Android, Kotlin + Jetpack Compose.

## Project identity

- **Package name:** `com.abhijit.footlog` (permanent — don't change after Play Store publish)
- **Min SDK:** 26 (Android 8.0)
- **Target/Compile SDK:** 36 (Android 16)
- **Language:** Kotlin 2.2.10
- **UI:** Jetpack Compose with Material3, Compose BOM 2026.02.01

## Build commands

```bash
./gradlew assembleDebug          # debug build
./gradlew assembleRelease        # release build
./gradlew :app:kspDebugKotlin    # KSP annotation processing (Room)
./gradlew signingReport          # get SHA-1 fingerprint for Maps API key
```

## Key files

- `research/footlog-design-spec.md` — complete 12-screen build spec with composable structure
- `research/build-roadmap.md` — granular task list (6 phases, ~35 tasks)
- `research/tech-stack.md` — all library versions with compatibility notes
- `research/open-questions.md` — decisions needed before/during build
- `research/color-theme.md` — color token system and Material3 integration
- `gradle/libs.versions.toml` — dependency version catalog

## Color tokens (locked)

Dark mode (primary target):
- Background: `#1E1C18`
- Surface: `#2C2A25`
- Text primary: `#F0ECE2`
- Text secondary: `#9C9890`
- Route line / primary accent: `#7FA77E`
- Highlight accent: `#E0945A`
- Nav inactive: `#6B6862`
- Border: `#3A3833`
- Danger: `#D85A30`

Light mode surface (`#B7B2A4`) is a placeholder — confirm before shipping.

## Architecture

- **No DI framework** — manual ViewModel factories or `viewModel()` lambdas. No Hilt.
- **No backend** — fully offline, all data in Room + internal storage.
- **No analytics, no crash reporting** — privacy-first by design.
- Pattern: ViewModel → Repository → Room DAO. Single-activity with Compose Navigation.

## Navigation

Navigation 2.9 with type-safe routes (`@Serializable` objects). No magic string routes.
4 bottom nav tabs: Home, History, Routes, Stats.
Full-screen destinations: ActiveTracking, SessionSummary, ShareCard, SessionDetail, NoteWriting, NoteView, HighlightDetail, Onboarding.

## External dependencies

- **Google Maps SDK:** `play-services-maps:19.2.0` + `maps-compose:6.4.1`
- **Location:** `play-services-location:21.3.0` via Foreground Service (type=location)
- **No background location** — foreground service only, no `ACCESS_BACKGROUND_LOCATION`
- **Voice notes:** `MediaRecorder` → AAC/M4A, stored in `context.filesDir/voice_notes/`
- **Charts:** Vico 2.0.0 (Compose-native, M3-aware)
- **Images:** Coil 2.7.0

## Build order (6 phases)

1. Color tokens + navigation scaffold (empty screen stubs)
2. Home + ActiveTracking + SessionSummary (core record loop)
3. History + SessionDetail (review loop)
4. NoteWriting + NoteView + Highlights (annotation layer)
5. Routes + Stats (secondary views)
6. ShareCard + Onboarding (polish layer)

Fog-of-war visualization deferred until after core tracking is stable. The `ExploredCell` Room table is added in Phase 2 and populated during tracking.

## Secrets management

- Maps API key in `local.properties` as `MAPS_API_KEY=...` — never commit
- Upload keystore stored outside the project folder — never commit
- `local.properties` is in `.gitignore` by default

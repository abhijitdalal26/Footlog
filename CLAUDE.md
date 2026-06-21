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
./gradlew signingReport          # get SHA-1 fingerprint
```

## Build status — ALL 6 PHASES COMPLETE

All 12 screens built and compiling. `assembleDebug` passes clean.

### What's built
- **Phase 1:** `FootlogColors`, `FootlogTypography`, type-safe Navigation 2.9 routes (`Screen.kt`), `AppNavHost`, `DataStore` onboarding flag
- **Phase 2:** Room DB (`SessionEntity`, `NoteEntity`, `HighlightEntity`, `ExploredCellEntity`), `LocationTrackingService` (foreground, FusedLocation 3s interval), `HomeScreen`, `ActiveTrackingScreen`, `SessionSummaryScreen` + ViewModels
- **Phase 3:** `HistoryScreen` (date-grouped), `SessionDetailScreen`
- **Phase 4:** `NoteWritingScreen` (MediaRecorder + pulsing mic animation), `NoteViewScreen` (MediaPlayer playback), `HighlightTagSheet` (ModalBottomSheet), `HighlightDetailScreen`, camera system intent
- **Phase 5:** `RoutesScreen`, `StatsScreen` (Canvas bar chart, consecutive-day streak, km² from ExploredCells)
- **Phase 6:** `ShareCardScreen` (GraphicsLayer bitmap capture + FileProvider), `OnboardingScreen` (HorizontalPager 3 pages)

### Bucket list (needs manual work before shipping)
- **MapLibre version** — using `org.maplibre.gl:android-sdk:11.8.3`. If Gradle can't resolve it, check https://github.com/maplibre/maplibre-native for latest version and update `libs.versions.toml`
- **MapLibre tile style** — `https://tiles.openfreemap.org/styles/liberty`. If tiles don't load, try `https://tiles.openfreemap.org/styles/bright`
- **Upload keystore** — generate and store outside project folder for Play Store release
- **App icon** — still using default Android launcher icon
- **Fog-of-war visualization** — `ExploredCell` table populated during tracking, Canvas overlay not yet built (see `research/fog-of-war-approach.md`)
- **Map thumbnails in History/Routes** — placeholder icon only; real snapshot capture deferred to v2
- **SpeechRecognizer transcription** — MediaRecorder records audio but live transcription not wired to `SpeechRecognizer` yet (the toggle UI is there)

## Key files

- `app/src/main/java/com/abhijit/footlog/ui/navigation/Screen.kt` — all 12 route destinations
- `app/src/main/java/com/abhijit/footlog/ui/navigation/AppNavHost.kt` — full nav graph
- `app/src/main/java/com/abhijit/footlog/data/db/FootlogDatabase.kt` — Room singleton
- `app/src/main/java/com/abhijit/footlog/service/LocationTrackingService.kt` — GPS foreground service
- `app/src/main/java/com/abhijit/footlog/ui/components/MapLibreView.kt` — MapLibre Compose wrapper
- `research/footlog-design-spec.md` — 12-screen build spec
- `gradle/libs.versions.toml` — all dependency versions

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

Light mode surface: `#EDE8DB` (confirmed — lighter than background as expected for cards).

## Architecture

- **No DI framework** — manual ViewModel factories via `ViewModelProvider.Factory`. No Hilt.
- **No backend** — fully offline, all data in Room + internal storage.
- **No analytics, no crash reporting** — privacy-first by design.
- Pattern: ViewModel → `SessionRepository` → Room DAOs. Single-activity with Compose Navigation.
- **No Google Maps** — MapLibre + OpenFreeMap tiles (no API key needed)
- `android.disallowKotlinSourceSets=false` in `gradle.properties` — required for KSP + AGP 9.x

## Navigation

Navigation 2.9 with type-safe routes (`@Serializable` sealed interface `Screen`). No magic string routes.
4 bottom nav tabs: Home, History, Routes, Stats.
Full-screen destinations: ActiveTracking, SessionSummary, ShareCard, SessionDetail, NoteWriting, NoteView, HighlightDetail, Onboarding.

## External dependencies

- **Map:** `org.maplibre.gl:android-sdk:11.8.3` + OpenFreeMap tiles (no API key)
- **Location:** `play-services-location:21.3.0` via Foreground Service (type=location) — no API key needed
- **No background location** — foreground service only
- **Voice notes:** `MediaRecorder` → AAC/M4A, stored in `context.filesDir/voice_notes/`
- **Charts:** Canvas-drawn bar chart in StatsScreen (no external charting lib)
- **Images:** Coil 2.7.0

## Secrets management

- No API keys required for current build
- Upload keystore stored outside project folder — never commit
- `local.properties` is in `.gitignore` by default

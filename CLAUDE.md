# Footlog — Android App

Walk/run/cycle tracker with session journaling, point-tagged highlights (cafes, shops, custom spots), and a lifetime fog-of-war exploration map. Native Android, Kotlin + Jetpack Compose.

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

## Build status — 6 PHASES + PROFILE + FIREBASE COMPLETE

13 navigation destinations built and compiling. `assembleDebug` passes clean.

### What's built
- **Phase 1:** `FootlogColors`, `FootlogTypography`, type-safe Navigation 2.9 routes (`Screen.kt`), `AppNavHost`, `DataStore` onboarding flag
- **Phase 2:** Room DB (`SessionEntity`, `NoteEntity`, `HighlightEntity`, `ExploredCellEntity`), `LocationTrackingService` (foreground, FusedLocation 3s interval), `HomeScreen`, `ActiveTrackingScreen`, `SessionSummaryScreen` + ViewModels
- **Phase 3:** `HistoryScreen` (date-grouped), `SessionDetailScreen`
- **Phase 4:** `NoteWritingScreen` (text-only BasicTextField), `NoteViewScreen`, `HighlightTagSheet` (ModalBottomSheet), `HighlightDetailScreen`, camera system intent
- **Phase 5:** `RoutesScreen`, `StatsScreen` (Canvas bar chart, consecutive-day streak, km² from ExploredCells)
- **Phase 6:** `ShareCardScreen` (GraphicsLayer bitmap capture + FileProvider), `OnboardingScreen` (HorizontalPager 3 pages)
- **Profile:** `ProfileScreen` (local name + photo picker, DataStore-backed), `ProfileViewModel`, avatar in HomeScreen TopAppBar
- **Firebase:** Google Sign-In (CredentialManager → Firebase Auth), Firestore write-through sync, Crashlytics crash reporting, `FootlogApplication` class

### App icon
- **Foreground (`ic_launcher_foreground.xml`):** Geometric footprint from SVGs — 2 ellipses (foot body + heel) + 5 circles (toes), all `#7FA77E`
- **Background (`ic_launcher_background.xml`):** White `#FFFFFF`
- Source SVGs in `app logo svg/` — `footlog_footprint.svg` (foreground), `footlog_background.svg` (background)
- Adaptive icon wired in `mipmap-anydpi/ic_launcher.xml` with `<monochrome>` support

### Bucket list (needs manual work before shipping)
- **MapLibre version** — using `org.maplibre.gl:android-sdk:11.8.3`. If Gradle can't resolve it, check the MapLibre GitHub releases and update `libs.versions.toml`
- **MapLibre tile styles** — dark: `https://tiles.openfreemap.org/styles/dark`, light: `https://tiles.openfreemap.org/styles/liberty`
- **Upload keystore** — generate and store outside project folder for Play Store release
- **Map thumbnails in History/Routes** — placeholder icon only; real snapshot capture deferred to next update

## Key files

- `app/src/main/java/com/abhijit/footlog/FootlogApplication.kt` — Application class; enables Crashlytics in release, disables in debug
- `app/src/main/java/com/abhijit/footlog/ui/navigation/Screen.kt` — all 13 route destinations
- `app/src/main/java/com/abhijit/footlog/ui/navigation/AppNavHost.kt` — full nav graph
- `app/src/main/java/com/abhijit/footlog/data/db/FootlogDatabase.kt` — Room singleton (version 2)
- `app/src/main/java/com/abhijit/footlog/service/LocationTrackingService.kt` — GPS foreground service
- `app/src/main/java/com/abhijit/footlog/data/repository/SessionRepository.kt` — single source of truth; write-through Firestore sync
- `app/src/main/java/com/abhijit/footlog/data/sync/FirebaseSyncRepository.kt` — all Firestore read/write operations; path: `users/{uid}/{collection}`
- `app/src/main/java/com/abhijit/footlog/ui/components/MapLibreView.kt` — MapLibre Compose wrapper (dark/light tile switching)
- `app/src/main/java/com/abhijit/footlog/util/AuthHelper.kt` — Google Sign-In via CredentialManager; returns `GoogleUserProfile` with `idToken`
- `app/src/main/res/drawable/ic_launcher_foreground.xml` — adaptive icon foreground (footprint geometry)
- `app/src/main/res/drawable/ic_launcher_background.xml` — adaptive icon background (white)
- `gradle/libs.versions.toml` — all dependency versions
- `firestore.rules` — Firestore security rules (`users/{uid}/**` locked to matching auth UID)

## Debugging map

| Symptom | Where to look |
|---|---|
| Session not saving | `SessionRepository.saveSession` → `SessionDao.insert` → check Room version |
| GPS not updating | `LocationTrackingService.locationFlow` + `accuracy > 50f` filter in `ActiveTrackingViewModel` |
| Firestore write silently failing | Logcat tag `SessionRepository` — `fireSync()` logs `Log.w` on failure |
| Sign-in not working | Logcat tag `ProfileViewModel` — common cause: SHA-1 not in Firebase Console |
| App crashes in production | Firebase Console → Crashlytics dashboard |
| Cloud data missing on new device | `SessionRepository.mergeFromCloud()` → check Firestore Console `users/{uid}/sessions` |
| Onboarding repeating | `AppPreferences.isOnboardingComplete` — check `OnboardingScreen` calls `setOnboardingComplete()` |
| Stats wrong | `getWeeklyDistances()` / `getCurrentStreak()` in `SessionRepository` |
| Map not loading | `MapLibreView.kt` tile URL; check network + MapLibre version in `libs.versions.toml` |

## Color tokens

Dark mode (primary target):
- Background: `#1E1C18`
- Surface: `#2C2A25`
- Text primary: `#F0ECE2`
- Text secondary: `#9C9890`
- Route line / primary accent: `#27C96B` (bright green — updated in Color.kt)
- Highlight accent: `#E0945A`
- Nav inactive: `#6B6862`
- Border: `#3A3833`
- Danger: `#D85A30`

Light mode:
- Background: `#F4EFE4`, Surface: `#EDE8DB`
- Route line: `#1A9048` (darker green)
- Text primary: `#2E2C28`, Text secondary: `#7A7870`

App icon foreground uses `#7FA77E` (original brand green — intentionally muted for icon use).

## Architecture

- **No DI framework** — manual ViewModel factories via `ViewModelProvider.Factory`. No Hilt.
- **Firebase backend** — Auth (Google Sign-In only), Firestore (cross-device sync), Crashlytics (crash reporting)
- **Firebase project:** `learningkotlin-7db3a688` (project number: `1056161409729`)
- **Firestore path structure:** `users/{uid}/sessions`, `users/{uid}/notes`, `users/{uid}/highlights`, `users/{uid}/explored_cells`
- **Sync strategy:** write-through (Room save → Firestore); merge on sign-in (upload local, pull missing remote)
- **Crashlytics:** disabled in debug builds (`BuildConfig.DEBUG`), auto-enabled in release
- Pattern: ViewModel → `SessionRepository` → Room DAOs + `FirebaseSyncRepository`. Single-activity with Compose Navigation.
- **No Google Maps** — MapLibre + OpenFreeMap tiles (no API key needed)
- `android.disallowKotlinSourceSets=false` in `gradle.properties` — required for KSP + AGP 9.x

## Navigation

Navigation 2.9 with type-safe routes (`@Serializable` sealed interface `Screen`). No magic string routes.
4 bottom nav tabs: Home, History, Routes, Stats.
Full-screen destinations: ActiveTracking, SessionSummary, ShareCard, SessionDetail, NoteWriting, NoteView, HighlightDetail, Onboarding, Profile.
Profile is reached via the avatar/icon in HomeScreen's TopAppBar (not a bottom tab).

## External dependencies

- **Map:** `org.maplibre.gl:android-sdk:11.8.3` + OpenFreeMap tiles (no API key)
- **Location:** `play-services-location:21.3.0` via Foreground Service (type=location) — no API key needed
- **No background location** — foreground service only
- **Charts:** Canvas-drawn bar chart in StatsScreen (no external charting lib)
- **Images:** Coil 2.7.0
- **Firebase:** BOM 33.7.0 — Auth, Firestore, Crashlytics (+ `kotlinx-coroutines-play-services` for `Task.await()`)
- **Google Sign-In:** Credential Manager `credentials:1.3.0-rc01` + `googleid:1.1.1`
- **Crashlytics Gradle plugin:** `3.0.2` (uploads mapping file on release builds)

## Secrets management

- `local.properties` — contains `WEB_CLIENT_ID` (Firebase OAuth client ID). Gitignored, never commit.
- `app/google-services.json` — Firebase config. Contains API key — do not add to `.gitignore` (standard Firebase practice; the key is restricted by package name + SHA-1)
- Upload keystore stored outside project folder — never commit

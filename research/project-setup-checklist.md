# Project Setup Checklist — Footlog

This project was created via Android Studio (Empty Activity / Compose template). Much of this is already done. Checkboxes mark what's complete vs. still needed.

---

## Phase 0 — What's already done (verify these)

- [x] Android Studio project created: "Footlog"
- [x] Package name: `com.abhijit.footlog`
- [x] Language: Kotlin
- [x] MinSDK: 26 (Android 8.0)
- [x] CompileSDK: 36 (Android 16)
- [x] Compose enabled: `buildFeatures { compose = true }`
- [x] Kotlin: 2.2.10
- [x] AGP: 9.2.1
- [x] Compose BOM: 2026.02.01 (in `libs.versions.toml`)
- [x] `libs.versions.toml` version catalog set up
- [x] `local.properties` in `.gitignore`
- [x] Research directory with design spec: `research/footlog-design-spec.md`

---

## Phase 1 — Add dependencies (DO THIS FIRST)

### 1.1 Update `gradle/libs.versions.toml`

Add to `[versions]`:
```toml
ksp = "2.2.10-2.0.2"
navigationCompose = "2.9.8"
room = "2.8.4"
datastorePreferences = "1.2.1"
playServicesLocation = "21.3.0"
playServicesMaps = "19.2.0"
mapsCompose = "6.4.1"
lifecycleViewmodelCompose = "2.10.0"
coil = "2.7.0"
vico = "2.0.0"
```

Add to `[libraries]`:
```toml
# Navigation
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }

# Room
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# DataStore
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastorePreferences" }

# Location + Maps
play-services-location = { group = "com.google.android.gms", name = "play-services-location", version.ref = "playServicesLocation" }
play-services-maps = { group = "com.google.android.gms", name = "play-services-maps", version.ref = "playServicesMaps" }
maps-compose = { group = "com.google.maps.android", name = "maps-compose", version.ref = "mapsCompose" }

# ViewModel
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewmodelCompose" }

# Images
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }

# Charts
vico-compose = { group = "com.patrykandpatrick.vico", name = "compose", version.ref = "vico" }
vico-compose-m3 = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }
```

Add to `[plugins]`:
```toml
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

### 1.2 Update root `build.gradle.kts`

No changes needed — AGP + Kotlin already declared via `libs.versions.toml`.

### 1.3 Update `app/build.gradle.kts`

In `plugins {}` block, add:
```kotlin
alias(libs.plugins.ksp)
```

In `android {}` block, add to `defaultConfig {}`:
```kotlin
manifestPlaceholders["MAPS_API_KEY"] = project.findProperty("MAPS_API_KEY") ?: ""
```

In `dependencies {}`, add:
```kotlin
implementation(libs.androidx.navigation.compose)
implementation(libs.androidx.room.runtime)
implementation(libs.androidx.room.ktx)
ksp(libs.androidx.room.compiler)
implementation(libs.androidx.datastore.preferences)
implementation(libs.play.services.location)
implementation(libs.play.services.maps)
implementation(libs.maps.compose)
implementation(libs.androidx.lifecycle.viewmodel.compose)
implementation(libs.coil.compose)
implementation(libs.vico.compose)
implementation(libs.vico.compose.m3)
```

### 1.4 Sync and verify

Run `./gradlew :app:dependencies` and check for version conflicts. Fix any before proceeding.

---

## Phase 2 — Google Maps API key

- [ ] Go to [Google Cloud Console](https://console.cloud.google.com)
- [ ] Create new project (or use existing Google account project)
- [ ] Enable "Maps SDK for Android": APIs & Services → Library → search "Maps SDK for Android" → Enable
- [ ] Create API key: APIs & Services → Credentials → + Create Credentials → API key
- [ ] Restrict the key:
  - Application restrictions: Android apps
  - Add package name: `com.abhijit.footlog`
  - Add SHA-1 fingerprint (debug): run `./gradlew signingReport` in Android Studio terminal, copy the SHA1 under `Variant: debug`
- [ ] Copy API key to `local.properties`:
  ```
  MAPS_API_KEY=AIza...your_key...
  ```
- [ ] Add to `AndroidManifest.xml` inside `<application>` tag:
  ```xml
  <meta-data
      android:name="com.google.android.geo.API_KEY"
      android:value="${MAPS_API_KEY}" />
  ```
- [ ] Verify `local.properties` is in `.gitignore` — it is, by default. Never commit the API key.

---

## Phase 3 — AndroidManifest.xml updates

Add all permissions to `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="28" />
```

Add the tracking foreground service inside `<application>`:
```xml
<service
    android:name=".tracking.LocationTrackingService"
    android:foregroundServiceType="location"
    android:exported="false" />
```

---

## Phase 4 — Package structure

Create these directories under `app/src/main/java/com/abhijit/footlog/`:

```
data/
  db/           → FootlogDatabase.kt, AppDatabase singleton
  dao/          → SessionDao.kt, NoteDao.kt, HighlightDao.kt, ExploredCellDao.kt
  entity/       → Session.kt, Note.kt, Highlight.kt, ExploredCell.kt
  repository/   → SessionRepository.kt, NoteRepository.kt

tracking/
  LocationTrackingService.kt
  LocationTrackingManager.kt

ui/
  theme/        → Color.kt (UPDATE with Footlog tokens), Theme.kt, Type.kt
  navigation/   → AppNavHost.kt, Screen.kt (sealed class for routes)
  screens/
    onboarding/ → OnboardingScreen.kt
    home/       → HomeScreen.kt, HomeViewModel.kt
    tracking/   → ActiveTrackingScreen.kt, ActiveTrackingViewModel.kt
    note/       → NoteWritingScreen.kt, NoteViewModel.kt, NoteViewScreen.kt
    summary/    → SessionSummaryScreen.kt, SessionSummaryViewModel.kt
    sharecard/  → ShareCardScreen.kt
    history/    → HistoryScreen.kt, HistoryViewModel.kt
    sessiondetail/ → SessionDetailScreen.kt, SessionDetailViewModel.kt
    highlight/  → HighlightTagSheet.kt, HighlightDetailScreen.kt
    routes/     → RoutesScreen.kt, RoutesViewModel.kt
    stats/      → StatsScreen.kt, StatsViewModel.kt
  components/  → reusable composables (StatChip.kt, SessionCard.kt, etc.)
```

---

## Phase 5 — Play Store keystore (DO THIS BEFORE FIRST RELEASE)

**Generate the upload keystore now. Never change it.**

In Android Studio:
1. Build → Generate Signed Bundle / APK
2. Select "Android App Bundle" (preferred for Play Store)
3. Click "Create new..." under Key store path
4. Choose a location **outside the project folder** (e.g., `C:\Users\abhijit\keystores\footlog.jks`)
5. Fill in:
   - Key store password: strong password, write it down
   - Alias: `footlog-key`
   - Key password: strong password (can be same as keystore password)
   - Validity: 10000 years (or 25+)
   - Certificate name/org: your details
6. Save the `.jks` file and passwords somewhere secure (password manager, not git)

**Why outside the project:** The keystore must never be committed to git. Even with `.gitignore`, keeping it outside the repo removes all risk of accidental commit.

**For CI/CD later:** Store keystore as a base64 environment variable in GitHub Secrets. For now, manual signing is fine.

**Get the release SHA-1 for Maps API key restriction:**
```
keytool -list -v -keystore C:\Users\abhijit\keystores\footlog.jks -alias footlog-key
```
Add this release SHA-1 to your Maps API key restriction alongside the debug SHA-1.

---

## Phase 6 — .gitignore audit

Verify these are in `.gitignore`:
```
# Secrets
local.properties
*.jks
*.keystore

# IDE
.idea/
*.iml

# Build outputs
build/
.gradle/
```

The default Android Studio `.gitignore` already covers most of this. Confirm `local.properties` is listed (it is by default). If you add a `keystore/` folder inside the repo for any reason, add it explicitly.

---

## Phase 7 — First build verification

After all the above:
- [ ] `./gradlew assembleDebug` — must succeed with no errors
- [ ] App launches on device/emulator showing "Hello Android!" (current placeholder)
- [ ] No "Missing API key" crashes (Maps SDK)
- [ ] `./gradlew :app:kspDebugKotlin` — KSP annotation processing runs cleanly (Room)

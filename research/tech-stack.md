# Tech Stack — Footlog

Verified against actual project configuration (`libs.versions.toml`, `build.gradle.kts`) as of June 2026.

## Core framework — already in project

| Library | Version in project | Source |
|---|---|---|
| Kotlin | 2.2.10 | `libs.versions.toml` |
| Android Gradle Plugin | 9.2.1 | `libs.versions.toml` |
| Compose BOM | 2026.02.01 | `libs.versions.toml` |
| Compose UI | 1.11.x (via BOM) | BOM-managed |
| Compose Material3 | 1.4.0 (via BOM) | BOM-managed |
| Core KTX | 1.19.0 | `libs.versions.toml` |
| Lifecycle Runtime KTX | 2.10.0 | `libs.versions.toml` |
| Activity Compose | 1.13.0 | `libs.versions.toml` |
| CompileSDK | 36 (Android 16) | `build.gradle.kts` |
| MinSDK | 26 (Android 8.0) | `build.gradle.kts` |
| TargetSDK | 36 | `build.gradle.kts` |
| Java compatibility | VERSION_11 | `build.gradle.kts` |

## Dependencies to add — not yet in project

Add these to `libs.versions.toml` [versions] and [libraries], then reference in `app/build.gradle.kts`.

### Versions to add to `[versions]`

```toml
ksp = "2.2.10-2.0.2"
navigationCompose = "2.9.8"
room = "2.8.4"
datastorePreferences = "1.2.1"
playServicesLocation = "21.3.0"
playServicesMaps = "19.2.0"
lifecycleViewmodelCompose = "2.10.0"
coroutines = "1.10.2"
coil = "2.7.0"
vico = "2.0.0"
```

> **KSP version rule:** the version prefix (`2.2.10`) must exactly match the Kotlin version in `[versions]`. AGP 9.0+ auto-upgrades KSP to `2.2.10-2.0.2` if you go lower, but pinning explicitly avoids ambiguity.

### Libraries to add to `[libraries]`

```toml
# KSP-processed
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Navigation
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }

# DataStore
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastorePreferences" }

# Location + Maps
play-services-location = { group = "com.google.android.gms", name = "play-services-location", version.ref = "playServicesLocation" }
play-services-maps = { group = "com.google.android.gms", name = "play-services-maps", version.ref = "playServicesMaps" }

# ViewModel in Compose
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewmodelCompose" }

# Coroutines
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# Image loading (for photo pins)
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }

# Charts (Stats screen bar chart — lightweight Canvas-based)
vico-compose = { group = "com.patrykandpatrick.vico", name = "compose", version.ref = "vico" }
vico-compose-m3 = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }
```

### Plugins to add to `[plugins]`

```toml
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

### `app/build.gradle.kts` additions

Add `alias(libs.plugins.ksp)` to the `plugins {}` block, then add to `dependencies {}`:

```kotlin
// Navigation
implementation(libs.androidx.navigation.compose)

// Room
implementation(libs.androidx.room.runtime)
implementation(libs.androidx.room.ktx)
ksp(libs.androidx.room.compiler)

// DataStore
implementation(libs.androidx.datastore.preferences)

// Location + Maps
implementation(libs.play.services.location)
implementation(libs.play.services.maps)

// ViewModel in Compose
implementation(libs.androidx.lifecycle.viewmodel.compose)

// Coroutines
implementation(libs.kotlinx.coroutines.android)

// Coil (image loading)
implementation(libs.coil.compose)

// Vico charts
implementation(libs.vico.compose)
implementation(libs.vico.compose.m3)
```

## Version compatibility notes

| Risk | Status |
|---|---|
| KSP must match Kotlin prefix | Pin `ksp = "2.2.10-2.0.2"` exactly — AGP will enforce this |
| Room 2.8.x requires KSP, not KAPT | Already using KSP — correct |
| Navigation 2.9.x: type-safe routes only | API changed from string routes to `@Serializable` objects in 2.8+. New pattern — see `build-roadmap.md` |
| Coil 2.x vs 3.x | Coil 3.0 is alpha/RC; stay on 2.7.0 stable |
| Vico 2.0.0 | Released stable in 2025 — Compose-first, M3-aware, no legacy Views dependency |
| Maps SDK 20.x | Requires API 23+. Min SDK 26 is fine |
| Java 11 target | Sufficient for all above libs |

## What is NOT in the stack (deliberate omissions)

- **Hilt / Dagger**: DI overkill for a single-developer offline app. Use manual ViewModel factories or `viewModel()` with factory lambdas.
- **Retrofit**: No network calls needed in v1.
- **Firebase**: No backend, no analytics, no crash reporting in v1. Add if needed later.
- **MLKit / SpeechRecognizer paid**: Use Android's built-in `SpeechRecognizer` for transcription — free, on-device. See `voice-notes-implementation.md`.
- **Kotlin Multiplatform (KMP)**: Android-only for v1; Room 3.0 KMP support exists if ever needed.

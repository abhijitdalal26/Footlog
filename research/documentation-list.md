# Documentation Reference List — Footlog

Every official documentation source you'll need during development. When something breaks or you're unsure, this is where to look first.

---

## Android Core

| Topic | URL |
|---|---|
| Jetpack Compose release notes + versions | https://developer.android.com/jetpack/androidx/releases/compose |
| Compose BOM version mapping | https://developer.android.com/jetpack/compose/bom/bom-mapping |
| Compose UI fundamentals | https://developer.android.com/develop/ui/compose |
| Compose state and ViewModel | https://developer.android.com/develop/ui/compose/state |
| Compose side effects (LaunchedEffect, DisposableEffect) | https://developer.android.com/develop/ui/compose/side-effects |
| Compose graphics + Canvas | https://developer.android.com/develop/ui/compose/graphics |
| GraphicsLayer / capture to bitmap | https://developer.android.com/develop/ui/compose/graphics/draw/modifiers |
| Compose performance | https://developer.android.com/develop/ui/compose/performance |

## Material Design 3

| Topic | URL |
|---|---|
| Material3 components for Compose | https://developer.android.com/develop/ui/compose/components |
| Material3 color system | https://m3.material.io/styles/color/the-color-system/key-colors-tones |
| Material3 typography | https://m3.material.io/styles/typography/overview |
| ModalBottomSheet | https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#ModalBottomSheet |
| HorizontalPager (Foundation) | https://developer.android.com/reference/kotlin/androidx/compose/foundation/pager/package-summary |

## Navigation

| Topic | URL |
|---|---|
| Navigation Compose release notes | https://developer.android.com/jetpack/androidx/releases/navigation |
| Navigation Compose get started | https://developer.android.com/develop/ui/compose/navigation |
| Type-safe navigation (2.8+ routes) | https://developer.android.com/guide/navigation/design/type-safety |
| Pass data between destinations | https://developer.android.com/guide/navigation/use-graph/pass-data |
| Navigate with bottom nav | https://developer.android.com/develop/ui/compose/navigation#bottom-nav |

## Room Database

| Topic | URL |
|---|---|
| Room release notes | https://developer.android.com/jetpack/androidx/releases/room |
| Room with Compose | https://developer.android.com/training/data-storage/room |
| Room DAO queries | https://developer.android.com/training/data-storage/room/accessing-data |
| Room with Kotlin Flows | https://developer.android.com/training/data-storage/room/async-queries |
| Room type converters (for LatLng list) | https://developer.android.com/training/data-storage/room/referencing-data |
| Room with KSP | https://developer.android.com/build/migrate-to-ksp |

## DataStore

| Topic | URL |
|---|---|
| DataStore preferences | https://developer.android.com/topic/libraries/architecture/datastore |
| DataStore release notes | https://developer.android.com/jetpack/androidx/releases/datastore |

## Location Services

| Topic | URL |
|---|---|
| FusedLocationProviderClient | https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient |
| Request location updates | https://developer.android.com/develop/sensors-and-location/location/request-updates |
| Location permissions guide | https://developer.android.com/develop/sensors-and-location/location/permissions |
| Background location limits | https://developer.android.com/about/versions/oreo/background-location-limits |
| Foreground service with location | https://developer.android.com/guide/components/foreground-services |
| Foreground service types (API 34) | https://developer.android.com/about/versions/14/changes/fgs-types-required |
| Codelab: while-in-use location | https://codelabs.developers.google.com/codelabs/while-in-use-location |

## Google Maps SDK for Android

| Topic | URL |
|---|---|
| Maps SDK for Android overview | https://developers.google.com/maps/documentation/android-sdk/overview |
| Maps SDK quickstart | https://developers.google.com/maps/documentation/android-sdk/start |
| Maps SDK API key setup + restrictions | https://developers.google.com/maps/documentation/android-sdk/get-api-key |
| Maps SDK usage and billing | https://developers.google.com/maps/documentation/android-sdk/usage-and-billing |
| Maps SDK release notes | https://developers.google.com/maps/documentation/android-sdk/release-notes |
| Polylines and polygons | https://developers.google.com/maps/documentation/android-sdk/polygon-tutorial |
| Custom markers | https://developers.google.com/maps/documentation/android-sdk/marker |
| Map snapshot (static capture) | https://developers.google.com/maps/documentation/android-sdk/snapshot |
| TileOverlay (for fog-of-war alternative) | https://developers.google.com/maps/documentation/android-sdk/tileoverlay |
| maps-compose library (GitHub) | https://github.com/googlemaps/android-maps-compose |

## Audio (Voice Notes)

| Topic | URL |
|---|---|
| MediaRecorder overview | https://developer.android.com/media/platform/mediarecorder |
| Audio capture guide | https://developer.android.com/guide/topics/media/mediarecorder |
| MediaPlayer overview | https://developer.android.com/guide/topics/media/mediaplayer |
| SpeechRecognizer | https://developer.android.com/reference/android/speech/SpeechRecognizer |

## Permissions

| Topic | URL |
|---|---|
| Permissions overview | https://developer.android.com/guide/topics/permissions/overview |
| Request runtime permissions | https://developer.android.com/training/permissions/requesting |
| RECORD_AUDIO restrictions | https://developer.android.com/training/permissions/requesting#explain |
| Android 13 notification permission | https://developer.android.com/develop/ui/views/notifications/notification-permission |
| Privacy best practices | https://developer.android.com/privacy/best-practices |

## Sharing and Intents

| Topic | URL |
|---|---|
| Share content with intents | https://developer.android.com/training/sharing/send |
| FileProvider setup | https://developer.android.com/reference/androidx/core/content/FileProvider |
| Save files to MediaStore (gallery) | https://developer.android.com/training/data-storage/shared/media |

## Play Store

| Topic | URL |
|---|---|
| App signing overview | https://developer.android.com/studio/publish/app-signing |
| Play App Signing (upload key) | https://support.google.com/googleplay/android-developer/answer/9842756 |
| Data Safety form guide | https://support.google.com/googleplay/android-developer/answer/10787469 |
| Target API level requirements | https://support.google.com/googleplay/android-developer/answer/11926878 |
| Play Console | https://play.google.com/console |

## Third-party Libraries

| Library | Documentation URL |
|---|---|
| Coil (image loading) | https://coil-kt.github.io/coil/ |
| Vico (charts) | https://patrykandpatrick.com/vico/wiki/ |
| KSP (Kotlin Symbol Processing) | https://kotlinlang.org/docs/ksp-overview.html |

## Build Tools

| Topic | URL |
|---|---|
| Android Gradle Plugin release notes | https://developer.android.com/build/releases/gradle-plugin |
| Version catalog (libs.versions.toml) | https://docs.gradle.org/current/userguide/platforms.html |
| Build variants and flavors | https://developer.android.com/build/build-variants |
| signingReport task (get SHA-1) | https://developer.android.com/studio/publish/app-signing#generate-key |

## Architecture

| Topic | URL |
|---|---|
| Architecture recommendations | https://developer.android.com/topic/architecture |
| ViewModel + Compose | https://developer.android.com/develop/ui/compose/libraries#viewmodel |
| Repository pattern | https://developer.android.com/topic/architecture/data-layer |
| Foreground services | https://developer.android.com/guide/components/foreground-services |

---

## Quick-reference: common problems → where to look

| Problem | Go to |
|---|---|
| KSP / Room annotation processing fails | Room release notes, KSP compatibility guide |
| Navigation: can't pass arguments between screens | Type-safe navigation guide |
| Map doesn't load (blank white screen) | Maps SDK API key setup, check logs for `AIzaNotFound` |
| Location not updating in background | Foreground service types, FGS documentation |
| `MediaRecorder` crashes on start | MediaRecorder docs, check RECORD_AUDIO permission granted |
| Share intent fails on WhatsApp | FileProvider setup guide |
| Permission dialog not showing | Runtime permissions guide, check if permanently denied |
| Bitmap capture is blank | GraphicsLayer documentation |

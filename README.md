# Footlog

A privacy-first activity tracker for Android. Record walks, runs, and cycles, journal your sessions, pin highlights along the route, and watch your lifetime exploration map fill in over time.

## Features

- Track walks, runs, and cycling sessions with live GPS route recording
- Journal notes on any session
- Pin highlights (cafes, shops, custom spots) with emoji tags and photos
- Lifetime fog-of-war exploration map built from every route you've taken
- Share session summary cards
- Google Sign-In with cross-device sync via Firestore
- Fully works offline — all data stored locally in Room, synced to cloud when signed in

## Tech stack

- Kotlin + Jetpack Compose (Material3)
- Room for local storage, DataStore for preferences
- MapLibre + OpenFreeMap tiles (no API key required)
- Firebase Auth (Google Sign-In), Firestore (sync), Crashlytics (crash reporting)
- FusedLocationProviderClient foreground service for GPS

## Build

Requires Android Studio with min SDK 26 (Android 8.0), target SDK 36.

```bash
./gradlew assembleDebug
```

To enable Google Sign-In, add your Firebase OAuth web client ID to `local.properties`:

```
WEB_CLIENT_ID="your-web-client-id.apps.googleusercontent.com"
```

`google-services.json` is already included in `app/` for the Firebase project.

## Project structure

```
app/src/main/java/com/abhijit/footlog/
  data/
    dao/          Room DAOs
    db/           Database singleton + type converters
    entity/       Room entities (Session, Note, Highlight, ExploredCell)
    preferences/  DataStore wrapper
    repository/   SessionRepository — single source of truth
    sync/         FirebaseSyncRepository — Firestore read/write
  service/        LocationTrackingService — foreground GPS service
  ui/
    components/   Reusable Compose components
    navigation/   Type-safe nav routes + NavHost
    screens/      One file per screen (13 screens total)
    theme/        Color tokens, typography
    viewmodels/   One ViewModel per screen
  util/           CellUtils (fog-of-war grid), AuthHelper (Google Sign-In)
```

## License

Personal project — all rights reserved.

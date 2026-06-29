# APIs and Services — Footlog

All external dependencies. For each: cost, setup, permissions required.

---

## 1. Google Maps SDK for Android

**Library:** `com.google.android.gms:play-services-maps:19.2.0`

**Cost:** Free for basic map display and polyline drawing.
- Maps SDK for Android bills under the "Mobile Native Dynamic Maps" SKU
- The free tier ($200/month Google Maps credit) covers ~28,000 map loads/month — far above any indie app volume
- This app does NOT use Places API, Geocoding API, Directions API, or Routes API — those are the paid ones
- Confirmed: route polyline drawing on a live map is covered under the base dynamic maps SKU

**What it provides:**
- `GoogleMap` (legacy View-based) and `MapView` Composable integration via `maps-compose`
- Route polyline drawing (`PolylineOptions`)
- Camera control (`CameraUpdateFactory`)
- Custom markers for photo/highlight pins
- Static map snapshots via `GoogleMap.snapshot()` callback

**Companion library for Compose:**
```
com.google.maps.android:maps-compose:6.4.1
```
This wraps the Maps SDK in a `GoogleMap { }` composable. Essential — do not use `AndroidView` to embed `MapView` manually.

**Setup steps:**
1. Google Cloud Console → Create project (or use existing)
2. Enable "Maps SDK for Android" in APIs & Services → Library
3. Create an API key: APIs & Services → Credentials → + Create Credentials → API key
4. Restrict the key: Application restrictions → Android apps → add package `com.abhijit.footlog` + SHA-1 from signingReport
5. Add to `local.properties`:
   ```
   MAPS_API_KEY=your_key_here
   ```
6. Read the key in `build.gradle.kts` via `buildConfigField` or directly in `AndroidManifest.xml`:
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="${MAPS_API_KEY}" />
   ```
7. For manifest placeholder, add to `android {}` block in `build.gradle.kts`:
   ```kotlin
   defaultConfig {
       manifestPlaceholders["MAPS_API_KEY"] = project.findProperty("MAPS_API_KEY") ?: ""
   }
   ```

**Permissions required in manifest:** None specific to Maps SDK — the map displays without additional permissions. Location permissions are for the location tracking, not the map rendering.

**API key restriction best practices:**
- Restrict by SHA-1 fingerprint: get debug SHA-1 with `./gradlew signingReport`
- Add both debug AND release fingerprints to the same key (or use separate keys per environment)
- Store key in `local.properties` (gitignored), never commit to git
- In CI/production: inject via environment variable

---

## 2. Android Location Services — FusedLocationProviderClient

**Library:** `com.google.android.gms:play-services-location:21.3.0`

**Cost:** Free — part of Google Play Services installed on all Android devices.

**FusedLocationProviderClient vs LocationManager:**

| Factor | FusedLocationProvider | LocationManager |
|---|---|---|
| Power usage | Automatically batches and duty-cycles | Manual — you manage duty cycling |
| Accuracy | Fuses GPS + WiFi + cell towers | GPS-only (usually) |
| API simplicity | Single callback-based API | Multiple providers to manage |
| Background behavior | Plays well with system restrictions | Raw system — restrictive on modern Android |
| Recommendation | **Use this** | Only if GPS-only is a hard requirement |

**For Footlog's use case (live tracking during active session):**
- Run a Foreground Service with `foregroundServiceType="location"` while session is active
- Request `Priority.PRIORITY_HIGH_ACCURACY` with 3-5 second intervals
- Use `requestLocationUpdates()` with a `LocationCallback`
- When session stops, remove updates and stop the service

**Key code pattern:**
```kotlin
val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
    .setMinUpdateIntervalMillis(1000L)
    .build()
fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
```

**Android version-specific requirements:**

| Android version | Requirement |
|---|---|
| API 26+ (baseline) | `ACCESS_FINE_LOCATION` in manifest, runtime request |
| API 29 (Android 10) | Declare `foregroundServiceType="location"` on the service in manifest |
| API 31 (Android 12) | User can force coarse-only even if fine was granted — test for this |
| API 33 (Android 13) | `POST_NOTIFICATIONS` needed for the foreground service notification |
| API 34 (Android 14) | Foreground service of type `location` requires verifying location permission at creation time |

**Foreground Service manifest entry (required):**
```xml
<service
    android:name=".tracking.LocationTrackingService"
    android:foreground ServiceType="location"
    android:exported="false" />
```

**Permissions in manifest:**
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
<!-- Android 13+ notifications -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

**Do NOT declare `ACCESS_BACKGROUND_LOCATION`:** The session tracking happens via a foreground service while the user has the app visible or in foreground. Background location (the "Allow all the time" permission) is NOT needed and NOT requested — it adds Play Store friction and a full policy review.

---

## 3. Android SpeechRecognizer (Voice Transcription)

**Cost:** Free — uses on-device or Google's recognition service, no API key, no billing.

**What it does:** Converts the recorded voice note audio to text, giving users the option to save/review as text.

**API:** `android.speech.SpeechRecognizer` — built into Android.

**Limitations:**
- Requires internet for best accuracy (uses Google's cloud recognizer on connected devices)
- Works offline with the on-device model if downloaded (settings-dependent on the device)
- Language detection is automatic but works best with explicit locale
- Max audio length: typically up to 60 seconds per recognition request

**Alternative considered:** Google Cloud Speech-to-Text API — paid, requires API key, adds backend complexity. Rejected for v1.

**Required manifest entry:**
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

No additional permissions needed for SpeechRecognizer itself — it uses the microphone permission already needed for MediaRecorder.

---

## 4. MediaStore API (Save to Gallery)

**Cost:** Free — Android system API.

**Purpose:** Save the generated share card image to the user's photo gallery.

**API:** `android.provider.MediaStore.Images.Media`

**Required permissions:**
- Android 10+ (API 29+): No extra permission needed for app-created files via MediaStore
- Android 9 and below: `WRITE_EXTERNAL_STORAGE` (irrelevant given MinSDK 26, but Android 9 = API 28)
  - For API 28 (Android 9, within our API 26-28 range): still need `WRITE_EXTERNAL_STORAGE` in manifest with `android:maxSdkVersion="28"`

**Pattern:**
```kotlin
val values = ContentValues().apply {
    put(MediaStore.Images.Media.DISPLAY_NAME, "footlog_${sessionId}.jpg")
    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Footlog")
}
val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
```

---

## 5. FileProvider (Share card image via Intent)

**Cost:** Free — AndroidX library, no setup beyond manifest.

**Purpose:** Share the rendered bitmap to WhatsApp/Instagram via `Intent.ACTION_SEND`. Direct file paths can't be shared to other apps — FileProvider creates a content:// URI.

**Setup:** Already bundled in `androidx.core`. Requires one-time entry in `AndroidManifest.xml` and a `file_paths.xml` resource.

---

## Summary table

| Service | Cost | API Key | Permissions |
|---|---|---|---|
| Google Maps SDK | Free (display) | Yes | None for map itself |
| FusedLocationProvider | Free | No | ACCESS_FINE_LOCATION, FOREGROUND_SERVICE |
| SpeechRecognizer | Free | No | RECORD_AUDIO |
| MediaStore (gallery) | Free | No | None (API 29+) |
| FileProvider (share) | Free | No | None |

# Permissions and Privacy — Footlog

Full permission list, Android version-specific behavior, request flow for each, and Play Store data safety outline.

---

## Complete permission manifest

```xml
<!-- Location — core tracking feature -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Foreground service — required for GPS tracking while in app -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />

<!-- Notifications — Android 13+ requires explicit runtime permission for service notification -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Camera — for photo pins during sessions -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- Microphone — for voice notes -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />

<!-- External storage write — Android 9 and below only (covers API 26-28 in our range) -->
<uses-permission
    android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" />
```

**NOT declared:**
- `ACCESS_BACKGROUND_LOCATION` — not needed. Tracking only runs via foreground service while user is actively in-session. Play Store requires additional policy justification for background location — avoid entirely.
- `READ_EXTERNAL_STORAGE` — not needed if audio files are in `context.filesDir` (internal storage, no permission required).
- `INTERNET` — needed if you use Google Maps SDK (maps load tiles over network). Add it: `<uses-permission android:name="android.permission.INTERNET" />`. Maps SDK technically adds this automatically, but be explicit.

---

## Per-permission: when to ask, what happens if denied

### 1. ACCESS_FINE_LOCATION + ACCESS_COARSE_LOCATION

**When to ask:** Onboarding screen 2 ("Allow location access" button tap). This is the first launch.

**What to show before asking:** Brief rationale — "Footlog tracks your route while you walk. Location is only used while a session is active and never leaves your device."

**If denied:**
- User can't start a tracking session (the Walk/Run/Cycle buttons become disabled or show a snackbar)
- Show persistent banner on Home: "Location access is required to track walks. Tap to open settings."
- Deep link to app settings: `Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)`

**Android 12+ note:** Android now shows "Precise" and "Approximate" as two location options. FusedLocationProviderClient degrades gracefully to approximate if that's all that's granted — but GPS accuracy will be poor. Display a one-time warning if only approximate is granted.

**Runtime request pattern:**
```kotlin
val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)
ActivityCompat.requestPermissions(activity, locationPermissions, REQUEST_CODE_LOCATION)
```

### 2. POST_NOTIFICATIONS (Android 13+, API 33+)

**When to ask:** At the start of the first tracking session (when the foreground service notification is about to show), OR during onboarding if targeting simplicity.

**What to show before asking:** "Footlog shows a notification while tracking your walk so it can't be interrupted by the system. You can dismiss it anytime after your walk ends."

**If denied:** The foreground service can still run (Android 13+ allows this — the notification just doesn't appear in the notification drawer, but the Task Manager still shows it). Tracking still works — notification denial is non-fatal.

### 3. CAMERA

**When to ask:** First time the user taps the camera FAB during an active session.

**What to show before asking:** "To drop a photo pin at this spot, Footlog needs camera access."

**If denied:** Show a snackbar: "Camera access denied — photo pins won't be available. You can still add highlight pins and notes."

**If denied permanently (user ticked "Don't ask again"):** Show dialog with "Open settings" button.

### 4. RECORD_AUDIO

**When to ask:** First time the user taps the mic button in the Note Writing screen.

**What to show before asking:** "Tap the microphone to record a voice note. Audio is stored privately on your device."

**If denied:** Hide the mic button and show only the text input. The note still works as text-only — the feature degrades, not breaks.

---

## Android version-specific behavior summary

| API Level | Change | Impact |
|---|---|---|
| 29 (Android 10) | Background location restrictions | Not relevant — we don't use background location |
| 31 (Android 12) | User can force "Approximate" location | Degrade gracefully; warn user about GPS accuracy |
| 31 (Android 12) | `FOREGROUND_SERVICE_LOCATION` not required until API 34 | Declare it anyway for future-proofing |
| 33 (Android 13) | `POST_NOTIFICATIONS` is now a dangerous permission | Request at runtime before starting tracking |
| 33 (Android 13) | `READ_MEDIA_IMAGES` replaces `READ_EXTERNAL_STORAGE` | Not needed if using `filesDir` for audio; only needed for gallery photo picker |
| 34 (Android 14) | `FOREGROUND_SERVICE_LOCATION` required | Already declared — fine |
| 34 (Android 14) | FGS type `microphone` verification | Not using microphone FGS (RECORD_AUDIO is in-app only) |

---

## Request flow — onboarding sequence

```
App first launch
│
├── Onboarding Page 1: App intro, no permission yet
│
├── Onboarding Page 2: Location permission
│   → Show rationale
│   → Tap "Allow location" → system dialog
│   → Granted: show checkmark, enable Next
│   → Denied: show message "You can grant this later from settings", still allow Next
│   → "Precise" vs "Approximate" granted: note if approximate
│
└── Onboarding Page 3: Camera + Notifications
    → "Allow camera" button → system dialog
    → "Allow notifications" button → system dialog (only shown on API 33+)
    → Both optional — can tap "Skip for now"
    → "Get started" → navigate to Home, save onboarding_complete=true in DataStore
```

RECORD_AUDIO is NOT asked at onboarding — it's contextual (asked when user taps mic). This reduces upfront permission fatigue.

---

## Play Store data safety form — draft outline

When filling out the Data Safety section in Play Console:

**Does your app collect or share any of the required data types?** YES

**Location data:**
- Collected: YES — Precise location
- Shared with third parties: NO
- Used for: "App functionality" (GPS route tracking)
- Ephemeral processing only: NO (stored persistently in local database)
- Required or optional: Required (core app feature)
- User can delete: YES (via session deletion)

**Audio data (voice notes):**
- Collected: YES (optional feature)
- Shared with third parties: NO
- Used for: "App functionality" (voice notes attached to sessions)
- Ephemeral: NO (stored in device storage)
- Required or optional: Optional
- User can delete: YES

**Photos / video (optional photo pins):**
- Collected: YES (optional feature — only if user taps camera FAB)
- Shared: NO
- User can delete: YES (via highlight deletion)

**Personal info (name, email, etc.):** NO — no account, no identity

**Device identifiers:** NO — no advertising IDs, no crash reporting SDKs

**Financial info:** NO

**Health and fitness data:** YES — distance, pace, duration
- Shared: NO
- User can delete: YES (session deletion)

**Is the data encrypted in transit?** YES (location data never leaves device; no transit)
**Does your app allow users to request data deletion?** YES (delete session/note/highlight from within app)

**Privacy policy URL required:** Yes — you must provide one even for a privacy-first app. Simplest option: GitHub Pages hosted privacy policy page stating data stays on device.

---

## Privacy-first design checklist

- [ ] No analytics SDK (Firebase Analytics, Mixpanel, etc.)
- [ ] No crash reporting SDK (Crashlytics, Sentry) — add opt-in later if needed
- [ ] No advertising IDs read or transmitted
- [ ] No network calls for core feature (all data stays local)
- [ ] Audio files stored in app-private directory (other apps can't access)
- [ ] Photos stored in app-private directory unless user explicitly saves to gallery
- [ ] No READ_CONTACTS, READ_CALL_LOG, or other high-sensitivity permissions
- [ ] No `ACCESS_BACKGROUND_LOCATION` declared

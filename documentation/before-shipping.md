# Before shipping to Play Store

Things that need manual work — can't be done in code alone.

---

## 1. Generate upload keystore

```bash
keytool -genkey -v -keystore footlog-upload.jks -keyalg RSA -keysize 2048 -validity 10000 -alias footlog
```

- Store the `.jks` file **outside** the project folder (never commit it)
- Keep a copy somewhere safe — losing it means you can't update the app on Play Store
- Add signing config to `app/build.gradle.kts` under `buildTypes { release { ... } }`
- Store the keystore password somewhere safe (password manager)

---

## 2. Add SHA-1 fingerprint to Firebase Console

After generating the keystore, get the release SHA-1:

```bash
./gradlew signingReport
```

Go to [Firebase Console](https://console.firebase.google.com) → Project settings → Your Android app → Add fingerprint.
Add both the debug SHA-1 (for dev) and the release SHA-1 (for production Google Sign-In to work).

---

## 3. Verify MapLibre resolves

Before submitting, do a clean build and confirm `org.maplibre.gl:android-sdk:11.8.3` resolves from Maven.
If it fails:
- Check latest release at https://github.com/maplibre/maplibre-native/releases
- Update `maplibre` version in `gradle/libs.versions.toml`

---

## 4. Deploy Firestore security rules

The rules file is at `firestore.rules`. Deploy it before going live:

```bash
npx firebase-tools@latest deploy --only firestore:rules --project learningkotlin-7db3a688
```

Without this, Firestore is either open or locked depending on your current console state.

---

## 5. Enable Crashlytics in Firebase Console

Crashlytics is wired in code — it auto-enables in release builds. But the Firebase Console needs at least one crash report before the dashboard activates.
After first release build install, force a test crash or just use the app — the dashboard will appear within a few minutes.

---

## Deferred to v2 (not blocking)

- **Map thumbnails** in History and Routes screens show a placeholder icon — real MapLibre snapshot capture is the proper fix but adds complexity. Fine for v1.
- **Voice notes** — recording was removed. Existing `VOICE` type entries in Room display as "Voice note" text label. No action needed unless you want to add recording back.

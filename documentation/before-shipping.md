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

Once you have the release keystore, get its SHA-1:

```bash
./gradlew signingReport
```

Then go to [Firebase Console → Project settings → Your Android app → Add fingerprint](https://console.firebase.google.com/project/learningkotlin-7db3a688/settings/general/android:com.abhijit.footlog) and add the **release** SHA-1. The debug SHA-1 is already registered.

---

## 2. Verify MapLibre resolves

Before submitting, do a clean build and confirm `org.maplibre.gl:android-sdk:11.8.3` resolves from Maven.
If it fails:
- Check latest release at https://github.com/maplibre/maplibre-native/releases
- Update `maplibre` version in `gradle/libs.versions.toml`

---

## Done — no action needed

- ✅ **Firestore security rules** deployed (`users/{uid}/**` locked to matching auth UID)
- ✅ **Debug SHA-1** already registered in Firebase Console
- ✅ **Crashlytics** — wired in code, auto-enables on release builds. Dashboard activates after first crash report lands.

---

## Deferred to next update (not blocking)

- **Map thumbnails** in History and Routes screens show a placeholder icon — real MapLibre snapshot capture is the proper fix but adds complexity. Fine for first release.

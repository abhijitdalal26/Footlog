# Premium Assets & External Service Roadmap

To elevate **Footlog** from a well-built tracking app to a premium, high-end experience, we need external assets and services. This doc catalogs everything that cannot be coded from scratch — APIs, keys, design assets, and third-party integrations.

Research base: Strava Premium, AllTrails, Komoot, and top fitness tracking apps (2024-2026).

---

## 1. Map Aesthetics — Premium Tile Styles

OpenFreeMap tiles (current) are functional but generic. Premium tile styling is the single biggest visual upgrade.

| Source | Free Tier | Style | Cost if over |
|---|---|---|---|
| **MapTiler** (recommended) | 100k tile views/month | "Outdoor", "Streets", "Satellite" — optimized for hiking/running | ~€99/yr |
| **Stadia Maps** | 250k tile views/month | "Alidade Smooth Dark", "Outdoors" — clean dark-mode friendly | ~$40/mo |

**What to fetch:**
- MapTiler API key
- Style URLs for dark + light modes (e.g. `https://api.maptiler.com/maps/outdoor/style.json?key=KEY`)

**Integration:** Replace `STYLE_LIBERTY` / `STYLE_DARK` constants in `MapLibreView.kt:39-40` with the style URLs. Fall back to OpenFreeMap if key is missing.

---

## 2. Elevation Data — Add Gain/Loss to Every Session

Premium tracking apps show elevation gain, loss, and profiles. This is the #1 missing stat.

| Service | Cost | What it provides |
|---|---|---|
| **Open-Meteo Elevation API** | **Free, no API key** | SRTM 90m resolution for any lat/lng. Up to 100 points/request |
| openrouteservice Elevation | Free (2500 req/day) | Same SRTM data, plus line-sampled elevation profiles |

**What to fetch:** Nothing — Open-Meteo elevation API is free and needs no key.

**Integration plan:**
- On session stop, sample every Nth route point for elevation
- Store `totalElevationGain` / `totalElevationLoss` in `SessionEntity`
- Display on SessionSummary, SessionDetail, ShareCard
- Canvas elevation profile chart on SessionDetail

---

## 3. Weather Conditions — Attach to Each Session

Every premium tracking app shows "What was the weather like?" on past sessions.

| Service | Free Tier | Key needed? |
|---|---|---|
| **Open-Meteo Weather API** | **Free, no API key** | No |
| OpenWeatherMap | 1M calls/month | Yes (free key) |
| WeatherAPI.com | 1M calls/month | Yes (free key) |

**Recommendation:** Open-Meteo (free, no key, no rate limits for indie use).

**What to fetch:** Nothing if using Open-Meteo.

**Integration plan:**
- On session stop, fetch `https://api.open-meteo.com/v1/forecast?latitude=...&longitude=...&current=temperature_2m,weather_code,wind_speed_10m`
- Store weather condition code + temperature as optional fields on `SessionEntity`
- Display weather chip on SessionSummary, SessionDetail, ShareCard

---

## 4. Reverse Geocoding — Auto-Name Sessions

Instead of "Walk", premium apps name sessions "Walk near Marine Drive" or "Morning Run in Central Park".

| Service | Free Tier | Key needed? |
|---|---|---|
| **Nominatim (OSM)** | **Free** (1 req/sec, no key) | No |
| openrouteservice Geocoding | 2500 req/day | Yes (free key) |
| MapTiler Geocoding | Included with tile plan | Yes |

**Recommendation:** Nominatim for v1 (free, simple). Upgrade to MapTiler if volume grows.

**What to fetch:** Nothing for Nominatim.

**Integration plan:**
- On session stop, reverse-geocode the midpoint of the route: `https://nominatim.openstreetmap.org/reverse?lat=...&lon=...&format=json`
- Auto-generate title: `"{activityType} near {city/neighbourhood}"`
- User can still override in SessionSummary

---

## 5. Voice Notes — Transcription

Already documented in `voice-notes-implementation.md`:

| Service | Cost |
|---|---|
| **Android SpeechRecognizer** (on-device) | Free |
| Google Cloud Speech-to-Text (cloud, more accurate) | ~$0.006/15s (free tier: 60 min/month) |

**What to fetch:** Nothing (on-device) or GCP key for cloud transcription.

---

## 6. Route Planning & Turn-by-Turn Navigation

Like Strava Routes + Komoot navigation. A major premium upgrade.

| Service | Free Tier | Key needed? |
|---|---|---|
| **openrouteservice** | 2500 req/day | Yes (free key) |
| GraphHopper | 5,000 req/day | Yes (free key) |
| Valhalla (self-hosted) | Free (run your own server) | No |

**Recommendation:** openrouteservice for v1.

**What to fetch:** openrouteservice API key (free signup).

**Integration plan:**
- "Plan a route" feature: user picks start/end or distance → ORS returns a GPX path
- Navigate with turn-by-turn voice prompts (TTS)
- Heatmap overlay of popular explored cells

---

## 7. Health Connect Integration

Write sessions to Android Health Connect so they appear in Google Fit, Samsung Health, etc.

| Service | Cost |
|---|---|
| **Health Connect API** | Free (Android system API) |

**What to fetch:** Nothing.

**Integration plan:**
- On session stop, write distance/duration/activity type to Health Connect
- Request `HealthConnectManager` permissions on first session
- Shows Footlog as a "connected app" in system health settings

---

## 8. Strava API Import

Let users import their existing Strava activities into Footlog.

| Service | Cost |
|---|---|
| **Strava API v3** | Free (OAuth app registration needed) |

**What to fetch:** Strava API client ID + client secret. Register at https://www.strava.com/settings/api

**Integration plan:**
- OAuth flow to link Strava account
- Paginated fetch of activities → convert to SessionEntity
- One-time import on sign-in

---

## 9. Micro-interactions & Lottie Animations

(From existing doc — confirmed by research. Strava uses celebratory animations on segment PRs.)

| Animation | Use case | LottieFiles search keywords |
|---|---|---|
| Countdown 3-2-1 | ActiveTracking start screen | "countdown 3 2 1 timer" |
| Confetti / celebration | SessionSummary after completion | "confetti celebration success" |
| Flame / streak | HomeScreen streak display | "fire flame burning" |
| Activity loops | HomeScreen walk/run/cycle cards | "walking animation loop", "running animation loop", "cycling animation loop" |
| Map marker pulse | MapLibre highlight pins | "pulse radar location" |
| Sync loading | ProfileScreen sign-in spinner | "loading sync data" |

**Where to find:**
- **LottieFiles** — https://lottiefiles.com/ (free `.json` or `.lottie` downloads)
- **LottieFiles Icon library** — https://lottiefiles.com/icons (small free icons that loop)
- **IconScout** — https://iconscout.com/lotties (filter by "Free")

**What to fetch:** 6-8 Lottie JSON files. Place in `app/src/main/assets/lottie/`.

---

## 9b. Audio Feedback (System Sounds — No Assets Needed)

Music is skipped intentionally (users have their own Spotify/Apple Music). Audio feedback is light — just confirmations and cues.

| Sound | When | Implementation |
|---|---|---|
| Countdown beep (3-2-1) | Before tracking starts | `MediaActionSound(MediaActionSound.FOCUS_COMPLETE)` — 3 rising tones |
| Session start chime | GPS locked, tracking begins | `MediaActionSound(MediaActionSound.START_EFFECT)` — single positive chime |
| Session end chime | User stops tracking | `MediaActionSound(MediaActionSound.STOP_EFFECT)` — conclusive tone |
| Highlight pin drop | User adds a highlight | Haptic (`HapticFeedbackType.LongPress`) + `MediaActionSound(MediaActionSound.LAST_CLICK)` |

**No external assets needed.** Android's `MediaActionSound` uses built-in system sounds — zero file size, zero licensing. Sounds play with minimal latency.

Integration: Create a `SoundEffects.kt` utility object with functions like `playCountdownBeep()`, `playSessionStart()`, etc. Call them from ViewModels.
---

## 10. Custom Typography

| Font | Use | Source |
|---|---|---|
| **Outfit** (headings, display) | Session distance numbers, stats | Google Fonts |
| **Inter** (body, labels) | Stats, pace, date text | Google Fonts |

**What to fetch:** TTF files. Place in `app/src/main/res/font/`. Update `Type.kt`.

---

## 11. Custom Icon Set

| Icon Set | Why |
|---|---|
| **Phosphor Icons** | Clean, rounded, weight-uniform. Better than Material for editorial feel |
| **Lucide Icons** | Alternative — more geometric, minimal |

**What to fetch:** SVG or XML drawables. Or add the Android library dependency.

---

## 12. Premium Export — GPX, PDF, Share

| Feature | How |
|---|---|
| **GPX export** | Generate GPX XML from route points → share via intent |
| **PDF report** | Generate a styled A4 report with route map + stats summary |
| **Print-quality share card** | Higher resolution than current `graphicsLayer` capture |

**External dependencies:** None — GPX is XML, PDF can use Android's `PrintedPdfDocument` API.

---

## 13. Segment / Challenge System (Stretch)

Like Strava segments — auto-detect popular route sections and let users compare times.

| Service | Cost |
|---|---|
| **Self-built** (your own DB) | Free — store segment leaderboards in Firestore |
| **Clustering API** | DBSCAN on route points to discover common route sections |

**What to fetch:** Nothing on the API side — this is pure backend logic.

---

## Summary — What to fetch right now (in priority order)

| # | What | From | Cost | Complexity |
|---|---|---|---|---|
| 1 | MapTiler API key + style URLs | maptiler.com | Free tier | Easy |
| 2 | openrouteservice API key (elevation + geocoding + routing) | openrouteservice.org | Free tier | Easy |
| 3 | Lottie JSON animations (countdown, confetti, streak, activities, pulse, loading) | lottiefiles.com | Free | Easy |
| 4 | Outfit + Inter TTF font files | Google Fonts | Free | Easy |
| 5 | Phosphor icons (SVG or Android lib) | phosphoricons.com | Free | Medium |
| 6 | Strava API client ID + secret | strava.com/settings/api | Free | Medium |
| 7 | Health Connect setup | Android system | Free | Medium |
| 8 | PDF / GPX export (no external deps) | Code-only | Free | Medium |
| 9 | Audio feedback (system sounds) | **Nothing to fetch** — built into Android | Free | Easy |

## How to deliver

1. **API keys:** Paste in chat → I'll add to `local.properties` and wire them in
2. **Font files / Lottie JSONs:** Drop in `app/src/main/res/font/` and `app/src/main/assets/` → I'll write the integration code
3. **Icons:** Share SVGs or the library name → I'll add the dependency and replace Material icons

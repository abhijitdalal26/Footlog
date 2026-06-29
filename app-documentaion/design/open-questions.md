# Open Questions — Footlog

Genuine uncertainties, decisions that need your input, and risks in the current plan. Don't guess silently on these — they affect implementation choices.

---

## Decisions needed before building

### Q1 — Package name ✅ RESOLVED
**Decision:** `com.abhijit.footlog` — permanent, locked. The Notion page had `com.abhijitdalal.footlog` but the Android Studio project is the source of truth. Do not change.

### Q2 — Voice transcription in v1?
**Issue:** The user description says "option of typing if user don't want to use voice" and asks about transcription. Android `SpeechRecognizer` is free but requires internet for good accuracy and doesn't post-process saved .m4a files (it transcribes in real-time only).
**Options:**
- A: Voice recording only, type-mode only — no transcription in v1
- B: Include real-time SpeechRecognizer transcription when user taps "Transcribe" after recording
- C: Defer transcription entirely, ship voice + text as the two modes
**Recommendation:** Option B — include it, it's free and low-effort. But your call.

### Q3 — Light mode surface color
**Issue:** The light mode surface token is `#B7B2A4`. On a white/cream background (`#F4EFE4`), this is a noticeably dark grey for a card surface — it may feel like a shadow/stroke rather than a card fill. Usually light-mode cards are lighter than the background, not darker.
**Question:** Confirm this is intentional (dark warm grey card surface in light mode), or adjust to something lighter, e.g. `#EDE8DB`.

### Q4 — Streak definition
**Issue:** Stats screen shows a "streak" stat. The streak logic is undefined.
**Options:**
- A: Consecutive calendar days with at least one session
- B: Consecutive 7-day weeks with at least one session
- C: Sessions within the last N days
**Question:** Which definition? Does a rest day break the streak?

### Q5 — "Areas explored" stat
**Issue:** Stats screen 2×2 grid includes "Areas explored". What does this show?
**Options:**
- A: Total count of ExploredCell grid cells (raw number, e.g. "1,204 cells") — not meaningful to users
- B: Converted to km² (cell count × 25m² → km²) — more meaningful
- C: Unique neighborhoods/areas — requires reverse geocoding or manual zone definition
**Recommendation:** Option B (km²) — simple to compute, intuitive unit.

### Q6 — "Copy link" in Share card
**Issue:** Screen 6 (Share card) has 4 share options: WhatsApp, Instagram, Save, Copy link. "Copy link" implies a URL — but this is a no-backend, offline app. There's no server to host a link to.
**Options:**
- A: Remove "Copy link" — replace with "Copy to clipboard" (copies the image or the stats as formatted text)
- B: Keep placeholder for future backend integration
- C: "Copy stats" — copies formatted text summary to clipboard (e.g. "5.2km walk · 47 min · June 20, 2026")
**Recommendation:** Option C — useful without any backend.

### Q7 — Map thumbnails in History/Routes
**Issue:** History and Routes screens show "route thumbnail map" per session. Generating real map thumbnails requires either `GoogleMap.snapshot()` (async, resource-heavy) or a static map tile API (costs money / requires separate key).
**Options:**
- A: Static placeholder icon (activity-type icon with distance text) — no thumbnail
- B: Render a tiny `GoogleMap` per list item — very expensive, will lag
- C: On session end, trigger a one-time `GoogleMap.snapshot()` in the background and cache the bitmap
**Recommendation:** Option A for v1 (placeholder), Option C for v2. Confirm.

### Q8 — Fog-of-war inclusion in v1
**Issue:** The design spec says "fog-of-war map rendering intentionally excluded." The build roadmap defers the fog visualization but still adds the `ExploredCell` table and inserts cells during tracking in Phase 2.
**Question:** Should the fog visualization screen (the large card on Home labeled "Your explored map") be:
- A: Hidden entirely in v1 (empty state only, never shown)
- B: Shown as an empty state card from day 1, but fog rendering deferred
- C: Include basic fog rendering in v1 using the Canvas overlay approach from `fog-of-war-approach.md`
The `ExploredCell` data pipeline is cheap to add during tracking regardless — the question is when to build the visualization.

### Q9 — Camera: in-app CameraX vs system camera intent
**Issue:** When user taps the camera FAB during tracking, do we open the system camera app (simple intent) or use CameraX inside the app?
**Options:**
- A: System intent (`MediaStore.ACTION_IMAGE_CAPTURE`) — simpler, no CameraX dependency, but user leaves Footlog briefly
- B: CameraX in-app — seamless but ~5-10 hours of implementation work
**Recommendation:** System intent for v1. Confirm.

---

## Technical risks

### R1 — Navigation 2.9 type-safe routes are a breaking API change
Navigation 2.8+ replaced string-based routes with `@Serializable` Kotlin objects. The Notion page mentions `navigation-compose:2.8.0`. The current latest stable is 2.9.8. The API is different from tutorials written before 2024. Make sure to follow 2.8+ type-safe route documentation when building the NavHost.

### R2 — Google Maps SDK 20.x requires API 23+
Play-services-maps version 20.x bumped the minimum API to 23. Our MinSDK is 26 — we're safe. But if using a `maps-compose` wrapper, verify `maps-compose` version 6.x is compatible with play-services-maps 19.2.0 (it is — maps-compose 6.x targets maps SDK 19.x and 20.x).

### R3 — `MediaRecorder` constructor API change in API 31
`MediaRecorder(Context)` constructor is the correct form for API 31+. The deprecated zero-arg `MediaRecorder()` still works but shows a lint warning. Always use the context-aware constructor with a version check. See `voice-notes-implementation.md`.

### R4 — Foreground service restrictions (Android 14)
On Android 14, starting a foreground service of type `location` from the background throws an exception. Only start the tracking service from a user action (button tap in the foreground). This is the design — just verify the service start is always triggered by the Stop/Start buttons, never by a background alarm or notification action.

### R5 — KSP and Kotlin version must match exactly
KSP 2.2.10-2.0.2 is required for Kotlin 2.2.10. AGP 9.0+ enforces this automatically. If you ever update Kotlin (e.g., to 2.3.x), KSP must be updated to 2.3.x-Y.Z.W in the same commit. Desynchronizing these breaks compilation immediately.

---

## Things deliberately not questioned (don't re-litigate)

- Google Maps SDK choice (confirmed in Notion)
- No backend / accounts / social (confirmed)
- Dark-first design (confirmed)
- Play Store as target distribution (confirmed)
- 12-screen scope for v1 (confirmed)

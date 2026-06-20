# Build Roadmap — Footlog

Granular task list derived from the design spec's 6-phase build order. Each task is sized for one focused sitting (1-4 hours). Tasks within a phase can be done in parallel; phases must be done in order.

---

## Phase 1 — Foundation scaffold
*Color tokens + Navigation + empty screen stubs. Goal: app navigates between all 12 screens with placeholder text.*

- [ ] **1.1** Update `ui/theme/Color.kt` with all Footlog color tokens (both dark and light from design spec). Remove placeholder Material purple colors.
- [ ] **1.2** Update `ui/theme/Theme.kt` to apply `FootlogColors` to a custom `ColorScheme`. Wire up dark/light system theme. Test both in Preview.
- [ ] **1.3** Update `ui/theme/Type.kt` to define the two-weight (400/500) typescale with no 700/bold.
- [ ] **1.4** Create `ui/navigation/Screen.kt` — sealed class or `@Serializable` objects for all 12 route destinations. Use Navigation 2.9 type-safe routes (no magic strings).
- [ ] **1.5** Create `ui/navigation/AppNavHost.kt` — `NavHost` with all 12 destinations wired as empty `Composable` stubs (just `Text("Screen name")` placeholders). Bottom nav for Home/History/Routes/Stats.
- [ ] **1.6** Update `MainActivity.kt` to host `AppNavHost` wrapped in `FootlogTheme`.
- [ ] **1.7** Add DataStore setup: create `AppPreferences.kt` singleton. Add `ONBOARDING_COMPLETE` key. Read on app start, navigate to Onboarding or Home accordingly.
- [ ] **1.8** Verify all 12 screens are reachable by tapping through the app. Fix any navigation graph errors.

---

## Phase 2 — Core record loop
*Home screen, Active Tracking, Session Summary. Goal: user can start a walk, track GPS, stop, and see a summary.*

### 2A — Data layer

- [ ] **2.1** Create Room entities: `Session`, `Note`, `Highlight`, `ExploredCell` in `data/entity/`.
- [ ] **2.2** Create DAOs: `SessionDao`, `NoteDao`, `HighlightDao`, `ExploredCellDao` in `data/dao/`. Add basic CRUD + flow-returning queries.
- [ ] **2.3** Create `FootlogDatabase` (Room abstract class, singleton) in `data/db/`.
- [ ] **2.4** Create `SessionRepository` wrapping the DAOs with coroutine-safe suspend functions.
- [ ] **2.5** Add `ExploredCell` insertion logic: `latToCell()` / `lngToCell()` utility functions.

### 2B — Location tracking service

- [ ] **2.6** Create `LocationTrackingService` (Foreground Service, `foregroundServiceType=location`). Posts a persistent notification ("Tracking your walk...") using a notification channel.
- [ ] **2.7** Wire `FusedLocationProviderClient` in the service: `requestLocationUpdates()` at 3s interval, high accuracy. Broadcast updates via `StateFlow` or a bound service interface.
- [ ] **2.8** Add `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`, `INTERNET` permissions to manifest. Add service declaration with `foregroundServiceType`.

### 2C — Home screen

- [ ] **2.9** Build `HomeScreen` composable: greeting, 3 activity-type cards (Walk/Run/Cycle), recent sessions list (stub with fake data first), fog map placeholder card.
- [ ] **2.10** Create `HomeViewModel`: load recent sessions from Room, expose as `StateFlow<List<Session>>`.
- [ ] **2.11** Wire navigation: tapping Walk/Run/Cycle starts `LocationTrackingService` and navigates to `ActiveTrackingScreen` with `activityType` arg.

### 2D — Active tracking screen

- [ ] **2.12** Build `ActiveTrackingScreen` layout: `GoogleMap` composable showing live polyline, stat bar overlay (distance/time/pace), floating FABs (camera, highlight, note), Stop button in danger color.
- [ ] **2.13** Create `ActiveTrackingViewModel`: receives location updates from service, computes distance (Haversine formula), elapsed time, pace. Exposes as `StateFlow`.
- [ ] **2.14** Draw the GPS polyline as a `Polyline` on the `GoogleMap` using the route-line accent color.
- [ ] **2.15** Wire Stop button: stops service, saves `Session` to Room, navigates to `SessionSummaryScreen(sessionId)`.

### 2E — Session summary screen

- [ ] **2.16** Build `SessionSummaryScreen`: map snapshot of completed route (static `GoogleMap` or snapshot callback), editable title TextField, stat chips (distance/duration/pace), favorite route toggle, Done/Share buttons.
- [ ] **2.17** Create `SessionSummaryViewModel`: load session by ID, handle title edits, save-as-favorite, finalize session record.
- [ ] **2.18** Wire Done → Home (clear back stack). Wire Share → `ShareCardScreen(sessionId)`.

### 2F — Runtime permissions

- [ ] **2.19** Add location permission request flow: check on Home entry, show rationale if denied, request system dialog. Handle all outcomes (granted/denied/permanently denied).
- [ ] **2.20** Add `POST_NOTIFICATIONS` request for Android 13+ before starting the foreground service.
- [ ] **2.21** Add Onboarding permission flow screens (pages 2 and 3) using the patterns above. Wire DataStore flag so onboarding only shows on first launch.

---

## Phase 3 — Review loop
*History and Session Detail. Goal: user can browse all past sessions and view a specific session's route.*

- [ ] **3.1** Build `HistoryScreen`: `LazyColumn` with date-grouped headers, session row items (map thumbnail, name, distance, date). Load from `SessionRepository`.
- [ ] **3.2** Create `HistoryViewModel`: query all sessions ordered by start time desc, group by date in UI layer.
- [ ] **3.3** Build `SessionDetailScreen`: static route map (`GoogleMap` in non-interactive mode), stat chips, "View note" button (visible only if note exists), pin markers for highlights.
- [ ] **3.4** Create `SessionDetailViewModel`: load session + its highlights + note existence from Room.
- [ ] **3.5** Wire navigation: History row tap → `SessionDetail(sessionId)`. Home recent list tap → same.
- [ ] **3.6** Generate map thumbnails for session cards: either use `GoogleMap.snapshot()` callback and store path, or use a small static map composable. Decide: static fake map icon for now vs real thumbnail.

---

## Phase 4 — Annotation layer
*Note writing, note viewing, highlight tagging, highlight detail. Goal: user can record/type a note and drop highlight pins during and after a session.*

- [ ] **4.1** Build `NoteWritingScreen`: mic FAB (80dp, pulsing animation while recording), "or type instead" toggle, multiline TextField, Save button.
- [ ] **4.2** Create `NoteViewModel`: `MediaRecorder` lifecycle (start/stop recording), `MediaPlayer` for playback, `SpeechRecognizer` for transcription (optional). Save `Note` entity to Room.
- [ ] **4.3** Wire RECORD_AUDIO permission request on mic tap.
- [ ] **4.4** Build `NoteViewScreen`: voice player UI (play button, static waveform placeholder, duration label) or text display depending on note type.
- [ ] **4.5** Build `HighlightTagSheet` (ModalBottomSheet): category chips (Cafe/Shop/Viewpoint/Custom), emoji input, name TextField, optional note TextField, "Add to route" button.
- [ ] **4.6** Wire camera FAB in ActiveTracking: request `CAMERA` permission, open system camera intent, on result create `Highlight` entity with `photoPath` and current GPS coordinate.
- [ ] **4.7** Wire highlight FAB in ActiveTracking: show `HighlightTagSheet`, on confirm create `Highlight` entity.
- [ ] **4.8** Build `HighlightDetailScreen`: photo display (Coil), emoji + name, category chip, personal note, "Open in maps" button (geo Intent).

---

## Phase 5 — Secondary views
*Routes and Stats. Goal: user can see favorited routes and aggregate statistics.*

- [ ] **5.1** Build `RoutesScreen`: `LazyColumn` of favorited sessions (isFavoriteRoute=true), route thumbnail, name, distance, last-walked date. Empty state.
- [ ] **5.2** Create `RoutesViewModel`: query sessions where `isFavoriteRoute=true`.
- [ ] **5.3** Build `StatsScreen`: week/all-time toggle (SegmentedButton), 7-day bar chart (Vico), 2×2 stat grid (total distance, total sessions, streak, areas explored).
- [ ] **5.4** Create `StatsViewModel`: compute aggregates from Room queries (total distance sum, session count, streak logic — consecutive days with sessions, areas explored = ExploredCell count).
- [ ] **5.5** Wire Vico chart: pass 7 days of distance data (0 if no session), style with route-line accent color.

---

## Phase 6 — Polish layer
*Share card and Onboarding. Goal: shareable images, first-launch experience complete.*

- [ ] **6.1** Build the share card composable layout: 9:16 Card, dark background, route polyline, distance/duration/date overlay. See `share-card-rendering.md` for bitmap capture approach.
- [ ] **6.2** Implement bitmap capture: `rememberGraphicsLayer()` + `graphicsLayer.toImageBitmap()`. Save to temp file in `cacheDir`.
- [ ] **6.3** Implement share Intent: `FileProvider` URI from the saved bitmap. `Intent.ACTION_SEND` with MIME type `image/jpeg`. Test on WhatsApp and Instagram.
- [ ] **6.4** Implement "Save to gallery": MediaStore API — save image to `Pictures/Footlog`.
- [ ] **6.5** Build `OnboardingScreen`: `HorizontalPager` (3 pages), dot indicators, permission request buttons on pages 2 and 3, "Get started" → Home navigation + save `ONBOARDING_COMPLETE=true`.
- [ ] **6.6** Wire Onboarding as the default destination when `ONBOARDING_COMPLETE` is false.

---

## Deferred (explicitly not in this roadmap)

- **Fog-of-war rendering screen**: The `ExploredCell` table is built in Phase 2.1 and cells are inserted during tracking in Phase 2B — the data pipeline is ready. The fog visualization overlay is deferred as a separate engineering task after core tracking is stable. See `fog-of-war-approach.md`.
- **Photo thumbnail generation for History**: Full implementation deferred if static map icons are used as placeholders in Phase 3.
- **Vico chart advanced styling**: Basic colored bars in Phase 5; custom axis labels and animations are polish work.

---

## Decisions that must be made before each phase

| Phase | Decision needed |
|---|---|
| 2D | Camera: open system camera intent vs in-app CameraX? System intent is simpler. |
| 3.6 | Map thumbnails: real snapshot or placeholder icon? Real snapshot requires async work. |
| 4.2 | Transcription: include SpeechRecognizer in v1, or text-only fallback? |
| 5.3 | Streak logic: does a "rest day" break the streak? Define rule. |
| 5.4 | "Areas explored" stat: show raw cell count, or convert to km²? |

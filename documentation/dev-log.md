# Footlog Dev Log

Autonomous build log — tracks what was worked on, when, and what changed each loop iteration.

---

## 2026-06-23 06:43 — Task 1: Fog-of-war map visualization

**Status:** Done (06:43 → 06:47, build clean)

**What:** Build the exploration map overlay. `ExploredCell` table has been populated since Phase 2 but nothing has ever rendered it. The Routes tab gets a two-tab layout: "Explore" (fog-of-war map) and "Saved" (favourite routes list).

**Files changing:**
- `util/CellUtils.kt` — add `cellBoundsPolygon()` to convert (cellX, cellY) back to lat/lng polygon
- `data/dao/ExploredCellDao.kt` — add `getAllFlow()` for reactive cell updates
- `data/repository/SessionRepository.kt` — expose `getAllExploredCells(): Flow`
- `ui/components/MapLibreView.kt` — add `exploredCells` parameter + GeoJSON FillLayer
- `ui/viewmodels/RoutesViewModel.kt` — load explored cells into StateFlow
- `ui/screens/RoutesScreen.kt` — add PrimaryTabRow with Explore / Saved tabs

---

## 2026-06-23 07:47 — Task 2: Fix all deprecation warnings

**Status:** Done (07:47 → 07:50, build clean — zero warnings)

**What:** Eliminated every compiler deprecation warning. Build output is now completely silent.

**Changes:**
- `HomeScreen.kt` — `Icons.Filled.DirectionsWalk/Run/Bike` → `Icons.AutoMirrored.Filled.*`
- `ShareCardScreen.kt` — `LocalClipboardManager.setText(AnnotatedString)` → `LocalClipboard.setClipEntry(ClipEntry(ClipData))` inside `scope.launch`
- `MapLibreView.kt` — removed `map.clear()`, `map.addMarker()`, `MarkerOptions` entirely; replaced with GeoJSON `CircleLayer` (orange circle) + `SymbolLayer` (emoji centred) for highlights

---

## Planned tasks

| # | Task | Status |
|---|---|---|
| 1 | Fog-of-war map visualization | done |
| 2 | Fix deprecation warnings (MapLibreView markers, AutoMirrored icons, LocalClipboard) | done |
| 3 | Room proper migrations + exportSchema | pending |
| 4 | LocationTrackingService — move callbacks to HandlerThread | pending |

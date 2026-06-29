# Fog-of-War Approach — Footlog

This is the "explored area" lifetime visualization on the secondary map view. It shows cumulative coverage of everywhere the user has ever walked, not the live tracking map.

---

## Decision: Grid-cell reveal

**Recommended approach: grid-cell, stored in Room, rendered as a Canvas overlay on Google Maps.**

### Why not path-radius (circle union)?

Path-radius (reveal a circle of N meters around every GPS point ever recorded) is what FogOff uses. It looks smooth but has a critical problem at scale:

- After 1 year of daily walking, you have ~500,000–1,000,000 GPS points stored across all sessions
- Rendering requires unioning all those circles at draw time (or every time the map pans/zooms)
- Computing the union of 1M circles on the GPU at interactive frame rates is expensive and complex
- Storing raw points for years = large database, slow queries

**Grid-cell approach solves all of this:**
- During each session, convert each GPS point to its cell ID → upsert into a small `explored_cells` table (INSERT OR IGNORE)
- Rendering: query the set of revealed cells once, draw them as rectangles on Canvas
- Each cell is just two integers — the set is deduplicated automatically
- Queries are `O(1)` per point insertion, rendering is `O(unique_cells)` not `O(total_points)`

---

## Grid cell definition

**Cell size: 25 meters**

A 25m cell is:
- Small enough to feel precise on a map at zoom level 14–16
- Large enough to give comfortable "exploration credit" (you don't need to walk the exact same pixel twice)
- ~0.000225 degrees latitude per cell (varies slightly with longitude due to Earth's curvature, acceptable approximation for city-scale)

**Cell ID computation:**
```kotlin
const val CELL_SIZE_METERS = 25.0
const val METERS_PER_DEGREE_LAT = 111320.0

fun latToCell(lat: Double): Int = floor(lat * METERS_PER_DEGREE_LAT / CELL_SIZE_METERS).toInt()

fun lngToCell(lng: Double, lat: Double): Int {
    val metersPerDegreeLng = cos(Math.toRadians(lat)) * METERS_PER_DEGREE_LAT
    return floor(lng * metersPerDegreeLng / CELL_SIZE_METERS).toInt()
}
```

> Note: For city-scale exploration at latitudes 10°–35° (India), the longitude correction is 1–5% at most. Approximation is acceptable for a fog visualization.

---

## Room schema

```kotlin
@Entity(
    tableName = "explored_cells",
    primaryKeys = ["latBucket", "lngBucket"]
)
data class ExploredCell(
    val latBucket: Int,
    val lngBucket: Int,
    val firstVisited: Long = System.currentTimeMillis()
)

@Dao
interface ExploredCellDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun markVisited(cells: List<ExploredCell>)

    @Query("SELECT * FROM explored_cells WHERE latBucket BETWEEN :minLat AND :maxLat AND lngBucket BETWEEN :minLng AND :maxLng")
    suspend fun getCellsInBounds(minLat: Int, maxLat: Int, minLng: Int, maxLng: Int): List<ExploredCell>

    @Query("SELECT COUNT(*) FROM explored_cells")
    suspend fun getTotalCells(): Int
}
```

**Insertion pattern during tracking:**
```kotlin
// Called in LocationTrackingService when a new GPS point arrives
val cell = ExploredCell(
    latBucket = latToCell(location.latitude),
    lngBucket = lngToCell(location.longitude, location.latitude)
)
database.exploredCellDao().markVisited(listOf(cell))
// INSERT OR IGNORE — no duplicate writes, idempotent
```

---

## Data volume estimates

Assumptions: 1 hour walk per day, ~4km, 3-second GPS interval = 1200 points per session.

| Time period | Sessions | Gross points | Unique cells (est.) | Storage (Room) |
|---|---|---|---|---|
| 1 week | 7 | 8,400 | ~2,000 | ~16 KB |
| 1 month | 30 | 36,000 | ~8,000 | ~64 KB |
| 6 months | 180 | 216,000 | ~35,000 | ~280 KB |
| 1 year | 365 | 438,000 | ~60,000 | ~480 KB |
| 3 years | 1,095 | 1,314,000 | ~120,000 | ~960 KB |

Unique cells plateau as the user re-walks familiar areas. After a year of walking in a city, most routes overlap. Storage is never a problem — under 1 MB even for an obsessive daily walker over 3 years.

**The Session table's `routePoints` (LatLng list) is a separate concern** — that's the per-session replay data, serialized as JSON in Room. The `explored_cells` table is the lifetime cumulative index derived from it, optimized for fog rendering.

---

## Rendering approach

**Recommended: Canvas overlay on Google Maps Compose**

```
GoogleMap composable
└── Canvas overlay (drawn on top via Box composable)
    - Draws dark semi-transparent rectangles for every screen cell
    - Skips drawing where ExploredCell exists (the "revealed" area)
    - Result: dark fog everywhere EXCEPT where user has walked
```

**Implementation pattern:**

```kotlin
@Composable
fun FogOverlay(
    revealedCells: Set<Pair<Int, Int>>,  // (latBucket, lngBucket)
    cameraPositionState: CameraPositionState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // 1. Compute visible lat/lng bounds from cameraPositionState
        // 2. Enumerate all grid cells that fall within visible bounds
        // 3. Draw dark rect for each cell NOT in revealedCells
        // 4. Skip (or draw transparent) for cells IN revealedCells
    }
}
```

In `Box`:
```kotlin
Box {
    GoogleMap(cameraPositionState = cameraState) { /* markers etc */ }
    FogOverlay(revealedCells = state.revealedCells, cameraPositionState = cameraState)
}
```

**Alternative: Google Maps TileOverlay**
- Draw custom PNG tiles where revealed cells are transparent and unrevealed are opaque dark
- Better performance at high zoom or for very large cell sets (avoids drawing thousands of rects on every frame)
- More complex: requires `TileProvider` implementation and tile caching
- Recommend starting with Canvas overlay and switching to TileOverlay if performance degrades

**Performance guidance:**
- At zoom 14, viewport shows ~500×500 cells. Only render cells in the current viewport.
- Cache the visible cells query result; re-query only when camera moves significantly.
- `revealedCells` lookup is `O(1)` per cell (it's a `HashSet`).

---

## Open questions before building

1. **Fog opacity**: fully opaque dark (no map below visible in unrevealed areas) or semi-transparent (see map faintly through fog)? Semi-transparent is more visually interesting but the color needs tuning.
2. **Fog color**: should match the app's `backgroundDark` (#1E1C18) or a slightly different tint?
3. **Cell reveal radius**: should each GPS point reveal 1 cell (exactly 25m) or a 3×3 neighborhood (75m)? 3×3 gives a more satisfying reveal per step.
4. **Defer or include in v1?**: The Notion spec treats this as a separate engineering task after core tracking. Good call — implement core tracking and session data first, then add the cells table and fog rendering screen.

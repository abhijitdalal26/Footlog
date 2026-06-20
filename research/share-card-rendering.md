# Share Card Rendering — Footlog

Screen 6: generate a polished 9:16 shareable image from the completed session stats and route. This is the post-walk "achievement card."

---

## Bitmap capture approach

**Use `rememberGraphicsLayer()` — the official Compose 1.7+ method.**

```kotlin
@Composable
fun ShareCardScreen(sessionId: String, viewModel: ShareCardViewModel = viewModel()) {
    val graphicsLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier
        .aspectRatio(9f / 16f)
        .fillMaxWidth()
        .drawWithContent {
            graphicsLayer.record { this@drawWithContent.drawContent() }
            drawLayer(graphicsLayer)
        }
    ) {
        ShareCardContent(session = viewModel.session)
    }

    Button(onClick = {
        coroutineScope.launch {
            val bitmap = graphicsLayer.toImageBitmap()
            // Save to file, then share via FileProvider
            viewModel.shareCard(bitmap)
        }
    }) {
        Text("Share")
    }
}
```

**Why this approach over alternatives:**

| Approach | Status | Notes |
|---|---|---|
| `rememberGraphicsLayer()` + `toImageBitmap()` | **Recommended** | Official API, Compose 1.7+, no lifecycle issues |
| `Picture`-based capture (Compose 1.6.x) | Also valid | One step more complex, pre-1.7 only |
| `Capturable` library (PatilShreyas) | Reasonable | Third-party, thin wrapper, but adds a dependency |
| `AndroidView` + `View.drawToBitmap()` | Legacy | Don't use — mixing Views defeats the Compose approach |

Compose BOM 2026.02.01 ships Compose UI 1.11.x — `rememberGraphicsLayer()` is fully stable.

---

## Share flow implementation

```kotlin
// In ViewModel or utility function
fun shareImageBitmap(context: Context, bitmap: ImageBitmap, sessionId: String) {
    // 1. Convert ImageBitmap → android.graphics.Bitmap
    val androidBitmap = bitmap.asAndroidBitmap()

    // 2. Save to cache directory (NOT gallery — this is a temp file for sharing)
    val file = File(context.cacheDir, "share_${sessionId}.jpg")
    FileOutputStream(file).use { out ->
        androidBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
    }

    // 3. Create FileProvider URI (cross-app accessible)
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    // 4. Fire share intent
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share your walk"))
}
```

**FileProvider setup in AndroidManifest.xml (one-time, inside `<application>`):**
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

**`res/xml/file_paths.xml`:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <cache-path name="shared_images" path="." />
    <files-path name="voice_notes" path="voice_notes/" />
</paths>
```

**Save to gallery (MediaStore):**
```kotlin
fun saveToGallery(context: Context, bitmap: Bitmap, sessionId: String) {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "footlog_walk_${sessionId}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Footlog")
    }
    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    uri?.let { dest ->
        context.contentResolver.openOutputStream(dest)?.use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
    }
}
```

---

## Share card visual layout

**The card always uses dark theme regardless of system theme** — dark background reads better as a social media image and provides better contrast. This is consistent with the design decision noted in the Notion page.

**9:16 portrait layout (the card content):**

```
┌─────────────────────────────────┐
│                                 │  ← Background: #1E1C18
│                                 │
│    [Route polyline,             │  ← Static GoogleMap or hand-drawn
│     centered, fills             │    Canvas polyline on dark background
│     top 65% of card]            │
│                                 │
│                                 │
├─────────────────────────────────┤
│                                 │  ← Surface: #2C2A25, 24dp top corners
│  5.2 km                         │  ← Distance: 56sp, weight 500, #F0ECE2
│  Walk · 47 min                  │  ← Activity + duration: 18sp, #9C9890
│  June 20, 2026                  │  ← Date: 14sp, #9C9890
│                                 │
│  ████████ Footlog               │  ← App icon (24dp) + "Footlog" text
└─────────────────────────────────┘
```

**Typography hierarchy:**
- Hero stat (distance): 56sp, weight 500, `textPrimaryDark` (#F0ECE2)
- Activity type + duration: 18sp, weight 400, `textSecondaryDark` (#9C9890)
- Date: 14sp, weight 400, `textSecondaryDark` (#9C9890)
- App attribution: 12sp, weight 400, `navInactiveDark` (#6B6862)

**Route visualization in the card:**
- Option A: Embed a small `GoogleMap` with route polyline and `uiSettings.isAllGesturesEnabled = false` — maps render within a composable, so `graphicsLayer` can capture it. Requires the map to fully load before capture.
- Option B: Hand-draw the route polyline on a Compose `Canvas` using the stored `routePoints` list — project lat/lng to pixel coordinates within the card bounds. No Google Maps dependency in the share card.
- **Recommendation: Option B** — avoids async map loading timing issues during bitmap capture. The route is just a polyline — you can draw it manually on Canvas in the card without needing the full maps SDK.

**Polyline normalization for Canvas drawing:**
```kotlin
// Normalize all lat/lng points to fit within the card's Canvas bounds
fun List<LatLng>.toCanvasPoints(canvasWidth: Float, canvasHeight: Float): List<Offset> {
    val minLat = minOf { it.latitude }
    val maxLat = maxOf { it.latitude }
    val minLng = minOf { it.longitude }
    val maxLng = maxOf { it.longitude }
    val padding = 0.15f  // 15% padding on each side
    return map { point ->
        val x = ((point.longitude - minLng) / (maxLng - minLng))
            .let { lerp(canvasWidth * padding, canvasWidth * (1 - padding), it.toFloat()) }
        val y = ((maxLat - point.latitude) / (maxLat - minLat))
            .let { lerp(canvasHeight * padding, canvasHeight * (1 - padding), it.toFloat()) }
        Offset(x, y)
    }
}
```

---

## 4 share buttons (bottom row)

```kotlin
Row(horizontalArrangement = Arrangement.SpaceEvenly) {
    ShareButton(icon = R.drawable.ic_whatsapp,  label = "WhatsApp")   // ACTION_SEND to WhatsApp
    ShareButton(icon = R.drawable.ic_instagram, label = "Instagram")   // ACTION_SEND to Instagram
    ShareButton(icon = Icons.Default.SaveAlt,   label = "Save")        // MediaStore save
    ShareButton(icon = Icons.Default.ContentCopy, label = "Copy text") // Clipboard: formatted stats text
}
```

**WhatsApp direct share:**
```kotlin
val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
    type = "image/*"
    setPackage("com.whatsapp")
    putExtra(Intent.EXTRA_STREAM, uri)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}
if (packageManager.resolveActivity(whatsappIntent, 0) != null) {
    startActivity(whatsappIntent)
} else {
    // WhatsApp not installed — fall back to system share sheet
    startActivity(Intent.createChooser(genericShareIntent, "Share"))
}
```

**"Copy text" (replaces "Copy link" which has no URL to copy):**
```kotlin
val clipboardManager = context.getSystemService(ClipboardManager::class.java)
val statsText = "${session.distanceKm} km ${session.activityType} · ${session.durationFormatted} · ${session.dateFormatted}"
clipboardManager.setPrimaryClip(ClipData.newPlainText("Footlog walk", statsText))
// Show snackbar: "Copied to clipboard"
```

---

## Performance notes

- Capture the composable AFTER it has fully composed and drawn — use `LaunchedEffect` with a slight delay or the `onSizeChanged` callback to ensure the composable is measured before calling `graphicsLayer.toImageBitmap()`
- Run the bitmap conversion and save in `viewModelScope` (IO dispatcher) to avoid blocking the UI thread
- Cache the rendered bitmap in the ViewModel — don't re-render on every button tap

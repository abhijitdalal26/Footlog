package com.abhijit.footlog.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.abhijit.footlog.data.entity.ExploredCellEntity
import com.abhijit.footlog.data.entity.HighlightEntity
import com.abhijit.footlog.data.entity.LatLngPoint
import com.abhijit.footlog.util.cellBoundsPolygon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon

private const val STYLE_LIBERTY = "https://tiles.openfreemap.org/styles/liberty"
private const val STYLE_DARK = "https://tiles.openfreemap.org/styles/dark"
private const val ROUTE_SOURCE_ID = "route-source"
private const val ROUTE_LAYER_ID = "route-layer"
private const val EXPLORE_SOURCE_ID = "explore-source"
private const val EXPLORE_LAYER_ID = "explore-layer"
private const val HIGHLIGHTS_SOURCE_ID = "highlights-source"
private const val HIGHLIGHTS_CIRCLE_LAYER_ID = "highlights-circle"
private const val HIGHLIGHTS_TEXT_LAYER_ID = "highlights-text"

@Composable
fun MapLibreView(
    routePoints: List<LatLngPoint>,
    currentLocation: android.location.Location?,
    highlights: List<HighlightEntity> = emptyList(),
    exploredCells: List<ExploredCellEntity> = emptyList(),
    routeColor: Color,
    showMyLocation: Boolean = false,
    isInteractive: Boolean = true,
    modifier: Modifier = Modifier
) {
    var mapRef: MapLibreMap? by remember { mutableStateOf(null) }
    val isDark = isSystemInDarkTheme()

    val routeColorHex = remember(routeColor) {
        "#%06X".format(routeColor.toArgb() and 0xFFFFFF)
    }
    val mapParkColor = remember(routeColor) {
        "#%06X".format(routeColor.copy(alpha = 0.4f).toArgb() and 0xFFFFFF)
    }
    val exploreColorHex = remember(routeColor) {
        "#%06X".format(routeColor.copy(alpha = 0.35f).toArgb() and 0xFFFFFF)
    }
    val highlightColorHex = if (isDark)
        "#%06X".format(0xE0945A) else "#%06X".format(0xC9783A)

    // Build explored-cells GeoJSON off the main thread
    var exploreGeoJson by remember { mutableStateOf<FeatureCollection?>(null) }
    LaunchedEffect(exploredCells) {
        if (exploredCells.isEmpty()) {
            exploreGeoJson = null
            return@LaunchedEffect
        }
        exploreGeoJson = withContext(Dispatchers.Default) {
            val features = exploredCells.map { cell ->
                val ring = cellBoundsPolygon(cell.cellX, cell.cellY)
                Feature.fromGeometry(Polygon.fromLngLats(listOf(ring)))
            }
            FeatureCollection.fromFeatures(features)
        }
    }

    val styleUri = if (isDark) STYLE_DARK else STYLE_LIBERTY

    AndroidView(
        factory = { context ->
            MapLibre.getInstance(context)
            MapView(context).apply {
                getMapAsync { map ->
                    mapRef = map
                    map.uiSettings.isRotateGesturesEnabled = false

                    if (showMyLocation) {
                        map.moveCamera(CameraUpdateFactory.zoomTo(15.0))
                    }

                    map.setStyle(Style.Builder().fromUri(styleUri)) { style ->
                        // Route line
                        style.addSource(GeoJsonSource(ROUTE_SOURCE_ID))
                        style.addLayer(
                            LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID).withProperties(
                                lineColor(routeColorHex),
                                lineWidth(5f),
                                lineCap("round"),
                                lineJoin("round")
                            )
                        )

                        // Explored cells fill (below route line)
                        style.addSource(GeoJsonSource(EXPLORE_SOURCE_ID))
                        style.addLayerBelow(
                            FillLayer(EXPLORE_LAYER_ID, EXPLORE_SOURCE_ID).withProperties(
                                fillColor(exploreColorHex),
                                fillOpacity(0.55f)
                            ),
                            ROUTE_LAYER_ID
                        )

                        // Highlight markers: circle background + emoji text
                        style.addSource(GeoJsonSource(HIGHLIGHTS_SOURCE_ID))
                        style.addLayer(
                            CircleLayer(HIGHLIGHTS_CIRCLE_LAYER_ID, HIGHLIGHTS_SOURCE_ID).withProperties(
                                circleRadius(14f),
                                circleColor(highlightColorHex),
                                circleStrokeWidth(2f),
                                circleStrokeColor("#FFFFFF")
                            )
                        )
                        style.addLayer(
                            SymbolLayer(HIGHLIGHTS_TEXT_LAYER_ID, HIGHLIGHTS_SOURCE_ID).withProperties(
                                textField(get("emoji")),
                                textSize(13f),
                                textAnchor("center"),
                                textAllowOverlap(true),
                                textIgnorePlacement(true)
                            )
                        )

                        val greeneryLayers = listOf(
                            "landuse_park", "landuse_forest", "landuse_grass",
                            "landcover_wood", "landcover_grass", "landuse_meadow"
                        )
                        greeneryLayers.forEach { layerId ->
                            style.getLayer(layerId)?.let { layer ->
                                if (layer is FillLayer) layer.setProperties(fillColor(mapParkColor))
                            }
                        }

                        if (showMyLocation) {
                            try {
                                map.locationComponent.activateLocationComponent(
                                    LocationComponentActivationOptions.builder(context, style)
                                        .useDefaultLocationEngine(true)
                                        .build()
                                )
                                map.locationComponent.isLocationComponentEnabled = true
                                map.locationComponent.cameraMode = CameraMode.TRACKING
                                map.locationComponent.renderMode = RenderMode.COMPASS
                            } catch (_: Exception) {}
                        }
                    }

                    if (!isInteractive) {
                        map.uiSettings.setAllGesturesEnabled(false)
                    }
                }
                onStart()
            }
        },
        update = { _ ->
            mapRef?.let { map ->
                map.getStyle { style ->
                    // Route color + geometry
                    style.getLayer(ROUTE_LAYER_ID)?.let { layer ->
                        if (layer is LineLayer) layer.setProperties(lineColor(routeColorHex))
                    }
                    val routeSource = style.getSourceAs<GeoJsonSource>(ROUTE_SOURCE_ID)
                    if (routePoints.size >= 2) {
                        val points = routePoints.map { Point.fromLngLat(it.lng, it.lat) }
                        routeSource?.setGeoJson(
                            FeatureCollection.fromFeature(
                                Feature.fromGeometry(LineString.fromLngLats(points))
                            )
                        )
                    }

                    // Explored cells
                    val exploreSource = style.getSourceAs<GeoJsonSource>(EXPLORE_SOURCE_ID)
                    exploreSource?.setGeoJson(
                        exploreGeoJson ?: FeatureCollection.fromFeatures(emptyList())
                    )
                    style.getLayer(EXPLORE_LAYER_ID)?.let { layer ->
                        if (layer is FillLayer) layer.setProperties(fillColor(exploreColorHex))
                    }

                    // Highlight markers via GeoJSON — no deprecated addMarker/clear
                    val highlightSource = style.getSourceAs<GeoJsonSource>(HIGHLIGHTS_SOURCE_ID)
                    val highlightFeatures = highlights.map { h ->
                        Feature.fromGeometry(Point.fromLngLat(h.lng, h.lat)).apply {
                            addStringProperty("emoji", h.emoji)
                        }
                    }
                    highlightSource?.setGeoJson(FeatureCollection.fromFeatures(highlightFeatures))
                }

                if (!showMyLocation) {
                    currentLocation?.let { loc ->
                        map.animateCamera(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder()
                                    .target(LatLng(loc.latitude, loc.longitude))
                                    .zoom(16.0)
                                    .build()
                            ), 800
                        )
                    }
                    if (routePoints.size >= 2 && currentLocation == null) {
                        try {
                            val bounds = LatLngBounds.Builder()
                                .includes(routePoints.map { LatLng(it.lat, it.lng) })
                                .build()
                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80))
                        } catch (_: Exception) {}
                    }
                }
            }
        },
        onRelease = { mapView ->
            mapView.onStop()
            mapView.onDestroy()
        },
        modifier = modifier
    )
}

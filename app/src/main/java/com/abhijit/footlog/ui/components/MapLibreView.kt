package com.abhijit.footlog.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.abhijit.footlog.data.entity.HighlightEntity
import com.abhijit.footlog.data.entity.LatLngPoint
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
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
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

private const val STYLE_LIBERTY = "https://tiles.openfreemap.org/styles/liberty"
private const val STYLE_DARK = "https://tiles.openfreemap.org/styles/dark"
private const val ROUTE_SOURCE_ID = "route-source"
private const val ROUTE_LAYER_ID = "route-layer"

@Composable
fun MapLibreView(
    routePoints: List<LatLngPoint>,
    currentLocation: android.location.Location?,
    highlights: List<HighlightEntity> = emptyList(),
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

    val styleUri = if (isDark) STYLE_DARK else STYLE_LIBERTY

    AndroidView(
        factory = { context ->
            MapLibre.getInstance(context)
            MapView(context).apply {
                getMapAsync { map ->
                    mapRef = map
                    map.uiSettings.isRotateGesturesEnabled = false

                    // Set a comfortable zoom before the GPS dot locks in
                    if (showMyLocation) {
                        map.moveCamera(CameraUpdateFactory.zoomTo(15.0))
                    }

                    map.setStyle(Style.Builder().fromUri(styleUri)) { style ->
                        style.addSource(GeoJsonSource(ROUTE_SOURCE_ID))
                        style.addLayer(
                            LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID).withProperties(
                                lineColor(routeColorHex),
                                lineWidth(5f),
                                lineCap("round"),
                                lineJoin("round")
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

                        // Blue GPS dot + camera tracking for active sessions
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
                    style.getLayer(ROUTE_LAYER_ID)?.let { layer ->
                        if (layer is LineLayer) {
                            layer.setProperties(lineColor(routeColorHex))
                        }
                    }
                    val source = style.getSourceAs<GeoJsonSource>(ROUTE_SOURCE_ID)
                    if (routePoints.size >= 2) {
                        val points = routePoints.map { Point.fromLngLat(it.lng, it.lat) }
                        source?.setGeoJson(
                            FeatureCollection.fromFeature(
                                Feature.fromGeometry(LineString.fromLngLats(points))
                            )
                        )
                    }
                }

                map.clear()
                highlights.forEach { h ->
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(h.lat, h.lng))
                            .title("${h.emoji} ${h.name}")
                            .snippet(h.note ?: "")
                    )
                }

                // Manual camera only when location component is not handling it
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

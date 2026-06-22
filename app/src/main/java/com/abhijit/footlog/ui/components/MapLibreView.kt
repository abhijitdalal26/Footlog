package com.abhijit.footlog.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.abhijit.footlog.data.entity.HighlightEntity
import com.abhijit.footlog.data.entity.LatLngPoint
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

private const val TILE_STYLE = "https://tiles.openfreemap.org/styles/liberty"
private const val ROUTE_SOURCE_ID = "route-source"
private const val ROUTE_LAYER_ID = "route-layer"

@Composable
fun MapLibreView(
    routePoints: List<LatLngPoint>,
    currentLocation: android.location.Location?,
    highlights: List<HighlightEntity> = emptyList(),
    routeColor: Color,
    isInteractive: Boolean = true,
    modifier: Modifier = Modifier
) {
    var mapRef: MapLibreMap? by remember { mutableStateOf(null) }
    val routeColorHex = remember(routeColor) {
        "#%06X".format(routeColor.toArgb() and 0xFFFFFF)
    }

    AndroidView(
        factory = { context ->
            MapLibre.getInstance(context)
            MapView(context).apply {
                getMapAsync { map ->
                    mapRef = map
                    map.uiSettings.isRotateGesturesEnabled = false
                    map.setStyle(Style.Builder().fromUri(TILE_STYLE)) { style ->
                        style.addSource(GeoJsonSource(ROUTE_SOURCE_ID))
                        style.addLayer(
                            LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID).withProperties(
                                lineColor(routeColorHex),
                                lineWidth(4f),
                                lineCap("round"),
                                lineJoin("round")
                            )
                        )
                    }
                    if (!isInteractive) {
                        map.uiSettings.setAllGesturesEnabled(false)
                    }
                }
                onStart()
            }
        },
        update = { mapView ->
            mapRef?.let { map ->
                map.getStyle { style ->
                    val source = style.getSourceAs<GeoJsonSource>(ROUTE_SOURCE_ID)
                    if (routePoints.size >= 2) {
                        val points = routePoints.map { Point.fromLngLat(it.lng, it.lat) }
                        source?.setGeoJson(FeatureCollection.fromFeature(
                            Feature.fromGeometry(LineString.fromLngLats(points))
                        ))
                    }
                }
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
        },
        onRelease = { mapView ->
            mapView.onStop()
            mapView.onDestroy()
        },
        modifier = modifier
    )
}

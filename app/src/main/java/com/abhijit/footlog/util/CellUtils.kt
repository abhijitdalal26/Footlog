package com.abhijit.footlog.util

import org.maplibre.geojson.Point

private const val CELL_SIZE_METERS = 25.0

fun latToCell(lat: Double): Int = (lat * 111320 / CELL_SIZE_METERS).toInt()

fun lngToCell(lng: Double, lat: Double): Int =
    (lng * 111320 * Math.cos(Math.toRadians(lat)) / CELL_SIZE_METERS).toInt()

// Returns the 5-point closed ring (SW→SE→NE→NW→SW) for a cell polygon in GeoJSON order (lng, lat)
fun cellBoundsPolygon(cellX: Int, cellY: Int): List<Point> {
    val latMin = cellX * CELL_SIZE_METERS / 111320.0
    val latMax = (cellX + 1) * CELL_SIZE_METERS / 111320.0
    val metersPerDeg = 111320.0 * Math.cos(Math.toRadians(latMin))
    val lngMin = cellY * CELL_SIZE_METERS / metersPerDeg
    val lngMax = (cellY + 1) * CELL_SIZE_METERS / metersPerDeg
    return listOf(
        Point.fromLngLat(lngMin, latMin),
        Point.fromLngLat(lngMax, latMin),
        Point.fromLngLat(lngMax, latMax),
        Point.fromLngLat(lngMin, latMax),
        Point.fromLngLat(lngMin, latMin)
    )
}

fun cellCenterLatLng(cellX: Int, cellY: Int): Pair<Double, Double> {
    val lat = (cellX + 0.5) * CELL_SIZE_METERS / 111320.0
    val metersPerDeg = 111320.0 * Math.cos(Math.toRadians(lat))
    val lng = (cellY + 0.5) * CELL_SIZE_METERS / metersPerDeg
    return Pair(lat, lng)
}

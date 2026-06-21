package com.abhijit.footlog.util

private const val CELL_SIZE_METERS = 25.0

fun latToCell(lat: Double): Int = (lat * 111320 / CELL_SIZE_METERS).toInt()

fun lngToCell(lng: Double): Int = (lng * 111320 * Math.cos(Math.toRadians(lng)) / CELL_SIZE_METERS).toInt()

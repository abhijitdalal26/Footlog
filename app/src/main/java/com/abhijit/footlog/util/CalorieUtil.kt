package com.abhijit.footlog.util

fun estimateCalories(distanceMeters: Float, activityType: String): Int {
    val calPerKm = when (activityType.lowercase()) {
        "run" -> 80f
        "cycle" -> 40f
        else -> 60f
    }
    return (distanceMeters / 1000f * calPerKm).toInt()
}

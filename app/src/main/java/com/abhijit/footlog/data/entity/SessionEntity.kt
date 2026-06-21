package com.abhijit.footlog.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.abhijit.footlog.data.db.Converters

@Entity(tableName = "sessions")
@TypeConverters(Converters::class)
data class SessionEntity(
    @PrimaryKey val id: String,
    val activityType: String,
    val startTime: Long,
    val endTime: Long,
    val distanceMeters: Float,
    val title: String,
    val isFavoriteRoute: Boolean = false,
    val routePoints: List<LatLngPoint> = emptyList(),
    val noteId: String? = null
)

data class LatLngPoint(val lat: Double, val lng: Double)

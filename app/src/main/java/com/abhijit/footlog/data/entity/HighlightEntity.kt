package com.abhijit.footlog.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "highlights")
data class HighlightEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val lat: Double,
    val lng: Double,
    val category: String,
    val emoji: String,
    val name: String,
    val note: String? = null,
    val photoPath: String? = null
)

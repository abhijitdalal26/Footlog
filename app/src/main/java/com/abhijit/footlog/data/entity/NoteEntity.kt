package com.abhijit.footlog.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NoteType { VOICE, TEXT }

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val type: NoteType,
    val content: String,
    val createdAt: Long
)

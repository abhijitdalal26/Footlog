package com.abhijit.footlog.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class NoteType { VOICE, TEXT }

@Entity(tableName = "notes", indices = [Index(value = ["sessionId"], unique = true)])
data class NoteEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val type: NoteType,
    val content: String,
    val createdAt: Long
)

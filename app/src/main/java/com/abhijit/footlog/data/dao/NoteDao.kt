package com.abhijit.footlog.data.dao

import androidx.room.*
import com.abhijit.footlog.data.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE sessionId = :sessionId LIMIT 1")
    fun getForSessionFlow(sessionId: String): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getForSession(sessionId: String): NoteEntity?

    @Query("SELECT COUNT(*) FROM notes WHERE sessionId = :sessionId")
    fun hasNoteFlow(sessionId: String): Flow<Boolean>

    @Query("SELECT * FROM notes")
    suspend fun getAll(): List<NoteEntity>
}

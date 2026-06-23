package com.abhijit.footlog.data.dao

import androidx.room.*
import com.abhijit.footlog.data.entity.HighlightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HighlightDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(highlight: HighlightEntity)

    @Query("SELECT * FROM highlights WHERE sessionId = :sessionId")
    fun getForSessionFlow(sessionId: String): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights WHERE id = :id")
    fun getByIdFlow(id: String): Flow<HighlightEntity?>

    @Query("SELECT * FROM highlights")
    suspend fun getAll(): List<HighlightEntity>
}

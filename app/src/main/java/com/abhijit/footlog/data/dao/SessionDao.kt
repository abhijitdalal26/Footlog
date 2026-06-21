package com.abhijit.footlog.data.dao

import androidx.room.*
import com.abhijit.footlog.data.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionEntity)

    @Update
    suspend fun update(session: SessionEntity)

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAllFlow(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions ORDER BY startTime DESC LIMIT 10")
    fun getRecentFlow(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    fun getByIdFlow(id: String): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getById(id: String): SessionEntity?

    @Query("SELECT * FROM sessions WHERE isFavoriteRoute = 1 ORDER BY startTime DESC")
    fun getFavoritesFlow(): Flow<List<SessionEntity>>

    @Query("SELECT SUM(distanceMeters) FROM sessions")
    fun getTotalDistanceFlow(): Flow<Float?>

    @Query("SELECT COUNT(*) FROM sessions")
    fun getSessionCountFlow(): Flow<Int>

    @Query("SELECT startTime FROM sessions ORDER BY startTime DESC")
    suspend fun getAllStartTimes(): List<Long>

    @Query("SELECT SUM(distanceMeters) FROM sessions WHERE startTime >= :startMs AND startTime < :endMs")
    suspend fun getDistanceBetween(startMs: Long, endMs: Long): Float?

    @Delete
    suspend fun delete(session: SessionEntity)
}

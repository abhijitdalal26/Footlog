package com.abhijit.footlog.data.dao

import androidx.room.*
import com.abhijit.footlog.data.entity.ExploredCellEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExploredCellDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(cell: ExploredCellEntity)

    @Query("SELECT COUNT(*) FROM explored_cells")
    fun getCellCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM explored_cells")
    suspend fun getCellCount(): Int
}

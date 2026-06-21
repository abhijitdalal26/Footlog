package com.abhijit.footlog.data.entity

import androidx.room.Entity

@Entity(tableName = "explored_cells", primaryKeys = ["cellX", "cellY"])
data class ExploredCellEntity(
    val cellX: Int,
    val cellY: Int,
    val firstVisitedAt: Long
)

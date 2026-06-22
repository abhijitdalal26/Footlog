package com.abhijit.footlog.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.abhijit.footlog.data.dao.ExploredCellDao
import com.abhijit.footlog.data.dao.HighlightDao
import com.abhijit.footlog.data.dao.NoteDao
import com.abhijit.footlog.data.dao.SessionDao
import com.abhijit.footlog.data.entity.ExploredCellEntity
import com.abhijit.footlog.data.entity.HighlightEntity
import com.abhijit.footlog.data.entity.NoteEntity
import com.abhijit.footlog.data.entity.SessionEntity

@Database(
    entities = [SessionEntity::class, NoteEntity::class, HighlightEntity::class, ExploredCellEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FootlogDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun noteDao(): NoteDao
    abstract fun highlightDao(): HighlightDao
    abstract fun exploredCellDao(): ExploredCellDao

    companion object {
        @Volatile private var instance: FootlogDatabase? = null

        fun get(context: Context): FootlogDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FootlogDatabase::class.java,
                    "footlog.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build().also { instance = it }
            }
    }
}

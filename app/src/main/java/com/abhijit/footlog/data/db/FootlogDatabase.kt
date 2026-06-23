package com.abhijit.footlog.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FootlogDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun noteDao(): NoteDao
    abstract fun highlightDao(): HighlightDao
    abstract fun exploredCellDao(): ExploredCellDao

    companion object {
        @Volatile private var instance: FootlogDatabase? = null

        // Added unique index on notes.sessionId (one note per session).
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_notes_sessionId` ON `notes` (`sessionId`)"
                )
            }
        }

        fun get(context: Context): FootlogDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FootlogDatabase::class.java,
                    "footlog.db"
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}

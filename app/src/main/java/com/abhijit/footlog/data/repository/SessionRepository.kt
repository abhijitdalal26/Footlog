package com.abhijit.footlog.data.repository

import android.content.Context
import com.abhijit.footlog.data.dao.ExploredCellDao
import com.abhijit.footlog.data.dao.HighlightDao
import com.abhijit.footlog.data.dao.NoteDao
import com.abhijit.footlog.data.dao.SessionDao
import com.abhijit.footlog.data.db.FootlogDatabase
import com.abhijit.footlog.data.entity.*
import com.abhijit.footlog.util.latToCell
import com.abhijit.footlog.util.lngToCell
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class SessionRepository(context: Context) {
    private val db = FootlogDatabase.get(context)
    private val sessionDao: SessionDao = db.sessionDao()
    private val noteDao: NoteDao = db.noteDao()
    private val highlightDao: HighlightDao = db.highlightDao()
    private val exploredCellDao: ExploredCellDao = db.exploredCellDao()

    // Sessions
    fun getAllSessions(): Flow<List<SessionEntity>> = sessionDao.getAllFlow()
    fun getRecentSessions(): Flow<List<SessionEntity>> = sessionDao.getRecentFlow()
    fun getSessionById(id: String): Flow<SessionEntity?> = sessionDao.getByIdFlow(id)
    fun getFavoriteSessions(): Flow<List<SessionEntity>> = sessionDao.getFavoritesFlow()
    fun getTotalDistance(): Flow<Float?> = sessionDao.getTotalDistanceFlow()
    fun getSessionCount(): Flow<Int> = sessionDao.getSessionCountFlow()

    suspend fun saveSession(session: SessionEntity) = sessionDao.insert(session)
    suspend fun updateSession(session: SessionEntity) = sessionDao.update(session)
    suspend fun getSessionByIdOnce(id: String): SessionEntity? = sessionDao.getById(id)

    // Notes
    fun getNoteForSession(sessionId: String): Flow<NoteEntity?> = noteDao.getForSessionFlow(sessionId)
    fun hasNote(sessionId: String): Flow<Boolean> = noteDao.hasNoteFlow(sessionId)
    suspend fun saveNote(note: NoteEntity) = noteDao.insert(note)
    suspend fun getNoteForSessionOnce(sessionId: String): NoteEntity? = noteDao.getForSession(sessionId)

    // Highlights
    fun getHighlightsForSession(sessionId: String): Flow<List<HighlightEntity>> =
        highlightDao.getForSessionFlow(sessionId)
    fun getHighlightById(id: String): Flow<HighlightEntity?> = highlightDao.getByIdFlow(id)
    suspend fun saveHighlight(highlight: HighlightEntity) = highlightDao.insert(highlight)

    // Explored cells
    suspend fun insertExploredCell(lat: Double, lng: Double) {
        val cellX = latToCell(lat)
        val cellY = lngToCell(lng)
        exploredCellDao.insert(ExploredCellEntity(cellX, cellY, System.currentTimeMillis()))
    }
    fun getExploredCellCount(): Flow<Int> = exploredCellDao.getCellCountFlow()
    suspend fun getExploredCellCountOnce(): Int = exploredCellDao.getCellCount()

    // Stats
    suspend fun getWeeklyDistances(): List<Float> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val today = cal.timeInMillis
        return (6 downTo 0).map { daysAgo ->
            val dayStart = today - daysAgo * 86400000L
            val dayEnd = dayStart + 86400000L
            sessionDao.getDistanceBetween(dayStart, dayEnd) ?: 0f
        }
    }

    suspend fun getCurrentStreak(): Int {
        val startTimes = sessionDao.getAllStartTimes()
        if (startTimes.isEmpty()) return 0
        val cal = Calendar.getInstance()
        fun dayKey(ms: Long): Long {
            cal.timeInMillis = ms
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }
        val uniqueDays = startTimes.map { dayKey(it) }.toSortedSet().toList().sortedDescending()
        val today = dayKey(System.currentTimeMillis())
        var streak = 0
        var expected = today
        for (day in uniqueDays) {
            if (day == expected) { streak++; expected -= 86400000L } else break
        }
        return streak
    }
}

package com.abhijit.footlog.data.repository

import android.content.Context
import android.util.Log
import com.abhijit.footlog.data.dao.ExploredCellDao
import com.abhijit.footlog.data.dao.HighlightDao
import com.abhijit.footlog.data.dao.NoteDao
import com.abhijit.footlog.data.dao.SessionDao
import com.abhijit.footlog.data.db.FootlogDatabase
import com.abhijit.footlog.data.entity.*
import com.abhijit.footlog.data.preferences.AppPreferences
import com.abhijit.footlog.data.sync.FirebaseSyncRepository
import com.abhijit.footlog.util.latToCell
import com.abhijit.footlog.util.lngToCell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar

private const val TAG = "SessionRepository"

class SessionRepository(context: Context) {
    private val db = FootlogDatabase.get(context)
    private val sessionDao: SessionDao = db.sessionDao()
    private val noteDao: NoteDao = db.noteDao()
    private val highlightDao: HighlightDao = db.highlightDao()
    private val exploredCellDao: ExploredCellDao = db.exploredCellDao()

    private val prefs = AppPreferences(context)
    private val syncRepo = FirebaseSyncRepository()
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // ── Sessions ───────────────────────────────────────────────────────────

    fun getAllSessions(): Flow<List<SessionEntity>> = sessionDao.getAllFlow()
    fun getRecentSessions(): Flow<List<SessionEntity>> = sessionDao.getRecentFlow()
    fun getSessionById(id: String): Flow<SessionEntity?> = sessionDao.getByIdFlow(id)
    fun getFavoriteSessions(): Flow<List<SessionEntity>> = sessionDao.getFavoritesFlow()
    fun getTotalDistance(): Flow<Float?> = sessionDao.getTotalDistanceFlow()
    fun getSessionCount(): Flow<Int> = sessionDao.getSessionCountFlow()

    suspend fun saveSession(session: SessionEntity) {
        sessionDao.insert(session)
        fireSync { uid -> syncRepo.syncSession(uid, session) }
    }

    suspend fun updateSession(session: SessionEntity) {
        sessionDao.update(session)
        fireSync { uid -> syncRepo.syncSession(uid, session) }
    }

    suspend fun getSessionByIdOnce(id: String): SessionEntity? = sessionDao.getById(id)
    suspend fun getAllSessionsOnce(): List<SessionEntity> = sessionDao.getAll()

    // ── Notes ──────────────────────────────────────────────────────────────

    fun getNoteForSession(sessionId: String): Flow<NoteEntity?> = noteDao.getForSessionFlow(sessionId)
    fun hasNote(sessionId: String): Flow<Boolean> = noteDao.hasNoteFlow(sessionId)

    suspend fun saveNote(note: NoteEntity) {
        noteDao.insert(note)
        fireSync { uid -> syncRepo.syncNote(uid, note) }
    }

    suspend fun getNoteForSessionOnce(sessionId: String): NoteEntity? = noteDao.getForSession(sessionId)
    suspend fun getAllNotesOnce(): List<NoteEntity> = noteDao.getAll()

    // ── Highlights ─────────────────────────────────────────────────────────

    fun getHighlightsForSession(sessionId: String): Flow<List<HighlightEntity>> =
        highlightDao.getForSessionFlow(sessionId)
    fun getHighlightById(id: String): Flow<HighlightEntity?> = highlightDao.getByIdFlow(id)

    suspend fun saveHighlight(highlight: HighlightEntity) {
        highlightDao.insert(highlight)
        fireSync { uid -> syncRepo.syncHighlight(uid, highlight) }
    }

    suspend fun getAllHighlightsOnce(): List<HighlightEntity> = highlightDao.getAll()

    // ── Explored cells ─────────────────────────────────────────────────────

    suspend fun insertExploredCell(lat: Double, lng: Double) {
        val cellX = latToCell(lat)
        val cellY = lngToCell(lng, lat)
        val cell = ExploredCellEntity(cellX, cellY, System.currentTimeMillis())
        exploredCellDao.insert(cell)
        fireSync { uid -> syncRepo.syncExploredCell(uid, cellX, cellY, cell.firstVisitedAt) }
    }

    fun getExploredCellCount(): Flow<Int> = exploredCellDao.getCellCountFlow()
    suspend fun getExploredCellCountOnce(): Int = exploredCellDao.getCellCount()
    suspend fun getAllExploredCellsOnce(): List<ExploredCellEntity> = exploredCellDao.getAll()

    // ── Stats ──────────────────────────────────────────────────────────────

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

    // ── Initial sync helpers (called from ProfileViewModel on sign-in) ──────

    suspend fun uploadAllToCloud(uid: String) {
        getAllSessionsOnce().forEach { runCatching { syncRepo.syncSession(uid, it) } }
        getAllNotesOnce().forEach { runCatching { syncRepo.syncNote(uid, it) } }
        getAllHighlightsOnce().forEach { runCatching { syncRepo.syncHighlight(uid, it) } }
        getAllExploredCellsOnce().forEach {
            runCatching { syncRepo.syncExploredCell(uid, it.cellX, it.cellY, it.firstVisitedAt) }
        }
    }

    suspend fun mergeFromCloud(uid: String) {
        val localSessionIds = getAllSessionsOnce().map { it.id }.toSet()
        syncRepo.fetchRemoteSessions(uid)
            .filter { it.id !in localSessionIds }
            .forEach { sessionDao.insert(it) }

        val localNoteIds = getAllNotesOnce().map { it.id }.toSet()
        syncRepo.fetchRemoteNotes(uid)
            .filter { it.id !in localNoteIds }
            .forEach { noteDao.insert(it) }

        val localHighlightIds = getAllHighlightsOnce().map { it.id }.toSet()
        syncRepo.fetchRemoteHighlights(uid)
            .filter { it.id !in localHighlightIds }
            .forEach { highlightDao.insert(it) }

        syncRepo.fetchRemoteExploredCells(uid)
            .forEach { exploredCellDao.insert(it) }
    }

    // ── Internal ───────────────────────────────────────────────────────────

    private fun fireSync(block: suspend (uid: String) -> Unit) {
        syncScope.launch {
            val uid = prefs.firebaseUid.firstOrNull() ?: return@launch
            runCatching { block(uid) }.onFailure { Log.w(TAG, "Sync failed", it) }
        }
    }
}

package com.abhijit.footlog.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.abhijit.footlog.data.entity.ExploredCellEntity
import com.abhijit.footlog.data.entity.HighlightEntity
import com.abhijit.footlog.data.entity.LatLngPoint
import com.abhijit.footlog.data.entity.NoteEntity
import com.abhijit.footlog.data.entity.NoteType
import com.abhijit.footlog.data.entity.SessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var db: FootlogDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FootlogDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun sessionDao_insertAndRetrieve() = runBlocking {
        val session = SessionEntity(
            id = "s1",
            activityType = "walk",
            startTime = 1000L,
            endTime = 2000L,
            distanceMeters = 1500f,
            title = "Morning Walk"
        )
        db.sessionDao().insert(session)

        val all = db.sessionDao().getAll()
        assertEquals(1, all.size)
        assertEquals("Morning Walk", all[0].title)
        assertEquals(1500f, all[0].distanceMeters, 0.01f)
    }

    @Test
    fun sessionDao_update() = runBlocking {
        val session = SessionEntity(
            id = "s2", activityType = "run", startTime = 1000L,
            endTime = 2000L, distanceMeters = 5000f, title = "Run"
        )
        db.sessionDao().insert(session)
        db.sessionDao().update(session.copy(title = "Evening Run"))

        val updated = db.sessionDao().getById("s2")
        assertEquals("Evening Run", updated!!.title)
    }

    @Test
    fun sessionDao_delete() = runBlocking {
        val s1 = SessionEntity("s1", "walk", 1000L, 2000L, 100f, "W1")
        val s2 = SessionEntity("s2", "run", 3000L, 4000L, 200f, "R1")
        db.sessionDao().insert(s1)
        db.sessionDao().insert(s2)
        db.sessionDao().delete(s1)

        val all = db.sessionDao().getAll()
        assertEquals(1, all.size)
        assertEquals("s2", all[0].id)
    }

    @Test
    fun sessionDao_getByIdOnce_returnsNullForMissing() = runBlocking {
        val result = db.sessionDao().getById("nonexistent")
        assertNull(result)
    }

    @Test
    fun sessionDao_getTotalDistanceFlow() = runBlocking {
        db.sessionDao().insert(SessionEntity("s1", "walk", 1000L, 2000L, 1000f, "W1"))
        db.sessionDao().insert(SessionEntity("s2", "run", 3000L, 4000L, 2000f, "R1"))

        val total = db.sessionDao().getTotalDistanceFlow().first()
        assertEquals(3000f, total!!, 0.01f)
    }

    @Test
    fun sessionDao_getDistanceBetween() = runBlocking {
        db.sessionDao().insert(SessionEntity("s1", "walk", 500L, 1000L, 500f, "W1"))
        db.sessionDao().insert(SessionEntity("s2", "walk", 2000L, 3000L, 1000f, "W2"))

        val dist = db.sessionDao().getDistanceBetween(1000L, 2500L)
        assertEquals(1000f, dist!!, 0.01f)
    }

    @Test
    fun sessionDao_getAllStartTimes() = runBlocking {
        db.sessionDao().insert(SessionEntity("s1", "walk", 3000L, 4000L, 100f, "W1"))
        db.sessionDao().insert(SessionEntity("s2", "run", 1000L, 2000L, 200f, "R1"))

        val times = db.sessionDao().getAllStartTimes()
        assertEquals(listOf(3000L, 1000L), times)
    }

    @Test
    fun sessionDao_getSessionCountFlow() = runBlocking {
        assertEquals(0, db.sessionDao().getSessionCountFlow().first())
        db.sessionDao().insert(SessionEntity("s1", "walk", 1000L, 2000L, 100f, "W1"))
        assertEquals(1, db.sessionDao().getSessionCountFlow().first())
    }

    @Test
    fun exploredCellDao_insertAndCount() = runBlocking {
        db.exploredCellDao().insert(ExploredCellEntity(100, 200, 1000L))
        db.exploredCellDao().insert(ExploredCellEntity(101, 200, 2000L))

        assertEquals(2, db.exploredCellDao().getCellCount())
    }

    @Test
    fun exploredCellDao_ignoreDuplicate() = runBlocking {
        db.exploredCellDao().insert(ExploredCellEntity(100, 200, 1000L))
        db.exploredCellDao().insert(ExploredCellEntity(100, 200, 2000L))

        assertEquals(1, db.exploredCellDao().getCellCount())
    }

    @Test
    fun exploredCellDao_getAllFlow() = runBlocking {
        val cell = ExploredCellEntity(50, 60, 500L)
        db.exploredCellDao().insert(cell)

        val cells = db.exploredCellDao().getAllFlow().first()
        assertEquals(1, cells.size)
        assertEquals(50, cells[0].cellX)
        assertEquals(60, cells[0].cellY)
        assertEquals(500L, cells[0].firstVisitedAt)
    }

    @Test
    fun noteDao_insertAndRetrieve() = runBlocking {
        val note = NoteEntity("n1", "s1", NoteType.TEXT, "Hello", 1000L)
        db.noteDao().insert(note)

        val retrieved = db.noteDao().getForSession("s1")
        assertNotNull(retrieved)
        assertEquals("Hello", retrieved!!.content)
    }

    @Test
    fun noteDao_hasNoteFlow() = runBlocking {
        db.noteDao().insert(NoteEntity("n1", "s1", NoteType.TEXT, "Hi", 1000L))

        val hasNote = db.noteDao().hasNoteFlow("s1").first()
        assertEquals(true, hasNote)
    }

    @Test
    fun noteDao_enforceOneNotePerSession() = runBlocking {
        db.noteDao().insert(NoteEntity("n1", "s1", NoteType.TEXT, "First", 1000L))
        db.noteDao().insert(NoteEntity("n2", "s1", NoteType.TEXT, "Second", 2000L))

        val notes = db.noteDao().getAll()
        assertEquals(1, notes.size)
        assertEquals("Second", notes[0].content)
    }

    @Test
    fun highlightDao_insertAndGetForSession() = runBlocking {
        val h = HighlightEntity("h1", "s1", 51.5, -0.13, "cafe", "☕", "Coffee Shop")
        db.highlightDao().insert(h)

        val highlights = db.highlightDao().getForSession("s1")
        assertEquals(1, highlights.size)
        assertEquals("Coffee Shop", highlights[0].name)
    }

    @Test
    fun highlightDao_getById() = runBlocking {
        val h = HighlightEntity("h1", "s1", 51.5, -0.13, "cafe", "☕", "Shop")
        db.highlightDao().insert(h)

        val retrieved = db.highlightDao().getById("h1")
        assertNotNull(retrieved)
        assertEquals("cafe", retrieved!!.category)
    }

    @Test
    fun sessionDao_withRoutePoints() = runBlocking {
        val points = listOf(LatLngPoint(51.5, -0.13), LatLngPoint(51.51, -0.12))
        val session = SessionEntity("s1", "walk", 1000L, 2000L, 500f, "W1", routePoints = points)
        db.sessionDao().insert(session)

        val retrieved = db.sessionDao().getById("s1")
        assertEquals(2, retrieved!!.routePoints.size)
        assertEquals(51.5, retrieved.routePoints[0].lat, 0.001)
        assertEquals(-0.12, retrieved.routePoints[1].lng, 0.001)
    }
}

package com.abhijit.footlog.data.sync

import com.abhijit.footlog.data.entity.*
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class FirebaseSyncRepository {

    private val db = Firebase.firestore

    private fun userRef(uid: String) = db.collection("users").document(uid)

    // ── Upload individual entities ──────────────────────────────────────────

    suspend fun syncSession(uid: String, session: SessionEntity) {
        val data = hashMapOf(
            "id" to session.id,
            "activityType" to session.activityType,
            "startTime" to session.startTime,
            "endTime" to session.endTime,
            "distanceMeters" to session.distanceMeters,
            "title" to session.title,
            "isFavoriteRoute" to session.isFavoriteRoute,
            "routePoints" to session.routePoints.map { mapOf("lat" to it.lat, "lng" to it.lng) },
            "noteId" to session.noteId
        )
        userRef(uid).collection("sessions").document(session.id).set(data).await()
    }

    suspend fun syncNote(uid: String, note: NoteEntity) {
        val data = hashMapOf(
            "id" to note.id,
            "sessionId" to note.sessionId,
            "type" to note.type.name,
            "content" to note.content,
            "createdAt" to note.createdAt
        )
        userRef(uid).collection("notes").document(note.id).set(data).await()
    }

    suspend fun syncHighlight(uid: String, highlight: HighlightEntity) {
        val data = hashMapOf(
            "id" to highlight.id,
            "sessionId" to highlight.sessionId,
            "lat" to highlight.lat,
            "lng" to highlight.lng,
            "category" to highlight.category,
            "emoji" to highlight.emoji,
            "name" to highlight.name,
            "note" to highlight.note,
            "photoPath" to highlight.photoPath
        )
        userRef(uid).collection("highlights").document(highlight.id).set(data).await()
    }

    suspend fun syncExploredCell(uid: String, cellX: Int, cellY: Int, firstVisitedAt: Long) {
        val data = hashMapOf("cellX" to cellX, "cellY" to cellY, "firstVisitedAt" to firstVisitedAt)
        userRef(uid).collection("explored_cells").document("${cellX}_${cellY}").set(data).await()
    }

    // ── Fetch all remote data (used on sign-in to a new device) ────────────

    suspend fun fetchRemoteSessions(uid: String): List<SessionEntity> =
        userRef(uid).collection("sessions").get().await().documents.mapNotNull { doc ->
            runCatching {
                val d = doc.data ?: return@mapNotNull null
                @Suppress("UNCHECKED_CAST")
                val points = (d["routePoints"] as? List<Map<String, Double>>)
                    ?.map { LatLngPoint(it["lat"] ?: 0.0, it["lng"] ?: 0.0) } ?: emptyList()
                SessionEntity(
                    id = d["id"] as? String ?: doc.id,
                    activityType = d["activityType"] as? String ?: "",
                    startTime = (d["startTime"] as? Long) ?: 0L,
                    endTime = (d["endTime"] as? Long) ?: 0L,
                    distanceMeters = (d["distanceMeters"] as? Double)?.toFloat()
                        ?: (d["distanceMeters"] as? Long)?.toFloat() ?: 0f,
                    title = d["title"] as? String ?: "",
                    isFavoriteRoute = d["isFavoriteRoute"] as? Boolean ?: false,
                    routePoints = points,
                    noteId = d["noteId"] as? String
                )
            }.getOrNull()
        }

    suspend fun fetchRemoteNotes(uid: String): List<NoteEntity> =
        userRef(uid).collection("notes").get().await().documents.mapNotNull { doc ->
            runCatching {
                val d = doc.data ?: return@mapNotNull null
                NoteEntity(
                    id = d["id"] as? String ?: doc.id,
                    sessionId = d["sessionId"] as? String ?: "",
                    type = NoteType.valueOf(d["type"] as? String ?: NoteType.TEXT.name),
                    content = d["content"] as? String ?: "",
                    createdAt = (d["createdAt"] as? Long) ?: 0L
                )
            }.getOrNull()
        }

    suspend fun fetchRemoteHighlights(uid: String): List<HighlightEntity> =
        userRef(uid).collection("highlights").get().await().documents.mapNotNull { doc ->
            runCatching {
                val d = doc.data ?: return@mapNotNull null
                HighlightEntity(
                    id = d["id"] as? String ?: doc.id,
                    sessionId = d["sessionId"] as? String ?: "",
                    lat = (d["lat"] as? Double) ?: 0.0,
                    lng = (d["lng"] as? Double) ?: 0.0,
                    category = d["category"] as? String ?: "",
                    emoji = d["emoji"] as? String ?: "",
                    name = d["name"] as? String ?: "",
                    note = d["note"] as? String,
                    photoPath = d["photoPath"] as? String
                )
            }.getOrNull()
        }

    suspend fun fetchRemoteExploredCells(uid: String): List<ExploredCellEntity> =
        userRef(uid).collection("explored_cells").get().await().documents.mapNotNull { doc ->
            runCatching {
                val d = doc.data ?: return@mapNotNull null
                ExploredCellEntity(
                    cellX = (d["cellX"] as? Long)?.toInt() ?: 0,
                    cellY = (d["cellY"] as? Long)?.toInt() ?: 0,
                    firstVisitedAt = (d["firstVisitedAt"] as? Long) ?: 0L
                )
            }.getOrNull()
        }
}

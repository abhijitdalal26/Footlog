package com.abhijit.footlog.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable object Home : Screen
    @Serializable object History : Screen
    @Serializable object Routes : Screen
    @Serializable object Stats : Screen
    @Serializable data class ActiveTracking(val activityType: String) : Screen
    @Serializable data class SessionSummary(val sessionId: String) : Screen
    @Serializable data class ShareCard(val sessionId: String) : Screen
    @Serializable data class SessionDetail(val sessionId: String) : Screen
    @Serializable data class NoteWriting(val sessionId: String) : Screen
    @Serializable data class NoteView(val sessionId: String) : Screen
    @Serializable data class HighlightDetail(val highlightId: String) : Screen
    @Serializable object Onboarding : Screen
}

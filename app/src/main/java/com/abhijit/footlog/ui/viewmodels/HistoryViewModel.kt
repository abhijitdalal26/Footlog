package com.abhijit.footlog.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abhijit.footlog.data.entity.SessionEntity
import com.abhijit.footlog.data.repository.SessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SessionRepository(app)

    val groupedSessions: StateFlow<Map<String, List<SessionEntity>>> =
        repo.getAllSessions().map { sessions ->
            val now = LocalDate.now()
            val zone = ZoneId.systemDefault()
            sessions.groupBy { s ->
                val sessionDate = Instant.ofEpochMilli(s.startTime).atZone(zone).toLocalDate()
                val daysAgo = ChronoUnit.DAYS.between(sessionDate, now)
                when {
                    daysAgo < 7 -> "This week"
                    daysAgo < 14 -> "Last week"
                    else -> DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()).format(sessionDate)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun deleteSession(session: SessionEntity) {
        viewModelScope.launch { repo.deleteSession(session) }
    }

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(
                modelClass: Class<T>,
                extras: androidx.lifecycle.viewmodel.CreationExtras
            ): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                return HistoryViewModel(app) as T
            }
        }
    }
}

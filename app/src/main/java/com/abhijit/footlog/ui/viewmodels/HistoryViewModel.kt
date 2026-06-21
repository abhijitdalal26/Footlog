package com.abhijit.footlog.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abhijit.footlog.data.entity.SessionEntity
import com.abhijit.footlog.data.repository.SessionRepository
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SessionRepository(app)

    val groupedSessions: StateFlow<Map<String, List<SessionEntity>>> =
        repo.getAllSessions().map { sessions ->
            val cal = Calendar.getInstance()
            val now = System.currentTimeMillis()
            val weekMs = 7 * 86400000L
            sessions.groupBy { s ->
                when {
                    now - s.startTime < weekMs -> "This week"
                    now - s.startTime < 2 * weekMs -> "Last week"
                    else -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(s.startTime))
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

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

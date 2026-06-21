package com.abhijit.footlog.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abhijit.footlog.data.entity.SessionEntity
import com.abhijit.footlog.data.repository.SessionRepository
import kotlinx.coroutines.flow.*

class SessionDetailViewModel(app: Application, sessionId: String) : AndroidViewModel(app) {
    private val repo = SessionRepository(app)

    val session: StateFlow<SessionEntity?> = repo.getSessionById(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val hasNote: StateFlow<Boolean> = repo.hasNote(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    companion object {
        fun Factory(sessionId: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(
                modelClass: Class<T>,
                extras: androidx.lifecycle.viewmodel.CreationExtras
            ): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                return SessionDetailViewModel(app, sessionId) as T
            }
        }
    }
}

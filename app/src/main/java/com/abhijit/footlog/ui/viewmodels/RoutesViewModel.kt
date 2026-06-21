package com.abhijit.footlog.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abhijit.footlog.data.entity.SessionEntity
import com.abhijit.footlog.data.repository.SessionRepository
import kotlinx.coroutines.flow.*

class RoutesViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SessionRepository(app)

    val favoriteRoutes: StateFlow<List<SessionEntity>> = repo.getFavoriteSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(
                modelClass: Class<T>,
                extras: androidx.lifecycle.viewmodel.CreationExtras
            ): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                return RoutesViewModel(app) as T
            }
        }
    }
}

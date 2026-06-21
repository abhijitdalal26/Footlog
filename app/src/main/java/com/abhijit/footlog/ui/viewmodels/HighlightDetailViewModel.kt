package com.abhijit.footlog.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abhijit.footlog.data.entity.HighlightEntity
import com.abhijit.footlog.data.repository.SessionRepository
import kotlinx.coroutines.flow.*

class HighlightDetailViewModel(app: Application, highlightId: String) : AndroidViewModel(app) {
    private val repo = SessionRepository(app)

    val highlight: StateFlow<HighlightEntity?> = repo.getHighlightById(highlightId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    companion object {
        fun Factory(highlightId: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(
                modelClass: Class<T>,
                extras: androidx.lifecycle.viewmodel.CreationExtras
            ): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                return HighlightDetailViewModel(app, highlightId) as T
            }
        }
    }
}

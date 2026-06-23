package com.abhijit.footlog.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.abhijit.footlog.data.entity.SessionEntity
import com.abhijit.footlog.data.repository.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SessionRepository(app)

    val recentSessions: StateFlow<List<SessionEntity>> = repo.getRecentSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val prefs = com.abhijit.footlog.data.preferences.AppPreferences(app)

    val userName = prefs.userName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val profilePhotoUri = prefs.profilePhotoUri.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setProfilePhoto(uri: String?) {
        viewModelScope.launch {
            prefs.setProfilePhotoUri(uri)
        }
    }
    
    fun setUserProfile(name: String?, email: String?, photoUri: String?) {
        viewModelScope.launch {
            prefs.setUserProfile(name, email, photoUri)
        }
    }

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                return HomeViewModel(app) as T
            }
        }
    }
}

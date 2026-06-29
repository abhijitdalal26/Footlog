package com.abhijit.footlog.ui.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.abhijit.footlog.data.preferences.AppPreferences
import com.abhijit.footlog.data.repository.SessionRepository
import com.abhijit.footlog.util.AuthHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "ProfileViewModel"

class ProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = AppPreferences(app)
    private val repo = SessionRepository(app)

    val userName = prefs.userName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val userEmail = prefs.userEmail.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val profilePhotoUri = prefs.profilePhotoUri.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val themeMode = prefs.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")
    val gpsAccuracy = prefs.gpsAccuracy.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 50f)

    val isSignedIn: StateFlow<Boolean> = prefs.firebaseUid
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _signInError = MutableStateFlow<String?>(null)
    val signInError: StateFlow<String?> = _signInError.asStateFlow()

    init {
        // Reconcile Firebase auth state with DataStore on app start
        viewModelScope.launch {
            val firebaseUser = Firebase.auth.currentUser
            val storedUid = prefs.firebaseUid
                .stateIn(viewModelScope, SharingStarted.Eagerly, null).value
            when {
                firebaseUser != null && storedUid == null ->
                    prefs.setFirebaseUid(firebaseUser.uid)
                firebaseUser == null && storedUid != null ->
                    prefs.setFirebaseUid(null)
            }
        }
    }

    fun setUserName(name: String?) {
        viewModelScope.launch { prefs.setUserName(name) }
    }

    fun setProfilePhoto(uri: String?) {
        viewModelScope.launch { prefs.setProfilePhotoUri(uri) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }

    fun setGpsAccuracy(accuracy: Float) {
        viewModelScope.launch { prefs.setGpsAccuracy(accuracy) }
    }

    fun setUserProfile(name: String?, email: String?, photoUri: String?) {
        viewModelScope.launch { prefs.setUserProfile(name, email, photoUri) }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _isSyncing.value = true
            _signInError.value = null
            try {
                val profile = AuthHelper.signInWithGoogle(context)
                if (profile == null) {
                    _signInError.value = "Sign-in cancelled"
                    return@launch
                }

                val credential = GoogleAuthProvider.getCredential(profile.idToken, null)
                val authResult = Firebase.auth.signInWithCredential(credential).await()
                val uid = authResult.user?.uid ?: run {
                    _signInError.value = "Authentication failed"
                    return@launch
                }

                prefs.setFirebaseUid(uid)
                prefs.setUserProfile(profile.name, profile.email, profile.profilePictureUri)

                // Upload local data then pull anything new from cloud
                repo.uploadAllToCloud(uid)
                repo.mergeFromCloud(uid)

            } catch (e: Exception) {
                Log.e(TAG, "Sign-in failed", e)
                _signInError.value = "Sign-in failed: ${e.localizedMessage}"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            Firebase.auth.signOut()
            prefs.setFirebaseUid(null)
        }
    }

    fun clearSignInError() {
        _signInError.value = null
    }

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                return ProfileViewModel(app) as T
            }
        }
    }
}

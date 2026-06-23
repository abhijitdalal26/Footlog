package com.abhijit.footlog.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "footlog_prefs")

class AppPreferences(private val context: Context) {

    companion object {
        private val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val PROFILE_PHOTO_URI = stringPreferencesKey("profile_photo_uri")
        private val FIREBASE_UID = stringPreferencesKey("firebase_uid")
    }

    val isOnboardingComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETE] ?: false
    }

    val userName: Flow<String?> = context.dataStore.data.map { prefs -> prefs[USER_NAME] }
    val userEmail: Flow<String?> = context.dataStore.data.map { prefs -> prefs[USER_EMAIL] }
    val profilePhotoUri: Flow<String?> = context.dataStore.data.map { prefs -> prefs[PROFILE_PHOTO_URI] }
    val firebaseUid: Flow<String?> = context.dataStore.data.map { prefs -> prefs[FIREBASE_UID] }

    suspend fun setOnboardingComplete() {
        context.dataStore.edit { prefs -> prefs[ONBOARDING_COMPLETE] = true }
    }

    suspend fun setUserProfile(name: String?, email: String?, photoUri: String?) {
        context.dataStore.edit { prefs ->
            if (name != null) prefs[USER_NAME] = name else prefs.remove(USER_NAME)
            if (email != null) prefs[USER_EMAIL] = email else prefs.remove(USER_EMAIL)
            if (photoUri != null) prefs[PROFILE_PHOTO_URI] = photoUri else prefs.remove(PROFILE_PHOTO_URI)
        }
    }

    suspend fun setUserName(name: String?) {
        context.dataStore.edit { prefs ->
            if (name != null) prefs[USER_NAME] = name else prefs.remove(USER_NAME)
        }
    }

    suspend fun setProfilePhotoUri(photoUri: String?) {
        context.dataStore.edit { prefs ->
            if (photoUri != null) prefs[PROFILE_PHOTO_URI] = photoUri else prefs.remove(PROFILE_PHOTO_URI)
        }
    }

    suspend fun setFirebaseUid(uid: String?) {
        context.dataStore.edit { prefs ->
            if (uid != null) prefs[FIREBASE_UID] = uid else prefs.remove(FIREBASE_UID)
        }
    }
}

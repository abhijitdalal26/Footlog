package com.abhijit.footlog.util

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.abhijit.footlog.BuildConfig

object AuthHelper {
    private const val TAG = "AuthHelper"

    suspend fun signInWithGoogle(context: Context): GoogleUserProfile? {
        val credentialManager = CredentialManager.create(context)

        val webClientId = BuildConfig.WEB_CLIENT_ID
        if (webClientId.isBlank()) {
            Log.e(TAG, "Web Client ID is empty.")
            return null
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(request = request, context = context)
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                try {
                    val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    GoogleUserProfile(
                        name = tokenCredential.displayName,
                        email = tokenCredential.id,
                        profilePictureUri = tokenCredential.profilePictureUri?.toString(),
                        idToken = tokenCredential.idToken
                    )
                } catch (e: GoogleIdTokenParsingException) {
                    Log.e(TAG, "Invalid google id token response", e)
                    null
                }
            } else {
                Log.e(TAG, "Unexpected credential type")
                null
            }
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Sign in failed", e)
            null
        }
    }
}

data class GoogleUserProfile(
    val name: String?,
    val email: String,
    val profilePictureUri: String?,
    val idToken: String
)

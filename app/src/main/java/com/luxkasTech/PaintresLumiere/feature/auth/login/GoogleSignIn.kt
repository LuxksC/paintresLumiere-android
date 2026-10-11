package com.luxkasTech.PaintresLumiere.feature.auth.login

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.luxkasTech.PaintresLumiere.BuildConfig

/**
 * "Sign in with Google" through Credential Manager. Needs an Activity [Context] to show the
 * account picker, so it runs in the screen, and only the resulting ID token reaches the ViewModel.
 */
object GoogleSignIn {

    /** False until the OAuth web client ID is configured — the button must not be shown then. */
    val isConfigured: Boolean get() = BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()

    sealed interface Result {
        data class Token(val idToken: String) : Result
        /** The user closed the picker: not an error, nothing to show. */
        data object Cancelled : Result
        data object Failed : Result
    }

    suspend fun requestIdToken(activityContext: Context): Result {
        if (!isConfigured) return Result.Failed
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID).build())
            .build()
        return try {
            val credential = CredentialManager.create(activityContext).getCredential(activityContext, request).credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                Result.Token(GoogleIdTokenCredential.createFrom(credential.data).idToken)
            } else {
                Result.Failed
            }
        } catch (e: GetCredentialCancellationException) {
            Result.Cancelled
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Google sign-in failed", e)
            Result.Failed
        } catch (e: GoogleIdTokenParsingException) {
            Log.w(TAG, "Invalid Google ID token", e)
            Result.Failed
        }
    }

    private const val TAG = "GoogleSignIn"
}

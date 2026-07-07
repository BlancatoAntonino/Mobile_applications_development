package it.polito.mad.cookbookcommunity.ui.authentication

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import it.polito.mad.cookbookcommunity.R

suspend fun requestGoogleIdToken(context: Context): Result<String> = runCatching {
    val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
        serverClientId = context.getString(R.string.default_web_client_id)
    ).build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInWithGoogleOption)
        .build()

    val credentialManager = CredentialManager.create(context)

    val result = credentialManager.getCredential(
        context = context,
        request = request
    )

    val credential = result.credential

    if (
        credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        googleIdTokenCredential.idToken
    } else {
        error("Unsupported credential type.")
    }
}.recoverCatching { throwable ->
    if (throwable is NoCredentialException) {
        error(
            "No Google credentials available. Check that a Google account is added to the device," +
                    "that Google Play Services is updated, and that the account is enabled as OAuth test user."
        )
    } else {
        throw throwable
    }
}

fun Throwable.isGoogleSignInUserCancellation(): Boolean {
    return this is GetCredentialCancellationException ||
            message?.contains("Cancelled by user", ignoreCase = true) == true ||
            message?.contains("Canceled by user", ignoreCase = true) == true
}
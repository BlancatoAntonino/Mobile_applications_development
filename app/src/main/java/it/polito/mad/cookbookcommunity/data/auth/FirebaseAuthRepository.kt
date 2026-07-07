package it.polito.mad.cookbookcommunity.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    private val _currentUser = MutableStateFlow(firebaseAuth.currentUser?.toAuthUser())
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        _currentUser.value = auth.currentUser?.toAuthUser()
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        val user = result.user ?: error("Firebase authentication completed without a user.")

        user.toAuthUser().also { authenticatedUser ->
            _currentUser.value = authenticatedUser
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        _currentUser.value = null
    }

    override fun isUserLoggedIn(): Boolean {
        return _currentUser.value != null
    }

    override fun currentUserId(): String? {
        return _currentUser.value?.uid
    }

    fun release() {
        firebaseAuth.removeAuthStateListener(authStateListener)
    }
}

private fun FirebaseUser.toAuthUser(): AuthUser {
    return AuthUser(
        uid = uid,
        email = email.orEmpty(),
        displayName = displayName.orEmpty(),
        photoUrl = photoUrl?.toString().orEmpty()
    )
}
package it.polito.mad.cookbookcommunity.data.auth

import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<AuthUser?>

    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>

    suspend fun signOut()

    fun isUserLoggedIn(): Boolean

    fun currentUserId(): String?
}
package it.polito.mad.cookbookcommunity.session

import it.polito.mad.cookbookcommunity.data.auth.AuthRepository
import it.polito.mad.cookbookcommunity.data.auth.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {
    const val LEGACY_LOGGED_IN_USER_ID = "1"

    private val fallbackCurrentUser = MutableStateFlow<AuthUser?>(null)

    private var authRepository: AuthRepository? = null

    private var legacyLoggedIn: Boolean = false

    val currentUser: StateFlow<AuthUser?>
        get() = authRepository?.currentUser ?: fallbackCurrentUser.asStateFlow()

    val CURRENT_LOGGED_IN_USER_ID: String
        get() = currentUserId() ?: LEGACY_LOGGED_IN_USER_ID

    var loggedIn: Boolean
        get() = isUserLoggedIn()
        set(value) {
            legacyLoggedIn = value

            fallbackCurrentUser.value = if (value) {
                AuthUser(
                    uid = LEGACY_LOGGED_IN_USER_ID,
                    email = "",
                    displayName = "Demo user",
                    photoUrl = ""
                )
            } else {
                null
            }
        }

    fun init(authRepository: AuthRepository) {
        this.authRepository = authRepository
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository?.isUserLoggedIn() == true || legacyLoggedIn
    }

    fun currentUserId(): String? {
        return authRepository?.currentUserId()
            ?: if (legacyLoggedIn) fallbackCurrentUser.value?.uid else null
    }

    fun authenticatedUserIdOrNull(): String? {
        return authRepository?.currentUserId()
    }

    fun requireAuthenticatedUserId(): String {
        return authenticatedUserIdOrNull()
            ?: error("Authentication required.")
    }

    suspend fun signOut() {
        authRepository?.signOut()
        legacyLoggedIn = false
        fallbackCurrentUser.value = null
    }
}
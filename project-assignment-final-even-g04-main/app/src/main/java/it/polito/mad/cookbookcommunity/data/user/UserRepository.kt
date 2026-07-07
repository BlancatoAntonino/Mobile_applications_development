package it.polito.mad.cookbookcommunity.data.user

import it.polito.mad.cookbookcommunity.data.auth.AuthUser
import it.polito.mad.cookbookcommunity.model.profile.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUserById(userId: String): Flow<UserProfile?>
    suspend fun getUserById(userId: String): UserProfile?
    suspend fun createUser(userId: String, profile: UserProfile)
    suspend fun updateUser(profile: UserProfile)

    suspend fun ensureUserProfile(authUser: AuthUser): UserProfile
}
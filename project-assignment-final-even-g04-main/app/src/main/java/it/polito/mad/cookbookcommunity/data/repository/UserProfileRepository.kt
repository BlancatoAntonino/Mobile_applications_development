package it.polito.mad.cookbookcommunity.data.repository

import it.polito.mad.cookbookcommunity.model.profile.UserProfile
import kotlinx.coroutines.flow.Flow

const val USER_PROFILES_BOOK_NAME = "user_profiles"

interface UserProfileRepository {
    fun getAllUserProfiles(): Flow<List<UserProfile>>

    fun getUserProfileById(id: String): Flow<UserProfile?>

    fun getCachedUserProfileById(id: String): UserProfile?

    suspend fun addUserProfile(profile: UserProfile)

    suspend fun updateUserProfile(profile: UserProfile)

    suspend fun deleteUserProfile(id: String)
}
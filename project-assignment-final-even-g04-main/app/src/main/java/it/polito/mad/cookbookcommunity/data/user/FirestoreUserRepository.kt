package it.polito.mad.cookbookcommunity.data.user

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import it.polito.mad.cookbookcommunity.data.auth.AuthUser
import it.polito.mad.cookbookcommunity.data.firestore.FirestoreCollections
import it.polito.mad.cookbookcommunity.model.profile.CookingRole
import it.polito.mad.cookbookcommunity.model.profile.ProfileImage
import it.polito.mad.cookbookcommunity.model.profile.ProfilePreferences
import it.polito.mad.cookbookcommunity.model.profile.UserData
import it.polito.mad.cookbookcommunity.model.profile.UserProfile
import it.polito.mad.cookbookcommunity.model.profile.toUserData
import it.polito.mad.cookbookcommunity.model.profile.toUserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreUserRepository : UserRepository {

    private val db = Firebase.firestore

    private val collection
        get() = db.collection(FirestoreCollections.USERS)

    override fun observeUserById(userId: String): Flow<UserProfile?> =
        collection
            .document(userId)
            .snapshots()
            .map { snapshot -> snapshot.toObject<UserData>()?.toUserProfile() }

    override suspend fun getUserById(userId: String): UserProfile? {
        return try {
            collection
                .document(userId)
                .get()
                .await()
                .toObject<UserData>()
                ?.toUserProfile()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createUser(userId: String, profile: UserProfile) {
        val now = System.currentTimeMillis()
        val normalizedProfile = profile.copy(internalId = userId)

        collection
            .document(userId)
            .set(normalizedProfile.toUserData(existingCreatedAt = now))
            .await()
    }

    override suspend fun updateUser(profile: UserProfile) {
        val document = collection.document(profile.internalId)
        val snapshot = document.get().await()

        val existingCreatedAt =
            snapshot.getLong(FirestoreCollections.User.CREATED_AT)
                ?: System.currentTimeMillis()

        val userData = profile.toUserData(existingCreatedAt = existingCreatedAt)

        document
            .set(userData)
            .await()
    }

    override suspend fun ensureUserProfile(authUser: AuthUser): UserProfile {
        val existingProfile = getUserById(authUser.uid)

        if (existingProfile != null) {
            val shouldBackfillGooglePhoto =
                existingProfile.profileImage.isMonogram &&
                        authUser.photoUrl.isNotBlank()

            if (shouldBackfillGooglePhoto) {
                val updatedProfile = existingProfile.copy(
                    profileImage = ProfileImage.localUri(authUser.photoUrl)
                )

                updateUser(updatedProfile)

                return updatedProfile
            }

            return existingProfile
        }

        val displayName = authUser.displayName.trim()
        val emailPrefix = authUser.email.substringBefore("@").trim()
        val fallbackName = emailPrefix.ifBlank { "User" }

        val nameSource = displayName.ifBlank { fallbackName }
        val nameParts = nameSource.split(Regex("\\s+"), limit = 2)

        val firstName = nameParts.getOrNull(0).orEmpty().ifBlank { "User" }
        val lastName = nameParts.getOrNull(1).orEmpty()

        val nickname = emailPrefix
            .ifBlank { firstName.lowercase() }
            .filter { it.isLetterOrDigit() || it == '.' || it == '_' || it == '-' }
            .take(30)
            .ifBlank { "user" }

        val profile = UserProfile(
            internalId = authUser.uid,
            firstName = firstName,
            lastName = lastName,
            nickname = nickname,
            email = authUser.email,
            phone = null,
            bio = null,
            cookingRole = CookingRole.FOOD_LOVER,
            preferences = ProfilePreferences(),
            socialLinks = emptyList(),
            profileImage = if (authUser.photoUrl.isNotBlank()) {
                ProfileImage.localUri(authUser.photoUrl)
            } else {
                ProfileImage.Monogram
            }
        )

        createUser(authUser.uid, profile)

        return profile
    }
}
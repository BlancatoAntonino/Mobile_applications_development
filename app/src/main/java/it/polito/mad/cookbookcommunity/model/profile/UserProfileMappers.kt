package it.polito.mad.cookbookcommunity.model.profile

import it.polito.mad.cookbookcommunity.model.recipe.CuisineType
import it.polito.mad.cookbookcommunity.model.recipe.DietaryRestriction

private val presetAvatarKeys = setOf(
    "avatar_1",
    "avatar_2",
    "avatar_3"
)

private fun String.toProfileImage(): ProfileImage {
    val normalizedValue = trim()

    return when {
        normalizedValue.isBlank() -> ProfileImage.Monogram
        normalizedValue in presetAvatarKeys -> ProfileImage.presetAvatar(normalizedValue)
        else -> ProfileImage.localUri(normalizedValue)
    }
}
fun UserData.toUserProfile(): UserProfile {
    return UserProfile(
        internalId = id,
        firstName = firstName,
        lastName = lastName,
        nickname = nickname,
        email = email,
        bio = bio.ifBlank { null },
        cookingRole = runCatching { CookingRole.valueOf(cookingRole) }
            .getOrDefault(CookingRole.FOOD_LOVER),
        preferences = ProfilePreferences(
            favoriteCuisines = favoriteCuisines
                .mapNotNull { runCatching { CuisineType.valueOf(it) }.getOrNull() }
                .toSet(),
            dietaryRestrictions = dietaryRestrictions
                .mapNotNull { runCatching { DietaryRestriction.valueOf(it) }.getOrNull() }
                .toSet(),
            favoriteIngredients = favoriteIngredients,
            allergies = allergies
        ),
        profileImage = photoUrl.toProfileImage()
    )
}
fun UserProfile.toUserData(
    existingCreatedAt: Long = 0L
): UserData {
    return UserData(
        id = internalId,
        email = email,
        displayName = fullname.ifBlank { nickname },
        firstName = firstName,
        lastName = lastName,
        nickname = nickname,
        photoUrl = when {
            profileImage.isLocalUri -> profileImage.value
            profileImage.isPresetAvatar -> profileImage.value
            else -> ""
        },
        bio = bio ?: "",
        cookingRole = cookingRole.name,
        favoriteCuisines = preferences.favoriteCuisines.map { it.name },
        dietaryRestrictions = preferences.dietaryRestrictions.map { it.name },
        favoriteIngredients = preferences.favoriteIngredients,
        allergies = preferences.allergies,
        createdAt = existingCreatedAt,
        updatedAt = System.currentTimeMillis()
    )
}
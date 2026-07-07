package it.polito.mad.cookbookcommunity.model.profile

data class UserProfile(
    val internalId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val nickname: String = "",
    val email: String = "",
    val phone: String? = null,
    val bio: String? = null,
    val cookingRole: CookingRole = CookingRole.FOOD_LOVER,
    val preferences: ProfilePreferences = ProfilePreferences(),
    val socialLinks: List<SocialLink> = emptyList(),
    val profileImage: ProfileImage = ProfileImage.Monogram
) {
    val fullname: String
        get() = "$firstName $lastName".trim()

    val initials: String
        get() {
            val firstInitial = firstName.trim().firstOrNull()?.uppercaseChar()
            val lastInitial = lastName.trim().firstOrNull()?.uppercaseChar()

            return listOfNotNull(firstInitial, lastInitial)
                .joinToString("")
                .ifBlank { nickname.trim().take(2).uppercase() }
                .ifBlank { "?" }
        }

    fun isOwnedBy(userId: String): Boolean {
        return internalId == userId
    }

    companion object {
        fun empty(internalId: String = ""): UserProfile {
            return UserProfile(internalId = internalId)
        }
    }
}
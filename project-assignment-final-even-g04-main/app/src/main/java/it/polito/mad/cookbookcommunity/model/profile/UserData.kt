package it.polito.mad.cookbookcommunity.model.profile

data class UserData(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val nickname: String = "",
    val photoUrl: String = "",
    val bio: String = "",
    val cookingRole: String = "",
    val favoriteCuisines: List<String> = emptyList(),
    val dietaryRestrictions: List<String> = emptyList(),
    val favoriteIngredients: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

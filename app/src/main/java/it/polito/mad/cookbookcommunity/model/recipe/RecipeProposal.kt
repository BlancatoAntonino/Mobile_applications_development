package it.polito.mad.cookbookcommunity.model.recipe

import java.util.UUID

data class RecipeProposal(
    var id: String = UUID.randomUUID().toString(),
    var ownerId: String = "",
    var ownerDisplayName: String = "",
    var ownerPhotoUrl: String = "",
    var title: String = "",
    var description: String = "",
    var imageUri: String = "",
    var servings: Int = 1,
    var priceRange: String = "MEDIUM",
    var difficulty: String = "MEDIUM",
    var cookTimeMinutes: Int = 0,
    var calories: Int? = null,
    var recipeType: String = "MAIN_COURSE",
    var cuisineType: String? = null,
    var dietaryRestrictions: List<String> = emptyList(),
    var ingredients: List<IngredientItem> = emptyList(),
    var steps: List<InstructionStep> = emptyList(),


    var originalRecipeId: String? = null,
    var originalRecipeTitle: String = "",
    var originalRecipeImageUri: String = "",
    var originalAuthorId: String = "",
    var originalAuthorDisplayName: String = "",
    var originalAuthorPhotoUrl: String = "",
    var adaptationNote: String = "",

    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    var averageRating: Double = 0.0,
    var reviewCount: Int = 0
) {
    fun isOwnedBy(userId: String): Boolean = ownerId == userId

    companion object {
        fun empty(ownerId: String): RecipeProposal {
            return RecipeProposal(ownerId = ownerId)
        }
    }
}
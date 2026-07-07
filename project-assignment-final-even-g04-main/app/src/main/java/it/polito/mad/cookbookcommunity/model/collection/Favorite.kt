package it.polito.mad.cookbookcommunity.model.collection

import java.util.UUID

data class Favorite(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val recipeId: String = "",
    val recipeTitle: String = "",
    val recipeImageUri: String = "",
    val recipeOwnerId: String = "",
    val collectionId: String? = null,
    val collectionName: String = FavoriteCollection.SAVED_RECIPES_NAME,
    val savedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun idFor(userId: String, recipeId: String, collectionName: String): String {
            val safeCollection = collectionName.trim().replace(" ", "_").lowercase()
            return "${userId}_${recipeId}_$safeCollection"
        }
    }
}

package it.polito.mad.cookbookcommunity.model.review

import java.util.UUID

data class TriedRecipeLog(
    val id: String = UUID.randomUUID().toString(),
    val recipeId: String = "",
    val userId: String = "",
    val reviewId: String? = null,

    val cookedAt: Long = System.currentTimeMillis(),

    val result: CookingResult = CookingResult.GOOD,
    val modifications: String = "",
    val personalNote: String = "",
    val finalPhotoUri: String = "",
    val wouldCookAgain: Boolean = true,

    val recipeTitle: String = "",
    val recipeImageUri: String = "",

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    val notes: String? = null
) {
    fun belongsTo(userId: String): Boolean = this.userId == userId

    fun hasLinkedReview(): Boolean = !reviewId.isNullOrBlank()

    fun hasFinalPhoto(): Boolean = finalPhotoUri.isNotBlank()

    fun hasRecipeSnapshot(): Boolean =
        recipeTitle.isNotBlank() || recipeImageUri.isNotBlank()

    fun effectivePersonalNote(): String =
        personalNote.ifBlank { notes.orEmpty() }
}
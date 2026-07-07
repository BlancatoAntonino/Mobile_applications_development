package it.polito.mad.cookbookcommunity.model.review

import java.util.UUID

data class Review(
    val id: String = UUID.randomUUID().toString(),
    val recipeId: String = "",
    val recipeTitle: String = "",
    val recipeOwnerId: String = "",
    val authorId: String = "",
    val authorDisplayName: String = "",
    val authorPhotoUrl: String = "",
    val rating: Int = 0,
    val title: String = "",
    val text: String = "",
    val photoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val hasPhoto: Boolean
        get() = !photoUri.isNullOrBlank()

    fun isWrittenBy(userId: String): Boolean = authorId == userId
}
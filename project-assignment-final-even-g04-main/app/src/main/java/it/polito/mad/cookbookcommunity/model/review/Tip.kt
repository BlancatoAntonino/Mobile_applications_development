package it.polito.mad.cookbookcommunity.model.review

import java.util.UUID

data class Tip(
    val id: String = UUID.randomUUID().toString(),
    val recipeId: String = "",
    val authorId: String = "",
    val text: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun isWrittenBy(userId: String): Boolean = authorId == userId
}
package it.polito.mad.cookbookcommunity.model.notification

import java.util.UUID

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val recipientUserId: String = "",
    val actorUserId: String = "",
    val actorDisplayName: String = "",
    val type: NotificationType = NotificationType.RECOMMENDED_RECIPE,
    val title: String = "",
    val message: String = "",
    val recipeId: String? = null,
    val reviewId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val read: Boolean = false
)

enum class NotificationType {
    RECIPE_DUPLICATED,
    RECOMMENDED_RECIPE,
    REVIEW_RECEIVED
}

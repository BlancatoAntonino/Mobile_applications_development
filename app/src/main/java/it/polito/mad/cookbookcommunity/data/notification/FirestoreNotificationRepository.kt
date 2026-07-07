package it.polito.mad.cookbookcommunity.data.notification

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import it.polito.mad.cookbookcommunity.data.firestore.FirestoreCollections
import it.polito.mad.cookbookcommunity.data.firestore.FirestoreCollections.Notification as NotificationFirestore
import it.polito.mad.cookbookcommunity.model.notification.AppNotification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreNotificationRepository : NotificationRepository {
    private val db = Firebase.firestore

    private val collection
        get() = db.collection(FirestoreCollections.NOTIFICATIONS)

    override fun observeNotificationsForUser(userId: String): Flow<List<AppNotification>> =
        collection
            .whereEqualTo(NotificationFirestore.RECIPIENT_USER_ID, userId)
            .snapshots()
            .map { snapshot -> snapshot.toObjects<AppNotification>() }

    override suspend fun createNotification(notification: AppNotification) {
        val safeId = notification.id.ifBlank { collection.document().id }
        collection
            .document(safeId)
            .set(notification.copy(id = safeId).toFirestoreMap())
            .await()
    }

    override suspend fun markAsRead(notificationId: String) {
        collection
            .document(notificationId)
            .update(NotificationFirestore.READ, true)
            .await()
    }

    override suspend fun deleteNotification(notificationId: String) {
        collection
            .document(notificationId)
            .delete()
            .await()
    }

    private fun AppNotification.toFirestoreMap(): Map<String, Any?> = mapOf(
        NotificationFirestore.ID to id,
        NotificationFirestore.RECIPIENT_USER_ID to recipientUserId,
        NotificationFirestore.ACTOR_USER_ID to actorUserId,
        NotificationFirestore.ACTOR_DISPLAY_NAME to actorDisplayName,
        NotificationFirestore.TYPE to type.name,
        NotificationFirestore.TITLE to title,
        NotificationFirestore.MESSAGE to message,
        NotificationFirestore.RECIPE_ID to recipeId,
        NotificationFirestore.REVIEW_ID to reviewId,
        NotificationFirestore.CREATED_AT to createdAt,
        NotificationFirestore.READ to read
    )
}

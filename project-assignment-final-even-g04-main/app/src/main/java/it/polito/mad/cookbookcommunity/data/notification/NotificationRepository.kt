package it.polito.mad.cookbookcommunity.data.notification

import it.polito.mad.cookbookcommunity.model.notification.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeNotificationsForUser(userId: String): Flow<List<AppNotification>>
    suspend fun createNotification(notification: AppNotification)
    suspend fun markAsRead(notificationId: String)
    suspend fun deleteNotification(notificationId: String)
}

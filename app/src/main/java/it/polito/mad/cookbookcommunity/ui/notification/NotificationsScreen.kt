package it.polito.mad.cookbookcommunity.ui.notification

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.polito.mad.cookbookcommunity.model.notification.AppNotification
import it.polito.mad.cookbookcommunity.model.notification.NotificationType
import it.polito.mad.cookbookcommunity.viewmodel.NotificationsUiState

@Composable
fun NotificationsScreen(
    uiState: NotificationsUiState,
    onBackClick: () -> Unit,
    onNotificationClick: (AppNotification) -> Unit,
    onMarkAllAsReadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                NotificationsContent(
                    notifications = uiState.notifications,
                    unreadCount = uiState.unreadCount,
                    onBackClick = onBackClick,
                    onNotificationClick = onNotificationClick,
                    onMarkAllAsReadClick = onMarkAllAsReadClick
                )
            }
        }

    }
}

@Composable
private fun NotificationsContent(
    notifications: List<AppNotification>,
    unreadCount: Int,
    onBackClick: () -> Unit,
    onNotificationClick: (AppNotification) -> Unit,
    onMarkAllAsReadClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            NotificationsHeader(
                unreadCount = unreadCount,
                onBackClick = onBackClick,
                onMarkAllAsReadClick = onMarkAllAsReadClick
            )
        }

        if (notifications.isEmpty()) {
            item {
                EmptyNotificationsCard()
            }
        } else {
            val todayNotifications = notifications.filter { it.isToday() }
            val earlierNotifications = notifications.filterNot { it.isToday() }

            if (todayNotifications.isNotEmpty()) {
                item { NotificationsSectionTitle("Today") }
                items(
                    items = todayNotifications,
                    key = { notification -> notification.id }
                ) { notification ->
                    NotificationRow(
                        notification = notification,
                        onClick = { onNotificationClick(notification) }
                    )
                }
            }

            if (earlierNotifications.isNotEmpty()) {
                item { NotificationsSectionTitle("Earlier") }
                items(
                    items = earlierNotifications,
                    key = { notification -> notification.id }
                ) { notification ->
                    NotificationRow(
                        notification = notification,
                        onClick = { onNotificationClick(notification) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationsHeader(
    unreadCount: Int,
    onBackClick: () -> Unit,
    onMarkAllAsReadClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (unreadCount == 1) "1 unread update" else "$unreadCount unread updates",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onMarkAllAsReadClick,
                enabled = unreadCount > 0,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("Mark all as read")
            }
        }
    }
}

@Composable
private fun NotificationsSectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(top = 14.dp, bottom = 2.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun EmptyNotificationsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Column {
                Text(
                    text = "No notifications",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Recipe updates and reviews will appear here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NotificationRow(
    notification: AppNotification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (notification.read) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NotificationIcon(type = notification.type, read = notification.read)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.relativeTimeLabel(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!notification.read) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                Text(
                    text = notification.title.ifBlank { notification.type.defaultTitle() },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (notification.read) FontWeight.Medium else FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = notification.message.ifBlank { notification.type.defaultMessage() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NotificationIcon(
    type: NotificationType,
    read: Boolean
) {
    val icon = when (type) {
        NotificationType.RECIPE_DUPLICATED -> Icons.Default.RestaurantMenu
        NotificationType.RECOMMENDED_RECIPE -> Icons.Default.Star
        NotificationType.REVIEW_RECEIVED -> Icons.Default.RateReview
    }

    val background = if (read) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

private fun AppNotification.isToday(): Boolean {
    return DateUtils.isToday(createdAt)
}

private fun AppNotification.relativeTimeLabel(): String {
    return DateUtils.getRelativeTimeSpanString(
        createdAt,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    ).toString()
}

private fun NotificationType.defaultTitle(): String {
    return when (this) {
        NotificationType.RECIPE_DUPLICATED -> "Recipe adapted"
        NotificationType.RECOMMENDED_RECIPE -> "Recommended for you"
        NotificationType.REVIEW_RECEIVED -> "New review"
    }
}

private fun NotificationType.defaultMessage(): String {
    return when (this) {
        NotificationType.RECIPE_DUPLICATED -> "Someone adapted one of your recipes."
        NotificationType.RECOMMENDED_RECIPE -> "A new recipe matches your preferences."
        NotificationType.REVIEW_RECEIVED -> "Someone reviewed one of your recipes."
    }
}

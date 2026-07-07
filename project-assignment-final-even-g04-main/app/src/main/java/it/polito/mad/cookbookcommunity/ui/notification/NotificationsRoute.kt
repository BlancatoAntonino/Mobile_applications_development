package it.polito.mad.cookbookcommunity.ui.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import it.polito.mad.cookbookcommunity.data.notification.NotificationRepository
import it.polito.mad.cookbookcommunity.model.notification.AppNotification
import it.polito.mad.cookbookcommunity.model.notification.NotificationType
import it.polito.mad.cookbookcommunity.viewmodel.NotificationsViewModel

@Composable
fun NotificationsRoute(
    userId: String,
    repository: NotificationRepository,
    onBackClick: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onReviewsClick: (String) -> Unit,
    onFeedback: (String) -> Unit = {}
) {
    val viewModel: NotificationsViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                NotificationsViewModel(
                    userId = userId,
                    repository = repository
                )
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let(onFeedback)
    }

    NotificationsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onMarkAllAsReadClick = viewModel::markAllAsRead,
        onNotificationClick = { notification ->
            viewModel.markAsRead(notification.id)
            notification.navigate(
                onRecipeClick = onRecipeClick,
                onReviewsClick = onReviewsClick
            )
        }
    )
}

private fun AppNotification.navigate(
    onRecipeClick: (String) -> Unit,
    onReviewsClick: (String) -> Unit
) {
    val targetRecipeId = recipeId ?: return

    when (type) {
        NotificationType.RECIPE_DUPLICATED,
        NotificationType.RECOMMENDED_RECIPE -> onRecipeClick(targetRecipeId)

        NotificationType.REVIEW_RECEIVED -> onReviewsClick(targetRecipeId)
    }
}

package it.polito.mad.cookbookcommunity.navigation.graphs

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import it.polito.mad.cookbookcommunity.data.AppContainer
import it.polito.mad.cookbookcommunity.navigation.AppNavigation
import it.polito.mad.cookbookcommunity.navigation.NotificationsRoute
import it.polito.mad.cookbookcommunity.ui.notification.NotificationsRoute as NotificationsScreenRoute

internal fun NavGraphBuilder.notificationsGraph(
    appNavigation: AppNavigation,
    appContainer: AppContainer,
    onFeedback: (String) -> Unit,
) {
    val authRepository = appContainer.authRepository
    val notificationRepository = appContainer.notificationRepository

    composable<NotificationsRoute> {
        val currentAuthUser by authRepository.currentUser.collectAsStateWithLifecycle()
        val userId = currentAuthUser?.uid ?: return@composable

        NotificationsScreenRoute(
            userId = userId,
            repository = notificationRepository,
            onBackClick = {
                appNavigation.navigateBack()
            },
            onRecipeClick = { recipeId ->
                appNavigation.navigateToRecipeDetail(recipeId, false)
            },
            onReviewsClick = { recipeId ->
                appNavigation.navigateToRecipeReviews(recipeId)
            },
            onFeedback = { message ->
                onFeedback(message)
            }
        )
    }
}
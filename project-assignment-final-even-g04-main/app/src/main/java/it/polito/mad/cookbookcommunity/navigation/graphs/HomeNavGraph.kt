package it.polito.mad.cookbookcommunity.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import it.polito.mad.cookbookcommunity.data.AppContainer
import it.polito.mad.cookbookcommunity.navigation.AppNavigation
import it.polito.mad.cookbookcommunity.navigation.HomeRoute
import it.polito.mad.cookbookcommunity.navigation.PostLoginDestination
import it.polito.mad.cookbookcommunity.ui.home.HomeScreenRoute


internal fun NavGraphBuilder.homeGraph(
    appNavigation: AppNavigation,
    appContainer: AppContainer,
    navigateIfLoggedIn: (PostLoginDestination, () -> Unit) -> Unit,
    onFeedback: (String) -> Unit,
) {
    val recipeRepository = appContainer.recipeRepository
    val notificationRepository = appContainer.notificationRepository
    val userProfileRepository = appContainer.userProfileRepository

    composable<HomeRoute> {
        HomeScreenRoute(
            repository = recipeRepository,
            userProfileRepository = userProfileRepository,
            notificationRepository = notificationRepository,
            onRecipeClick = { id ->
                appNavigation.navigateToRecipeDetail(id, true)
            },
            onSeeAllClick = {
                appNavigation.navigateToExplore()
            },
            onNotificationsClick = {
                navigateIfLoggedIn(PostLoginDestination.NOTIFICATIONS) {
                    appNavigation.navigateToNotifications()
                }
            },
            onFeedback = { message ->
                onFeedback(message)
            }
        )
    }
}
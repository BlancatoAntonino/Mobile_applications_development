package it.polito.mad.cookbookcommunity.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import it.polito.mad.cookbookcommunity.data.notification.NotificationRepository
import it.polito.mad.cookbookcommunity.model.notification.AppNotification
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.data.repository.UserProfileRepository
import it.polito.mad.cookbookcommunity.data.user.UserRepository
import it.polito.mad.cookbookcommunity.session.SessionManager
import it.polito.mad.cookbookcommunity.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf

@Composable
fun HomeScreenRoute(
    repository: RecipeRepository,
    userProfileRepository: UserRepository,
    notificationRepository: NotificationRepository,
    onRecipeClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onFeedback: (String) -> Unit = {}
) {
    val viewModel: HomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                HomeViewModel(
                    repository = repository,
                    userProfileRepository = userProfileRepository
                )
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by SessionManager.currentUser.collectAsStateWithLifecycle()
    val currentUserId = currentUser?.uid
    val notifications by if (currentUserId == null) {
        flowOf<List<AppNotification>>(emptyList())
    } else {
        notificationRepository.observeNotificationsForUser(currentUserId)
            .catch { error ->
                onFeedback(error.message ?: "Unable to load notifications.")
                emit(emptyList())
            }
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    HomeScreen(
        uiState = uiState,
        onRecipeClick = onRecipeClick,
        onSeeAllClick = onSeeAllClick,
        onNotificationsClick = onNotificationsClick,
        unreadNotificationsCount = notifications.count { !it.read }
    )
}

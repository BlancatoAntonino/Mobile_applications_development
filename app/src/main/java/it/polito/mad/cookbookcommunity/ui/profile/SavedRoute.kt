package it.polito.mad.cookbookcommunity.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import it.polito.mad.cookbookcommunity.data.favorite.FavoriteCollectionRepository
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import it.polito.mad.cookbookcommunity.viewmodel.SavedUiEvent
import it.polito.mad.cookbookcommunity.viewmodel.SavedViewModel

@Composable
fun SavedRoute(
    ownerId: String,
    repository: FavoriteCollectionRepository,
    onCollectionClick: (id: String, name: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: SavedViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                SavedViewModel(repository,ownerId)
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.navigateToCollectionId) {
        uiState.navigateToCollectionId?.let { id->
            val name = uiState.favoriteCollections.firstOrNull { it.id == id }?.name ?: ""
            onCollectionClick(id, name)
            viewModel.onEvent(SavedUiEvent.NavigationConsumed)
        }
    }

    SavedScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}
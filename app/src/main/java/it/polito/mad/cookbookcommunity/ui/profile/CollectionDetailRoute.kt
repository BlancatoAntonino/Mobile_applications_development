package it.polito.mad.cookbookcommunity.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import it.polito.mad.cookbookcommunity.data.favorite.FavoriteCollectionRepository
import it.polito.mad.cookbookcommunity.data.favorite.FavoriteRepository
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.viewmodel.CollectionDetailUiEvent
import it.polito.mad.cookbookcommunity.viewmodel.CollectionDetailViewModel



@Composable
fun CollectionDetailRoute(
    collectionId: String,
    collectionName: String,
    repository: FavoriteCollectionRepository,
    favoriteRepository: FavoriteRepository,
    recipeProposalRepository: RecipeRepository,
    onBackClick: () -> Unit,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
){
    val viewModel: CollectionDetailViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                CollectionDetailViewModel(collectionRepository = repository, favoriteRepository = favoriteRepository, recipeRepository = recipeProposalRepository, collectionId)
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.navigateToRecipeId) {
        uiState.navigateToRecipeId?.let { id->
            onRecipeClick(id)
            viewModel.onEvent(CollectionDetailUiEvent.NavigationConsumed)
        }
    }

    CollectionDetailScreen(
        collectionName= collectionName,
        uiState= uiState,
        onEvent = viewModel::onEvent,
        onBackClick= onBackClick,
        modifier=modifier
    )
}

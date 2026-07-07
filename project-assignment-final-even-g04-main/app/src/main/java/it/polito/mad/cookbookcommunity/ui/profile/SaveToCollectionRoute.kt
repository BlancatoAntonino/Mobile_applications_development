package it.polito.mad.cookbookcommunity.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.produceState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import it.polito.mad.cookbookcommunity.data.favorite.FavoriteCollectionRepository
import it.polito.mad.cookbookcommunity.data.favorite.FavoriteRepository
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.model.recipe.RecipeProposal
import it.polito.mad.cookbookcommunity.viewmodel.SaveToCollectionViewModel
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun SaveToCollectionRoute(
    ownerId: String,
    recipeId: String,
    repository: FavoriteCollectionRepository,
    favoriteRepository: FavoriteRepository,
    recipeRepository: RecipeRepository,
    onDismiss: () -> Unit,
    onFeedback: (String) -> Unit = {},
    modifier: Modifier = Modifier
){
    val recipeState by produceState<RecipeProposal?>(initialValue = null, recipeId) {
        value = recipeRepository.getRecipeProposalById(recipeId).firstOrNull()
    }

    val recipe = recipeState ?: return

    val viewModel: SaveToCollectionViewModel= viewModel(
        key = "save_to_collection_${ownerId}_${recipeId}",
        factory = viewModelFactory {
            initializer {
                SaveToCollectionViewModel(ownerId = ownerId ,recipe = recipe, collectionRepository = repository, favoriteRepository = favoriteRepository )
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) {
            onFeedback("Recipe saved to favorites.")
            onDismiss()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let(onFeedback)
    }

    SaveToCollectionScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onDismiss = onDismiss,
        modifier = modifier
    )
}

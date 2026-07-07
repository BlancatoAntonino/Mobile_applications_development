package it.polito.mad.cookbookcommunity.ui.recipeProposal


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.domain.usecase.DeleteRecipeProposalUseCase
import it.polito.mad.cookbookcommunity.viewmodel.RecipeListEvent
import it.polito.mad.cookbookcommunity.viewmodel.RecipeListViewModel

@Composable
fun RecipeListRoute(
    ownerId: String,
    isOwnerView: Boolean,
    ownerDisplayName: String,
    repository: RecipeRepository,
    deleteRecipeProposalUseCase: DeleteRecipeProposalUseCase,
    onBackClick: () -> Unit,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: RecipeListViewModel = viewModel(
        key = "recipe_list_$ownerId",
        factory = viewModelFactory {
            initializer {
                RecipeListViewModel(
                    repository = repository,
                    deleteRecipeProposalUseCase = deleteRecipeProposalUseCase
                )
            }
        }
    )

    LaunchedEffect(ownerId) {
        viewModel.onEvent(
            RecipeListEvent.LoadRecipes(
                ownerId = ownerId,
                isOwnerView = isOwnerView,
                ownerDisplayName = ownerDisplayName
            )
        )
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.navigateToRecipeId) {
        uiState.navigateToRecipeId?.let { id ->
            onRecipeClick(id)
            viewModel.onEvent(RecipeListEvent.NavigationConsumed)
        }
    }

    RecipeListScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        modifier = modifier
    )
}
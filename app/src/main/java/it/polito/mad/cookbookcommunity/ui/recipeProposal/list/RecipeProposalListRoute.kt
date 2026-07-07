package it.polito.mad.cookbookcommunity.ui.recipeProposal.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.viewmodel.RecipeProposalListViewModel

@Composable
fun RecipeProposalListRoute(
    repository: RecipeRepository,
    onBackClick: () -> Unit,
    onRecipeClick: (String) -> Unit,
    showBackButton: Boolean,
    modifier: Modifier = Modifier
) {
    val viewModel: RecipeProposalListViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                RecipeProposalListViewModel(repository)
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RecipeProposalListScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRecipeClick = onRecipeClick,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onIngredientFilterToggle = viewModel::toggleIngredientFilter,
        onPriceRangeFilterToggle = viewModel::togglePriceRangeFilter,
        onRecipeTypeFilterToggle = viewModel::toggleRecipeTypeFilter,
        onDietaryRestrictionFilterToggle = viewModel::toggleDietaryRestrictionFilter,
        onClearFilters = viewModel::clearFilters,
        onRetryLoad = viewModel::retryLoad,
        showBackButton = showBackButton,
        modifier = modifier
    )
}

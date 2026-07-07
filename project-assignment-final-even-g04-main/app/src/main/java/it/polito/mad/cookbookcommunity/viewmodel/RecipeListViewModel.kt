package it.polito.mad.cookbookcommunity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.domain.usecase.DeleteRecipeProposalUseCase
import it.polito.mad.cookbookcommunity.model.recipe.RecipeProposal
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeListUiState(
    val recipes: List<RecipeProposal> = emptyList(),
    val isLoading: Boolean = true,
    val deleteConfirmId: String? = null,
    val navigateToRecipeId: String? = null,
    val isOwnerView: Boolean = true,
    val ownerDisplayName: String = "My",
    val errorMessage: String? = null

)

sealed interface RecipeListEvent {
    data class LoadRecipes(val ownerId: String, val isOwnerView: Boolean, val ownerDisplayName: String = "My"): RecipeListEvent
    data class RecipeClicked(val id: String): RecipeListEvent
    data class DeleteRequested(val id: String): RecipeListEvent
    data object DeleteConfirmed: RecipeListEvent
    data object DeleteDismissed: RecipeListEvent
    data object NavigationConsumed: RecipeListEvent
    data object DismissErrorMessage : RecipeListEvent
}

class RecipeListViewModel(
    private val repository: RecipeRepository,
    private val deleteRecipeProposalUseCase: DeleteRecipeProposalUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeListUiState())
    val uiState: StateFlow<RecipeListUiState> = _uiState.asStateFlow()

    private var loadRecipesJob: Job? = null

    fun onEvent(event: RecipeListEvent) {
        when (event) {
            is RecipeListEvent.LoadRecipes       -> loadRecipesInternal(event)
            is RecipeListEvent.RecipeClicked     -> recipeClickedInternal(event.id)
            is RecipeListEvent.DeleteRequested   -> deleteRequestedInternal(event.id)
            is RecipeListEvent.DeleteConfirmed   -> deleteConfirmedInternal()
            is RecipeListEvent.DeleteDismissed   -> deleteDismissedInternal()
            is RecipeListEvent.NavigationConsumed -> navigationConsumedInternal()
            is RecipeListEvent.DismissErrorMessage-> dismissErrorMessageInternal()
        }
    }

    private fun loadRecipesInternal(event: RecipeListEvent.LoadRecipes) {
        loadRecipesJob?.cancel()

        _uiState.update {
            it.copy(
                isLoading = true,
                isOwnerView = event.isOwnerView,
                ownerDisplayName = event.ownerDisplayName,
                deleteConfirmId = null,
                navigateToRecipeId = null
            )
        }

        loadRecipesJob = viewModelScope.launch {
            repository
                .getOwnedRecipeProposals(event.ownerId)
                .catch {
                    _uiState.update { currentState ->
                        currentState.copy(
                            recipes = emptyList(),
                            isLoading = false,
                            errorMessage = "Unable to load recipes. Please try again."
                        )
                    }
                }
                .collect { recipes ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            recipes = recipes,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private fun recipeClickedInternal(id: String) {
        _uiState.update { it.copy(navigateToRecipeId = id) }
    }

    private fun deleteRequestedInternal(id: String) {
        _uiState.update { it.copy(deleteConfirmId = id) }
    }

    private fun deleteConfirmedInternal() {
        val id = _uiState.value.deleteConfirmId ?: return
        if (!_uiState.value.isOwnerView) {
            _uiState.update { it.copy(deleteConfirmId = null) }
            return
        }
        _uiState.update { it.copy(deleteConfirmId = null) }
        viewModelScope.launch {
            try {
                deleteRecipeProposalUseCase(id)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to delete recipe. Please try again.")
                }
            }
        }
    }

    private fun deleteDismissedInternal() {
        _uiState.update { it.copy(deleteConfirmId = null) }
    }

    private fun navigationConsumedInternal() {
        _uiState.update { it.copy(navigateToRecipeId = null) }
    }

    private fun dismissErrorMessageInternal(){
        _uiState.update { it.copy(errorMessage = null) }
    }
}
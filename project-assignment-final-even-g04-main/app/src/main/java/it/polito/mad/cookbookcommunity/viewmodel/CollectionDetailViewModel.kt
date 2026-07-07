package it.polito.mad.cookbookcommunity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.mad.cookbookcommunity.data.favorite.FavoriteCollectionRepository
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.data.favorite.FavoriteRepository
import it.polito.mad.cookbookcommunity.model.recipe.RecipeProposal
import it.polito.mad.cookbookcommunity.session.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.String

data class CollectionDetailUiState(
    val recipes: List<RecipeProposal> = emptyList(),
    val isLoading: Boolean = true,
    val recipeIdToDelete: String? = null,
    val navigateToRecipeId: String? = null
)

sealed interface CollectionDetailUiEvent{
    data class RecipeClicked(val id: String): CollectionDetailUiEvent
    data class DeleteRequested(val id: String): CollectionDetailUiEvent
    data object DeleteConfirmed: CollectionDetailUiEvent
    data object DeleteDismissed: CollectionDetailUiEvent
    data object NavigationConsumed: CollectionDetailUiEvent
}

class CollectionDetailViewModel(
    private val collectionRepository: FavoriteCollectionRepository,
    private val favoriteRepository: FavoriteRepository,
    private val recipeRepository: RecipeRepository,
    private val collectionID: String
): ViewModel() {
    private val _uiState = MutableStateFlow(CollectionDetailUiState())
    val uiState: StateFlow<CollectionDetailUiState> = _uiState.asStateFlow()

    private var loadCollectionDetailsJob: Job? =null

    init{
        loadCollectionDetailsInternal()
    }

    fun onEvent(event: CollectionDetailUiEvent){
        when(event){
            is CollectionDetailUiEvent.RecipeClicked->recipeClickedInternal(event.id)
            is CollectionDetailUiEvent.DeleteRequested->deleteRequestedInternal(event.id)
            is CollectionDetailUiEvent.DeleteConfirmed->deleteConfirmedInternal()
            is CollectionDetailUiEvent.DeleteDismissed->deleteDismissedInternal()
            is CollectionDetailUiEvent.NavigationConsumed->navigationConsumedInternal()
        }
    }

    private fun loadCollectionDetailsInternal(){
        loadCollectionDetailsJob?.cancel()

        _uiState.update {
            it.copy(
                isLoading = true,
                recipeIdToDelete = null,
                navigateToRecipeId = null
            )
        }

        loadCollectionDetailsJob= viewModelScope.launch{
            favoriteRepository.observeFavoritesByUser(userId = SessionManager.currentUserId() ?: return@launch).collect { favorites ->

                val collectionFavorites = favorites.filter { it.collectionId == collectionID }

                val recipes = collectionFavorites.mapNotNull { fav ->
                    recipeRepository.getRecipeProposalById(fav.recipeId).first()
                }

                _uiState.update {
                    it.copy(
                        recipes = recipes,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun recipeClickedInternal(id: String){
        _uiState.update { it.copy(navigateToRecipeId = id) }
    }


    private fun deleteRequestedInternal(id: String){
        _uiState.update { it.copy(recipeIdToDelete = id) }
    }

    private fun deleteConfirmedInternal(){
        val recipeId = _uiState.value.recipeIdToDelete ?: return
        val userId = SessionManager.currentUserId() ?: return
        _uiState.update { it.copy(recipeIdToDelete = null) }

        viewModelScope.launch {
            favoriteRepository.removeRecipeFromFavorites(userId, recipeId)
        }
    }

    private fun deleteDismissedInternal(){
        _uiState.update { it.copy(recipeIdToDelete = null) }
    }

    private fun navigationConsumedInternal(){
        _uiState.update { it.copy(navigateToRecipeId = null) }
    }
}
package it.polito.mad.cookbookcommunity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.mad.cookbookcommunity.data.favorite.FavoriteCollectionRepository
import it.polito.mad.cookbookcommunity.data.favorite.FavoriteRepository
import it.polito.mad.cookbookcommunity.model.collection.Favorite
import it.polito.mad.cookbookcommunity.model.collection.FavoriteCollection
import it.polito.mad.cookbookcommunity.model.recipe.RecipeProposal
import it.polito.mad.cookbookcommunity.session.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CollectionWithCount(
    val collection: FavoriteCollection,
    val recipeCount: Int
)
data class SaveToCollectionUiState(
    val favoriteCollections: List<CollectionWithCount> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val showCreateDialog: Boolean = false,
    val errorMessage: String? = null
)

sealed interface SaveToCollectionUiEvent {
    data class CollectionSelected(val collectionId: String) : SaveToCollectionUiEvent
    data class CreateCollectionConfirmed(val name: String): SaveToCollectionUiEvent
    data object CreateCollectionRequested : SaveToCollectionUiEvent
    data object CreateCollectionDismissed: SaveToCollectionUiEvent
}

class SaveToCollectionViewModel(
    private val ownerId: String,
    private val recipe: RecipeProposal,
    private val collectionRepository: FavoriteCollectionRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SaveToCollectionUiState())
    val uiState: StateFlow<SaveToCollectionUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadCollectionsInternal()
    }

    fun onEvent(event: SaveToCollectionUiEvent) {
        when (event) {
            is SaveToCollectionUiEvent.CollectionSelected -> saveToCollectionInternal(event.collectionId)
            is SaveToCollectionUiEvent.CreateCollectionConfirmed -> createCollectionConfirmedInternal(event.name)
            is SaveToCollectionUiEvent.CreateCollectionDismissed -> createCollectionDismissedInternal()
            is SaveToCollectionUiEvent.CreateCollectionRequested->createCollectionRequestedInternal()
        }
    }

    private fun loadCollectionsInternal() {
        loadJob?.cancel()
        _uiState.update { it.copy(isLoading = true) }

        loadJob = viewModelScope.launch {
            combine(
                collectionRepository.observeCollectionsByUser(ownerId),
                favoriteRepository.observeFavoritesByUser(ownerId)
            ) { collections, favorites ->
                collections
                    .filterNot { it.isSystem }
                    .map { collection ->
                        CollectionWithCount(
                            collection = collection,
                            recipeCount = favorites.count { it.collectionId == collection.id }
                        )
                    }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to load collections."
                    )
                }
            }
            .collect { paired ->
                _uiState.update {
                    it.copy(
                        favoriteCollections = paired,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    private fun saveToCollectionInternal(collectionId: String) {
        val currentUserId = SessionManager.authenticatedUserIdOrNull()
        if (currentUserId == null || currentUserId != ownerId) {
            _uiState.update {
                it.copy(errorMessage = "Sign in to save recipes.")
            }
            return
        }

        val targetCollection = _uiState.value.favoriteCollections
            .firstOrNull { it.collection.id == collectionId }?.collection ?: return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            runCatching {
                val favorite = Favorite(
                    id             = Favorite.idFor(ownerId, recipe.id, targetCollection.name),
                    userId         = ownerId,
                    recipeId       = recipe.id,
                    recipeTitle    = recipe.title,
                    recipeImageUri = recipe.imageUri,
                    recipeOwnerId  = recipe.ownerId,
                    collectionId   = collectionId,
                    collectionName = targetCollection.name
                )
                favoriteRepository.saveFavorite(favorite)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        savedSuccessfully = true,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Unable to save recipe."
                    )
                }
            }
        }
    }

    private fun createCollectionRequestedInternal(){
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    private fun createCollectionConfirmedInternal(name: String) {
        if (name.isBlank()) return

        val currentUserId = SessionManager.authenticatedUserIdOrNull()
        if (currentUserId == null || currentUserId != ownerId) {
            _uiState.update {
                it.copy(
                    showCreateDialog = false,
                    errorMessage = "Sign in to create collections."
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                showCreateDialog = false,
                isSaving = true
            )
        }

        viewModelScope.launch {
            runCatching {
                val newCollectionId = collectionRepository.createCollection(ownerId = ownerId, name = name)

                val favorite = Favorite(
                    id             = Favorite.idFor(ownerId, recipe.id, name),
                    userId         = ownerId,
                    recipeId       = recipe.id,
                    recipeTitle    = recipe.title,
                    recipeImageUri = recipe.imageUri,
                    recipeOwnerId  = recipe.ownerId,
                    collectionId   = newCollectionId,
                    collectionName = name
                )
                favoriteRepository.saveFavorite(favorite)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        savedSuccessfully = true,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Unable to save recipe."
                    )
                }
            }
        }
    }

    private fun createCollectionDismissedInternal(){
        _uiState.update { it.copy(showCreateDialog = false) }
    }

}

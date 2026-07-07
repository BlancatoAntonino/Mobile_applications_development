package it.polito.mad.cookbookcommunity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.mad.cookbookcommunity.data.favorite.FavoriteCollectionRepository
import it.polito.mad.cookbookcommunity.model.collection.FavoriteCollection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SavedUiState(
    val favoriteCollections: List<FavoriteCollection> = emptyList(),
    val isLoading: Boolean = true,
    val collectionIdToDelete: String? = null,
    val navigateToCollectionId: String? = null,
    val showCreateDialog: Boolean = false
)

sealed interface SavedUiEvent{
    data class CollectionClicked(val id: String): SavedUiEvent
    data class DeleteRequested(val id: String): SavedUiEvent
    data class CreateCollectionConfirmed(val name: String): SavedUiEvent
    data object CreateCollectionRequested : SavedUiEvent
    data object CreateCollectionDismissed: SavedUiEvent
    data object DeleteConfirmed: SavedUiEvent
    data object DeleteDismissed: SavedUiEvent
    data object NavigationConsumed: SavedUiEvent
}

class SavedViewModel(
    private  val repository: FavoriteCollectionRepository,
    private val ownerId: String
): ViewModel() {
    private val _uiState = MutableStateFlow(SavedUiState())
    val uiState: StateFlow<SavedUiState> = _uiState.asStateFlow()

    private var loadCollectionsJob: Job? =null

    init{
        loadCollectionsInternal(ownerId)
    }

    fun onEvent(event: SavedUiEvent){
        when(event){
            is SavedUiEvent.CollectionClicked->collectionClickedInternal(event.id)
            is SavedUiEvent.DeleteRequested->deleteRequestedInternal(event.id)
            is SavedUiEvent.DeleteConfirmed->deleteConfirmedInternal()
            is SavedUiEvent.DeleteDismissed->deleteDismissedInternal()
            is SavedUiEvent.CreateCollectionConfirmed -> createCollectionConfirmedInternal(event.name)
            is SavedUiEvent.CreateCollectionDismissed -> createCollectionDismissedInternal()
            is SavedUiEvent.CreateCollectionRequested->createCollectionRequestedInternal()
            is SavedUiEvent.NavigationConsumed->navigationConsumedInternal()
        }
    }

    private fun loadCollectionsInternal(ownerId: String){
        loadCollectionsJob?.cancel()

        _uiState.update {
            it.copy(
                isLoading = true,
                collectionIdToDelete = null,
                navigateToCollectionId = null
            )
        }

        loadCollectionsJob = viewModelScope.launch {
            repository.observeCollectionsByUser(ownerId).collect { collections ->
                val visibleCollections = collections.filterNot { collection ->
                    collection.isSystem
                }

                _uiState.update {
                    it.copy(
                        favoriteCollections = visibleCollections,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun collectionClickedInternal(id: String){
        _uiState.update { it.copy(navigateToCollectionId= id) }
    }

    private fun deleteRequestedInternal(id: String) {
        val collection = _uiState.value.favoriteCollections.firstOrNull { it.id == id } ?: return
        if (collection.isSystem) return
        _uiState.update { it.copy(collectionIdToDelete = id) }
    }

    private fun deleteConfirmedInternal(){
        val id = _uiState.value.collectionIdToDelete ?: return
        _uiState.update { it.copy(collectionIdToDelete = null) }
        viewModelScope.launch {
            repository.deleteCollection(id)
        }
    }

    private fun deleteDismissedInternal(){
        _uiState.update { it.copy(collectionIdToDelete = null) }
    }

    private fun createCollectionRequestedInternal(){
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    private fun createCollectionConfirmedInternal(name: String){
        if (name.isBlank()) return
        _uiState.update { it.copy(showCreateDialog = false) }
        viewModelScope.launch {
            repository.createCollection(ownerId = ownerId, name = name)
        }
    }

    private fun createCollectionDismissedInternal(){
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    private fun navigationConsumedInternal(){
        _uiState.update { it.copy(navigateToCollectionId= null) }
    }
}
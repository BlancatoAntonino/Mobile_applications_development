package it.polito.mad.cookbookcommunity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.data.review.ReviewRepository
import it.polito.mad.cookbookcommunity.data.repository.TriedRecipeRepository
import it.polito.mad.cookbookcommunity.model.recipe.RecipeProposal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TriedRecipeListItem(
    val logId: String,
    val recipe: RecipeProposal,
    val cookedAt: Long,
    val rating: Int?,
    val reviewTitle: String?,
    val reviewText: String?
)

data class TriedRecipesUiState(
    val items: List<TriedRecipeListItem> = emptyList(),
    val isLoading: Boolean = true,
    val recipeIdToOpen: String? = null,
    val logIdToDelete: String? = null
)

sealed interface TriedRecipesUiEvent {
    data class RecipeClicked(val recipeId: String) : TriedRecipesUiEvent
    data class DeleteRequested(val logId: String) : TriedRecipesUiEvent
    data object DeleteConfirmed : TriedRecipesUiEvent
    data object DeleteDismissed : TriedRecipesUiEvent
    data object NavigationConsumed : TriedRecipesUiEvent
}

class TriedRecipesViewModel(
    private val userId: String,
    private val triedRecipeRepository: TriedRecipeRepository,
    private val recipeRepository: RecipeRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TriedRecipesUiState())
    val uiState: StateFlow<TriedRecipesUiState> = _uiState.asStateFlow()

    init {
        observeTriedRecipes()
    }

    fun onEvent(event: TriedRecipesUiEvent) {
        when (event) {
            is TriedRecipesUiEvent.RecipeClicked -> {
                _uiState.update {
                    it.copy(recipeIdToOpen = event.recipeId)
                }
            }

            is TriedRecipesUiEvent.DeleteRequested -> {
                _uiState.update {
                    it.copy(logIdToDelete = event.logId)
                }
            }

            TriedRecipesUiEvent.DeleteConfirmed -> {
                deleteSelectedLog()
            }

            TriedRecipesUiEvent.DeleteDismissed -> {
                _uiState.update {
                    it.copy(logIdToDelete = null)
                }
            }

            TriedRecipesUiEvent.NavigationConsumed -> {
                _uiState.update {
                    it.copy(recipeIdToOpen = null)
                }
            }
        }
    }

    private fun observeTriedRecipes() {
        viewModelScope.launch {
            combine(
                triedRecipeRepository.getTriedRecipesByUser(userId),
                recipeRepository.getAllRecipeProposals(),
                reviewRepository.getReviewsByAuthor(userId)
            ) { logs, recipes, reviews ->

                val recipesById = recipes.associateBy { it.id }
                val reviewsById = reviews.associateBy { it.id }

                logs.mapNotNull { log ->
                    val recipe = recipesById[log.recipeId] ?: return@mapNotNull null
                    val linkedReview = log.reviewId?.let { reviewId ->
                        reviewsById[reviewId]
                    }

                    TriedRecipeListItem(
                        logId = log.id,
                        recipe = recipe,
                        cookedAt = log.cookedAt,
                        rating = linkedReview?.rating,
                        reviewTitle = linkedReview?.title,
                        reviewText = linkedReview?.text ?: log.effectivePersonalNote()
                    )
                }
            }.collect { items ->
                _uiState.update {
                    it.copy(
                        items = items,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun deleteSelectedLog() {
        val logId = _uiState.value.logIdToDelete ?: return

        _uiState.update {
            it.copy(logIdToDelete = null)
        }

        viewModelScope.launch {
            triedRecipeRepository.deleteTriedRecipe(logId)
        }
    }
}
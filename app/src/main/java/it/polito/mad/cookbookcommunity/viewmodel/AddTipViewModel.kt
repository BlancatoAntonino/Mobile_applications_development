package it.polito.mad.cookbookcommunity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.data.repository.TipRepository
import it.polito.mad.cookbookcommunity.model.review.Tip
import it.polito.mad.cookbookcommunity.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddTipUiState(
    val recipeId: String,
    val recipeTitle: String = "Recipe",
    val recipeImageUri: String = "",
    val isLoadingRecipe: Boolean = true,
    val recipeExists: Boolean = false,
    val isOwnRecipe: Boolean = false,
    val tipText: String = "",
    val isPublishing: Boolean = false,
    val errorMessage: String? = null,
    val isPublished: Boolean = false
) {
    val tipBlockedMessage: String?
        get() = when {
            isLoadingRecipe -> null
            !recipeExists -> "Recipe not found. You cannot publish a tip."
            isOwnRecipe -> "You cannot add tips to your own recipe."
            else -> null
        }

    val canAddTip: Boolean
        get() = tipBlockedMessage == null && !isLoadingRecipe

    val canPublish: Boolean
        get() = canAddTip &&
                tipText.isNotBlank() &&
                !isPublishing
}

class AddTipViewModel(
    private val recipeId: String,
    private val recipeRepository: RecipeRepository,
    private val tipRepository: TipRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTipUiState(recipeId = recipeId))
    val uiState: StateFlow<AddTipUiState> = _uiState.asStateFlow()

    init {
        observeRecipeSummary()
    }

    fun updateTipText(text: String) {
        _uiState.update { current ->
            current.copy(
                tipText = text,
                errorMessage = null
            )
        }
    }

    fun publishTip() {
        val current = _uiState.value

        val currentUserId = SessionManager.authenticatedUserIdOrNull()

        if (currentUserId == null) {
            _uiState.update {
                it.copy(errorMessage = "Sign in to publish a tip.")
            }
            return
        }

        current.tipBlockedMessage?.let { message ->
            _uiState.update {
                it.copy(errorMessage = message)
            }
            return
        }

        if (current.tipText.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Write a tip before publishing.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPublishing = true,
                    errorMessage = null
                )
            }

            try {
                val now = System.currentTimeMillis()

                val tip = Tip(
                    recipeId = recipeId,
                    authorId = currentUserId,
                    text = current.tipText.trim(),
                    createdAt = now,
                    updatedAt = now
                )

                tipRepository.addTip(tip)

                _uiState.update {
                    it.copy(
                        isPublishing = false,
                        isPublished = true
                    )
                }
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(
                        isPublishing = false,
                        errorMessage = error.message ?: "Unable to publish the tip."
                    )
                }
            }
        }
    }

    private fun observeRecipeSummary() {
        viewModelScope.launch {
            recipeRepository.getRecipeProposalById(recipeId).collect { recipe ->
                _uiState.update { current ->
                    if (recipe == null) {
                        current.copy(
                            recipeTitle = "Recipe not found",
                            recipeImageUri = "",
                            isLoadingRecipe = false,
                            recipeExists = false,
                            isOwnRecipe = false
                        )
                    } else {
                        current.copy(
                            recipeTitle = recipe.title,
                            recipeImageUri = recipe.imageUri,
                            isLoadingRecipe = false,
                            recipeExists = true,
                            isOwnRecipe = recipe.ownerId == SessionManager.authenticatedUserIdOrNull()
                        )
                    }
                }
            }
        }
    }
}
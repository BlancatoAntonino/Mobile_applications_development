package it.polito.mad.cookbookcommunity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.data.repository.TriedRecipeRepository
import it.polito.mad.cookbookcommunity.model.review.CookingResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiaryListItem(
    val entryId: String,
    val recipeId: String,
    val recipeTitle: String,
    val imageUri: String,
    val cookedAt: Long,
    val result: CookingResult,
    val personalNotePreview: String,
    val modificationsPreview: String,
    val wouldCookAgain: Boolean,
    val hasFinalPhoto: Boolean,
    val reviewId: String?
)

data class DiaryUiState(
    val entries: List<DiaryListItem> = emptyList(),
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val feedbackMessage: String? = null,
    val entryIdToDelete: String? = null,
    val entryIdToOpen: String? = null
)

sealed interface DiaryUiEvent {
    data class DiaryEntryClicked(val entryId: String) : DiaryUiEvent
    data class DeleteDiaryEntryRequested(val entryId: String) : DiaryUiEvent
    data object DeleteDiaryEntryConfirmed : DiaryUiEvent
    data object DeleteDiaryEntryDismissed : DiaryUiEvent
    data object DiaryEntryOpenConsumed : DiaryUiEvent
    data object ErrorShown : DiaryUiEvent
    data object FeedbackConsumed : DiaryUiEvent
}

class DiaryViewModel(
    private val userId: String,
    private val triedRecipeRepository: TriedRecipeRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    init {
        observeDiaryEntries()
    }

    fun onEvent(event: DiaryUiEvent) {
        when (event) {
            is DiaryUiEvent.DiaryEntryClicked -> {
                _uiState.update {
                    it.copy(entryIdToOpen = event.entryId)
                }
            }

            is DiaryUiEvent.DeleteDiaryEntryRequested -> {
                _uiState.update {
                    it.copy(entryIdToDelete = event.entryId)
                }
            }

            DiaryUiEvent.DeleteDiaryEntryConfirmed -> {
                deleteSelectedDiaryEntry()
            }

            DiaryUiEvent.DeleteDiaryEntryDismissed -> {
                _uiState.update {
                    it.copy(entryIdToDelete = null)
                }
            }

            DiaryUiEvent.DiaryEntryOpenConsumed -> {
                _uiState.update {
                    it.copy(entryIdToOpen = null)
                }
            }

            DiaryUiEvent.ErrorShown -> {
                _uiState.update {
                    it.copy(errorMessage = null)
                }
            }

            DiaryUiEvent.FeedbackConsumed -> {
                _uiState.update {
                    it.copy(feedbackMessage = null)
                }
            }
        }
    }

    private fun observeDiaryEntries() {
        viewModelScope.launch {
            combine(
                triedRecipeRepository.getDiaryEntriesByUser(userId),
                recipeRepository.getAllRecipeProposals()
            ) { diaryEntries, recipes ->
                val recipesById = recipes.associateBy { it.id }

                diaryEntries.map { entry ->
                    val linkedRecipe = recipesById[entry.recipeId]

                    val recipeTitle = entry.recipeTitle
                        .ifBlank { linkedRecipe?.title.orEmpty() }
                        .ifBlank { "Deleted recipe" }

                    val imageUri = entry.finalPhotoUri
                        .ifBlank { entry.recipeImageUri }
                        .ifBlank { linkedRecipe?.imageUri.orEmpty() }

                    DiaryListItem(
                        entryId = entry.id,
                        recipeId = entry.recipeId,
                        recipeTitle = recipeTitle,
                        imageUri = imageUri,
                        cookedAt = entry.cookedAt,
                        result = entry.result,
                        personalNotePreview = entry.effectivePersonalNote().toPreview(),
                        modificationsPreview = entry.modifications.toPreview(),
                        wouldCookAgain = entry.wouldCookAgain,
                        hasFinalPhoto = entry.finalPhotoUri.isNotBlank(),
                        reviewId = entry.reviewId
                    )
                }
            }
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load diary entries."
                        )
                    }
                }
                .collect { entries ->
                    _uiState.update {
                        it.copy(
                            entries = entries,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private fun deleteSelectedDiaryEntry() {
        val entryId = _uiState.value.entryIdToDelete ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isDeleting = true)
            }

            runCatching {
                triedRecipeRepository.deleteDiaryEntry(entryId)
            }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            entryIdToDelete = null,
                            errorMessage = null,
                            feedbackMessage = "Diary entry deleted."
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            entryIdToDelete = null,
                            feedbackMessage = throwable.message
                                ?: "Unable to delete diary entry."
                        )
                    }
                }
        }
    }

    private fun String.toPreview(maxLength: Int = 90): String {
        val normalized = trim()

        return when {
            normalized.isBlank() -> ""
            normalized.length <= maxLength -> normalized
            else -> normalized.take(maxLength).trimEnd() + "..."
        }
    }
}

class DiaryViewModelFactory(
    private val userId: String,
    private val triedRecipeRepository: TriedRecipeRepository,
    private val recipeRepository: RecipeRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiaryViewModel::class.java)) {
            return DiaryViewModel(
                userId = userId,
                triedRecipeRepository = triedRecipeRepository,
                recipeRepository = recipeRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
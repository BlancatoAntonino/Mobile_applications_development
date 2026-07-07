package it.polito.mad.cookbookcommunity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.data.repository.TriedRecipeRepository
import it.polito.mad.cookbookcommunity.model.review.CookingResult
import it.polito.mad.cookbookcommunity.model.review.TriedRecipeLog
import it.polito.mad.cookbookcommunity.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

private val DIARY_PHOTO_ASSET_URIS = listOf(
    "file:///android_asset/avocado_toast.jpg",
    "file:///android_asset/brownies.jpg",
    "file:///android_asset/cookies.jpg",
    "file:///android_asset/crostata.jpg",
    "file:///android_asset/curry.jpg",
    "file:///android_asset/fish.jpg",
    "file:///android_asset/frittura.jpg",
    "file:///android_asset/greek_salad.jpg",
    "file:///android_asset/hummus.jpg",
    "file:///android_asset/lasagna.jpg",
    "file:///android_asset/pancakes.jpg",
    "file:///android_asset/pasta.jpg",
    "file:///android_asset/pizza.jpg",
    "file:///android_asset/salame.jpg",
    "file:///android_asset/soup.jpg",
    "file:///android_asset/tacos.jpg",
    "file:///android_asset/tiramisu.jpg"
)

data class DiaryEntryEditorUiState(
    val recipeId: String,
    val entryId: String? = null,
    val recipeTitle: String = "Recipe",
    val recipeImageUri: String = "",
    val isLoading: Boolean = true,
    val recipeExists: Boolean = false,

    val reviewId: String? = null,
    val cookedAt: Long = System.currentTimeMillis(),
    val result: CookingResult = CookingResult.GOOD,
    val modifications: String = "",
    val personalNote: String = "",
    val finalPhotoUri: String = "",
    val wouldCookAgain: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),

    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
) {
    val isEditing: Boolean
        get() = entryId != null

    val screenTitle: String
        get() = if (isEditing) "Edit diary note" else "Add diary note"

    val canSave: Boolean
    get() = !isLoading &&
            !isSaving &&
            recipeId.isNotBlank() &&
            recipeTitle.isNotBlank() &&
            (
                    personalNote.isNotBlank() ||
                    modifications.isNotBlank() ||
                    finalPhotoUri.isNotBlank()
            )
}

class DiaryEntryEditorViewModel(
    private val recipeId: String,
    private val initialEntryId: String?,
    private val recipeRepository: RecipeRepository,
    private val triedRecipeRepository: TriedRecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DiaryEntryEditorUiState(
            recipeId = recipeId,
            entryId = initialEntryId
        )
    )
    val uiState: StateFlow<DiaryEntryEditorUiState> = _uiState.asStateFlow()

    private var hasPrefilledDiaryEntry = false

    init {
        observeRecipe()
        observeDiaryEntry()
    }

    fun updateResult(result: CookingResult) {
        _uiState.update {
            it.copy(result = result, errorMessage = null)
        }
    }

    fun updateModifications(value: String) {
        _uiState.update {
            it.copy(modifications = value, errorMessage = null)
        }
    }

    fun updatePersonalNote(value: String) {
        _uiState.update {
            it.copy(personalNote = value, errorMessage = null)
        }
    }

    fun updateWouldCookAgain(value: Boolean) {
        _uiState.update {
            it.copy(wouldCookAgain = value, errorMessage = null)
        }
    }

    fun selectNextPhotoAsset() {
        _uiState.update { current ->
            val currentIndex = DIARY_PHOTO_ASSET_URIS.indexOf(current.finalPhotoUri)
            val nextIndex =
                if (currentIndex == -1) 0
                else (currentIndex + 1) % DIARY_PHOTO_ASSET_URIS.size

            current.copy(
                finalPhotoUri = DIARY_PHOTO_ASSET_URIS[nextIndex],
                errorMessage = null
            )
        }
    }

    fun clearFinalPhoto() {
        _uiState.update {
            it.copy(finalPhotoUri = "", errorMessage = null)
        }
    }

    fun saveDiaryEntry() {
        val current = _uiState.value
        val currentUserId = SessionManager.authenticatedUserIdOrNull()

        if (currentUserId == null) {
            _uiState.update {
                it.copy(errorMessage = "Sign in to save a diary entry.")
            }
            return
        }

        if (current.recipeId.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Recipe not found.")
            }
            return
        }

        if (
            current.personalNote.isBlank() &&
            current.modifications.isBlank() &&
            current.finalPhotoUri.isBlank()
        ) {
            _uiState.update {
                it.copy(errorMessage = "Write a note, add adaptations or select a final photo before saving.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isSaving = true, errorMessage = null)
            }

            runCatching {
                val now = System.currentTimeMillis()

                val entry = TriedRecipeLog(
                    id = current.entryId ?: UUID.randomUUID().toString(),
                    recipeId = current.recipeId,
                    userId = currentUserId,
                    reviewId = current.reviewId,
                    cookedAt = current.cookedAt,
                    result = current.result,
                    modifications = current.modifications.trim(),
                    personalNote = current.personalNote.trim(),
                    finalPhotoUri = current.finalPhotoUri.trim(),
                    wouldCookAgain = current.wouldCookAgain,
                    recipeTitle = current.recipeTitle.trim(),
                    recipeImageUri = current.recipeImageUri.trim(),
                    createdAt = current.createdAt,
                    updatedAt = now,
                    notes = current.personalNote.trim().takeIf { it.isNotBlank() }
                )

                triedRecipeRepository.upsertDiaryEntry(entry)
            }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSaved = true
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.message ?: "Unable to save diary entry."
                        )
                    }
                }
        }
    }

    fun errorShown() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    private fun observeRecipe() {
        viewModelScope.launch {
            recipeRepository.getRecipeProposalById(recipeId).collect { recipe ->
                _uiState.update { current ->
                    if (recipe == null) {
                        current.copy(
                            isLoading = false,
                            recipeExists = false
                        )
                    } else {
                        current.copy(
                            recipeTitle = current.recipeTitle
                                .takeIf { it != "Recipe" && it.isNotBlank() }
                                ?: recipe.title,
                            recipeImageUri = current.recipeImageUri
                                .takeIf { it.isNotBlank() }
                                ?: recipe.imageUri,
                            isLoading = false,
                            recipeExists = true
                        )
                    }
                }
            }
        }
    }

    private fun observeDiaryEntry() {
        val currentUserId = SessionManager.authenticatedUserIdOrNull() ?: return

        viewModelScope.launch {
            val entryFlow =
                if (initialEntryId != null) {
                    triedRecipeRepository.getDiaryEntryById(initialEntryId)
                } else {
                    triedRecipeRepository.getDiaryEntryForRecipe(
                        userId = currentUserId,
                        recipeId = recipeId
                    )
                }

            entryFlow.collect { entry ->
                if (entry == null || hasPrefilledDiaryEntry) {
                    return@collect
                }

                hasPrefilledDiaryEntry = true

                _uiState.update { current ->
                    current.copy(
                        entryId = entry.id,
                        reviewId = entry.reviewId,
                        cookedAt = entry.cookedAt,
                        result = entry.result,
                        modifications = entry.modifications,
                        personalNote = entry.effectivePersonalNote(),
                        finalPhotoUri = entry.finalPhotoUri,
                        wouldCookAgain = entry.wouldCookAgain,
                        recipeTitle = entry.recipeTitle.ifBlank { current.recipeTitle },
                        recipeImageUri = entry.recipeImageUri.ifBlank { current.recipeImageUri },
                        createdAt = entry.createdAt,
                        isLoading = false
                    )
                }
            }
        }
    }
}

class DiaryEntryEditorViewModelFactory(
    private val recipeId: String,
    private val entryId: String?,
    private val recipeRepository: RecipeRepository,
    private val triedRecipeRepository: TriedRecipeRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiaryEntryEditorViewModel::class.java)) {
            return DiaryEntryEditorViewModel(
                recipeId = recipeId,
                initialEntryId = entryId,
                recipeRepository = recipeRepository,
                triedRecipeRepository = triedRecipeRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
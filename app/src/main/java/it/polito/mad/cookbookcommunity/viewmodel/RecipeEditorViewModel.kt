package it.polito.mad.cookbookcommunity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.data.user.UserRepository
import it.polito.mad.cookbookcommunity.domain.usecase.DeleteRecipeProposalUseCase
import it.polito.mad.cookbookcommunity.model.profile.UserProfile
import it.polito.mad.cookbookcommunity.model.recipe.CuisineType
import it.polito.mad.cookbookcommunity.model.recipe.DietaryRestriction
import it.polito.mad.cookbookcommunity.model.recipe.DifficultyLevel
import it.polito.mad.cookbookcommunity.model.recipe.IngredientItem
import it.polito.mad.cookbookcommunity.model.recipe.InstructionStep
import it.polito.mad.cookbookcommunity.model.recipe.PriceRange
import it.polito.mad.cookbookcommunity.model.recipe.RecipeProposal
import it.polito.mad.cookbookcommunity.model.recipe.RecipeType
import it.polito.mad.cookbookcommunity.model.recipe.normalizedForUnit
import it.polito.mad.cookbookcommunity.model.recipe.requiresQuantity
import it.polito.mad.cookbookcommunity.session.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RecipeProposalScreenMode {
    VIEW,
    EDIT
}

data class RecipeEditorFormData(
    val title: String,
    val description: String,
    val imageUri: String,
    val servings: Int,
    val priceRange: PriceRange,
    val difficulty: DifficultyLevel,
    val cookTimeMinutes: Int,
    val calories: Int?,
    val recipeType: RecipeType,
    val cuisineType: CuisineType?,
    val dietaryRestrictions: List<DietaryRestriction>,
    val ingredients: List<IngredientItem>,
    val steps: List<InstructionStep>
)

fun RecipeProposal.toEditorFormData() = RecipeEditorFormData(
    title = title,
    description = description,
    imageUri = imageUri,
    servings = servings,
    priceRange  = runCatching { PriceRange.valueOf(priceRange) }.getOrDefault(PriceRange.MEDIUM),
    difficulty = runCatching { DifficultyLevel.valueOf(difficulty) }.getOrDefault(DifficultyLevel.MEDIUM),
    cookTimeMinutes = cookTimeMinutes,
    calories = calories,
    recipeType = runCatching { RecipeType.valueOf(recipeType) }.getOrDefault(RecipeType.MAIN_COURSE),
    cuisineType = cuisineType?.let { runCatching { CuisineType.valueOf(it) }.getOrNull() },
    dietaryRestrictions = dietaryRestrictions.mapNotNull {
        runCatching { DietaryRestriction.valueOf(it) }.getOrNull()
    },
    ingredients = ingredients,
    steps = steps
)

data class RecipeEditorErrors(
    val title: String? = null,
    val description: String? = null,
    val servings: String? = null,
    val cookTimeMinutes: String? = null,
    val calories: String? = null,
    val ingredients: String? = null,
    val steps: String? = null
) {
    val hasAnyError: Boolean
        get() = title != null ||
                description != null ||
                servings != null ||
                cookTimeMinutes != null ||
                calories != null ||
                ingredients != null ||
                steps != null
}

data class RecipeEditorUiState(
    val formData: RecipeEditorFormData,
    val errors: RecipeEditorErrors = RecipeEditorErrors()
)

data class RecipeProposalUiState(
    val recipe: RecipeProposal? = null,
    val originalRecipe: RecipeProposal? = null,
    val isOriginalRecipeLoading: Boolean = false,
    val authorDisplayName: String? = null,
    val screenMode: RecipeProposalScreenMode = RecipeProposalScreenMode.VIEW,
    val editState: RecipeEditorUiState? = null,
    val successMessage: String? = null,
    val showDeleteConfirm: Boolean = false,
    val showDiscardEditConfirm: Boolean = false,
    val navigateBack: Boolean = false,
    val navigateToSaveCollection: Boolean = false
) {
    val isOwner: Boolean
        get() {
            val currentUserId = SessionManager.authenticatedUserIdOrNull()
            return currentUserId != null && recipe?.ownerId == currentUserId
        }

    val canEdit: Boolean
        get() = isOwner

    val canDelete: Boolean
        get() = isOwner

    val canSave: Boolean
        get() = !isOwner

    val canDuplicate: Boolean
        get() = !isOwner

    val canAddReview: Boolean
        get() = !isOwner

    val isEditing: Boolean
        get() = screenMode == RecipeProposalScreenMode.EDIT
}

sealed interface RecipeProposalUiEvent {

    data class LoadRecipe(val recipeId: String) : RecipeProposalUiEvent
    data object EditRequested : RecipeProposalUiEvent
    data object CancelEditRequested : RecipeProposalUiEvent
    data object DiscardEditConfirmed : RecipeProposalUiEvent
    data object DiscardEditDismissed : RecipeProposalUiEvent
    data object SaveEditRequested : RecipeProposalUiEvent
    data object DeleteRequested : RecipeProposalUiEvent
    data object DismissSuccessMessage : RecipeProposalUiEvent

    data object SaveRequested : RecipeProposalUiEvent
    data object SaveCollectionNavigationConsumed : RecipeProposalUiEvent

    data object DeleteConfirmed : RecipeProposalUiEvent
    data object DeleteDismissed : RecipeProposalUiEvent

    data class TitleChanged(val value: String) : RecipeProposalUiEvent
    data class DescriptionChanged(val value: String) : RecipeProposalUiEvent
    data class ImageUriChanged(val value: String) : RecipeProposalUiEvent
    data class ServingsChanged(val value: Int) : RecipeProposalUiEvent
    data class PriceRangeChanged(val value: PriceRange) : RecipeProposalUiEvent
    data class DifficultyChanged(val value: DifficultyLevel) : RecipeProposalUiEvent
    data class CookTimeChanged(val value: Int) : RecipeProposalUiEvent
    data class CaloriesChanged(val value: Int?) : RecipeProposalUiEvent
    data class RecipeTypeChanged(val value: RecipeType) : RecipeProposalUiEvent
    data class CuisineTypeChanged(val value: CuisineType?) : RecipeProposalUiEvent
    data class DietaryRestrictionToggled(val value: DietaryRestriction) : RecipeProposalUiEvent
    data class IngredientAdded(val item: IngredientItem) : RecipeProposalUiEvent
    data class IngredientRemoved(val id: String) : RecipeProposalUiEvent
    data class IngredientUpdated(val item: IngredientItem) : RecipeProposalUiEvent
    data class StepAdded(val item: InstructionStep) : RecipeProposalUiEvent
    data class StepRemoved(val id: String) : RecipeProposalUiEvent
    data class StepUpdated(val item: InstructionStep) : RecipeProposalUiEvent

}

private fun UserProfile.displayName(): String {
    return when {
        fullname.isNotBlank() && nickname.isNotBlank() -> "$fullname · @$nickname"
        fullname.isNotBlank() -> fullname
        nickname.isNotBlank() -> "@$nickname"
        else -> "User $internalId"
    }
}

class RecipeEditorViewModel(
    private val recipeRepository: RecipeRepository,
    private val userProfileRepository: UserRepository,
    private val deleteRecipeProposalUseCase: DeleteRecipeProposalUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeProposalUiState())
    val uiState: StateFlow<RecipeProposalUiState> = _uiState.asStateFlow()

    private var recipeObservationJob: Job? = null
    private var originalRecipeObservationJob: Job? = null
    private var observedOriginalRecipeId: String? = null

    fun onEvent(event: RecipeProposalUiEvent) {
        when (event) {
            is RecipeProposalUiEvent.LoadRecipe -> loadRecipeInternal(event.recipeId)
            is RecipeProposalUiEvent.EditRequested -> enterEditModeInternal()
            is RecipeProposalUiEvent.CancelEditRequested -> requestCancelEditInternal()
            is RecipeProposalUiEvent.DiscardEditConfirmed -> discardEditChangesInternal()
            is RecipeProposalUiEvent.DiscardEditDismissed -> dismissDiscardEditConfirmInternal()
            is RecipeProposalUiEvent.SaveEditRequested -> saveEditInternal()
            is RecipeProposalUiEvent.DismissSuccessMessage -> dismissSuccessMessageInternal()
            is RecipeProposalUiEvent.TitleChanged -> updateTitleInternal(event.value)
            is RecipeProposalUiEvent.DescriptionChanged -> updateDescriptionInternal(event.value)
            is RecipeProposalUiEvent.ImageUriChanged -> updateImageUriInternal(event.value)
            is RecipeProposalUiEvent.ServingsChanged -> updateServingsInternal(event.value)
            is RecipeProposalUiEvent.PriceRangeChanged -> updatePriceRangeInternal(event.value)
            is RecipeProposalUiEvent.DifficultyChanged -> updateDifficultyInternal(event.value)
            is RecipeProposalUiEvent.CookTimeChanged -> updateCookTimeInternal(event.value)
            is RecipeProposalUiEvent.CaloriesChanged -> updateCaloriesInternal(event.value)
            is RecipeProposalUiEvent.RecipeTypeChanged -> updateRecipeTypeInternal(event.value)
            is RecipeProposalUiEvent.CuisineTypeChanged -> updateCuisineTypeInternal(event.value)
            is RecipeProposalUiEvent.DietaryRestrictionToggled -> toggleDietaryRestrictionInternal(event.value)
            is RecipeProposalUiEvent.IngredientAdded -> addIngredientInternal(event.item)
            is RecipeProposalUiEvent.IngredientRemoved -> removeIngredientInternal(event.id)
            is RecipeProposalUiEvent.IngredientUpdated -> updateIngredientInternal(event.item)
            is RecipeProposalUiEvent.StepAdded -> addStepInternal(event.item)
            is RecipeProposalUiEvent.StepRemoved -> removeStepInternal(event.id)
            is RecipeProposalUiEvent.StepUpdated -> updateStepInternal(event.item)
            is RecipeProposalUiEvent.DeleteRequested  -> showDeleteConfirmInternal()
            is RecipeProposalUiEvent.DeleteConfirmed  -> confirmDeleteInternal()
            is RecipeProposalUiEvent.DeleteDismissed  -> dismissDeleteInternal()
            is RecipeProposalUiEvent.SaveRequested -> saveRequestedInternal()
            is RecipeProposalUiEvent.SaveCollectionNavigationConsumed -> saveCollectionNavigationConsumed()
        }
    }

    private fun loadRecipeInternal(id: String) {
        recipeObservationJob?.cancel()
        originalRecipeObservationJob?.cancel()
        observedOriginalRecipeId = null

        recipeObservationJob = viewModelScope.launch {
            recipeRepository.getRecipeProposalById(id).collect { recipe ->
                val authorDisplayName = recipe?.ownerId?.let { ownerId ->
                    userProfileRepository.getUserById(ownerId)?.displayName()
                        ?: "User $ownerId"
                }

                _uiState.update { current ->
                    if (current.isEditing) {
                        current
                    } else {
                        current.copy(
                            recipe = recipe,
                            authorDisplayName = authorDisplayName
                        )
                    }
                }

                observeOriginalRecipe(recipe?.originalRecipeId)
            }
        }
    }

    private fun observeOriginalRecipe(originalRecipeId: String?) {
        val normalizedOriginalRecipeId = originalRecipeId?.takeIf { it.isNotBlank() }

        if (
            normalizedOriginalRecipeId == observedOriginalRecipeId &&
            originalRecipeObservationJob?.isActive == true
        ) {
            return
        }

        originalRecipeObservationJob?.cancel()
        observedOriginalRecipeId = normalizedOriginalRecipeId

        if (normalizedOriginalRecipeId == null) {
            _uiState.update { state ->
                state.copy(
                    originalRecipe = null,
                    isOriginalRecipeLoading = false
                )
            }
            return
        }

        _uiState.update { state ->
            if (state.recipe?.originalRecipeId != normalizedOriginalRecipeId) {
                state
            } else {
                state.copy(
                    originalRecipe = null,
                    isOriginalRecipeLoading = true
                )
            }
        }

        originalRecipeObservationJob = viewModelScope.launch {
            recipeRepository
                .getRecipeProposalById(normalizedOriginalRecipeId)
                .collect { originalRecipe ->
                    _uiState.update { state ->
                        if (state.recipe?.originalRecipeId != normalizedOriginalRecipeId) {
                            state
                        } else {
                            state.copy(
                                originalRecipe = originalRecipe,
                                isOriginalRecipeLoading = false
                            )
                        }
                    }
                }
        }
    }

    private fun enterEditModeInternal() {
        _uiState.update { state ->
            val recipe = state.recipe ?: return@update state
            if (!state.canEdit) return@update state

            state.copy(
                screenMode = RecipeProposalScreenMode.EDIT,
                editState = RecipeEditorUiState(
                    formData = recipe.toEditorFormData(),
                    errors = RecipeEditorErrors()
                )
            )
        }
    }

    private fun requestCancelEditInternal() {
        _uiState.update { state ->
            if (!state.isEditing) return@update state

            if (state.hasUnsavedEditChanges()) {
                state.copy(showDiscardEditConfirm = true)
            } else {
                state.copy(
                    screenMode = RecipeProposalScreenMode.VIEW,
                    editState = null,
                    showDiscardEditConfirm = false
                )
            }
        }
    }

    private fun discardEditChangesInternal() {
        _uiState.update { state ->
            state.copy(
                screenMode = RecipeProposalScreenMode.VIEW,
                editState = null,
                showDiscardEditConfirm = false
            )
        }
    }

    private fun dismissDiscardEditConfirmInternal() {
        _uiState.update { state ->
            state.copy(showDiscardEditConfirm = false)
        }
    }

    private fun RecipeProposalUiState.hasUnsavedEditChanges(): Boolean {
        val originalRecipe = recipe ?: return false
        val currentForm = editState?.formData ?: return false

        return currentForm != originalRecipe.toEditorFormData()
    }

    private fun saveEditInternal() {
        val state = _uiState.value
        val editState = state.editState ?: return
        val form = editState.formData
        val recipe = state.recipe ?: return

        val currentUserId = SessionManager.authenticatedUserIdOrNull()
        if (currentUserId == null || recipe.ownerId != currentUserId) {
            _uiState.update {
                it.copy(successMessage = null)
            }
            return
        }

        val errors = validateForm(form)
        if (errors.hasAnyError) {
            _uiState.update { currentState ->
                val currentEditState = currentState.editState ?: return@update currentState
                currentState.copy(editState = currentEditState.copy(errors = errors))
            }
            return
        }

        val updated = recipe.copy(
            title = form.title.trim(),
            description = form.description.trim(),
            imageUri = form.imageUri,
            servings = form.servings,
            priceRange = form.priceRange.name,
            difficulty = form.difficulty.name,
            cookTimeMinutes = form.cookTimeMinutes,
            calories = form.calories,
            recipeType = form.recipeType.name,
            cuisineType = form.cuisineType?.name,
            dietaryRestrictions = form.dietaryRestrictions.map { it.name },
            ingredients = form.ingredients.map { ingredient ->
                ingredient.copy(
                    name = ingredient.name.trim(),
                    note = ingredient.note.trim()
                ).normalizedForUnit()
            },
            steps = form.steps.mapIndexed { index, step ->
                step.copy(
                    stepNumber = index + 1,
                    text = step.text.trim()
                )
            }
        )

        viewModelScope.launch {
            try {
                recipeRepository.updateRecipeProposal(updated)
                _uiState.update {
                    it.copy(
                        recipe = updated,
                        screenMode = RecipeProposalScreenMode.VIEW,
                        editState = null,
                        showDiscardEditConfirm = false,
                        successMessage = "Changes saved!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(successMessage = "Failed to save changes. Please try again.")
                }
            }
        }
    }

    private fun showDeleteConfirmInternal() {
        _uiState.update { state ->
            if (!state.canDelete) state else state.copy(showDeleteConfirm = true)
        }
    }

    private fun confirmDeleteInternal() {
        val currentState = _uiState.value
        val recipe = currentState.recipe ?: return

        val currentUserId = SessionManager.authenticatedUserIdOrNull()
        if (currentUserId == null || recipe.ownerId != currentUserId) {
            _uiState.update { it.copy(showDeleteConfirm = false) }
            return
        }

        _uiState.update { it.copy(showDeleteConfirm = false) }

        viewModelScope.launch {
            try {
                deleteRecipeProposalUseCase(recipe.id)
                _uiState.update { it.copy(navigateBack = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(successMessage = "Failed to delete recipe. Please try again.")
                }
            }
        }
    }

    private fun dismissDeleteInternal() {
        _uiState.update { it.copy(showDeleteConfirm = false) }
    }

    private fun dismissSuccessMessageInternal() {
        _uiState.update {
            it.copy(successMessage = null)
        }
    }

    private fun validateForm(form: RecipeEditorFormData): RecipeEditorErrors {
        val titleTrimmed = form.title.trim()

        val titleError = when {
            titleTrimmed.isEmpty() -> "Title is required."
            '\n' in form.title -> "Title must be a single line."
            titleTrimmed.length > 100 -> "Title must be at most 100 characters long."
            else -> null
        }

        val descriptionError = when {
            form.description.isBlank() -> null
            form.description.trim().length > 500 -> "Description must be at most 500 characters long."
            else -> null
        }

        val servingsError = when {
            form.servings < 1 -> "Servings must be at least 1."
            form.servings > 10 -> "Servings must be at most 10."
            else -> null
        }

        val cookTimeError = when {
            form.cookTimeMinutes < 1 -> "Cook time must be at least 1 minute."
            form.cookTimeMinutes > 720 -> "Cook time must be at most 720 minutes."
            else -> null
        }

        val caloriesError = when {
            form.calories == null -> null
            form.calories < 1 -> "Calories must be at least 1 kcal."
            form.calories > 5000 -> "Calories must be at most 5000 kcal."
            else -> null
        }

        val ingredientsError = when {
            form.ingredients.isEmpty() -> "Add at least one ingredient."
            form.ingredients.any { it.name.isBlank() } -> "All ingredients must have a name."

            form.ingredients.any { it.requiresQuantity && it.quantity == null } ->
                "All ingredients with a measurable unit must have a quantity."

            form.ingredients.any { it.requiresQuantity && (it.quantity ?: 0.0) <= 0.0 } ->
                "Ingredient quantities must be greater than 0."

            else -> null
        }

        val stepsError = when {
            form.steps.isEmpty() -> "Add at least one preparation step."
            form.steps.any { it.text.isBlank() } -> "All preparation steps must have a description."
            else -> null
        }

        return RecipeEditorErrors(
            title = titleError,
            description = descriptionError,
            servings = servingsError,
            cookTimeMinutes = cookTimeError,
            calories = caloriesError,
            ingredients = ingredientsError,
            steps = stepsError
        )
    }

    private fun updateTitleInternal(value: String) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state
            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(title = value),
                    errors = editState.errors.copy(title = null)
                )
            )
        }
    }

    private fun updateDescriptionInternal(value: String) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            val descriptionError =
                if (value.trim().length > 500)
                    "Description must be at most 500 characters long."
                else
                    null

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(description = value),
                    errors = editState.errors.copy(description = descriptionError)
                )
            )
        }
    }

    private fun updateImageUriInternal(value: String) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(imageUri = value)
                )
            )
        }
    }

    private fun updateServingsInternal(value: Int) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(servings = value),
                    errors = editState.errors.copy(servings = null)
                )
            )
        }
    }

    private fun updatePriceRangeInternal(value: PriceRange) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(priceRange = value)
                )
            )
        }
    }

    private fun updateDifficultyInternal(value: DifficultyLevel) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(difficulty = value)
                )
            )
        }
    }

    private fun updateCookTimeInternal(value: Int) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(cookTimeMinutes = value),
                    errors = editState.errors.copy(cookTimeMinutes = null)
                )
            )
        }
    }

    private fun updateCaloriesInternal(value: Int?) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(calories = value),
                    errors = editState.errors.copy(calories = null)
                )
            )
        }
    }

    private fun updateRecipeTypeInternal(value: RecipeType) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(recipeType = value)
                )
            )
        }
    }

    private fun updateCuisineTypeInternal(value: CuisineType?) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(cuisineType = value)
                )
            )
        }
    }

    private fun toggleDietaryRestrictionInternal(value: DietaryRestriction) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state
            val current = editState.formData.dietaryRestrictions
            val updated =
                if (value in current)
                    current - value
                else
                    current + value

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(dietaryRestrictions = updated)
                )
            )
        }
    }

    private fun addIngredientInternal(item: IngredientItem) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(
                        ingredients = editState.formData.ingredients + item
                    ),
                    errors = editState.errors.copy(ingredients = null)
                )
            )
        }
    }

    private fun removeIngredientInternal(id: String) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(
                        ingredients = editState.formData.ingredients.filter { it.id != id }
                    ),
                    errors = editState.errors.copy(ingredients = null)
                )
            )
        }
    }

    private fun updateIngredientInternal(item: IngredientItem) {
        val normalizedItem = item.normalizedForUnit()

        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(
                        ingredients = editState.formData.ingredients.map {
                            if (it.id == normalizedItem.id) normalizedItem else it
                        }
                    ),
                    errors = editState.errors.copy(ingredients = null)
                )
            )
        }
    }

    private fun addStepInternal(item: InstructionStep) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(
                        steps = editState.formData.steps + item
                    ),
                    errors = editState.errors.copy(steps = null)
                )
            )
        }
    }

    private fun removeStepInternal(id: String) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(
                        steps = editState.formData.steps.filter { it.id != id }
                    ),
                    errors = editState.errors.copy(steps = null)
                )
            )
        }
    }

    private fun updateStepInternal(item: InstructionStep) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(
                        steps = editState.formData.steps.map {
                            if (it.id == item.id) item else it
                        }
                    ),
                    errors = editState.errors.copy(steps = null)
                )
            )
        }
    }
    private fun saveRequestedInternal(){
        _uiState.update { it.copy(navigateToSaveCollection = true) }
    }

    private fun saveCollectionNavigationConsumed(){
        _uiState.update { it.copy(navigateToSaveCollection = false) }
    }

}
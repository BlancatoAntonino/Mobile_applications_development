package it.polito.mad.cookbookcommunity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.model.recipe.*
import it.polito.mad.cookbookcommunity.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NewRecipeStep(val index: Int) {
    BASIC_INFO(0),
    INGREDIENTS(1),
    STEPS(2),
    PREVIEW(3);

    val isFirst get() = this == BASIC_INFO
    val isLast get() = this == PREVIEW

    fun next(): NewRecipeStep = entries.getOrElse(index + 1) { this }
    fun prev(): NewRecipeStep = entries.getOrElse(index - 1) { this }
}

data class NewRecipeFormData(
    val title: String = "",
    val description: String = "",
    val imageUri: String = "",
    val servings: Int = 2,
    val priceRange: PriceRange = PriceRange.MEDIUM,
    val difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    val cookTimeMinutes: Int = 30,
    val calories: Int? = null,
    val recipeType: RecipeType = RecipeType.MAIN_COURSE,
    val cuisineType: CuisineType? = null,
    val dietaryRestrictions: List<DietaryRestriction> = emptyList(),
    val ingredients: List<IngredientItem> = emptyList(),
    val steps: List<InstructionStep> = emptyList(),

    val originalRecipeId: String? = null,
    val originalRecipeTitle: String = "",
    val originalRecipeImageUri: String = "",
    val originalAuthorId: String = "",
    val originalAuthorDisplayName: String = "",
    val originalAuthorPhotoUrl: String = "",
    val adaptationNote: String = ""
)

data class NewRecipeErrors(
    val title: String? = null,
    val servings: String? = null,
    val cookTimeMinutes: String? = null,
    val calories: String? = null,
    val adaptationNote: String? = null,
    val ingredients: String? = null,
    val steps: String? = null
) {
    val hasAnyError: Boolean
        get() = title != null ||
                servings != null ||
                cookTimeMinutes != null ||
                calories != null ||
                adaptationNote != null ||
                ingredients != null ||
                steps != null
}

data class NewRecipeUiState(
    val currentStep: NewRecipeStep = NewRecipeStep.BASIC_INFO,
    val form: NewRecipeFormData = NewRecipeFormData(),
    val errors: NewRecipeErrors = NewRecipeErrors(),
    val publishedRecipeId: String? = null,
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
) {
    val isPublishLocked: Boolean
        get() = isSaving || publishedRecipeId != null || successMessage != null

    val hasDraftData: Boolean
        get() = form != NewRecipeFormData()
}

sealed interface NewRecipeEvent {
    data object NextStep : NewRecipeEvent
    data object PrevStep : NewRecipeEvent
    data object DismissSuccessMessage: NewRecipeEvent
    data object DismissErrorMessage : NewRecipeEvent

    data class TitleChanged(val value: String) : NewRecipeEvent
    data class DescriptionChanged(val value: String) : NewRecipeEvent
    data class AdaptationNoteChanged(val value: String) : NewRecipeEvent
    data class ImageUriChanged(val value: String) : NewRecipeEvent
    data class ServingsChanged(val value: Int) : NewRecipeEvent
    data class PriceRangeChanged(val value: PriceRange) : NewRecipeEvent
    data class DifficultyChanged(val value: DifficultyLevel) : NewRecipeEvent
    data class CookTimeChanged(val value: Int) : NewRecipeEvent
    data class CaloriesChanged(val value: Int?) : NewRecipeEvent
    data class RecipeTypeChanged(val value: RecipeType) : NewRecipeEvent
    data class CuisineTypeChanged(val value: CuisineType?) : NewRecipeEvent
    data class DietaryRestrictionToggled(val value: DietaryRestriction) : NewRecipeEvent
    data object IngredientAdded : NewRecipeEvent
    data class IngredientRemoved(val id: String) : NewRecipeEvent
    data class IngredientUpdated(val item: IngredientItem) : NewRecipeEvent
    data object StepAdded : NewRecipeEvent
    data class StepRemoved(val id: String) : NewRecipeEvent
    data class StepUpdated(val item: InstructionStep) : NewRecipeEvent
    data class GoToStep(val step: NewRecipeStep) : NewRecipeEvent
    data object Publish : NewRecipeEvent
    data object ResetForm: NewRecipeEvent
}

class NewRecipeProposalViewModel(
    private val repository: RecipeRepository,
    private val sourceRecipeId: String? = null
): ViewModel() {
    private val _uiState = MutableStateFlow(NewRecipeUiState())
    val uiState: StateFlow<NewRecipeUiState> = _uiState.asStateFlow()

    init {
        prefillFromSourceRecipeIfNeeded()
    }

    fun onEvent(event: NewRecipeEvent) {
        when (event) {
            is NewRecipeEvent.NextStep -> nextStepInternal()
            is NewRecipeEvent.PrevStep -> prevStepInternal()
            is NewRecipeEvent.DismissErrorMessage -> errorMessageInternal()
            is NewRecipeEvent.DismissSuccessMessage -> dismissSuccessMessageInternal()
            is NewRecipeEvent.TitleChanged -> updateTitleInternal(event.value)
            is NewRecipeEvent.DescriptionChanged -> updateDescriptionInternal(event.value)
            is NewRecipeEvent.AdaptationNoteChanged -> updateAdaptationNoteInternal(event.value)
            is NewRecipeEvent.ImageUriChanged -> updateImageUriInternal(event.value)
            is NewRecipeEvent.ServingsChanged -> updateServingsInternal(event.value)
            is NewRecipeEvent.PriceRangeChanged -> updatePriceRangeInternal(event.value)
            is NewRecipeEvent.DifficultyChanged -> updateDifficultyInternal(event.value)
            is NewRecipeEvent.CookTimeChanged -> updateCookTimeInternal(event.value)
            is NewRecipeEvent.CaloriesChanged -> updateCaloriesInternal(event.value)
            is NewRecipeEvent.RecipeTypeChanged -> updateRecipeTypeInternal(event.value)
            is NewRecipeEvent.CuisineTypeChanged -> updateCuisineTypeInternal(event.value)
            is NewRecipeEvent.DietaryRestrictionToggled -> toggleDietaryRestrictionInternal(event.value)
            is NewRecipeEvent.IngredientAdded -> addIngredientInternal()
            is NewRecipeEvent.IngredientRemoved -> removeIngredientInternal(event.id)
            is NewRecipeEvent.IngredientUpdated -> updateIngredientInternal(event.item)
            is NewRecipeEvent.StepAdded -> addStepInternal()
            is NewRecipeEvent.StepRemoved -> removeStepInternal(event.id)
            is NewRecipeEvent.StepUpdated -> updateStepInternal(event.item)
            is NewRecipeEvent.GoToStep -> goToStepInternal(event.step)
            is NewRecipeEvent.Publish -> publishInternal()
            is NewRecipeEvent.ResetForm -> resetFormInternal()
        }
    }

    private fun nextStepInternal() {
        val current = _uiState.value
        val errors = validateStep(current.currentStep, current.form)

        if (errors.hasAnyError) {
            _uiState.update { it.copy(errors = errors) }
            return
        }

        if (!current.currentStep.isLast) {
            _uiState.update { it.copy(currentStep = it.currentStep.next(), errors = NewRecipeErrors()) }
        }
    }

    private fun prevStepInternal() {
        _uiState.update { state ->
            if (!state.currentStep.isFirst)
                state.copy(currentStep = state.currentStep.prev(), errors = NewRecipeErrors())
            else
                state
        }
    }

    private fun errorMessageInternal() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun dismissSuccessMessageInternal() {
        _uiState.value = NewRecipeUiState()
    }

    private fun validateStep(
        step: NewRecipeStep,
        form: NewRecipeFormData
    ): NewRecipeErrors {
        return when (step) {
            NewRecipeStep.BASIC_INFO -> {
                val titleTrimmed = form.title.trim()

                val titleError = when {
                    titleTrimmed.isEmpty() -> "Title required."
                    '\n' in form.title -> "Title must be a single line."
                    titleTrimmed.length > 100 -> "Title must be at most 100 characters."
                    else -> null
                }

                val servingsError = when {
                    form.servings < 1 -> "Servings must be at least 1."
                    form.servings > 20 -> "Servings must be at most 20."
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

                val adaptationNoteError = if (form.originalRecipeId != null) {
                    val trimmedNote = form.adaptationNote.trim()

                    when {
                        trimmedNote.isEmpty() ->
                            "Explain briefly what you changed or why you adapted this recipe."

                        trimmedNote.length < 10 ->
                            "Adaptation note must be at least 10 characters."

                        trimmedNote.length > 220 ->
                            "Adaptation note must be at most 220 characters."

                        else -> null
                    }
                } else {
                    null
                }

                NewRecipeErrors(
                    title = titleError,
                    servings = servingsError,
                    cookTimeMinutes = cookTimeError,
                    calories = caloriesError,
                    adaptationNote = adaptationNoteError
                )
            }

            NewRecipeStep.INGREDIENTS -> {
                val ingredientsError = when {
                    form.ingredients.isEmpty() -> "Add at least one ingredient."
                    form.ingredients.any { it.name.isBlank() } -> "All ingredients must have a name."

                    form.ingredients.any { it.requiresQuantity && it.quantity == null } ->
                        "All ingredients with a measurable unit must have a quantity."

                    form.ingredients.any { it.requiresQuantity && (it.quantity ?: 0.0) <= 0.0 } ->
                        "Ingredient quantities must be greater than 0."

                    else -> null
                }

                NewRecipeErrors(ingredients = ingredientsError)
            }

            NewRecipeStep.STEPS -> {
                val stepsError = when {
                    form.steps.isEmpty() -> "Add at least one step."
                    form.steps.any { it.text.isBlank() } -> "All steps must have a description."
                    else -> null
                }
                NewRecipeErrors(steps = stepsError)
            }

            NewRecipeStep.PREVIEW -> NewRecipeErrors()
        }
    }

    private fun updateTitleInternal(value: String) {
        _uiState.update { state ->
            state.copy(
                form = state.form.copy(title = value),
                errors = state.errors.copy(title = null)
            )
        }
    }

    private fun updateDescriptionInternal(value: String) {
        _uiState.update { state ->
            state.copy(form = state.form.copy(description = value))
        }
    }

    private fun updateAdaptationNoteInternal(value: String) {
        _uiState.update { state ->
            state.copy(
                form = state.form.copy(adaptationNote = value),
                errors = state.errors.copy(adaptationNote = null)
            )
        }
    }

    private fun updateImageUriInternal(value: String) {
        _uiState.update { state ->
            state.copy(form = state.form.copy(imageUri = value))
        }
    }

    private fun updateServingsInternal(value: Int) {
        _uiState.update { state ->
            state.copy(
                form = state.form.copy(servings = value),
                errors = state.errors.copy(servings = null)
            )
        }
    }

    private fun updatePriceRangeInternal(value: PriceRange) {
        _uiState.update { state ->
            state.copy(form = state.form.copy(priceRange = value))
        }
    }

    private fun updateDifficultyInternal(value: DifficultyLevel) {
        _uiState.update { state ->
            state.copy(form = state.form.copy(difficulty = value))
        }
    }

    private fun updateCookTimeInternal(value: Int) {
        _uiState.update { state ->
            state.copy(
                form = state.form.copy(cookTimeMinutes = value),
                errors = state.errors.copy(cookTimeMinutes = null)
            )
        }
    }

    private fun updateCaloriesInternal(value: Int?) {
        _uiState.update { state ->
            state.copy(
                form = state.form.copy(calories = value),
                errors = state.errors.copy(calories = null)
            )
        }
    }

    private fun updateRecipeTypeInternal(value: RecipeType) {
        _uiState.update { state ->
            state.copy(form = state.form.copy(recipeType = value))
        }
    }

    private fun updateCuisineTypeInternal(value: CuisineType?) {
        _uiState.update { state ->
            state.copy(form = state.form.copy(cuisineType = value))
        }
    }

    private fun toggleDietaryRestrictionInternal(value: DietaryRestriction) {
        _uiState.update { state ->
            val current = state.form.dietaryRestrictions
            val updated = if (value in current) current - value else current + value
            state.copy(form = state.form.copy(dietaryRestrictions = updated))
        }
    }

    private fun addIngredientInternal() {
        _uiState.update { state ->
            state.copy(
                form = state.form.copy(ingredients = state.form.ingredients + IngredientItem()),
                errors = state.errors.copy(ingredients = null)
            )
        }
    }

    private fun removeIngredientInternal(id: String) {
        _uiState.update { state ->
            state.copy(
                form = state.form.copy(
                    ingredients = state.form.ingredients.filter { it.id != id }
                ),
                errors = state.errors.copy(ingredients = null)
            )
        }
    }

    private fun updateIngredientInternal(item: IngredientItem) {
        val normalizedItem = item.normalizedForUnit()

        _uiState.update { state ->
            state.copy(
                form = state.form.copy(
                    ingredients = state.form.ingredients.map {
                        if (it.id == normalizedItem.id) normalizedItem else it
                    }
                ),
                errors = state.errors.copy(ingredients = null)
            )
        }
    }

    private fun addStepInternal() {
        _uiState.update { state ->
            val nextNumber = (state.form.steps.maxOfOrNull { it.stepNumber } ?: 0) + 1
            state.copy(
                form = state.form.copy(steps = state.form.steps + InstructionStep(stepNumber = nextNumber)),
                errors = state.errors.copy(steps = null)
            )
        }
    }

    private fun removeStepInternal(id: String) {
        _uiState.update { state ->
            val filtered = state.form.steps.filter { it.id != id }
            val renumbered = filtered.mapIndexed { idx, s -> s.copy(stepNumber = idx + 1) }

            state.copy(
                form = state.form.copy(steps = renumbered),
                errors = state.errors.copy(steps = null)
            )
        }
    }

    private fun updateStepInternal(item: InstructionStep) {
        _uiState.update { state ->
            state.copy(
                form = state.form.copy(
                    steps = state.form.steps.map {
                        if (it.id == item.id) item else it
                    }
                ),
                errors = state.errors.copy(steps = null)
            )
        }
    }

    private fun goToStepInternal(step: NewRecipeStep) {
        _uiState.update { state ->
            state.copy(currentStep = step, errors = NewRecipeErrors())
        }
    }

    private fun publishInternal() {
        val currentState = _uiState.value

        if (currentState.isPublishLocked) return

        val form = currentState.form

        val basicErrors = validateStep(NewRecipeStep.BASIC_INFO, form)
        val ingredientErrors = validateStep(NewRecipeStep.INGREDIENTS, form)
        val stepErrors = validateStep(NewRecipeStep.STEPS, form)

        val errors = NewRecipeErrors(
            title = basicErrors.title,
            servings = basicErrors.servings,
            cookTimeMinutes = basicErrors.cookTimeMinutes,
            calories = basicErrors.calories,
            adaptationNote = basicErrors.adaptationNote,
            ingredients = ingredientErrors.ingredients,
            steps = stepErrors.steps
        )

        if (errors.hasAnyError) {
            val firstInvalidStep = when {
                basicErrors.hasAnyError -> NewRecipeStep.BASIC_INFO
                ingredientErrors.hasAnyError -> NewRecipeStep.INGREDIENTS
                stepErrors.hasAnyError -> NewRecipeStep.STEPS
                else -> NewRecipeStep.PREVIEW
            }

            _uiState.update {
                it.copy(
                    currentStep = firstInvalidStep,
                    errors = errors,
                    isSaving = false
                )
            }
            return
        }

        val currentUserId = SessionManager.authenticatedUserIdOrNull()

        if (currentUserId == null) {
            _uiState.update {
                it.copy(
                    isSaving = false,
                    successMessage = null
                )
            }
            return
        }

        val currentUser = SessionManager.currentUser.value
        val recipe = RecipeProposal(
            ownerId = currentUserId,
            ownerDisplayName = currentUser?.displayName.orEmpty(),
            ownerPhotoUrl = currentUser?.photoUrl.orEmpty(),
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
            },

            originalRecipeId = form.originalRecipeId,
            originalRecipeTitle = form.originalRecipeTitle,
            originalRecipeImageUri = form.originalRecipeImageUri,
            originalAuthorId = form.originalAuthorId,
            originalAuthorDisplayName = form.originalAuthorDisplayName,
            originalAuthorPhotoUrl = form.originalAuthorPhotoUrl,
            adaptationNote = form.adaptationNote.trim()
        )

        _uiState.update {
            it.copy(
                isSaving = true,
                errors = NewRecipeErrors()
            )
        }

        viewModelScope.launch {
            try {
                repository.addRecipeProposal(recipe)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        publishedRecipeId = recipe.id,
                        successMessage = "Recipe published successfully!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to publish recipe. Please try again."
                    )
                }
            }
        }
    }

    private fun resetFormInternal() {
        _uiState.value = NewRecipeUiState()
    }

    private fun prefillFromSourceRecipeIfNeeded() {
        val sourceId = sourceRecipeId ?: return

        viewModelScope.launch {
            val sourceRecipe = repository
                .getRecipeProposalById(sourceId)
                .firstOrNull()
                ?: return@launch

            val rootRecipeId = sourceRecipe.originalRecipeId ?: sourceRecipe.id

            val rootRecipe = if (rootRecipeId == sourceRecipe.id) {
                sourceRecipe
            } else {
                repository
                    .getRecipeProposalById(rootRecipeId)
                    .firstOrNull()
            }

            _uiState.update { state ->
                state.copy(
                    form = sourceRecipe.toAdaptedRecipeFormData(rootRecipe),
                    currentStep = NewRecipeStep.BASIC_INFO,
                    errors = NewRecipeErrors(),
                    successMessage = null,
                    publishedRecipeId = null,
                    isSaving = false
                )
            }
        }
    }

    private fun RecipeProposal.toAdaptedRecipeFormData(
        rootRecipe: RecipeProposal?
    ): NewRecipeFormData {
        val rootRecipeId = rootRecipe?.id ?: originalRecipeId ?: id

        val rootRecipeTitle = rootRecipe?.title
            ?.takeIf { it.isNotBlank() }
            ?: originalRecipeTitle

        val rootRecipeImageUri = rootRecipe?.imageUri
            ?.takeIf { it.isNotBlank() }
            ?: originalRecipeImageUri

        val rootAuthorId = rootRecipe?.ownerId
            ?.takeIf { it.isNotBlank() }
            ?: originalAuthorId

        val rootAuthorDisplayName = rootRecipe?.ownerDisplayName
            ?.takeIf { it.isNotBlank() }
            ?: originalAuthorDisplayName

        val rootAuthorPhotoUrl = rootRecipe?.ownerPhotoUrl
            ?.takeIf { it.isNotBlank() }
            ?: originalAuthorPhotoUrl

        return NewRecipeFormData(
            title = title,
            description = description,
            imageUri = imageUri,
            servings = servings,
            priceRange = runCatching {
                PriceRange.valueOf(priceRange)
            }.getOrDefault(PriceRange.MEDIUM),
            difficulty = runCatching {
                DifficultyLevel.valueOf(difficulty)
            }.getOrDefault(DifficultyLevel.MEDIUM),
            cookTimeMinutes = cookTimeMinutes,
            calories = calories,
            recipeType = runCatching {
                RecipeType.valueOf(recipeType)
            }.getOrDefault(RecipeType.MAIN_COURSE),
            cuisineType = cuisineType?.let {
                runCatching { CuisineType.valueOf(it) }.getOrNull()
            },
            dietaryRestrictions = dietaryRestrictions.mapNotNull {
                runCatching { DietaryRestriction.valueOf(it) }.getOrNull()
            },
            ingredients = ingredients,
            steps = steps,

            originalRecipeId = rootRecipeId,
            originalRecipeTitle = rootRecipeTitle,
            originalRecipeImageUri = rootRecipeImageUri,
            originalAuthorId = rootAuthorId,
            originalAuthorDisplayName = rootAuthorDisplayName,
            originalAuthorPhotoUrl = rootAuthorPhotoUrl,

            adaptationNote = ""
        )
    }
}

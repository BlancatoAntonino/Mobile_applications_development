package it.polito.mad.cookbookcommunity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.model.recipe.DietaryRestriction
import it.polito.mad.cookbookcommunity.model.recipe.PriceRange
import it.polito.mad.cookbookcommunity.model.recipe.RecipeProposal
import it.polito.mad.cookbookcommunity.model.recipe.RecipeProposalFilters
import it.polito.mad.cookbookcommunity.model.recipe.RecipeType
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeProposalListUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val allRecipes: List<RecipeProposal> = emptyList(),
    val filteredRecipes: List<RecipeProposal> = emptyList(),
    val searchQuery: String = "",
    val selectedIngredients: Set<String> = emptySet(),
    val selectedPriceRanges: Set<PriceRange> = emptySet(),
    val selectedRecipeTypes: Set<RecipeType> = emptySet(),
    val selectedDietaryRestrictions: Set<DietaryRestriction> = emptySet(),
    val availableIngredients: List<String> = emptyList(),
    val availablePriceRanges: List<PriceRange> = PriceRange.entries.toList(),
    val availableRecipeTypes: List<RecipeType> = RecipeType.entries.toList(),
    val availableDietaryRestrictions: List<DietaryRestriction> = DietaryRestriction.entries.toList(),
    val hasActiveFilters: Boolean = false
)

private sealed interface RecipeProposalListLoadState {
    data object Loading : RecipeProposalListLoadState
    data object Ready : RecipeProposalListLoadState
    data class Error(val message: String) : RecipeProposalListLoadState
}

class RecipeProposalListViewModel(
    private val repository: RecipeRepository
) : ViewModel() {

    private val recipes = MutableStateFlow<List<RecipeProposal>>(emptyList())
    private val filters = MutableStateFlow(RecipeProposalFilters())
    private val loadState = MutableStateFlow<RecipeProposalListLoadState>(
        RecipeProposalListLoadState.Loading
    )
    private var loadJob: Job? = null

    val uiState: StateFlow<RecipeProposalListUiState> = combine(
        recipes,
        filters,
        loadState
    ) { allRecipes, activeFilters, currentLoadState ->
        buildUiState(
            allRecipes = allRecipes,
            filters = activeFilters,
            loadState = currentLoadState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecipeProposalListUiState()
    )

    init {
        retryLoad()
    }

    fun onSearchQueryChange(query: String) {
        filters.update { it.copy(query = query) }
    }

    fun toggleIngredientFilter(ingredient: String) {
        filters.update { current ->
            val normalizedIngredient = ingredient.trim()
            if (normalizedIngredient.isBlank()) {
                current
            } else {
                current.copy(
                    ingredientKeywords = current.ingredientKeywords.toggle(normalizedIngredient)
                )
            }
        }
    }

    fun togglePriceRangeFilter(priceRange: PriceRange) {
        filters.update { current ->
            current.copy(priceRanges = current.priceRanges.toggle(priceRange))
        }
    }

    fun toggleRecipeTypeFilter(recipeType: RecipeType) {
        filters.update { current ->
            current.copy(recipeTypes = current.recipeTypes.toggle(recipeType))
        }
    }

    fun toggleDietaryRestrictionFilter(restriction: DietaryRestriction) {
        filters.update { current ->
            current.copy(
                dietaryRestrictions = current.dietaryRestrictions.toggle(restriction)
            )
        }
    }

    fun clearFilters() {
        filters.value = RecipeProposalFilters()
    }

    fun retryLoad() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            repository.getAllRecipeProposals()
                .onStart {
                    loadState.value = RecipeProposalListLoadState.Loading
                }
                .catch { throwable ->
                    loadState.value = RecipeProposalListLoadState.Error(
                        throwable.message ?: "Unable to load recipes."
                    )
                }
                .collect { recipeList ->
                    recipes.value = recipeList
                    loadState.value = RecipeProposalListLoadState.Ready
                }
        }
    }

    private fun buildUiState(
        allRecipes: List<RecipeProposal>,
        filters: RecipeProposalFilters,
        loadState: RecipeProposalListLoadState
    ): RecipeProposalListUiState {
        val filteredRecipes = filterRecipeProposals(
            recipes = allRecipes,
            filters = filters
        )

        return RecipeProposalListUiState(
            isLoading = loadState is RecipeProposalListLoadState.Loading,
            errorMessage = (loadState as? RecipeProposalListLoadState.Error)?.message,
            allRecipes = allRecipes,
            filteredRecipes = filteredRecipes,
            searchQuery = filters.query,
            selectedIngredients = filters.ingredientKeywords,
            selectedPriceRanges = filters.priceRanges,
            selectedRecipeTypes = filters.recipeTypes,
            selectedDietaryRestrictions = filters.dietaryRestrictions,
            availableIngredients = allRecipes.availableIngredients(),
            availablePriceRanges = PriceRange.entries.toList(),
            availableRecipeTypes = RecipeType.entries.toList(),
            availableDietaryRestrictions = DietaryRestriction.entries.toList(),
            hasActiveFilters = filters.hasActiveFilters
        )
    }
}

private fun filterRecipeProposals(
    recipes: List<RecipeProposal>,
    filters: RecipeProposalFilters
): List<RecipeProposal> {
    val normalizedQuery = filters.query.normalized()
    val selectedIngredients = filters.ingredientKeywords.map { it.normalized() }.toSet()

    return recipes.filter { recipe ->
        recipe.matchesQuery(normalizedQuery) &&
                recipe.matchesIngredients(selectedIngredients) &&
                recipe.matchesPriceRanges(filters.priceRanges) &&
                recipe.matchesRecipeTypes(filters.recipeTypes) &&
                recipe.matchesDietaryRestrictions(filters.dietaryRestrictions)
    }
}

private fun RecipeProposal.matchesQuery(query: String): Boolean {
    if (query.isBlank()) return true

    val searchableFields = buildList {
        add(title)
        add(description)
        add(recipeType)
        cuisineType?.let(::add)
        addAll(ingredients.map { it.name })
        addAll(dietaryRestrictions)
    }

    return searchableFields.any { it.normalized().contains(query) }
}

private fun RecipeProposal.matchesIngredients(selectedIngredients: Set<String>): Boolean {
    if (selectedIngredients.isEmpty()) return true

    val recipeIngredients = ingredients.map { it.name.normalized() }.toSet()
    return recipeIngredients.any { it in selectedIngredients }
}

private fun RecipeProposal.matchesPriceRanges(selectedPriceRanges: Set<PriceRange>): Boolean {
    return selectedPriceRanges.isEmpty() || priceRange in selectedPriceRanges.map { it.name }
}

private fun RecipeProposal.matchesRecipeTypes(selectedRecipeTypes: Set<RecipeType>): Boolean {
    return selectedRecipeTypes.isEmpty() || recipeType in selectedRecipeTypes.map { it.name }
}

private fun RecipeProposal.matchesDietaryRestrictions(
    selectedRestrictions: Set<DietaryRestriction>
): Boolean {
    return selectedRestrictions.isEmpty() || dietaryRestrictions.any { it in selectedRestrictions.map { r -> r.name } }
}

private fun List<RecipeProposal>.availableIngredients(): List<String> {
    return flatMap { recipe -> recipe.ingredients }
        .map { it.name.trim() }
        .filter { it.isNotBlank() }
        .distinctBy { it.normalized() }
        .sortedWith(String.CASE_INSENSITIVE_ORDER)
}

private fun String.normalized(): String {
    return trim().lowercase(Locale.getDefault())
}

private fun <T> Set<T>.toggle(value: T): Set<T> {
    return if (value in this) this - value else this + value
}

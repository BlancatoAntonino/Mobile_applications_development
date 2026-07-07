@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package it.polito.mad.cookbookcommunity.ui.recipeProposal.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.polito.mad.cookbookcommunity.model.recipe.DietaryRestriction
import it.polito.mad.cookbookcommunity.model.recipe.PriceRange
import it.polito.mad.cookbookcommunity.model.recipe.RecipeProposal
import it.polito.mad.cookbookcommunity.model.recipe.RecipeType
import it.polito.mad.cookbookcommunity.ui.components.recipeproposal.RecipeProposalCard
import it.polito.mad.cookbookcommunity.ui.components.recipeproposal.displayName
import it.polito.mad.cookbookcommunity.viewmodel.RecipeProposalListUiState

@Composable
fun RecipeProposalListScreen(
    uiState: RecipeProposalListUiState,
    onBackClick: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onIngredientFilterToggle: (String) -> Unit,
    onPriceRangeFilterToggle: (PriceRange) -> Unit,
    onRecipeTypeFilterToggle: (RecipeType) -> Unit,
    onDietaryRestrictionFilterToggle: (DietaryRestriction) -> Unit,
    onClearFilters: () -> Unit,
    onRetryLoad: () -> Unit,
    showBackButton: Boolean,
    modifier: Modifier = Modifier
) {
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }

    if (showFilterSheet) {
        RecipeProposalFilterSheet(
            uiState = uiState,
            onDismissRequest = { showFilterSheet = false },
            onIngredientFilterToggle = onIngredientFilterToggle,
            onPriceRangeFilterToggle = onPriceRangeFilterToggle,
            onRecipeTypeFilterToggle = onRecipeTypeFilterToggle,
            onDietaryRestrictionFilterToggle = onDietaryRestrictionFilterToggle,
            onClearFilters = onClearFilters
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Explore recipes") },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SearchAndFilterBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
                onOpenFilters = { showFilterSheet = true },
                onClearQuery = { onSearchQueryChange("") }
            )

            ActiveFilterChips(
                uiState = uiState,
                onIngredientFilterToggle = onIngredientFilterToggle,
                onPriceRangeFilterToggle = onPriceRangeFilterToggle,
                onRecipeTypeFilterToggle = onRecipeTypeFilterToggle,
                onDietaryRestrictionFilterToggle = onDietaryRestrictionFilterToggle,
                onSearchQueryChange = onSearchQueryChange,
                onClearFilters = onClearFilters
            )

            when {
                uiState.isLoading -> LoadingState(modifier = Modifier.weight(1f))

                uiState.errorMessage != null -> ErrorState(
                    message = uiState.errorMessage,
                    onRetryLoad = onRetryLoad,
                    modifier = Modifier.weight(1f)
                )

                uiState.filteredRecipes.isEmpty() -> EmptyState(
                    hasActiveFilters = uiState.hasActiveFilters,
                    onClearFilters = onClearFilters,
                    modifier = Modifier.weight(1f)
                )

                else -> RecipeGrid(
                    recipes = uiState.filteredRecipes,
                    onRecipeClick = onRecipeClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SearchAndFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onOpenFilters: () -> Unit,
    onClearQuery: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            label = { Text("Search") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = onClearQuery) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search"
                        )
                    }
                }
            }
        )

        OutlinedButton(
            onClick = onOpenFilters,
            modifier = Modifier.height(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Filters")
        }
    }
}

@Composable
private fun ActiveFilterChips(
    uiState: RecipeProposalListUiState,
    onIngredientFilterToggle: (String) -> Unit,
    onPriceRangeFilterToggle: (PriceRange) -> Unit,
    onRecipeTypeFilterToggle: (RecipeType) -> Unit,
    onDietaryRestrictionFilterToggle: (DietaryRestriction) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    val hasChipFilters = uiState.selectedIngredients.isNotEmpty() ||
            uiState.selectedPriceRanges.isNotEmpty() ||
            uiState.selectedRecipeTypes.isNotEmpty() ||
            uiState.selectedDietaryRestrictions.isNotEmpty()

    if (!hasChipFilters && uiState.searchQuery.isBlank()) return

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 8.dp)
    ) {
        if (uiState.searchQuery.isNotBlank()) {
            item {
                RemovableFilterChip(
                    label = "\"${uiState.searchQuery}\"",
                    onRemove = { onSearchQueryChange("") }
                )
            }
        }

        items(uiState.selectedIngredients.toList(), key = { "ingredient_$it" }) { ingredient ->
            RemovableFilterChip(
                label = ingredient,
                onRemove = { onIngredientFilterToggle(ingredient) }
            )
        }

        items(uiState.selectedPriceRanges.toList(), key = { "price_${it.name}" }) { priceRange ->
            RemovableFilterChip(
                label = priceRange.displayName(),
                onRemove = { onPriceRangeFilterToggle(priceRange) }
            )
        }

        items(uiState.selectedRecipeTypes.toList(), key = { "type_${it.name}" }) { recipeType ->
            RemovableFilterChip(
                label = recipeType.displayName(),
                onRemove = { onRecipeTypeFilterToggle(recipeType) }
            )
        }

        items(
            uiState.selectedDietaryRestrictions.toList(),
            key = { "restriction_${it.name}" }
        ) { restriction ->
            RemovableFilterChip(
                label = restriction.displayName(),
                onRemove = { onDietaryRestrictionFilterToggle(restriction) }
            )
        }

        item {
            TextButton(onClick = onClearFilters) {
                Text("Clear all")
            }
        }
    }
}

@Composable
private fun RemovableFilterChip(
    label: String,
    onRemove: () -> Unit
) {
    InputChip(
        selected = true,
        onClick = onRemove,
        label = { Text(label) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove filter",
                modifier = Modifier.size(16.dp)
            )
        }
    )
}

@Composable
private fun RecipeGrid(
    recipes: List<RecipeProposal>,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 180.dp),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(
            items = recipes,
            key = { recipe -> recipe.id }
        ) { recipe ->
            RecipeProposalCard(
                recipe = recipe,
                onClick = { onRecipeClick(recipe.id) }
            )
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetryLoad: () -> Unit,
    modifier: Modifier = Modifier
) {
    PlaceholderState(
        title = "Unable to load recipes",
        message = message,
        modifier = modifier,
        action = {
            Button(onClick = onRetryLoad) {
                Text("Retry")
            }
        }
    )
}

@Composable
private fun EmptyState(
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    PlaceholderState(
        title = "No recipes found",
        message = "Try changing your search or filters",
        modifier = modifier,
        action = {
            if (hasActiveFilters) {
                Button(onClick = onClearFilters) {
                    Text("Clear filters")
                }
            }
        }
    )
}

@Composable
private fun PlaceholderState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                modifier = Modifier
                    .padding(18.dp)
                    .size(42.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        action()
    }
}

@Composable
private fun RecipeProposalFilterSheet(
    uiState: RecipeProposalListUiState,
    onDismissRequest: () -> Unit,
    onIngredientFilterToggle: (String) -> Unit,
    onPriceRangeFilterToggle: (PriceRange) -> Unit,
    onRecipeTypeFilterToggle: (RecipeType) -> Unit,
    onDietaryRestrictionFilterToggle: (DietaryRestriction) -> Unit,
    onClearFilters: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Filters",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(
                        enabled = uiState.hasActiveFilters,
                        onClick = onClearFilters
                    ) {
                        Text("Clear all")
                    }
                }
            }

            item {
                FilterSection(title = "Ingredients") {
                    if (uiState.availableIngredients.isEmpty()) {
                        Text(
                            text = "No ingredients available yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.availableIngredients.forEach { ingredient ->
                                FilterChip(
                                    selected = ingredient in uiState.selectedIngredients,
                                    onClick = { onIngredientFilterToggle(ingredient) },
                                    label = { Text(ingredient) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                FilterSection(title = "Price range") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.availablePriceRanges.forEach { priceRange ->
                            FilterChip(
                                selected = priceRange in uiState.selectedPriceRanges,
                                onClick = { onPriceRangeFilterToggle(priceRange) },
                                label = { Text(priceRange.displayName()) }
                            )
                        }
                    }
                }
            }

            item {
                FilterSection(title = "Recipe type") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.availableRecipeTypes.forEach { recipeType ->
                            FilterChip(
                                selected = recipeType in uiState.selectedRecipeTypes,
                                onClick = { onRecipeTypeFilterToggle(recipeType) },
                                label = { Text(recipeType.displayName()) }
                            )
                        }
                    }
                }
            }

            item {
                FilterSection(title = "Dietary restrictions") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.availableDietaryRestrictions.forEach { restriction ->
                            FilterChip(
                                selected = restriction in uiState.selectedDietaryRestrictions,
                                onClick = { onDietaryRestrictionFilterToggle(restriction) },
                                label = { Text(restriction.displayName()) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        content()
    }
}

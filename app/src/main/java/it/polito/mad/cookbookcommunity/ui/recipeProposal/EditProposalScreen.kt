package it.polito.mad.cookbookcommunity.ui.recipeProposal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import it.polito.mad.cookbookcommunity.model.recipe.CuisineType
import it.polito.mad.cookbookcommunity.model.recipe.DietaryRestriction
import it.polito.mad.cookbookcommunity.model.recipe.DifficultyLevel
import it.polito.mad.cookbookcommunity.model.recipe.IngredientItem
import it.polito.mad.cookbookcommunity.model.recipe.IngredientUnit
import it.polito.mad.cookbookcommunity.model.recipe.InstructionStep
import it.polito.mad.cookbookcommunity.model.recipe.PriceRange
import it.polito.mad.cookbookcommunity.model.recipe.RecipeType
import it.polito.mad.cookbookcommunity.model.recipe.requiresQuantity
import it.polito.mad.cookbookcommunity.ui.components.recipeproposal.RecipeImageContent
import it.polito.mad.cookbookcommunity.viewmodel.RecipeEditorUiState
import it.polito.mad.cookbookcommunity.viewmodel.RecipeProposalUiEvent

@Composable
fun EditProposalScreen(
    uiState: RecipeEditorUiState,
    onEvent: (RecipeProposalUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val form = uiState.formData
    val errors = uiState.errors

    LazyColumn(
        modifier = modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onEvent(RecipeProposalUiEvent.CancelEditRequested) }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel edit")
                }
                Text(
                    text = "Edit Recipe",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        item {
            EditableImageSection(
                imageUri = form.imageUri,
            )
        }
        item {
            OutlinedTextField(
                value = form.title,
                onValueChange = { onEvent(RecipeProposalUiEvent.TitleChanged(it)) },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errors.title != null,
                supportingText = errors.title?.let { { Text(it) } }
            )
        }

        item {
            OutlinedTextField(
                value = form.description,
                onValueChange = { onEvent(RecipeProposalUiEvent.DescriptionChanged(it)) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                isError = errors.description != null,
                supportingText = errors.description?.let { { Text(it) } }
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberStepperField(
                    modifier = Modifier.weight(1f),
                    label = "Cook time (min)",
                    value = form.cookTimeMinutes,
                    onDecrease = { if (form.cookTimeMinutes > 1) onEvent(RecipeProposalUiEvent.CookTimeChanged(form.cookTimeMinutes - 1)) },
                    onIncrease = { onEvent(RecipeProposalUiEvent.CookTimeChanged(form.cookTimeMinutes + 1)) },
                    onValueChange = { raw ->
                        raw.toIntOrNull()?.let { onEvent(RecipeProposalUiEvent.CookTimeChanged(it)) }
                    },
                    errorText = errors.cookTimeMinutes
                )
                NumberStepperField(
                    modifier = Modifier.weight(1f),
                    label = "Servings",
                    value = form.servings,
                    onDecrease = { if (form.servings > 1) onEvent(RecipeProposalUiEvent.ServingsChanged(form.servings - 1)) },
                    onIncrease = { onEvent(RecipeProposalUiEvent.ServingsChanged(form.servings + 1)) },
                    onValueChange = { raw ->
                        raw.toIntOrNull()?.let { onEvent(RecipeProposalUiEvent.ServingsChanged(it)) }
                    },
                    errorText = errors.servings
                )
            }
        }

        item {
            OutlinedTextField(
                value = form.calories?.toString() ?: "",
                onValueChange = { raw ->
                    val normalized = raw.trim()

                    when {
                        normalized.isEmpty() -> {
                            onEvent(RecipeProposalUiEvent.CaloriesChanged(null))
                        }

                        normalized.all { it.isDigit() } -> {
                            normalized.toIntOrNull()?.let { value ->
                                onEvent(RecipeProposalUiEvent.CaloriesChanged(value))
                            }
                        }
                    }
                },
                label = { Text("Calories (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = errors.calories != null,
                supportingText = errors.calories?.let { { Text(it) } }
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EnumDropdown(
                    modifier = Modifier.weight(1f),
                    label = "Difficulty",
                    selected = form.difficulty,
                    options = DifficultyLevel.entries,
                    display = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                    onSelected = { onEvent(RecipeProposalUiEvent.DifficultyChanged(it)) }
                )
                EnumDropdown(
                    modifier = Modifier.weight(1f),
                    label = "Price",
                    selected = form.priceRange,
                    options = PriceRange.entries,
                    display = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                    onSelected = { onEvent(RecipeProposalUiEvent.PriceRangeChanged(it)) }
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EnumDropdown(
                    modifier = Modifier.weight(1f),
                    label = "Recipe type",
                    selected = form.recipeType,
                    options = RecipeType.entries,
                    display = { it.name.replace('_', ' ').lowercase().replaceFirstChar { c -> c.uppercase() } },
                    onSelected = { onEvent(RecipeProposalUiEvent.RecipeTypeChanged(it)) }
                )
                NullableEnumDropdown(
                    modifier = Modifier.weight(1f),
                    label = "Cuisine",
                    selected = form.cuisineType,
                    options = CuisineType.entries,
                    display = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                    onSelected = { onEvent(RecipeProposalUiEvent.CuisineTypeChanged(it)) }
                )
            }
        }

        item {
            DietaryRestrictionsSection(
                selected = form.dietaryRestrictions,
                onToggle = { onEvent(RecipeProposalUiEvent.DietaryRestrictionToggled(it)) }
            )
        }

        item {
            EditableIngredientsSection(
                ingredients = form.ingredients,
                errorText = errors.ingredients,
                onAdd = { onEvent(RecipeProposalUiEvent.IngredientAdded(IngredientItem())) },
                onRemove = { onEvent(RecipeProposalUiEvent.IngredientRemoved(it)) },
                onUpdate = { onEvent(RecipeProposalUiEvent.IngredientUpdated(it)) }
            )
        }

        item {
            EditableStepsSection(
                steps = form.steps,
                errorText = errors.steps,
                onAdd = {
                    val nextNumber = (form.steps.maxOfOrNull { it.stepNumber } ?: 0) + 1
                    onEvent(RecipeProposalUiEvent.StepAdded(InstructionStep(stepNumber = nextNumber)))
                },
                onRemove = { onEvent(RecipeProposalUiEvent.StepRemoved(it)) },
                onUpdate = { onEvent(RecipeProposalUiEvent.StepUpdated(it)) }
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { onEvent(RecipeProposalUiEvent.CancelEditRequested) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = { onEvent(RecipeProposalUiEvent.SaveEditRequested) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun EditableImageSection(
    imageUri: String,
    onPickImageClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            RecipeImageContent(
                imageUri = imageUri,
                contentDescription = "Recipe cover image",
                modifier = Modifier.fillMaxSize(),
                placeholderText = "Cover image placeholder"
            )

            if (onPickImageClick != null) {
                IconButton(
                    onClick = onPickImageClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = MaterialTheme.shapes.small
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Change cover image",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberStepperField(
    modifier: Modifier = Modifier,
    label: String,
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onValueChange: (String) -> Unit,
    errorText: String? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (errorText != null) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        ElevatedCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDecrease, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }
                OutlinedTextField(
                    value = value.toString(),
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorText != null,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                )
                IconButton(onClick = onIncrease, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }
        }
        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumDropdown(
    modifier: Modifier = Modifier,
    label: String,
    selected: T,
    options: List<T>,
    display: (T) -> String,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = display(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(display(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> NullableEnumDropdown(
    modifier: Modifier = Modifier,
    label: String,
    selected: T?,
    options: List<T>,
    display: (T) -> String,
    onSelected: (T?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected?.let { display(it) } ?: "None",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("None") },
                onClick = { onSelected(null); expanded = false }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(display(option)) },
                    onClick = { onSelected(option); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun DietaryRestrictionsSection(
    selected: List<DietaryRestriction>,
    onToggle: (DietaryRestriction) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Dietary Restrictions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val all = DietaryRestriction.entries
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                all.take(all.size / 2).forEach { restriction ->
                    FilterChip(
                        selected = restriction in selected,
                        onClick = { onToggle(restriction) },
                        label = { Text(restriction.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                all.drop(all.size / 2).forEach { restriction ->
                    FilterChip(
                        selected = restriction in selected,
                        onClick = { onToggle(restriction) },
                        label = { Text(restriction.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun EditableIngredientsSection(
    ingredients: List<IngredientItem>,
    errorText: String?,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit,
    onUpdate: (IngredientItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Ingredients", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
            }
        }

        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        ingredients.forEachIndexed { index, ingredient ->
            IngredientRow(
                ingredient = ingredient,
                onRemove = { onRemove(ingredient.id) },
                onUpdate = onUpdate
            )
            if (index != ingredients.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }

        if (ingredients.isEmpty()) {
            Text(
                text = "No ingredients yet. Tap Add to start.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientRow(
    ingredient: IngredientItem,
    onRemove: () -> Unit,
    onUpdate: (IngredientItem) -> Unit
) {
    val quantityRequired = ingredient.requiresQuantity

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = ingredient.name,
                onValueChange = { onUpdate(ingredient.copy(name = it)) },
                label = { Text("Name") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            IconButton(
                onClick = onRemove,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove ingredient",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        if (quantityRequired) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = ingredient.quantity?.toString() ?: "",
                    onValueChange = { raw ->
                        onUpdate(
                            ingredient.copy(
                                quantity = raw.toDoubleOrNull()
                            )
                        )
                    },
                    label = { Text("Qty") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                IngredientUnitDropdown(
                    selectedUnit = runCatching { IngredientUnit.valueOf(ingredient.unit) }.getOrDefault(IngredientUnit.UNIT),
                    onUnitSelected = { unit ->
                        onUpdate(
                            ingredient.copy(
                                unit = unit.name,
                                quantity = if (unit != IngredientUnit.TO_TASTE) {
                                    ingredient.quantity
                                } else {
                                    null
                                }
                            )
                        )
                    },
                    modifier = Modifier.weight(1.25f)
                )
            }
        } else {
            IngredientUnitDropdown(
                selectedUnit = runCatching { IngredientUnit.valueOf(ingredient.unit) }.getOrDefault(IngredientUnit.UNIT),
                onUnitSelected = { unit ->
                    onUpdate(
                        ingredient.copy(
                            unit = unit.name,
                            quantity = if (unit != IngredientUnit.TO_TASTE) {
                                ingredient.quantity
                            } else {
                                null
                            }
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientUnitDropdown(
    selectedUnit: IngredientUnit,
    onUnitSelected: (IngredientUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    var unitExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = unitExpanded,
        onExpandedChange = { unitExpanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedUnit.compactLabel(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Unit") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = unitExpanded,
            onDismissRequest = { unitExpanded = false }
        ) {
            IngredientUnit.entries.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.dropdownLabel()) },
                    onClick = {
                        onUnitSelected(unit)
                        unitExpanded = false
                    }
                )
            }
        }
    }
}

private fun IngredientUnit.compactLabel(): String {
    return when (this) {
        IngredientUnit.G -> "g"
        IngredientUnit.KG -> "kg"
        IngredientUnit.ML -> "ml"
        IngredientUnit.L -> "l"
        IngredientUnit.TSP -> "tsp"
        IngredientUnit.TBSP -> "tbsp"
        IngredientUnit.CUP -> "cup"
        IngredientUnit.UNIT -> "unit"
        IngredientUnit.PINCH -> "pinch"
        IngredientUnit.TO_TASTE -> "to taste"
    }
}

private fun IngredientUnit.dropdownLabel(): String {
    return compactLabel()
}


@Composable
private fun EditableStepsSection(
    steps: List<InstructionStep>,
    errorText: String?,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit,
    onUpdate: (InstructionStep) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Steps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
            }
        }

        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        steps.forEach { step ->
            StepRow(
                step = step,
                onRemove = { onRemove(step.id) },
                onUpdate = onUpdate
            )
        }

        if (steps.isEmpty()) {
            Text(
                text = "No steps yet. Tap Add to start.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StepRow(
    step: InstructionStep,
    onRemove: () -> Unit,
    onUpdate: (InstructionStep) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "${step.stepNumber}.",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )
        OutlinedTextField(
            value = step.text,
            onValueChange = { onUpdate(step.copy(text = it)) },
            label = { Text("Step ${step.stepNumber}") },
            modifier = Modifier.weight(1f),
            minLines = 2
        )
        IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.CenterVertically)) {
            Icon(Icons.Default.Delete, contentDescription = "Remove step", tint = MaterialTheme.colorScheme.error)
        }
    }
}
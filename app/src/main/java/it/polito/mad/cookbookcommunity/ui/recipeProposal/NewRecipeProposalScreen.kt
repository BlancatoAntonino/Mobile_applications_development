package it.polito.mad.cookbookcommunity.ui.recipeProposal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.polito.mad.cookbookcommunity.model.recipe.*
import it.polito.mad.cookbookcommunity.ui.components.recipeproposal.RecipeImageContent
import it.polito.mad.cookbookcommunity.ui.theme.CookBookCommunityTheme
import it.polito.mad.cookbookcommunity.viewmodel.*

@Composable
fun NewRecipeProposalScreen(
    uiState: NewRecipeUiState,
    onEvent: (NewRecipeEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBackOnFirstStep: Boolean = false,
    title: String = "New recipe",
    onPickImageFromGallery: () -> Unit = {},
    onTakeRecipePhoto: () -> Unit = {}
) {

    Scaffold(
        topBar = {
            NewRecipeTopBar(
                title = title,
                currentStep = uiState.currentStep,
                showBackOnFirstStep = showBackOnFirstStep,
                onBackClick = {
                    if (uiState.currentStep.isFirst)
                        onBackClick()
                    else
                        onEvent(NewRecipeEvent.PrevStep)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            StepProgressIndicator(
                currentStep = uiState.currentStep,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                when (uiState.currentStep) {
                    NewRecipeStep.BASIC_INFO   -> BasicInfoStep(
                        form = uiState.form,
                        errors = uiState.errors,
                        onEvent = onEvent,
                        onPickImageFromGallery = onPickImageFromGallery,
                        onTakeRecipePhoto = onTakeRecipePhoto
                    )
                    NewRecipeStep.INGREDIENTS  -> IngredientsStep(
                        ingredients = uiState.form.ingredients,
                        error = uiState.errors.ingredients,
                        onEvent = onEvent
                    )
                    NewRecipeStep.STEPS        -> StepsStep(
                        steps = uiState.form.steps,
                        error = uiState.errors.steps,
                        onEvent = onEvent
                    )
                    NewRecipeStep.PREVIEW      -> PreviewStep(
                        form = uiState.form,
                        isSaving = uiState.isSaving,
                        onEvent = onEvent
                    )
                }
            }

            BottomNavButton(
                step = uiState.currentStep,
                isSaving = uiState.isSaving,
                isPublishLocked = uiState.isPublishLocked,
                onEvent = onEvent
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewRecipeTopBar(
    title: String,
    currentStep: NewRecipeStep,
    showBackOnFirstStep: Boolean,
    onBackClick: () -> Unit
) {
    val shouldShowBack = !currentStep.isFirst || showBackOnFirstStep

    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (shouldShowBack) {
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

@Composable
private fun StepProgressIndicator(
    currentStep: NewRecipeStep,
    modifier: Modifier = Modifier
) {
    val steps = NewRecipeStep.entries
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        steps.forEach { step ->
            val fraction = when {
                step.index < currentStep.index  -> 1f
                step.index == currentStep.index -> 0.5f
                else -> 0f
            }
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun BottomNavButton(
    step: NewRecipeStep,
    isSaving: Boolean,
    isPublishLocked: Boolean,
    onEvent: (NewRecipeEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                if (step.isLast)
                    onEvent(NewRecipeEvent.Publish)
                else
                    onEvent(NewRecipeEvent.NextStep)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !isPublishLocked
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    if (step.isLast)
                        "Publish"
                    else
                        "Next"
                )
            }
        }
    }
}

@Composable
private fun BasicInfoStep(
    form: NewRecipeFormData,
    errors: NewRecipeErrors,
    onEvent: (NewRecipeEvent) -> Unit,
    onPickImageFromGallery: () -> Unit,
    onTakeRecipePhoto: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {

            val assetImages = listOf(
                "file:///android_asset/pasta.jpg",
                "file:///android_asset/pancakes.jpg",
                "file:///android_asset/curry.jpg",
                "file:///android_asset/lasagna.jpg",
                "file:///android_asset/greek_salad.jpg",
                "file:///android_asset/brownies.jpg",
                "file:///android_asset/hummus.jpg",
                "file:///android_asset/avocado_toast.jpg",
                "file:///android_asset/cookies.jpg",
                "file:///android_asset/crostata.jpg",
                "file:///android_asset/fish.jpg",
                "file:///android_asset/frittura.jpg",
                "file:///android_asset/pizza.jpg",
                "file:///android_asset/salame.jpg",
                "file:///android_asset/sourp.jpg",
                "file:///android_asset/tacos.jpg",
                "file:///android_asset/tiramisu.jpg"
            )

            val currentIndex = assetImages.indexOf(form.imageUri)
            val nextUri = assetImages[(currentIndex + 1) % assetImages.size]

            CoverImagePicker(
                imageUri = form.imageUri,
                onCycleAsset = { onEvent(NewRecipeEvent.ImageUriChanged(nextUri)) },
                onPickImageFromGallery = onPickImageFromGallery,
                onTakeRecipePhoto = onTakeRecipePhoto
            )
        }

        item {
            OutlinedTextField(
                value = form.title,
                onValueChange = { onEvent(NewRecipeEvent.TitleChanged(it)) },
                label = { Text("Recipe title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errors.title != null,
                supportingText = errors.title?.let { { Text(it) } }
            )
        }

        item {
            OutlinedTextField(
                value = form.description,
                onValueChange = { onEvent(NewRecipeEvent.DescriptionChanged(it)) },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        }

        if (form.originalRecipeId != null) {
            item {
                AdaptationEditorSection(
                    originalRecipeTitle = form.originalRecipeTitle,
                    originalRecipeImageUri = form.originalRecipeImageUri,
                    originalAuthorDisplayName = form.originalAuthorDisplayName,
                    adaptationNote = form.adaptationNote,
                    error = errors.adaptationNote,
                    onAdaptationNoteChanged = {
                        onEvent(NewRecipeEvent.AdaptationNoteChanged(it))
                    }
                )
            }
        }

        item {
            EnumDropdown(
                label = "Recipe type",
                selected = form.recipeType,
                options = RecipeType.entries,
                display = { it.name.replace('_', ' ').lowercase().replaceFirstChar { c -> c.uppercase() } },
                onSelected = { onEvent(NewRecipeEvent.RecipeTypeChanged(it)) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StepperField(
                    modifier = Modifier.weight(1f),
                    label = "Portions",
                    value = form.servings,
                    onDecrease = { if (form.servings > 1) onEvent(NewRecipeEvent.ServingsChanged(form.servings - 1)) },
                    onIncrease = { onEvent(NewRecipeEvent.ServingsChanged(form.servings + 1)) },
                    onValueChange = { raw -> raw.toIntOrNull()?.let { onEvent(NewRecipeEvent.ServingsChanged(it)) } },
                    errorText = errors.servings
                )
                StepperField(
                    modifier = Modifier.weight(1f),
                    label = "Time (min)",
                    value = form.cookTimeMinutes,
                    onDecrease = { if (form.cookTimeMinutes > 1) onEvent(NewRecipeEvent.CookTimeChanged(form.cookTimeMinutes - 5)) },
                    onIncrease = { onEvent(NewRecipeEvent.CookTimeChanged(form.cookTimeMinutes + 5)) },
                    onValueChange = { raw -> raw.toIntOrNull()?.let { onEvent(NewRecipeEvent.CookTimeChanged(it)) } },
                    errorText = errors.cookTimeMinutes
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
                            onEvent(NewRecipeEvent.CaloriesChanged(null))
                        }

                        normalized.all { it.isDigit() } -> {
                            normalized.toIntOrNull()?.let { value ->
                                onEvent(NewRecipeEvent.CaloriesChanged(value))
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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EnumDropdown(
                    modifier = Modifier.weight(1f),
                    label = "Difficulty",
                    selected = form.difficulty,
                    options = DifficultyLevel.entries,
                    display = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                    onSelected = { onEvent(NewRecipeEvent.DifficultyChanged(it)) }
                )
                EnumDropdown(
                    modifier = Modifier.weight(1f),
                    label = "Price",
                    selected = form.priceRange,
                    options = PriceRange.entries,
                    display = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                    onSelected = { onEvent(NewRecipeEvent.PriceRangeChanged(it)) }
                )
            }
        }

        item {
            NullableEnumDropdown(
                label = "Cuisine (optional)",
                selected = form.cuisineType,
                options = CuisineType.entries,
                display = { it.name.replace('_', ' ').lowercase().replaceFirstChar { c -> c.uppercase() } },
                onSelected = { onEvent(NewRecipeEvent.CuisineTypeChanged(it)) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            DietaryChipsSection(
                selected = form.dietaryRestrictions,
                onToggle = { onEvent(NewRecipeEvent.DietaryRestrictionToggled(it)) }
            )
        }
    }
}

@Composable
private fun AdaptationEditorSection(
    originalRecipeTitle: String,
    originalRecipeImageUri: String,
    originalAuthorDisplayName: String,
    adaptationNote: String,
    error: String?,
    onAdaptationNoteChanged: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Adapting a recipe",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        OriginalRecipeSummaryCard(
            originalRecipeTitle = originalRecipeTitle,
            originalRecipeImageUri = originalRecipeImageUri,
            originalAuthorDisplayName = originalAuthorDisplayName
        )

        OutlinedTextField(
            value = adaptationNote,
            onValueChange = onAdaptationNoteChanged,
            label = { Text("What did you change?") },
            placeholder = {
                Text("Example: I replaced diary ingredients with plant-based alternatives.")
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            isError = error != null,
            supportingText = {
                Text(
                    text = error ?: "${adaptationNote.trim().length}/220 characters",
                    color = if (error != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        )
    }
}

@Composable
private fun OriginalRecipeSummaryCard(
    originalRecipeTitle: String,
    originalRecipeImageUri: String,
    originalAuthorDisplayName: String
) {
    val displayTitle = originalRecipeTitle.ifBlank { "Original recipe" }
    val displayAuthor = originalAuthorDisplayName.ifBlank { "Original author" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .width(96.dp)
                    .height(64.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                RecipeImageContent(
                    imageUri = originalRecipeImageUri,
                    contentDescription = "Original recipe cover image",
                    modifier = Modifier.fillMaxSize(),
                    placeholderText = "Original recipe"
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Original recipe",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Text(
                    text = "by $displayAuthor",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun IngredientsStep(
    ingredients: List<IngredientItem>,
    error: String?,
    onEvent: (NewRecipeEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (error != null) {
                item {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            items(ingredients, key = { it.id }) { ingredient ->
                IngredientRow(
                    ingredient = ingredient,
                    onRemove = { onEvent(NewRecipeEvent.IngredientRemoved(ingredient.id)) },
                    onUpdate = { onEvent(NewRecipeEvent.IngredientUpdated(it)) }
                )
            }

            if (ingredients.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No ingredients yet.\nTap the button to add one.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Button(
            onClick = { onEvent(NewRecipeEvent.IngredientAdded) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("New ingredient")
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
    var unitExpanded by remember { mutableStateOf(false) }

    val quantityEnabled = ingredient.requiresQuantity

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = ingredient.name,
                    onValueChange = { onUpdate(ingredient.copy(name = it)) },
                    label = { Text("Ingredient name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = if (quantityEnabled) ingredient.quantity?.toString() ?: "" else "",
                    onValueChange = { raw ->
                        if (quantityEnabled) {
                            onUpdate(ingredient.copy(quantity = raw.toDoubleOrNull()))
                        }
                    },
                    enabled = quantityEnabled,
                    label = {
                        Text(if (quantityEnabled) "Qty" else "To taste")
                    },
                    placeholder = {
                        if (!quantityEnabled) {
                            Text("Not required")
                        }
                    },
                    modifier = Modifier.width(100.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = it },
                    modifier = Modifier.width(130.dp)
                ) {
                    OutlinedTextField(
                        value = ingredient.unit.lowercase(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        IngredientUnit.entries.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit.name.lowercase()) },
                                onClick = {
                                    val updatedIngredient = ingredient.copy(
                                        unit = unit.name,
                                        quantity = if (unit == IngredientUnit.TO_TASTE) null else ingredient.quantity
                                    )

                                    onUpdate(updatedIngredient)
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepsStep(
    steps: List<InstructionStep>,
    error: String?,
    onEvent: (NewRecipeEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (error != null) {
                item {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            items(steps, key = { it.id }) { step ->
                StepRow(
                    step = step,
                    onRemove = { onEvent(NewRecipeEvent.StepRemoved(step.id)) },
                    onUpdate = { onEvent(NewRecipeEvent.StepUpdated(it)) }
                )
            }

            if (steps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No steps yet.\nTap the button to add one.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Button(
            onClick = { onEvent(NewRecipeEvent.StepAdded) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("New Step")
        }
    }
}

@Composable
private fun StepRow(
    step: InstructionStep,
    onRemove: () -> Unit,
    onUpdate: (InstructionStep) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${step.stepNumber}.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
            OutlinedTextField(
                value = step.text,
                onValueChange = { onUpdate(step.copy(text = it)) },
                label = { Text("Step description") },
                modifier = Modifier.weight(1f),
                minLines = 2
            )
            IconButton(onClick = onRemove, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Remove step", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun PreviewStep(
    form: NewRecipeFormData,
    isSaving: Boolean,
    onEvent: (NewRecipeEvent) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = form.title.ifBlank { "Untitled" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { onEvent(NewRecipeEvent.GoToStep(NewRecipeStep.BASIC_INFO)) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit steps")
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                RecipeImageContent(
                    imageUri = form.imageUri,
                    contentDescription = "Recipe cover image",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { StatChip(Icons.Default.Timer, "${form.cookTimeMinutes} min") }
                item { StatChip(Icons.Default.SignalCellularAlt, form.difficulty.name.lowercase().replaceFirstChar { it.uppercase() }) }
                item { StatChip(Icons.Default.AttachMoney, form.priceRange.name.lowercase().replaceFirstChar { it.uppercase() }) }
                item { StatChip(Icons.Default.People, "${form.servings} portions") }

                if (form.calories != null) {
                    item { StatChip(Icons.Default.LocalFireDepartment, "${form.calories} kcal") }
                }
            }
        }

        if (form.description.isNotBlank()) {
            item {
                Text(form.description, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (form.originalRecipeId != null) {
            item {
                AdaptationPreviewSection(form = form)
            }
        }

        item {
            SectionHeader(
                title = "Ingredients list",
                onEditClick = { onEvent(NewRecipeEvent.GoToStep(NewRecipeStep.INGREDIENTS)) }
            )
            Spacer(Modifier.height(4.dp))
            if (form.ingredients.isEmpty()) {
                Text("No ingredients added.", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
            } else {
                form.ingredients.forEach { ing ->
                    val quantityText = when {
                        ing.unit == IngredientUnit.TO_TASTE.name -> " — to taste"
                        ing.quantity != null -> " — ${ing.quantity} ${ing.unit.lowercase()}"
                        else -> ""
                    }

                    Text(
                        "• ${ing.name}$quantityText",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = "Steps",
                onEditClick = { onEvent(NewRecipeEvent.GoToStep(NewRecipeStep.STEPS)) }
            )
            Spacer(Modifier.height(4.dp))
            if (form.steps.isEmpty()) {
                Text("No steps added.", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
            } else {
                form.steps.forEach { step ->
                    Text(
                        "${step.stepNumber}. ${step.text}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdaptationPreviewSection(
    form: NewRecipeFormData
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Adapted from",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        OriginalRecipeSummaryCard(
            originalRecipeTitle = form.originalRecipeTitle,
            originalRecipeImageUri = form.originalRecipeImageUri,
            originalAuthorDisplayName = form.originalAuthorDisplayName
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Your changes",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = form.adaptationNote.ifBlank {
                    "No adaptation note provided."
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun StatChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(label, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
private fun SectionHeader(title: String, onEditClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        IconButton(onClick = onEditClick) {
            Icon(Icons.Default.Edit, contentDescription = "Edit $title", modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoverImagePicker(
    imageUri: String,
    onCycleAsset: () -> Unit,
    onPickImageFromGallery: () -> Unit,
    onTakeRecipePhoto: () -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            RecipeImageContent(
                imageUri = imageUri,
                contentDescription = "Recipe cover image",
                modifier = Modifier.fillMaxSize(),
                placeholderText = "Tap the arrow to cycle through images"
            )

            IconButton(
                onClick = {  showSheet = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = MaterialTheme.shapes.small
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Change cover image",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ){
                Text(
                    text = "Change cover image",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 24.dp, top = 12.dp, end = 24.dp, bottom = 20.dp)
                )
                HorizontalDivider()
                listOf(
                    "Choose from preset images" to { showSheet = false; onCycleAsset() },
                    "Select from gallery"       to { showSheet = false; onPickImageFromGallery() },
                    "Take a photo"              to { showSheet = false; onTakeRecipePhoto() }
                ).forEach { (label, action) ->
                    TextButton(
                        onClick = action,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 18.dp)
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

    }
}

@Composable
private fun StepperField(
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
            color =
                if (errorText != null)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        OutlinedCard {
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
                    textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
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
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
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
                    onClick = { onSelected(option); expanded = false }
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
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
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
            DropdownMenuItem(text = { Text("None") }, onClick = { onSelected(null); expanded = false })
            options.forEach { option ->
                DropdownMenuItem(text = { Text(display(option)) }, onClick = { onSelected(option); expanded = false })
            }
        }
    }
}

@Composable
private fun DietaryChipsSection(
    selected: List<DietaryRestriction>,
    onToggle: (DietaryRestriction) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Dietary restrictions (optional)", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        val all = DietaryRestriction.entries
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                all.take(all.size / 2).forEach { r ->
                    FilterChip(
                        selected = r in selected,
                        onClick = { onToggle(r) },
                        label = { Text(r.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                all.drop(all.size / 2).forEach { r ->
                    FilterChip(
                        selected = r in selected,
                        onClick = { onToggle(r) },
                        label = { Text(r.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NewRecipeProposalScreenPreview() {
    CookBookCommunityTheme {
        NewRecipeProposalScreen(
            uiState = NewRecipeUiState(
                currentStep = NewRecipeStep.BASIC_INFO,
                form = NewRecipeFormData(
                    title = "Pasta al pomodoro",
                    servings = 2,
                    cookTimeMinutes = 20
                )
            ),
            onEvent = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AdaptRecipeProposalScreenPreview() {
    CookBookCommunityTheme {
        NewRecipeProposalScreen(
            uiState = NewRecipeUiState(
                currentStep = NewRecipeStep.BASIC_INFO,
                form = NewRecipeFormData(
                    title = "Whole wheat tomato pasta",
                    description = "A lighter version of a classic tomato pasta",
                    imageUri = "file:///android_asset/pasta.jpg",
                    servings = 2,
                    cookTimeMinutes = 20,
                    originalRecipeId = "original-pasta-id",
                    originalRecipeTitle = "Classic tomato pasta",
                    originalRecipeImageUri = "file:///android_asset/pasta.jpg",
                    originalAuthorId = "user-luca",
                    originalAuthorDisplayName = "Luca Bianchi",
                    adaptationNote = "I used whole wheat pasta and added fresh basil."
                )
            ),
            onEvent = {},
            onBackClick = {},
            showBackOnFirstStep = true,
            title = "Adapt recipe"
        )
    }
}
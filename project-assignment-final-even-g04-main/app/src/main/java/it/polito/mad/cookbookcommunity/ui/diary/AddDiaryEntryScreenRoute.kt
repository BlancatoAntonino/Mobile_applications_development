package it.polito.mad.cookbookcommunity.ui.diary

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.data.repository.TriedRecipeRepository
import it.polito.mad.cookbookcommunity.model.review.CookingResult
import it.polito.mad.cookbookcommunity.ui.components.recipeproposal.RecipeImageContent
import it.polito.mad.cookbookcommunity.viewmodel.DiaryEntryEditorUiState
import it.polito.mad.cookbookcommunity.viewmodel.DiaryEntryEditorViewModel
import it.polito.mad.cookbookcommunity.viewmodel.DiaryEntryEditorViewModelFactory

@Composable
fun AddDiaryEntryScreenRoute(
    recipeId: String,
    entryId: String?,
    recipeRepository: RecipeRepository,
    triedRecipeRepository: TriedRecipeRepository,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    onFeedback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: DiaryEntryEditorViewModel = viewModel(
        key = "diary-editor-$recipeId-${entryId.orEmpty()}",
        factory = DiaryEntryEditorViewModelFactory(
            recipeId = recipeId,
            entryId = entryId,
            recipeRepository = recipeRepository,
            triedRecipeRepository = triedRecipeRepository
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onFeedback("Diary entry saved.")
            onSaved()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            onFeedback(message)
            viewModel.errorShown()
        }
    }

    AddDiaryEntryScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onResultChange = viewModel::updateResult,
        onModificationsChange = viewModel::updateModifications,
        onPersonalNoteChange = viewModel::updatePersonalNote,
        onWouldCookAgainChange = viewModel::updateWouldCookAgain,
        onSelectPhotoClick = viewModel::selectNextPhotoAsset,
        onClearPhotoClick = viewModel::clearFinalPhoto,
        onSaveClick = viewModel::saveDiaryEntry,
        modifier = modifier
    )
}

@Composable
private fun AddDiaryEntryScreen(
    uiState: DiaryEntryEditorUiState,
    onBackClick: () -> Unit,
    onResultChange: (CookingResult) -> Unit,
    onModificationsChange: (String) -> Unit,
    onPersonalNoteChange: (String) -> Unit,
    onWouldCookAgainChange: (Boolean) -> Unit,
    onSelectPhotoClick: () -> Unit,
    onClearPhotoClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            AddDiaryHeader(
                title = uiState.screenTitle,
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            DiarySaveBar(
                canSave = uiState.canSave,
                isSaving = uiState.isSaving,
                isEditing = uiState.isEditing,
                onSaveClick = onSaveClick
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            RecipeDiaryBanner(uiState = uiState)

            CookedDateSection(cookedAt = uiState.cookedAt)

            ResultSection(
                selectedResult = uiState.result,
                onResultChange = onResultChange
            )

            DiaryTextArea(
                title = "Adaptations",
                value = uiState.modifications,
                placeholder = "What did you change?",
                onValueChange = onModificationsChange
            )

            DiaryTextArea(
                title = "Personal note",
                value = uiState.personalNote,
                placeholder = "How did it go?",
                onValueChange = onPersonalNoteChange
            )

            FinalPhotoSection(
                photoUri = uiState.finalPhotoUri,
                onSelectPhotoClick = onSelectPhotoClick,
                onClearPhotoClick = onClearPhotoClick
            )

            WouldCookAgainSection(
                value = uiState.wouldCookAgain,
                onValueChange = onWouldCookAgainChange
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun AddDiaryHeader(
    title: String,
    onBackClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RecipeDiaryBanner(
    uiState: DiaryEntryEditorUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(76.dp)
            ) {
                RecipeImageContent(
                    imageUri = uiState.recipeImageUri,
                    contentDescription = uiState.recipeTitle,
                    placeholderText = "Recipe",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (uiState.isEditing) {
                        "EDITING DIARY NOTE"
                    } else {
                        "ADDING A DIARY NOTE"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = uiState.recipeTitle.ifBlank { "Recipe" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!uiState.recipeExists) {
                    Text(
                        text = "Original recipe unavailable",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun CookedDateSection(
    cookedAt: Long
) {
    val relativeDate = remember(cookedAt) {
        DateUtils.getRelativeTimeSpanString(
            cookedAt,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Cooked date",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = relativeDate,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ResultSection(
    selectedResult: CookingResult,
    onResultChange: (CookingResult) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Result",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CookingResult.entries.forEach { result ->
                ResultOption(
                    result = result,
                    selected = result == selectedResult,
                    onClick = {
                        onResultChange(result)
                    }
                )
            }
        }
    }
}

@Composable
private fun ResultOption(
    result: CookingResult,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = result.displayName(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun DiaryTextArea(
    title: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(placeholder)
            },
            minLines = 4,
            maxLines = 7
        )
    }
}

@Composable
private fun FinalPhotoSection(
    photoUri: String,
    onSelectPhotoClick: () -> Unit,
    onClearPhotoClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Final photo",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        ) {
            RecipeImageContent(
                imageUri = photoUri,
                contentDescription = "Final cooked result",
                placeholderText = "No final photo selected",
                modifier = Modifier.fillMaxSize()
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledTonalButton(
                onClick = onSelectPhotoClick
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = if (photoUri.isBlank()) {
                        "Choose photo"
                    } else {
                        "Change photo"
                    }
                )
            }

            if (photoUri.isNotBlank()) {
                TextButton(
                    onClick = onClearPhotoClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Remove")
                }
            }
        }
    }
}

@Composable
private fun WouldCookAgainSection(
    value: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Would cook again",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = if (value) {
                    "I would make this recipe again."
                } else {
                    "I would not make this recipe again."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = value,
            onCheckedChange = onValueChange
        )
    }
}

@Composable
private fun DiarySaveBar(
    canSave: Boolean,
    isSaving: Boolean,
    isEditing: Boolean,
    onSaveClick: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                enabled = canSave,
                onClick = onSaveClick
            ) {
                Text(
                    text = when {
                        isSaving -> "Saving..."
                        isEditing -> "Save changes"
                        else -> "Save entry"
                    }
                )
            }
        }
    }
}

private fun CookingResult.displayName(): String {
    return when (this) {
        CookingResult.EXCELLENT -> "Excellent"
        CookingResult.GOOD -> "Good"
        CookingResult.OK -> "Ok"
        CookingResult.FAILED -> "Failed"
    }
}
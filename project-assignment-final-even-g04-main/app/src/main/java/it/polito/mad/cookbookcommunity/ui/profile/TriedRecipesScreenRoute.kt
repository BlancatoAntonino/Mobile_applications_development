package it.polito.mad.cookbookcommunity.ui.profile

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.data.repository.TriedRecipeRepository
import it.polito.mad.cookbookcommunity.model.review.CookingResult
import it.polito.mad.cookbookcommunity.ui.components.recipeproposal.RecipeImageContent
import it.polito.mad.cookbookcommunity.viewmodel.DiaryListItem
import it.polito.mad.cookbookcommunity.viewmodel.DiaryUiEvent
import it.polito.mad.cookbookcommunity.viewmodel.DiaryUiState
import it.polito.mad.cookbookcommunity.viewmodel.DiaryViewModel
import it.polito.mad.cookbookcommunity.viewmodel.DiaryViewModelFactory

@Composable
fun TriedRecipesScreenRoute(
    userId: String,
    triedRecipeRepository: TriedRecipeRepository,
    recipeRepository: RecipeRepository,
    onBackClick: () -> Unit,
    onDiaryEntryClick: (recipeId: String, entryId: String) -> Unit,
    onFeedback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: DiaryViewModel = viewModel(
        key = "diary-$userId",
        factory = DiaryViewModelFactory(
            userId = userId,
            triedRecipeRepository = triedRecipeRepository,
            recipeRepository = recipeRepository
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.feedbackMessage) {
        val message = uiState.feedbackMessage ?: return@LaunchedEffect

        onFeedback(message)
        viewModel.onEvent(DiaryUiEvent.FeedbackConsumed)
    }

    LaunchedEffect(uiState.entryIdToOpen, uiState.entries) {
        val entryId = uiState.entryIdToOpen ?: return@LaunchedEffect
        val entry = uiState.entries.firstOrNull { it.entryId == entryId }
            ?: return@LaunchedEffect

        onDiaryEntryClick(entry.recipeId, entry.entryId)
        viewModel.onEvent(DiaryUiEvent.DiaryEntryOpenConsumed)
    }

    DiaryScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@Composable
private fun DiaryScreen(
    uiState: DiaryUiState,
    onEvent: (DiaryUiEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            DiaryHeader(onBackClick = onBackClick)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.errorMessage != null && uiState.entries.isEmpty() -> {
                    DiaryMessageState(
                        title = "Unable to load diary",
                        message = uiState.errorMessage
                            ?: "Please try again in a moment.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.entries.isEmpty() -> {
                    DiaryMessageState(
                        title = "No diary entries yet",
                        message = "Add a note after cooking a recipe.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = uiState.entries,
                            key = { it.entryId }
                        ) { item ->
                            DiaryEntryCard(
                                item = item,
                                onClick = {
                                    onEvent(
                                        DiaryUiEvent.DiaryEntryClicked(
                                            item.entryId
                                        )
                                    )
                                },
                                onDeleteClick = {
                                    onEvent(
                                        DiaryUiEvent.DeleteDiaryEntryRequested(
                                            item.entryId
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }

            if (uiState.entryIdToDelete != null) {
                DeleteDiaryEntryDialog(
                    isDeleting = uiState.isDeleting,
                    onConfirm = {
                        onEvent(DiaryUiEvent.DeleteDiaryEntryConfirmed)
                    },
                    onDismiss = {
                        onEvent(DiaryUiEvent.DeleteDiaryEntryDismissed)
                    }
                )
            }
        }
    }
}

@Composable
private fun DiaryHeader(
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
            text = "Diary",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DiaryMessageState(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DiaryEntryCard(
    item: DiaryListItem,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val relativeCookedAt = remember(item.cookedAt) {
        DateUtils.getRelativeTimeSpanString(
            item.cookedAt,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                RecipeImageContent(
                    imageUri = item.imageUri,
                    contentDescription = item.recipeTitle,
                    placeholderText = "Diary photo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.recipeTitle.ifBlank { "Untitled recipe" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "Cooked $relativeCookedAt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete diary entry",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                FlowRow (
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DiaryChip(label = item.result.displayName())

                    DiaryChip(
                        label = if (item.wouldCookAgain) {
                            "Would cook again"
                        } else {
                            "Would not cook again"
                        }
                    )

                    if (item.hasFinalPhoto) {
                        DiaryChip(label = "Final photo")
                    }
                }

                if (item.hasFinalPhoto) {
                    DiaryChip(label = "Final photo")
                }

                DiaryPreviewSection(
                    title = "Personal note",
                    text = item.personalNotePreview
                )

                DiaryPreviewSection(
                    title = "Adaptions",
                    text = item.modificationsPreview
                )
            }
        }
    }
}

@Composable
private fun DiaryPreviewSection(
    title: String,
    text: String
) {
    if (text.isBlank()) return

    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DiaryChip(
    label: String
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DeleteDiaryEntryDialog(
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!isDeleting) {
                onDismiss()
            }
        },
        title = {
            Text("Delete diary entry")
        },
        text = {
            Text(
                "This will permanently remove your personal note, adaptations and final photo."
            )
        },
        confirmButton = {
            TextButton(
                enabled = !isDeleting,
                onClick = onConfirm
            ) {
                Text(
                    text = if (isDeleting) "Deleting..." else "Delete",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isDeleting,
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

private fun CookingResult.displayName(): String {
    return when (this) {
        CookingResult.EXCELLENT -> "Excellent"
        CookingResult.GOOD -> "Good"
        CookingResult.OK -> "Ok"
        CookingResult.FAILED -> "Failed"
    }
}
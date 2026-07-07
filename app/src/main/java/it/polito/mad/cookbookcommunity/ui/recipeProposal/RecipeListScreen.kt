package it.polito.mad.cookbookcommunity.ui.recipeProposal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.polito.mad.cookbookcommunity.model.recipe.DifficultyLevel
import it.polito.mad.cookbookcommunity.model.recipe.PriceRange
import it.polito.mad.cookbookcommunity.model.recipe.RecipeProposal
import it.polito.mad.cookbookcommunity.model.recipe.RecipeType
import it.polito.mad.cookbookcommunity.ui.theme.CookBookCommunityTheme
import it.polito.mad.cookbookcommunity.viewmodel.RecipeListEvent
import it.polito.mad.cookbookcommunity.viewmodel.RecipeListUiState
import androidx.compose.material.icons.automirrored.filled.CallSplit
import it.polito.mad.cookbookcommunity.ui.components.recipeproposal.RecipeImageContent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun RecipeListScreen(
    uiState: RecipeListUiState,
    onEvent: (RecipeListEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            RecipeListHeader(
                recipeCount = uiState.recipes.size,
                ownerDisplayName = uiState.ownerDisplayName,
                onBackClick = onBackClick
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )

                            TextButton(
                                onClick = {
                                    onEvent(RecipeListEvent.DismissErrorMessage)
                                }
                            ) {
                                Text("Dismiss")
                            }
                        }
                    }
                }

                uiState.recipes.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No published recipes yet.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.recipes, key = { it.id }) { recipe ->
                            MyRecipeCard(
                                recipe = recipe,
                                isOwnerView = uiState.isOwnerView,
                                onClick = { onEvent(RecipeListEvent.RecipeClicked(recipe.id)) },
                                onDeleteClick = { onEvent(RecipeListEvent.DeleteRequested(recipe.id)) }
                            )
                        }
                    }
                }


            }

            if (uiState.deleteConfirmId != null) {
                DeleteConfirmDialog(
                    onConfirm = { onEvent(RecipeListEvent.DeleteConfirmed) },
                    onDismiss = { onEvent(RecipeListEvent.DeleteDismissed) }
                )
            }
        }
    }
}

@Composable
private fun RecipeListHeader(
    recipeCount: Int,
    ownerDisplayName: String,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "$ownerDisplayName Recipes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "$recipeCount Published ${if (recipeCount == 1) "Recipe" else "Recipes"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
    }
}

@Composable
private fun MyRecipeCard(
    recipe: RecipeProposal,
    isOwnerView: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {


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
                RecipeCardImage(imageUri = recipe.imageUri)
                RatingBadge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recipe.title.ifBlank { "Untitled" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Pub: ${formatDate(recipe.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (isOwnerView) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete recipe",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeCardImage(imageUri: String) {
    RecipeImageContent(
        imageUri = imageUri,
        contentDescription = null,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun RatingBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "—",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete recipe") },
        text = { Text("Are you sure you want to delete this recipe? This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


private fun formatDate(timestamp: Long): String {
    val formatter = DateTimeFormatter
        .ofPattern("dd/MM/yyyy", Locale.getDefault())
        .withZone(ZoneId.systemDefault())

    return formatter.format(Instant.ofEpochMilli(timestamp))
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RecipeListScreenOwnerPreview() {
    CookBookCommunityTheme {
        Surface {
            RecipeListScreen(
                uiState = RecipeListUiState(
                    isLoading = false,
                    isOwnerView = true,
                    ownerDisplayName = "My",
                    recipes = listOf(
                        RecipeProposal(
                            title = "Spaghetti al pomodoro",
                            imageUri = "file:///android_asset/pasta.jpg",
                            priceRange = PriceRange.LOW.name,
                            difficulty = DifficultyLevel.EASY.name,
                            cookTimeMinutes = 20,
                            recipeType = RecipeType.MAIN_COURSE.name
                        ),
                        RecipeProposal(
                            title = "Tiramisù",
                            imageUri = "",
                            priceRange = PriceRange.MEDIUM.name,
                            difficulty = DifficultyLevel.MEDIUM.name,
                            cookTimeMinutes = 45,
                            recipeType = RecipeType.DESSERT.name
                        )
                    )
                ),
                onEvent = {},
                onBackClick = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RecipeListScreenVisitorPreview() {
    CookBookCommunityTheme {
        Surface {
            RecipeListScreen(
                uiState = RecipeListUiState(
                    isLoading = false,
                    isOwnerView = false,
                    ownerDisplayName = "Marco's",
                    recipes = listOf(
                        RecipeProposal(
                            title = "Spaghetti al pomodoro",
                            imageUri = "file:///android_asset/pasta.jpg",
                            priceRange = PriceRange.LOW.name,
                            difficulty = DifficultyLevel.EASY.name,
                            cookTimeMinutes = 20,
                            recipeType = RecipeType.MAIN_COURSE.name
                        )
                    )
                ),
                onEvent = {},
                onBackClick = {}
            )
        }
    }
}

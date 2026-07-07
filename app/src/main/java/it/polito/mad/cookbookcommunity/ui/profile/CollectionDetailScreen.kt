package it.polito.mad.cookbookcommunity.ui.profile

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.polito.mad.cookbookcommunity.model.recipe.RecipeProposal
import it.polito.mad.cookbookcommunity.ui.components.recipeproposal.RecipeImageContent
import it.polito.mad.cookbookcommunity.viewmodel.CollectionDetailUiEvent
import it.polito.mad.cookbookcommunity.viewmodel.CollectionDetailUiState

@Composable
fun CollectionDetailScreen(
    collectionName: String,
    uiState: CollectionDetailUiState,
    onEvent: (CollectionDetailUiEvent)-> Unit,
    onBackClick: ()-> Unit,
    modifier: Modifier=Modifier,
){
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ){
        Column(modifier = Modifier.fillMaxSize()) {
            CollectionDetailHeader(
                collectionName=collectionName,
                onBackClick= onBackClick
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

                uiState.recipes.isEmpty()->{
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recipes saved in this collection",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else ->{
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.recipes, key = { it.id }) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                onClick = { onEvent(CollectionDetailUiEvent.RecipeClicked(recipe.id)) },
                                onRemoveClick = { onEvent(CollectionDetailUiEvent.DeleteRequested(recipe.id)) }
                            )
                        }
                    }
                }
            }
        }
        if (uiState.recipeIdToDelete != null) {
            DeleteConfirmDialog(
                onConfirm = { onEvent(CollectionDetailUiEvent.DeleteConfirmed) },
                onDismiss = { onEvent(CollectionDetailUiEvent.DeleteDismissed) }
            )
        }
    }

}

@Composable
private fun CollectionDetailHeader(
    collectionName: String,
    onBackClick: () -> Unit
){
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
        Text(
            text = collectionName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RecipeCard(
    recipe: RecipeProposal,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit
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
                RecipeImageContent(
                    imageUri = recipe.imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
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
                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove from collection",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
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
        text = { Text("Are you sure you want to delete this recipe from the collection? This action cannot be undone.") },
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

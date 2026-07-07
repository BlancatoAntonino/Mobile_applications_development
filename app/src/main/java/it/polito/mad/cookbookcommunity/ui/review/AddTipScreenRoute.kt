package it.polito.mad.cookbookcommunity.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.data.repository.TipRepository
import it.polito.mad.cookbookcommunity.viewmodel.AddTipUiState
import it.polito.mad.cookbookcommunity.viewmodel.AddTipViewModel

@Composable
fun AddTipScreenRoute(
    recipeId: String,
    recipeRepository: RecipeRepository,
    tipRepository: TipRepository,
    onBackClick: () -> Unit,
    onPublished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: AddTipViewModel = viewModel(
        key = "add_tip_$recipeId",
        factory = viewModelFactory {
            initializer {
                AddTipViewModel(
                    recipeId = recipeId,
                    recipeRepository = recipeRepository,
                    tipRepository = tipRepository
                )
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isPublished) {
        if (uiState.isPublished) {
            onPublished()
        }
    }

    AddTipScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onTipTextChange = viewModel::updateTipText,
        onPublishClick = viewModel::publishTip,
        modifier = modifier
    )
}

@Composable
private fun AddTipScreen(
    uiState: AddTipUiState,
    onBackClick: () -> Unit,
    onTipTextChange: (String) -> Unit,
    onPublishClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }

            item {
                TipRecipeBanner(
                    recipeTitle = uiState.recipeTitle,
                    recipeImageUri = uiState.recipeImageUri,
                    isLoadingRecipe = uiState.isLoadingRecipe
                )
            }

            item {
                TipTextSection(
                    tipText = uiState.tipText,
                    onTipTextChange = onTipTextChange,
                    enabled = uiState.canAddTip
                )
            }

            uiState.tipBlockedMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            uiState.errorMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onPublishClick,
                        enabled = uiState.canPublish
                    ) {
                        Text(if (uiState.isPublishing) "Publishing..." else "Publish")
                    }
                }
            }
        }
    }
}

@Composable
private fun TipRecipeBanner(
    recipeTitle: String,
    recipeImageUri: String,
    isLoadingRecipe: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AsyncImage(
                model = recipeImageUri,
                contentDescription = "Recipe for tip",
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "ADDING A TIP",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = if (isLoadingRecipe) "Loading recipe..." else recipeTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TipTextSection(
    tipText: String,
    onTipTextChange: (String) -> Unit,
    enabled: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Write your tip",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = tipText,
            onValueChange = onTipTextChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            placeholder = {
                Text("DO, DON'Ts, suggested variations...")
            },
            minLines = 4,
        )
    }
}
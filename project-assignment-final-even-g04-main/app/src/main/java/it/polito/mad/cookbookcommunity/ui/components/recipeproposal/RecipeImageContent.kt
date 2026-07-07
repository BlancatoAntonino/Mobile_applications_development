package it.polito.mad.cookbookcommunity.ui.components.recipeproposal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import it.polito.mad.cookbookcommunity.ui.profile.SharedProfileImage

@Composable
fun RecipeImageContent(
    imageUri: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholderText: String = "Recipe cover"
) {
    val placeholder: @Composable () -> Unit = {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = placeholderText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (imageUri.isBlank()) {
        placeholder()
    } else {
        SharedProfileImage(
            value = imageUri,
            contentDescription = contentDescription ?: "",
            contentScale = ContentScale.Crop,
            modifier = modifier,
            fallback = placeholder
        )
    }
}
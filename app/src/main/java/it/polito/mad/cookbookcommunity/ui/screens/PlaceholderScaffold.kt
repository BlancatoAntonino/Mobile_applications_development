package it.polito.mad.cookbookcommunity.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlaceholderScaffold(
    title: String,
    onBackClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 32.dp,
                bottom = 24.dp
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (onBackClick != null) {
            Button(onClick = onBackClick) {
                Text("Back")
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall
        )

        content()
    }
}
package it.polito.mad.cookbookcommunity.ui.authentication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AuthEntryPreferencesScreen(
    onCreationSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val preferences = remember { mutableStateListOf<String>() }

    val availablePreferences = listOf(
        "Italian", "Asian", "Vegan", "Vegetarian",
        "Gluten-free", "Quick meals", "Desserts", "BBQ"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Authentication procedure: preferences",
                    style = MaterialTheme.typography.titleMedium
                )},
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 17.dp, end = 32.dp, top = 32.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choose your\npreferences",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(32.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availablePreferences.forEach { pref ->
                    val selected = preferences.contains(pref)
                    FilterChip(
                        selected = selected,
                        onClick = {
                            if (selected) preferences.remove(pref)
                            else preferences.add(pref)
                        },
                        label = { Text(pref) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedButton(
                onClick = onCreationSuccess,
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "Skip for now",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onCreationSuccess,
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "Continue",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
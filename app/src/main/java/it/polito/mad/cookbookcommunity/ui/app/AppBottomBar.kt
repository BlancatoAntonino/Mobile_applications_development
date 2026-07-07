package it.polito.mad.cookbookcommunity.ui.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AppBottomBar(
    isHomeSelected: Boolean,
    isExploreSelected: Boolean,
    isPublishSelected: Boolean,
    isSavedSelected: Boolean,
    isProfileSelected: Boolean,
    onHomeClick: () -> Unit,
    onExploreClick: () -> Unit,
    onPublishClick: () -> Unit,
    onSavedClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = isHomeSelected,
            onClick = onHomeClick,
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = isExploreSelected,
            onClick = onExploreClick,
            icon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Explore"
                )
            },
            label = { Text("Explore") }
        )

        NavigationBarItem(
            selected = isPublishSelected,
            onClick = onPublishClick,
            icon = {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create Recipe"
                )
            },
            label = { Text("Publish") }
        )

        NavigationBarItem(
            selected = isSavedSelected,
            onClick = onSavedClick,
            icon = {
                Icon(
                    Icons.Default.Bookmark,
                    contentDescription = "My Collections"
                )
            },
            label = { Text("Saved") }
        )

        NavigationBarItem(
            selected = isProfileSelected,
            onClick = onProfileClick,
            icon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") }
        )
    }
}
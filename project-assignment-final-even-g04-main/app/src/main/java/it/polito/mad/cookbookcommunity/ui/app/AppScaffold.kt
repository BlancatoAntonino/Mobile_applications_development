package it.polito.mad.cookbookcommunity.ui.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppScaffold(
    snackbarHostState: SnackbarHostState,
    showBottomBar: Boolean,
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
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    isHomeSelected = isHomeSelected,
                    isExploreSelected = isExploreSelected,
                    isPublishSelected = isPublishSelected,
                    isSavedSelected = isSavedSelected,
                    isProfileSelected = isProfileSelected,
                    onHomeClick = onHomeClick,
                    onExploreClick = onExploreClick,
                    onPublishClick = onPublishClick,
                    onSavedClick = onSavedClick,
                    onProfileClick = onProfileClick
                )
            }
        },
        content = content
    )
}
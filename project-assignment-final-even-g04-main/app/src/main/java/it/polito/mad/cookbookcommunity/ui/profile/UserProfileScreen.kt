package it.polito.mad.cookbookcommunity.ui.profile

import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.polito.mad.cookbookcommunity.model.profile.UserProfile
import it.polito.mad.cookbookcommunity.ui.profile.components.Bio
import it.polito.mad.cookbookcommunity.ui.profile.components.Collections
import it.polito.mad.cookbookcommunity.ui.profile.components.Preferences
import it.polito.mad.cookbookcommunity.ui.profile.components.ProfileImage
import it.polito.mad.cookbookcommunity.ui.profile.components.ProfileTopBar
import it.polito.mad.cookbookcommunity.ui.profile.components.SocialRow
import it.polito.mad.cookbookcommunity.ui.profile.components.UserDetails
import it.polito.mad.cookbookcommunity.viewmodel.ProfileUiEvent
import it.polito.mad.cookbookcommunity.viewmodel.ProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    uiState: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    onMyRecipesClick: () -> Unit = {},
    onTriedRecipesClick: () -> Unit = {},
    onSavedClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    nestedScrollInteropConnection: NestedScrollConnection = rememberNestedScrollInteropConnection(),
) {
    val profile = uiState.viewedProfile

    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.errorMessage != null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        profile == null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Profile not available.")
            }
        }

        else -> {
            UserProfileScaffold(
                profile = profile,
                isOwner = uiState.canEdit,
                onEditRequested = {
                    onEvent(ProfileUiEvent.EditRequested)
                },
                onBackClick = onBackClick,
                onMyRecipesClick = onMyRecipesClick,
                onTriedRecipesClick = onTriedRecipesClick,
                nestedScrollInteropConnection = nestedScrollInteropConnection,
                onSavedClick = onSavedClick,
                modifier = modifier,
                onSignOutClick = onSignOutClick
            )
        }
    }
}

@Composable
private fun UserProfileScaffold(
    profile: UserProfile,
    isOwner: Boolean,
    onEditRequested: () -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onMyRecipesClick: () -> Unit = {},
    onTriedRecipesClick: () -> Unit = {},
    onSavedClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    nestedScrollInteropConnection: NestedScrollConnection = rememberNestedScrollInteropConnection()
) {
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollInteropConnection)
    ) {
        val screenHeight = maxHeight

        Scaffold(
            topBar = {
                ProfileTopBar(
                    isOwner = isOwner,
                    onEditRequested = onEditRequested,
                    onSignOutClick = onSignOutClick,
                    onBackClick = onBackClick
                )
            }
        ) { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (isLandscape) {
                    LandscapeProfileLayout(
                        profile = profile,
                        containerHeight = screenHeight,
                        scrollState = scrollState,
                        isOwner = isOwner,
                        onMyRecipesClick = onMyRecipesClick,
                        onTriedRecipesClick = onTriedRecipesClick,
                        onSavedClick = onSavedClick
                    )
                } else {
                    PortraitProfileLayout(
                        profile = profile,
                        containerHeight = screenHeight,
                        scrollState = scrollState,
                        isOwner = isOwner,
                        onMyRecipesClick = onMyRecipesClick,
                        onTriedRecipesClick = onTriedRecipesClick,
                        onSavedClick = onSavedClick
                    )
                }
            }
        }
    }
}

@Composable
private fun PortraitProfileLayout(
    profile: UserProfile,
    containerHeight: Dp,
    scrollState: ScrollState,
    onSavedClick: () -> Unit = {},
    isOwner: Boolean,
    onMyRecipesClick: () -> Unit = {},
    onTriedRecipesClick: () -> Unit = {},
    onSavedCollectionsClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(containerHeight * 0.33f),
            contentAlignment = Alignment.Center
        ) {
            ProfileImage(profile)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            UserDetails(profile)
            Bio(profile, Modifier.fillMaxWidth())

            profile.socialLinks.forEach { social ->
                SocialRow(social)
            }

            Preferences(
                userData = profile,
                modifier = Modifier.padding(top = 16.dp)
            )

            HorizontalDivider(
                thickness = 3.dp,
                modifier = Modifier.padding(vertical = 20.dp)
            )

            Collections(
                isOwner = isOwner,
                onMyRecipesClick = onMyRecipesClick,
                onTriedRecipesClick = onTriedRecipesClick,
                onSavedCollectionsClick = onSavedClick
            )
        }
    }
}

@Composable
private fun LandscapeProfileLayout(
    profile: UserProfile,
    containerHeight: Dp,
    scrollState: ScrollState,
    isOwner: Boolean,
    onMyRecipesClick: () -> Unit = {},
    onTriedRecipesClick: () -> Unit = {},
    onSavedClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                ProfileImage(profile)
            }

            Column(
                modifier = Modifier
                    .weight(2f)
                    .padding(start = 16.dp)
            ) {
                UserDetails(profile)
                Bio(profile, Modifier.fillMaxWidth())
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Preferences(profile)

        HorizontalDivider(
            thickness = 3.dp,
            modifier = Modifier.padding(vertical = 20.dp)
        )

        Collections(
            isOwner = isOwner,
            onMyRecipesClick = onMyRecipesClick,
            onTriedRecipesClick = onTriedRecipesClick,
            onSavedCollectionsClick = onSavedClick
        )
    }
}

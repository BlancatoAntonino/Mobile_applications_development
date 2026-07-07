package it.polito.mad.cookbookcommunity.ui.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.polito.mad.cookbookcommunity.model.profile.ProfileImageType
import it.polito.mad.cookbookcommunity.model.profile.SocialLink
import it.polito.mad.cookbookcommunity.model.profile.UserProfile
import it.polito.mad.cookbookcommunity.model.profile.label
import it.polito.mad.cookbookcommunity.ui.profile.SharedProfileImage
import it.polito.mad.cookbookcommunity.ui.profile.profileAvatarDrawableResIdOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar(
    isOwner: Boolean,
    onEditRequested: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("") },
        navigationIcon = {
            if (!isOwner) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            if (isOwner) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Profile actions"
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit profile") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onEditRequested()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Sign out") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onSignOutClick()
                            }
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun UserDetails(userData: UserProfile) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = userData.fullname,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "@${userData.nickname}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = "Cooking Role: ${userData.cookingRole.label}",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ProfileImage(userData: UserProfile, modifier: Modifier = Modifier) {
    val commonModifier = modifier
        .sizeIn(minWidth = 100.dp, maxWidth = 160.dp)
        .aspectRatio(1f)
        .clip(CircleShape)

    val image = userData.profileImage

    when (image.type) {
        ProfileImageType.MONOGRAM -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = commonModifier.background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = userData.initials,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        ProfileImageType.PRESET_AVATAR -> {
            val avatarResId = profileAvatarDrawableResIdOrNull(image.value)

            Box(
                modifier = commonModifier.background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (avatarResId != null) {
                    Image(
                        painter = painterResource(id = avatarResId),
                        contentDescription = "Preset profile avatar",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Preset profile avatar fallback",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        ProfileImageType.LOCAL_URI -> {
            SharedProfileImage(
                value = image.value,
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = commonModifier,
                fallback = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = commonModifier.background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            text = userData.initials,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun Bio(userData: UserProfile, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = userData.bio ?: "No bio available",
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun SocialRow(social: SocialLink) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "${social.platform.label.uppercase()}: ",
            fontWeight = FontWeight.Bold
        )
        Text(text = social.value)
    }
}

@Composable
fun Preferences(userData: UserProfile, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        PreferenceSection(
            title = "Cuisine Types",
            items = userData.preferences.favoriteCuisines.map { it.label }
        )

        Spacer(modifier = Modifier.height(8.dp))

        PreferenceSection(
            title = "Dietary Restrictions",
            items = userData.preferences.dietaryRestrictions.map { it.label }
        )

        Spacer(modifier = Modifier.height(8.dp))

        PreferenceSection(
            title = "Favorite Ingredients",
            items = userData.preferences.favoriteIngredients
        )

        Spacer(modifier = Modifier.height(8.dp))

        PreferenceSection(
            title = "Allergies",
            items = userData.preferences.allergies
        )
    }
}

@Composable
private fun PreferenceSection(
    title: String,
    items: List<String>
) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleSmall
    )

    LazyRow(modifier = Modifier.padding(vertical = 4.dp)) {
        items(items) { tag ->
            Card(
                shape = RoundedCornerShape(50),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = tag,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun Collections(
    isOwner: Boolean,
    onMyRecipesClick: () -> Unit = {},
    onTriedRecipesClick: () -> Unit = {},
    onSavedCollectionsClick: () -> Unit = {},
    onMyReviewsClick: () -> Unit = {}
) {
    if (isOwner) {
        Text(
            text = "Personal hub",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        MyAndTriedRecipes(
            onMyRecipesClick = onMyRecipesClick,
            onTriedRecipesClick = onTriedRecipesClick
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )


        OverviewCollections(
            onSavedCollectionsClick = onSavedCollectionsClick,
            onTriedRecipesClick = onTriedRecipesClick,
            onPublishedRecipesClick = onMyRecipesClick
        )
    } else {
        Text(
            text = "Collections",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        CollectionCard(
            title = "Published Recipes",
            backgroundAssetPath = "img_published.jpg",
            onClick = onMyRecipesClick,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun MyAndTriedRecipes(
    onMyRecipesClick: () -> Unit = {},
    onTriedRecipesClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CollectionCard(
            title = "My recipes",
            backgroundAssetPath = "img_published.jpg",
            modifier = Modifier.weight(1f),
            onClick = onMyRecipesClick
        )

        CollectionCard(
            title = "Diary",
            backgroundAssetPath = "img_diary.jpg",
            modifier = Modifier.weight(1f),
            onClick = onTriedRecipesClick
        )
    }
}

@Composable
private fun OverviewCollections(
    onTriedRecipesClick: () -> Unit = {},
    onPublishedRecipesClick: () -> Unit = {},
    onSavedCollectionsClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CollectionCard(
            title = "Saved",
            modifier = Modifier.weight(1f),
            backgroundAssetPath = "img_saved.jpg",
            height = 100.dp,
            onClick = onSavedCollectionsClick
        )

        CollectionCard(
            title = "Diary",
            modifier = Modifier.weight(1f),
            backgroundAssetPath = "img_diary.jpg",
            height = 100.dp,
            onClick = onTriedRecipesClick
        )

        CollectionCard(
            title = "Published",
            modifier = Modifier.weight(1f),
            backgroundAssetPath = "img_published.jpg",
            height = 100.dp,
            onClick = onPublishedRecipesClick
        )
    }
}

@Composable
private fun CollectionCard(
    title: String,
    modifier: Modifier = Modifier,
    height: Dp = 120.dp,
    backgroundAssetPath: String? = null,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(height),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (backgroundAssetPath != null) {
                AsyncImage(
                    model = "file:///android_asset/$backgroundAssetPath",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(radius = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )
            }
            Text(
                text = title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                color = if (backgroundAssetPath != null) Color.White
                else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (backgroundAssetPath != null) FontWeight.Bold
                else FontWeight.Normal
            )
        }
    }
}
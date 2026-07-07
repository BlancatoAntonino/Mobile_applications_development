package it.polito.mad.cookbookcommunity.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.mad.cookbookcommunity.data.user.UserRepository
import it.polito.mad.cookbookcommunity.model.profile.ProfileImage
import it.polito.mad.cookbookcommunity.viewmodel.ProfileUiEvent
import it.polito.mad.cookbookcommunity.viewmodel.ProfileViewModel
import it.polito.mad.cookbookcommunity.viewmodel.ProfileViewModelFactory
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ProfileRoute(
    profileUserId: String,
    viewerUserId: String,
    userProfileRepository: UserRepository,
    onExitProfile: () -> Unit,
    modifier: Modifier = Modifier,
    myProfile: Boolean = profileUserId == viewerUserId,
    onMyRecipesClick: (ownerId: String) -> Unit = {},
    onTriedRecipesClick: (ownerId: String) -> Unit = {},
    onEditModeChanged: (Boolean) -> Unit = {},
    onSavedClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    onEditingChanged: (Boolean) -> Unit = onEditModeChanged
) {
    val viewModel: ProfileViewModel = viewModel(
        key = "profile-$profileUserId-$viewerUserId",
        factory = ProfileViewModelFactory(
            repository = userProfileRepository,
            profileUserId = profileUserId,
            viewerUserId = viewerUserId
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isEditing) {
        onEditingChanged(uiState.isEditing)
    }

    DisposableEffect(Unit) {
        onDispose {
            onEditingChanged(false)
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingCameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            scope.launch {
                context.toSharedProfileImageDataUri(uri)?.let { sharedImageValue ->
                    viewModel.onEvent(
                        ProfileUiEvent.ProfileImageChanged(
                            ProfileImage.localUri(sharedImageValue)
                        )
                    )
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraImageUri

        if (success && uri != null) {
            scope.launch {
                context.toSharedProfileImageDataUri(uri)?.let { sharedImageValue ->
                    viewModel.onEvent(
                        ProfileUiEvent.ProfileImageChanged(
                            ProfileImage.localUri(sharedImageValue)
                        )
                    )
                }
            }
        }

        pendingCameraImageUri = null
    }

    BackHandler(enabled = !uiState.isEditing && !myProfile) {
        onExitProfile()
    }

    val editState = uiState.editState

    if (uiState.isEditing && editState != null) {
        EditProfileScreen(
            uiState = uiState,
            editState = editState,
            onEvent = { event ->
                if (event == ProfileUiEvent.SaveEditRequested) {
                    val currentImage = uiState.editState?.formData?.profileImage

                    if (currentImage != null && currentImage.needsSharedProfileImageConversion()) {
                        scope.launch {
                            val sharedImageValue = context.toSharedProfileImageDataUri(
                                Uri.parse(currentImage.value)
                            )

                            if (sharedImageValue != null) {
                                viewModel.onEvent(
                                    ProfileUiEvent.ProfileImageChanged(
                                        ProfileImage.localUri(sharedImageValue)
                                    )
                                )
                            }

                            viewModel.onEvent(ProfileUiEvent.SaveEditRequested)
                        }
                    } else {
                        viewModel.onEvent(event)
                    }
                } else {
                    viewModel.onEvent(event)
                }
            },
            onCancelEdit = {
                viewModel.onEvent(ProfileUiEvent.CancelEditRequested)
            },
            onPickImageFromGallery = {
                galleryLauncher.launch(arrayOf("image/*"))
            },
            onTakeProfilePhoto = {
                val uri = createProfileImageUri(context)
                pendingCameraImageUri = uri
                cameraLauncher.launch(uri)
            },
            modifier = modifier
        )
    } else {
        UserProfileScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onMyRecipesClick = { onMyRecipesClick(profileUserId) },
            onTriedRecipesClick = { onTriedRecipesClick(profileUserId) },
            onSavedClick = onSavedClick,
            onSignOutClick = onSignOutClick,
            onBackClick = onExitProfile,
            modifier = modifier
        )
    }
}

private fun createProfileImageUri(context: Context): Uri {
    val imagesDir = File(context.filesDir, "profile_images").apply {
        mkdirs()
    }

    val imageFile = File.createTempFile(
        "profile_${System.currentTimeMillis()}_",
        ".jpg",
        imagesDir
    )

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        imageFile
    )
}
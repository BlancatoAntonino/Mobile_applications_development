package it.polito.mad.cookbookcommunity.ui.recipeProposal

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import it.polito.mad.cookbookcommunity.ui.profile.toSharedProfileImageDataUri
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.ui.dialogs.SuccessDialog
import it.polito.mad.cookbookcommunity.viewmodel.NewRecipeEvent
import it.polito.mad.cookbookcommunity.viewmodel.NewRecipeProposalViewModel
import it.polito.mad.cookbookcommunity.viewmodel.NewRecipeStep

@Composable
fun NewRecipeProposalRoute(
    repository: RecipeRepository,
    onBackClick: () -> Unit,
    onPublished: (String) -> Unit,
    modifier: Modifier = Modifier,
    sourceRecipeId: String? = null,
    onStepChanged: (NewRecipeStep) -> Unit = {},
    onFormDataChanged: (Boolean) -> Unit = {},
    onRegisterReset: (reset: () -> Unit) -> Unit = {},
    onFeedback: (String) -> Unit = {}
) {
    val viewModel: NewRecipeProposalViewModel = viewModel(
        key = "new-recipe-${sourceRecipeId ?: "blank"}",
        factory = viewModelFactory {
            initializer {
                NewRecipeProposalViewModel(
                    repository = repository,
                    sourceRecipeId = sourceRecipeId
                )
            }
        }
    )

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
                context.toSharedProfileImageDataUri(uri)?.let { dataUri ->
                    viewModel.onEvent(NewRecipeEvent.ImageUriChanged(dataUri))
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
                context.toSharedProfileImageDataUri(uri)?.let { dataUri ->
                    viewModel.onEvent(NewRecipeEvent.ImageUriChanged(dataUri))
                }
            }
        }
        pendingCameraImageUri = null
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showDiscardDialog by remember {
        mutableStateOf(false)
    }

    fun requestExitFromWizard() {
        if (uiState.hasDraftData && !uiState.isPublishLocked) {
            showDiscardDialog = true
        } else {
            onBackClick()
        }
    }

    BackHandler {
        if (uiState.currentStep.isFirst) {
            requestExitFromWizard()
        } else {
            viewModel.onEvent(NewRecipeEvent.PrevStep)
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = {
                showDiscardDialog = false
            },
            title = {
                Text("Discard recipe draft?")
            },
            text = {
                Text("Leaving now will discard the data you've entered for this recipe.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        viewModel.onEvent(NewRecipeEvent.ResetForm)
                        onBackClick()
                    }
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        onRegisterReset { viewModel.onEvent(NewRecipeEvent.ResetForm) }
    }

    LaunchedEffect(uiState.currentStep) {
        onStepChanged(uiState.currentStep)
    }

    LaunchedEffect(uiState.hasDraftData) {
        onFormDataChanged(uiState.hasDraftData)
    }

    val successMessage = uiState.successMessage

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            onFeedback(
                if (sourceRecipeId == null) {
                    "Recipe published."
                } else {
                    "Recipe duplicated."
                }
            )
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let(onFeedback)
    }

    if (successMessage != null){
        SuccessDialog(
            title = "Operation successful",
            message = successMessage,
            onDismiss = {
                val newId = uiState.publishedRecipeId
                viewModel.onEvent(NewRecipeEvent.DismissSuccessMessage)
                newId?.let { onPublished(it) }
            }
        )
    }

    NewRecipeProposalScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = { requestExitFromWizard() },
        modifier = modifier,
        showBackOnFirstStep = sourceRecipeId != null,
        title = if (sourceRecipeId != null) "Adapt recipe" else "New recipe",
        onPickImageFromGallery = {
            galleryLauncher.launch(arrayOf("image/*"))
        },
        onTakeRecipePhoto = {
            val uri = createRecipeImageUri(context)
            pendingCameraImageUri = uri
            cameraLauncher.launch(uri)
        }
    )

}

private fun createRecipeImageUri(context: Context): Uri {
    val imagesDir = File(context.filesDir, "recipe_images").apply { mkdirs() }
    val imageFile = File.createTempFile(
        "recipe_${System.currentTimeMillis()}_",
        ".jpg",
        imagesDir
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        imageFile
    )
}
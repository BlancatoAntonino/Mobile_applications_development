package it.polito.mad.cookbookcommunity.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.polito.mad.cookbookcommunity.model.profile.CookingRole
import it.polito.mad.cookbookcommunity.model.profile.ProfileImage
import it.polito.mad.cookbookcommunity.model.profile.ProfileImageType
import it.polito.mad.cookbookcommunity.model.profile.SocialLink
import it.polito.mad.cookbookcommunity.model.profile.SocialPlatform
import it.polito.mad.cookbookcommunity.model.profile.label
import it.polito.mad.cookbookcommunity.model.recipe.CuisineType
import it.polito.mad.cookbookcommunity.model.recipe.DietaryRestriction
import it.polito.mad.cookbookcommunity.viewmodel.EditProfileUiState
import it.polito.mad.cookbookcommunity.viewmodel.ProfileUiEvent
import it.polito.mad.cookbookcommunity.viewmodel.ProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    uiState: ProfileUiState,
    editState: EditProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
    onCancelEdit: () -> Unit,
    onPickImageFromGallery: () -> Unit,
    onTakeProfilePhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    val form = editState.formData
    val errors = editState.errors

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    BackHandler {
        showSaveDialog = true
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            EditProfileTopBar(
                onBack = { showDiscardDialog = true },
                onSave = { showSaveDialog = true }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 24.dp,
                end = 16.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            item {
                EditableProfileImage(
                    profileImage = form.profileImage,
                    initials = buildInitials(
                        firstName = form.firstName,
                        lastName = form.lastName,
                        nickname = form.nickname
                    ),
                    onImageSelected = {
                        onEvent(ProfileUiEvent.ProfileImageChanged(it))
                    },
                    onPickImageFromGallery = onPickImageFromGallery,
                    onTakeProfilePhoto = onTakeProfilePhoto
                )
            }

            item {
                SectionTitle("Personal infos (cannot be changed)")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = form.firstName,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("First name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = form.lastName,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Last name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))

                OutlinedTextField(
                    value = form.email,
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                SectionTitle("Editable infos")

                ProfileTextField(
                    value = nicknameToFieldValue(form.nickname),
                    onValueChange = {
                        onEvent(ProfileUiEvent.NicknameChanged(fieldValueToNickname(it)))
                    },
                    label = "Nickname",
                    error = errors.nickname,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                ProfileTextField(
                    value = form.phone,
                    onValueChange = {
                        onEvent(ProfileUiEvent.PhoneChanged(it))
                    },
                    label = "Phone",
                    error = errors.phone,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    )
                )

                ProfileTextField(
                    value = form.bio,
                    onValueChange = {
                        onEvent(ProfileUiEvent.BioChanged(it))
                    },
                    label = "Bio",
                    error = errors.bio,
                    minLines = 4,
                    maxLines = 6,
                    supportingText = "${form.bio.length}/160",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Default
                    )
                )
            }

            item {
                SectionTitle("Cooking Role")

                CookingRoleDropdown(
                    selectedRole = form.cookingRole,
                    onRoleSelected = {
                        onEvent(ProfileUiEvent.CookingRoleChanged(it))
                    }
                )
            }

            item {
                SectionTitle("Cuisine Types")

                CuisineTypeSelector(
                    selectedValues = form.favoriteCuisines,
                    onToggle = {
                        onEvent(ProfileUiEvent.CuisineTypeToggled(it))
                    }
                )
            }

            item {
                SectionTitle("Dietary Restrictions")

                DietaryRestrictionSelector(
                    selectedValues = form.dietaryRestrictions,
                    onToggle = {
                        onEvent(ProfileUiEvent.DietaryRestrictionToggled(it))
                    }
                )
            }

            item {
                SectionTitle("Favorite Ingredients")

                EditableStringList(
                    values = form.favoriteIngredients,
                    placeholder = "Add ingredient",
                    onAdd = {
                        onEvent(ProfileUiEvent.FavoriteIngredientAdded(it))
                    },
                    onRemove = {
                        onEvent(ProfileUiEvent.FavoriteIngredientRemoved(it))
                    }
                )
            }

            item {
                SectionTitle("Allergies")

                EditableStringList(
                    values = form.allergies,
                    placeholder = "Add allergy",
                    onAdd = {
                        onEvent(ProfileUiEvent.AllergyAdded(it))
                    },
                    onRemove = {
                        onEvent(ProfileUiEvent.AllergyRemoved(it))
                    }
                )
            }

            item {
                HorizontalDivider()
            }

            item {
                SectionTitle("Social Links")

                EditableSocialLinks(
                    values = form.socialLinks,
                    onAdd = {
                        onEvent(ProfileUiEvent.SocialLinkAdded(it))
                    },
                    onRemove = {
                        onEvent(ProfileUiEvent.SocialLinkRemoved(it))
                    }
                )
            }

            item {
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?") },
            text = { Text("Your unsaved profile changes will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onCancelEdit()
                    }
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save profile changes?") },
            text = { Text("Your profile will be updated if all fields are valid.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSaveDialog = false
                        onEvent(ProfileUiEvent.SaveEditRequested)
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileTopBar(
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Discard profile changes"
                )
            }
        },
        actions = {
            TextButton(onClick = onSave) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null
                )
                Text(
                    text = "Save",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditableProfileImage(
    profileImage: ProfileImage,
    initials: String,
    onImageSelected: (ProfileImage) -> Unit,
    onPickImageFromGallery: () -> Unit,
    onTakeProfilePhoto: () -> Unit
) {
    var showPhotoPickerSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        ProfileImagePreview(
            profileImage = profileImage,
            initials = initials,
            modifier = Modifier.size(150.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 52.dp, y = 52.dp)
        ) {
            FilledIconButton(
                onClick = { showPhotoPickerSheet = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Change profile picture"
                )
            }
        }
    }

    if (showPhotoPickerSheet) {
        ProfilePhotoPickerBottomSheet(
            selectedImage = profileImage,
            initials = initials,
            onDismiss = {
                showPhotoPickerSheet = false
            },
            onImageSelected = { selectedImage ->
                showPhotoPickerSheet = false
                onImageSelected(selectedImage)
            },
            onPickImageFromGallery = {
                showPhotoPickerSheet = false
                onPickImageFromGallery()
            },
            onTakeProfilePhoto = {
                showPhotoPickerSheet = false
                onTakeProfilePhoto()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfilePhotoPickerBottomSheet(
    selectedImage: ProfileImage,
    initials: String,
    onDismiss: () -> Unit,
    onImageSelected: (ProfileImage) -> Unit,
    onPickImageFromGallery: () -> Unit,
    onTakeProfilePhoto: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = {
            BottomSheetDefaults.DragHandle()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Change profile photo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(
                    start = 24.dp,
                    top = 12.dp,
                    end = 24.dp,
                    bottom = 20.dp
                )
            )

            HorizontalDivider()

            Text(
                text = "Default avatars",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(
                    start = 24.dp,
                    top = 20.dp,
                    end = 24.dp,
                    bottom = 12.dp
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileAvatarChoice(
                    selected = selectedImage.type == ProfileImageType.MONOGRAM,
                    onClick = {
                        onImageSelected(ProfileImage.Monogram)
                    }
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                predefinedProfileAvatarOptions.forEach { avatarOption ->
                    ProfileAvatarChoice(
                        selected = selectedImage.type == ProfileImageType.PRESET_AVATAR &&
                                selectedImage.value == avatarOption.key,
                        onClick = {
                            onImageSelected(ProfileImage.presetAvatar(avatarOption.key))
                        }
                    ) {
                        Image(
                            painter = painterResource(id = avatarOption.drawableResId),
                            contentDescription = avatarOption.label,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 24.dp)
            )

            TextButton(
                onClick = onPickImageFromGallery,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = 24.dp,
                    vertical = 18.dp
                )
            ) {
                Text(
                    text = "Select from gallery",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            TextButton(
                onClick = onTakeProfilePhoto,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = 24.dp,
                    vertical = 18.dp
                )
            ) {
                Text(
                    text = "Take a photo",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ProfileAvatarChoice(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    val borderWidth = if (selected) {
        3.dp
    } else {
        2.dp
    }

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
private fun ProfileImagePreview(
    profileImage: ProfileImage,
    initials: String,
    modifier: Modifier = Modifier
) {
    val avatarModifier = modifier
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer)

    when (profileImage.type) {
        ProfileImageType.MONOGRAM -> {
            Box(
                modifier = avatarModifier,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        ProfileImageType.PRESET_AVATAR -> {
            val avatarResId = profileAvatarDrawableResIdOrNull(profileImage.value)

            Box(
                modifier = avatarModifier,
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
                        contentDescription = "Preset avatar avatar fallback",
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
                value = profileImage.value,
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = avatarModifier,
                fallback = {
                    Box(
                        modifier = avatarModifier,
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.displaySmall,
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
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    maxLines: Int = 1,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = {
            when {
                error != null -> Text(error)
                supportingText != null -> Text(supportingText)
            }
        },
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CookingRoleDropdown(
    selectedRole: CookingRole,
    onRoleSelected: (CookingRole) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedRole.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Cooking Role") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CookingRole.entries.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role.label) },
                    onClick = {
                        expanded = false
                        onRoleSelected(role)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CuisineTypeSelector(
    selectedValues: Set<CuisineType>,
    onToggle: (CuisineType) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CuisineType.entries.forEach { cuisine ->
            FilterChip(
                selected = cuisine in selectedValues,
                onClick = { onToggle(cuisine) },
                label = { Text(cuisine.label) },
                leadingIcon = if (cuisine in selectedValues) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    null
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DietaryRestrictionSelector(
    selectedValues: Set<DietaryRestriction>,
    onToggle: (DietaryRestriction) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DietaryRestriction.entries.forEach { restriction ->
            FilterChip(
                selected = restriction in selectedValues,
                onClick = { onToggle(restriction) },
                label = { Text(restriction.label) },
                leadingIcon = if (restriction in selectedValues) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    null
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditableStringList(
    values: List<String>,
    placeholder: String,
    onAdd: (String) -> Unit,
    onRemove: (Int) -> Unit
) {
    var input by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (values.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                values.forEachIndexed { index, value ->
                    InputChip(
                        selected = false,
                        onClick = {},
                        label = { Text(value) },
                        trailingIcon = {
                            IconButton(
                                onClick = { onRemove(index) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove $value",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text(placeholder) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            )

            IconButton(
                onClick = {
                    onAdd(input)
                    input = ""
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = placeholder
                )
            }
        }
    }
}

@Composable
private fun EditableSocialLinks(
    values: List<SocialLink>,
    onAdd: (SocialLink) -> Unit,
    onRemove: (Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        values.forEachIndexed { index, link ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = link.platform.label.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1.6f)
                    )

                    IconButton(
                        onClick = { onRemove(index) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove social link",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        TextButton(
            onClick = { showAddDialog = true },
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Text("Add social link")
        }
    }

    if (showAddDialog) {
        AddSocialLinkDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { socialLink ->
                onAdd(socialLink)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddSocialLinkDialog(
    onDismiss: () -> Unit,
    onConfirm: (SocialLink) -> Unit
) {
    var selectedPlatform by remember { mutableStateOf(SocialPlatform.INSTAGRAM) }
    var value by remember { mutableStateOf("") }
    var platformMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add social link") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box {
                    OutlinedTextField(
                        value = selectedPlatform.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Platform") },
                        trailingIcon = {
                            IconButton(onClick = { platformMenuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Chose platform"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = platformMenuExpanded,
                        onDismissRequest = { platformMenuExpanded = false }
                    ) {
                        SocialPlatform.entries.forEach { platform ->
                            DropdownMenuItem(
                                text = { Text(platform.label) },
                                onClick = {
                                    selectedPlatform = platform
                                    platformMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Handle or URL") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        SocialLink(
                            platform = selectedPlatform,
                            value = value
                        )
                    )
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun buildInitials(
    firstName: String,
    lastName: String,
    nickname: String
): String {
    val firstInitial = firstName.trim().firstOrNull()?.uppercaseChar()
    val lastInitial = lastName.trim().firstOrNull()?.uppercaseChar()

    return listOfNotNull(firstInitial, lastInitial)
        .joinToString("")
        .ifBlank { nickname.trim().take(2).uppercase() }
        .ifBlank { "?" }
}

private fun nicknameToFieldValue(nickname: String): String {
    val cleanNickname = fieldValueToNickname(nickname)
    return if (cleanNickname.isBlank()) "" else "@${cleanNickname}"
}

private fun fieldValueToNickname(value: String): String {
    return value.trim().removePrefix("@")
}
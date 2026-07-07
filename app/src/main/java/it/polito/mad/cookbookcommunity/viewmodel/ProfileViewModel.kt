package it.polito.mad.cookbookcommunity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import it.polito.mad.cookbookcommunity.data.user.UserRepository
import it.polito.mad.cookbookcommunity.model.profile.CookingRole
import it.polito.mad.cookbookcommunity.model.profile.ProfileImage
import it.polito.mad.cookbookcommunity.model.profile.ProfilePreferences
import it.polito.mad.cookbookcommunity.model.profile.SocialLink
import it.polito.mad.cookbookcommunity.model.profile.UserProfile
import it.polito.mad.cookbookcommunity.model.recipe.CuisineType
import it.polito.mad.cookbookcommunity.model.recipe.DietaryRestriction
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ProfileScreenMode {
    VIEW,
    EDIT
}

data class EditProfileFormData(
    val firstName: String,
    val lastName: String,
    val email: String,
    val nickname: String,
    val phone: String,
    val bio: String,
    val cookingRole: CookingRole,
    val favoriteCuisines: Set<CuisineType>,
    val dietaryRestrictions: Set<DietaryRestriction>,
    val favoriteIngredients: List<String>,
    val allergies: List<String>,
    val socialLinks: List<SocialLink>,
    val profileImage: ProfileImage
)

fun UserProfile.toEditProfileFormData(): EditProfileFormData =
    EditProfileFormData(
        firstName = firstName,
        lastName = lastName,
        email = email,
        nickname = nickname,
        phone = phone.orEmpty(),
        bio = bio.orEmpty(),
        cookingRole = cookingRole,
        favoriteCuisines = preferences.favoriteCuisines,
        dietaryRestrictions = preferences.dietaryRestrictions,
        favoriteIngredients = preferences.favoriteIngredients,
        allergies = preferences.allergies,
        socialLinks = socialLinks,
        profileImage = profileImage
    )

data class EditProfileErrors(
    val firstName: String? = null,
    val lastName: String? = null,
    val nickname: String? = null,
    val phone: String? = null,
    val bio: String? = null
) {
    val hasAnyError: Boolean
        get() = firstName != null ||
                lastName != null ||
                nickname != null ||
                phone != null ||
                bio != null
}

data class EditProfileUiState(
    val formData: EditProfileFormData,
    val errors: EditProfileErrors = EditProfileErrors()
)

data class ProfileUiState(
    val profileUserId: String? = null,
    val viewerUserId: String? = null,
    val viewedProfile: UserProfile? = null,
    val screenMode: ProfileScreenMode = ProfileScreenMode.VIEW,
    val editState: EditProfileUiState? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val isOwner: Boolean
        get() = profileUserId != null && viewerUserId == profileUserId

    val canEdit: Boolean
        get() = isOwner

    val isEditing: Boolean
        get() = screenMode == ProfileScreenMode.EDIT
}

sealed interface ProfileUiEvent {
    data class LoadProfile(
        val profileUserId: String,
        val viewerUserId: String
    ) : ProfileUiEvent

    data object EditRequested : ProfileUiEvent
    data object CancelEditRequested : ProfileUiEvent
    data object SaveEditRequested : ProfileUiEvent
    data object BackFromEditRequested : ProfileUiEvent

    data class FirstNameChanged(val value: String) : ProfileUiEvent
    data class LastNameChanged(val value: String) : ProfileUiEvent
    data class NicknameChanged(val value: String) : ProfileUiEvent
    data class PhoneChanged(val value: String) : ProfileUiEvent
    data class BioChanged(val value: String) : ProfileUiEvent
    data class CookingRoleChanged(val value: CookingRole) : ProfileUiEvent
    data class CuisineTypeToggled(val value: CuisineType) : ProfileUiEvent
    data class DietaryRestrictionToggled(val value: DietaryRestriction) : ProfileUiEvent
    data class FavoriteIngredientAdded(val value: String) : ProfileUiEvent
    data class FavoriteIngredientRemoved(val index: Int) : ProfileUiEvent
    data class AllergyAdded(val value: String) : ProfileUiEvent
    data class AllergyRemoved(val index: Int) : ProfileUiEvent
    data class SocialLinkAdded(val value: SocialLink) : ProfileUiEvent
    data class SocialLinkRemoved(val index: Int) : ProfileUiEvent
    data class ProfileImageChanged(val value: ProfileImage) : ProfileUiEvent
}

class ProfileViewModel(
    private val repository: UserRepository,
    profileUserId: String,
    viewerUserId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            profileUserId = profileUserId,
            viewerUserId = viewerUserId,
            viewedProfile = null,
            isLoading = true
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var profileCollectionJob: Job? = null

    init {
        startProfileCollection(
            profileUserId = profileUserId,
            viewerUserId = viewerUserId
        )
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.LoadProfile -> loadProfileInternal(
                profileUserId = event.profileUserId,
                viewerUserId = event.viewerUserId
            )

            ProfileUiEvent.EditRequested -> enterEditModeInternal()
            ProfileUiEvent.CancelEditRequested -> discardEditChangesInternal()
            ProfileUiEvent.SaveEditRequested -> saveEditInternal()
            ProfileUiEvent.BackFromEditRequested -> handleBackFromEditInternal()

            is ProfileUiEvent.FirstNameChanged -> updateFirstNameInternal(event.value)
            is ProfileUiEvent.LastNameChanged -> updateLastNameInternal(event.value)
            is ProfileUiEvent.NicknameChanged -> updateNicknameInternal(event.value)
            is ProfileUiEvent.PhoneChanged -> updatePhoneInternal(event.value)
            is ProfileUiEvent.BioChanged -> updateBioInternal(event.value)
            is ProfileUiEvent.CookingRoleChanged -> updateCookingRoleInternal(event.value)
            is ProfileUiEvent.CuisineTypeToggled -> toggleCuisineTypeInternal(event.value)
            is ProfileUiEvent.DietaryRestrictionToggled -> toggleDietaryRestrictionInterval(event.value)
            is ProfileUiEvent.FavoriteIngredientAdded -> addFavoriteIngredientInternal(event.value)
            is ProfileUiEvent.FavoriteIngredientRemoved -> removeIngredientInternal(event.index)
            is ProfileUiEvent.AllergyAdded -> addAllergyInternal(event.value)
            is ProfileUiEvent.AllergyRemoved -> removeAllergyInternal(event.index)
            is ProfileUiEvent.SocialLinkAdded -> addSocialLinkInternal(event.value)
            is ProfileUiEvent.SocialLinkRemoved -> removeSocialLinkInternal(event.index)
            is ProfileUiEvent.ProfileImageChanged -> updateProfileImageInternal(event.value)
        }
    }

    private fun isSameProfileRequest(
        profileUserId: String,
        viewerUserId: String
    ): Boolean {
        val currentState = _uiState.value

        return currentState.profileUserId == profileUserId &&
                currentState.viewerUserId == viewerUserId &&
                currentState.viewedProfile != null &&
                profileCollectionJob != null
    }

    private fun loadProfileInternal(
        profileUserId: String,
        viewerUserId: String
    ) {
        if (isSameProfileRequest(profileUserId, viewerUserId)) return

        profileCollectionJob?.cancel()

        _uiState.value = ProfileUiState(
            profileUserId = profileUserId,
            viewerUserId = viewerUserId,
            isLoading = true
        )

        profileCollectionJob = viewModelScope.launch {
            repository.observeUserById(profileUserId).collect { profile ->
                _uiState.update { currentState ->
                    if (
                        currentState.profileUserId != profileUserId ||
                        currentState.viewerUserId != viewerUserId
                    ) {
                        return@update currentState
                    }

                    val profileNotFound = profile == null &&
                            currentState.viewedProfile == null

                    if (currentState.isEditing) {
                        currentState.copy(
                            viewedProfile = currentState.viewedProfile ?: profile,
                            isLoading = false,
                            errorMessage = if (profileNotFound) "Profile not found." else null
                        )
                    } else {
                        currentState.copy(
                            viewedProfile = profile,
                            screenMode = ProfileScreenMode.VIEW,
                            editState = null,
                            isLoading = false,
                            errorMessage = if (profileNotFound) "Profile not found" else null
                        )
                    }
                }
            }
        }
    }

    private fun startProfileCollection(
        profileUserId: String,
        viewerUserId: String
    ) {
        profileCollectionJob?.cancel()

        profileCollectionJob = viewModelScope.launch {
            repository.observeUserById(profileUserId).collect { profile ->
                _uiState.update { currentState ->
                    if (
                        currentState.profileUserId != profileUserId ||
                        currentState.viewerUserId != viewerUserId
                    ) {
                        return@update currentState
                    }

                    val profileNotFound = profile == null &&
                            currentState.viewedProfile == null

                    if (currentState.isEditing) {
                        currentState.copy(
                            viewedProfile = currentState.viewedProfile ?: profile,
                            isLoading = false,
                            errorMessage = if (profileNotFound) "Profile not found" else null
                        )
                    } else {
                        currentState.copy(
                            viewedProfile = profile,
                            screenMode = ProfileScreenMode.VIEW,
                            editState = null,
                            isLoading = false,
                            errorMessage = if (profileNotFound) "Profile not found." else null
                        )
                    }
                }
            }
        }
    }

    private fun enterEditModeInternal() {
        _uiState.update { state ->
            val profile = state.viewedProfile ?: return@update state
            if (!state.canEdit) return@update state

            state.copy(
                screenMode = ProfileScreenMode.EDIT,
                editState = EditProfileUiState(
                    formData = profile.toEditProfileFormData(),
                    errors = EditProfileErrors()
                )
            )
        }
    }

    private fun updateFirstNameInternal(value: String) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(firstName = value),
                    errors = editState.errors.copy(firstName = null)
                )
            )
        }
    }

    private fun updateLastNameInternal(value: String) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(lastName = value),
                    errors = editState.errors.copy(lastName = null)
                )
            )
        }
    }

    private fun updateNicknameInternal(value: String) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(nickname = value),
                    errors = editState.errors.copy(nickname = null)
                )
            )
        }
    }

    private fun updatePhoneInternal(value: String) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(phone = value),
                    errors = editState.errors.copy(phone = null)
                )
            )
        }
    }

    private fun updateBioInternal(value: String) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state
            val bioError = if (value.length > 160) {
                "Bio must be at most 160 characters long."
            } else {
                null
            }

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(bio = value),
                    errors = editState.errors.copy(bio = bioError)
                )
            )
        }
    }

    private fun updateCookingRoleInternal(value: CookingRole) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(cookingRole = value)
                )
            )
        }
    }

    private fun toggleCuisineTypeInternal(value: CuisineType) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state
            val currentValues = editState.formData.favoriteCuisines
            val updatedValues = if (value in currentValues) {
                currentValues - value
            } else {
                currentValues + value
            }

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(
                        favoriteCuisines = updatedValues
                    )
                )
            )
        }
    }

    private fun toggleDietaryRestrictionInterval(value: DietaryRestriction) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state
            val currentValues = editState.formData.dietaryRestrictions
            val updatedValues = if (value in currentValues) {
                currentValues - value
            } else {
                currentValues + value
            }

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(
                        dietaryRestrictions = updatedValues
                    )
                )
            )
        }
    }

    private fun addFavoriteIngredientInternal(value: String) {
        val normalizedValue = value.trim()
        if (normalizedValue.isEmpty()) return

        _uiState.update { state ->
            val editState = state.editState ?: return@update state
            val currentValues = editState.formData.favoriteIngredients

            if (currentValues.any { it.equals(normalizedValue, ignoreCase = true) }) {
                return@update state
            }

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(
                        favoriteIngredients = currentValues + normalizedValue
                    )
                )
            )
        }
    }

    private fun removeIngredientInternal(index: Int) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(
                        favoriteIngredients = editState.formData.favoriteIngredients
                            .filterIndexed { currentIndex, _ -> currentIndex != index }
                    )
                )
            )
        }
    }

    private fun addAllergyInternal(value: String) {
        val normalizedValue = value.trim()
        if (normalizedValue.isEmpty()) return

        _uiState.update { state ->
            val editState = state.editState ?: return@update state
            val currentValues = editState.formData.allergies

            if (currentValues.any { it.equals(normalizedValue, ignoreCase = true) }) {
                return@update state
            }

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(
                        allergies = currentValues + normalizedValue
                    )
                )
            )
        }
    }

    private fun removeAllergyInternal(index: Int) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(
                        allergies = editState.formData.allergies
                            .filterIndexed { currentIndex, _ -> currentIndex != index }
                    )
                )
            )
        }
    }

    private fun addSocialLinkInternal(value: SocialLink) {
        val normalizedValue = value.value.trim()
        if (normalizedValue.isEmpty()) return

        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(
                        socialLinks = editState.formData.socialLinks + value.copy(
                            value = normalizedValue
                        )
                    )
                )
            )
        }
    }

    private fun removeSocialLinkInternal(index: Int) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(
                        socialLinks = editState.formData.socialLinks
                            .filterIndexed { currentIndex, _ -> currentIndex != index }
                    )
                )
            )
        }
    }

    private fun updateProfileImageInternal(value: ProfileImage) {
        _uiState.update { state ->
            val editState = state.editState ?: return@update state

            state.copy(
                editState = editState.copy(
                    formData = editState.formData.copy(profileImage = value)
                )
            )
        }
    }

    private fun validateRequiredSingleLineText(
        value: String,
        fieldName: String,
        maxLength: Int
    ): String? {
        val trimmed = value.trim()

        return when {
            trimmed.isEmpty() -> "$fieldName is required."
            '\n' in value -> "$fieldName must be a single line."
            trimmed.length > maxLength -> "$fieldName must be at most $maxLength characters long."
            else -> null
        }
    }

    private fun validateEditProfileForm(
        formData: EditProfileFormData
    ): EditProfileErrors {
        val firstNameError = validateRequiredSingleLineText(
            value = formData.firstName,
            fieldName = "First name",
            maxLength = 40
        )

        val lastNameError = validateRequiredSingleLineText(
            value = formData.lastName,
            fieldName = "Last name",
            maxLength = 40
        )

        val nicknameTrimmed = formData.nickname.trim()
        val phoneTrimmed = formData.phone.trim()
        val bioRaw = formData.bio
        val phoneRegex = Regex("^[0-9+\\- ]*$")

        val nicknameError = when {
            nicknameTrimmed.isEmpty() -> "Nickname is required."
            '\n' in formData.nickname -> "Nickname must be a single line."
            nicknameTrimmed.length < 3 -> "Nickname must be at least 3 characters long."
            nicknameTrimmed.length > 30 -> "Nickname must be at most 30 characters long."
            else -> null
        }

        val phoneError = when {
            phoneTrimmed.isEmpty() -> null
            phoneTrimmed.length < 7 -> "Phone number must be at least 7 characters long."
            phoneTrimmed.length > 20 -> "Phone number must be at most 20 characters long."
            !phoneTrimmed.matches(phoneRegex) -> "Phone number contains invalid characters."
            else -> null
        }

        val bioError = when {
            bioRaw.isEmpty() -> null
            bioRaw.trim().isEmpty() -> "Bio cannot contain only blank spaces."
            bioRaw.length > 160 -> "Bio must be at most 160 characters long."
            else -> null
        }

        return EditProfileErrors(
            firstName = firstNameError,
            lastName = lastNameError,
            nickname = nicknameError,
            phone = phoneError,
            bio = bioError
        )
    }

    private fun saveEditInternal() {
        val state = _uiState.value
        if (!state.canEdit) return

        val editState = state.editState ?: return
        val form = editState.formData
        val currentProfile = state.viewedProfile ?: return

        val errors = validateEditProfileForm(form)
        if (errors.hasAnyError) {
            _uiState.update { currentState ->
                val currentEditState = currentState.editState ?: return@update currentState
                currentState.copy(
                    editState = currentEditState.copy(errors = errors)
                )
            }
            return
        }

        val updatedProfile = currentProfile.copy(
            firstName = form.firstName.trim(),
            lastName = form.lastName.trim(),
            nickname = form.nickname.trim(),
            phone = form.phone.trim().ifBlank { null },
            bio = form.bio.trim().ifBlank { null },
            cookingRole = form.cookingRole,
            preferences = ProfilePreferences(
                favoriteCuisines = form.favoriteCuisines,
                dietaryRestrictions = form.dietaryRestrictions,
                favoriteIngredients = form.favoriteIngredients,
                allergies = form.allergies
            ),
            socialLinks = form.socialLinks,
            profileImage = form.profileImage
        )

        viewModelScope.launch {
            repository.updateUser(updatedProfile)

            _uiState.update { currentState ->
                currentState.copy(
                    viewedProfile = updatedProfile,
                    screenMode = ProfileScreenMode.VIEW,
                    editState = null,
                    isLoading = false,
                    errorMessage = null
                )
            }
        }
    }

    private fun discardEditChangesInternal() {
        _uiState.update { state ->
            state.copy(
                screenMode = ProfileScreenMode.VIEW,
                editState = null
            )
        }
    }

    private fun handleBackFromEditInternal() {
        saveEditInternal()
    }
}

class ProfileViewModelFactory(
    private val repository: UserRepository,
    private val profileUserId: String,
    private val viewerUserId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(
                repository = repository,
                profileUserId = profileUserId,
                viewerUserId = viewerUserId
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
package it.polito.mad.cookbookcommunity.model.profile

data class ProfileImage(
    val type: ProfileImageType = ProfileImageType.MONOGRAM,
    val value: String = ""
) {
    val isMonogram: Boolean
        get() = type == ProfileImageType.MONOGRAM

    val isPresetAvatar: Boolean
        get() = type == ProfileImageType.PRESET_AVATAR

    val isLocalUri: Boolean
        get() = type == ProfileImageType.LOCAL_URI

    companion object {
        val Monogram = ProfileImage()

        fun presetAvatar(key: String): ProfileImage {
            return ProfileImage(
                type = ProfileImageType.PRESET_AVATAR,
                value = key
            )
        }

        fun localUri(uri: String): ProfileImage {
            return ProfileImage(
                type = ProfileImageType.LOCAL_URI,
                value = uri
            )
        }
    }
}

enum class ProfileImageType {
    MONOGRAM,
    PRESET_AVATAR,
    LOCAL_URI
}
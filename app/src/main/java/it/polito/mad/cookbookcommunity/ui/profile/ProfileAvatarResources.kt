package it.polito.mad.cookbookcommunity.ui.profile

import it.polito.mad.cookbookcommunity.R

data class ProfileAvatarOption(
    val key: String,
    val label: String,
    val drawableResId: Int
)

val predefinedProfileAvatarOptions = listOf(
    ProfileAvatarOption(
        key = "avatar_1",
        label = "Use avatar 1",
        drawableResId = R.drawable.ic_profile_avatar_1
    ),
    ProfileAvatarOption(
        key = "avatar_2",
        label = "Use avatar 2",
        drawableResId = R.drawable.ic_profile_avatar_2
    ),
    ProfileAvatarOption(
        key = "avatar_3",
        label = "Use avatar 3",
        drawableResId = R.drawable.ic_profile_avatar_3
    )
)

fun profileAvatarDrawableResIdOrNull(key: String): Int? {
    return predefinedProfileAvatarOptions
        .firstOrNull { it.key == key }
        ?.drawableResId
}
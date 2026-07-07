package it.polito.mad.cookbookcommunity.model.profile

data class SocialLink(
    val platform: SocialPlatform = SocialPlatform.WEBSITE,
    val value: String = ""
)

enum class SocialPlatform(val label: String) {
    INSTAGRAM("Instagram"),
    TIKTOK("TikTok"),
    YOUTUBE("YouTube"),
    WEBSITE("Website"),
    FACEBOOK("Facebook")
}
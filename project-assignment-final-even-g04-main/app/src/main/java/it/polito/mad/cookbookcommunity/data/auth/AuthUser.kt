package it.polito.mad.cookbookcommunity.data.auth

data class AuthUser(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = ""
)
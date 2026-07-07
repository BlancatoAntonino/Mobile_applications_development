package it.polito.mad.cookbookcommunity.data.seed

import it.polito.mad.cookbookcommunity.model.profile.CookingRole
import it.polito.mad.cookbookcommunity.model.profile.ProfileImage
import it.polito.mad.cookbookcommunity.model.profile.ProfilePreferences
import it.polito.mad.cookbookcommunity.model.profile.SocialLink
import it.polito.mad.cookbookcommunity.model.profile.SocialPlatform
import it.polito.mad.cookbookcommunity.model.profile.UserProfile
import it.polito.mad.cookbookcommunity.model.recipe.CuisineType
import it.polito.mad.cookbookcommunity.model.recipe.DietaryRestriction
import it.polito.mad.cookbookcommunity.session.SessionManager

object SeedUserProfiles {
    val items: List<UserProfile> = listOf(
        UserProfile(
            internalId = SessionManager.CURRENT_LOGGED_IN_USER_ID,
            firstName = "Marta",
            lastName = "Rossi",
            nickname = "marta.cooks",
            email = "marta.rossi@example.com",
            phone = "+39 333 123 4567",
            bio = "Home cook passionate about simple Italian recipes, healthy meals and desserts to share with friends.",
            cookingRole = CookingRole.HOME_COOK,
            preferences = ProfilePreferences(
                favoriteCuisines = setOf(
                    CuisineType.ITALIAN,
                    CuisineType.MEDITERRANEAN,
                    CuisineType.AMERICAN
                ),
                dietaryRestrictions = setOf(
                    DietaryRestriction.VEGETARIAN,
                ),
                favoriteIngredients = listOf(
                    "Tomato",
                    "Basil",
                    "Pasta",
                    "Dark chocolate",
                    "Chickpeas"
                ),
                allergies = listOf(
                    "Nuts"
                )
            ),
            socialLinks = listOf(
                SocialLink(
                    platform = SocialPlatform.INSTAGRAM,
                    value = "@marta.cooks"
                ),
                SocialLink(
                    platform = SocialPlatform.WEBSITE,
                    value = "https://marta-cooks.example.com"
                )
            ),
            profileImage = ProfileImage.presetAvatar("avatar_1")
        ),

        UserProfile(
            internalId = "202",
            firstName = "Luca",
            lastName = "Bianchi",
            nickname = "luca.veggie",
            email = "luca.bianchi@example.com",
            phone = null,
            bio = "Vegetarian food lover focused on quick, healthy and colorful meals.",
            cookingRole = CookingRole.FOOD_LOVER,
            preferences = ProfilePreferences(
                favoriteCuisines = setOf(
                    CuisineType.INDIAN,
                    CuisineType.MEDITERRANEAN,
                    CuisineType.OTHER
                ),
                dietaryRestrictions = setOf(
                    DietaryRestriction.VEGETARIAN
                ),
                favoriteIngredients = listOf(
                    "Chickpeas",
                    "Avocado",
                    "Eggs"
                ),
                allergies = emptyList()
            ),
            socialLinks = listOf(
                SocialLink(
                    platform = SocialPlatform.INSTAGRAM,
                    value = "@luca.veggie"
                )
            ),
            profileImage = ProfileImage.presetAvatar("avatar_2")
        ),

        UserProfile(
            internalId = "303",
            firstName = "Giulia",
            lastName = "Ferrari",
            nickname = "giulia.homechef",
            email = "giulia.ferrari@example.com",
            phone = null,
            bio = "Home cook who loves traditional Italian dishes and Sunday recipes.",
            cookingRole = CookingRole.HOME_COOK,
            preferences = ProfilePreferences(
                favoriteCuisines = setOf(
                    CuisineType.ITALIAN,
                    CuisineType.MEDITERRANEAN
                ),
                dietaryRestrictions = emptySet(),
                favoriteIngredients = listOf(
                    "Pasta",
                    "Tomato sauce",
                    "Parmesan"
                ),
                allergies = emptyList()
            ),
            socialLinks = emptyList(),
            profileImage = ProfileImage.presetAvatar("avatar_3")
        ),

        UserProfile(
            internalId = "404",
            firstName = "Sara",
            lastName = "Conti",
            nickname = "sara.freshfood",
            email = "sara.conti@example.com",
            phone = null,
            bio = "Content creator focused on fresh appetizers, salads and Mediterranean meals.",
            cookingRole = CookingRole.CONTENT_CREATOR,
            preferences = ProfilePreferences(
                favoriteCuisines = setOf(
                    CuisineType.MEDITERRANEAN,
                    CuisineType.FUSION
                ),
                dietaryRestrictions = setOf(
                    DietaryRestriction.GLUTEN_FREE
                ),
                favoriteIngredients = listOf(
                    "Feta",
                    "Tomatoes",
                    "Olives"
                ),
                allergies = emptyList()
            ),
            socialLinks = listOf(
                SocialLink(
                    platform = SocialPlatform.INSTAGRAM,
                    value = "@sara.freshfood"
                )
            ),
            profileImage = ProfileImage.Monogram
        )
    )
}
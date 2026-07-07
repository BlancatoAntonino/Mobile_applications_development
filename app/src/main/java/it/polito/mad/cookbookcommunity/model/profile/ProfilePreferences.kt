package it.polito.mad.cookbookcommunity.model.profile

import it.polito.mad.cookbookcommunity.model.recipe.CuisineType
import it.polito.mad.cookbookcommunity.model.recipe.DietaryRestriction

data class ProfilePreferences(
    val favoriteCuisines: Set<CuisineType> = emptySet(),
    val dietaryRestrictions: Set<DietaryRestriction> = emptySet(),
    val favoriteIngredients: List<String> = emptyList(),
    val allergies: List<String> = emptyList()
)

val CuisineType.label: String
    get() = when (this) {
        CuisineType.ITALIAN -> "Italian"
        CuisineType.ASIAN -> "Asian"
        CuisineType.MEDITERRANEAN -> "Mediterranean"
        CuisineType.MEXICAN -> "Mexican"
        CuisineType.AMERICAN -> "American"
        CuisineType.INDIAN -> "Indian"
        CuisineType.MIDDLE_EASTERN -> "Middle Eastern"
        CuisineType.FUSION -> "Fusion"
        CuisineType.OTHER -> "Other"
    }

val DietaryRestriction.label: String
    get() = when (this) {
        DietaryRestriction.VEGETARIAN -> "Vegetarian"
        DietaryRestriction.VEGAN -> "Vegan"
        DietaryRestriction.GLUTEN_FREE -> "Gluten free"
        DietaryRestriction.DAIRY_FREE -> "Dairy free"
        DietaryRestriction.PESCETARIAN -> "Pescetarian"
        DietaryRestriction.HALAL -> "Halal"
        DietaryRestriction.KOSHER -> "Kosher"
        DietaryRestriction.NUT_FREE -> "Nut free"
    }
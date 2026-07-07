package it.polito.mad.cookbookcommunity.model.recipe

enum class PriceRange {
    LOW,
    MEDIUM,
    HIGH
}

enum class DifficultyLevel {
    EASY,
    MEDIUM,
    HARD
}

enum class RecipeType {
    APPETIZER,
    MAIN_COURSE,
    SIDE_DISH,
    DESSERT,
    BREAKFAST,
    SNACK,
    DRINK
}

enum class CuisineType {
    ITALIAN,
    ASIAN,
    MEDITERRANEAN,
    MEXICAN,
    AMERICAN,
    INDIAN,
    MIDDLE_EASTERN,
    FUSION,
    OTHER
}

enum class DietaryRestriction {
    VEGETARIAN,
    VEGAN,
    GLUTEN_FREE,
    DAIRY_FREE,
    PESCETARIAN,
    HALAL,
    KOSHER,
    NUT_FREE
}

enum class IngredientUnit {
    G,
    KG,
    ML,
    L,
    TSP,
    TBSP,
    CUP,
    UNIT,
    PINCH,
    TO_TASTE
}
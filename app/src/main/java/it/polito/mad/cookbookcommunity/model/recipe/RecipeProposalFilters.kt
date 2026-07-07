package it.polito.mad.cookbookcommunity.model.recipe

data class RecipeProposalFilters(
    val query: String = "",
    val ingredientKeywords: Set<String> = emptySet(),
    val priceRanges: Set<PriceRange> = emptySet(),
    val recipeTypes: Set<RecipeType> = emptySet(),
    val dietaryRestrictions: Set<DietaryRestriction> = emptySet()
) {
    val hasActiveFilters: Boolean
        get() = query.isNotBlank() ||
                ingredientKeywords.isNotEmpty() ||
                priceRanges.isNotEmpty() ||
                recipeTypes.isNotEmpty() ||
                dietaryRestrictions.isNotEmpty()
}
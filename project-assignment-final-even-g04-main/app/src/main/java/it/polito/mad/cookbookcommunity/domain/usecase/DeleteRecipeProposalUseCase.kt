package it.polito.mad.cookbookcommunity.domain.usecase

import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.data.favorite.FavoriteCollectionRepository
import it.polito.mad.cookbookcommunity.data.favorite.FavoriteRepository
import it.polito.mad.cookbookcommunity.data.review.ReviewRepository
import it.polito.mad.cookbookcommunity.data.repository.TipRepository
import it.polito.mad.cookbookcommunity.data.repository.TriedRecipeRepository

class DeleteRecipeProposalUseCase(
    private val recipeRepository: RecipeRepository,
    private val reviewRepository: ReviewRepository,
    private val favoriteRepository: FavoriteRepository,
    private val favoriteCollectionRepository: FavoriteCollectionRepository,
    private val tipRepository: TipRepository,
    private val triedRecipeRepository: TriedRecipeRepository,
) {
    suspend operator fun invoke(recipeId: String) {

        reviewRepository.deleteReviewsByRecipe(recipeId)
        recipeRepository.deleteRecipeProposal(recipeId)
        favoriteRepository.removeRecipeFromAllFavorites(recipeId)
        triedRecipeRepository.deleteTriedRecipesByRecipe(recipeId)
        tipRepository.deleteTipsByRecipe(recipeId)
    }
}
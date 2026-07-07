package it.polito.mad.cookbookcommunity.data

import it.polito.mad.cookbookcommunity.data.auth.AuthRepository
import it.polito.mad.cookbookcommunity.data.auth.FirebaseAuthRepository
import it.polito.mad.cookbookcommunity.data.notification.FirestoreNotificationRepository
import it.polito.mad.cookbookcommunity.data.notification.NotificationRepository
import it.polito.mad.cookbookcommunity.data.recipe.FirestoreRecipeRepository
import it.polito.mad.cookbookcommunity.data.favorite.FavoriteCollectionRepository
import it.polito.mad.cookbookcommunity.data.repository.FirestoreTipRepository
import it.polito.mad.cookbookcommunity.data.repository.FirestoreTriedRecipeRepository
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.data.favorite.FavoriteRepository
import it.polito.mad.cookbookcommunity.data.favorite.FirestoreFavoriteCollectionRepository
import it.polito.mad.cookbookcommunity.data.favorite.FirestoreFavoriteRepository
import it.polito.mad.cookbookcommunity.data.repository.FirestoreSeedRepository
import it.polito.mad.cookbookcommunity.data.review.ReviewRepository
import it.polito.mad.cookbookcommunity.data.repository.TipRepository
import it.polito.mad.cookbookcommunity.data.repository.TriedRecipeRepository
import it.polito.mad.cookbookcommunity.data.review.FirestoreReviewRepository
import it.polito.mad.cookbookcommunity.data.user.FirestoreUserRepository
import it.polito.mad.cookbookcommunity.data.user.UserRepository
import it.polito.mad.cookbookcommunity.domain.usecase.DeleteRecipeProposalUseCase
import it.polito.mad.cookbookcommunity.session.SessionManager

class AppContainer {
    val authRepository: AuthRepository =
        FirebaseAuthRepository()

    val notificationRepository: NotificationRepository =
        FirestoreNotificationRepository()

    val userProfileRepository: UserRepository =
        FirestoreUserRepository()

    val favoriteRepository: FavoriteRepository =
        FirestoreFavoriteRepository()

    val favoriteCollectionRepository: FavoriteCollectionRepository =
        FirestoreFavoriteCollectionRepository()

    val triedRecipeRepository: TriedRecipeRepository =
        FirestoreTriedRecipeRepository()

    val tipRepository: TipRepository =
        FirestoreTipRepository()

    val reviewRepository: ReviewRepository =
        FirestoreReviewRepository(notificationRepository)

    val recipeRepository: RecipeRepository =
        FirestoreRecipeRepository(notificationRepository)

    val seedRepository: FirestoreSeedRepository =
        FirestoreSeedRepository()

    val deleteRecipeProposalUseCase: DeleteRecipeProposalUseCase =
        DeleteRecipeProposalUseCase(
            recipeRepository = recipeRepository,
            reviewRepository = reviewRepository,
            favoriteRepository = favoriteRepository,
            favoriteCollectionRepository = favoriteCollectionRepository,
            tipRepository = tipRepository,
            triedRecipeRepository = triedRecipeRepository,
        )

    init {
        SessionManager.init(authRepository)
    }
}

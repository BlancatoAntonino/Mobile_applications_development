package it.polito.mad.cookbookcommunity.data.firestore

object FirestoreCollections {

    const val USERS         = "users"
    const val RECIPES       = "recipes"
    const val REVIEWS       = "reviews"
    const val TIPS          = "tips"
    const val TRIED_RECIPES = "triedRecipes"
    const val FAVORITES     = "favorites"
    const val FAVORITE_COLLECTIONS = "favoriteCollections"
    const val NOTIFICATIONS = "notifications"

    object User {
        const val ID                   = "id"
        const val EMAIL                = "email"
        const val DISPLAY_NAME         = "displayName"
        const val FIRST_NAME           = "firstName"
        const val LAST_NAME            = "lastName"
        const val NICKNAME             = "nickname"
        const val PHOTO_URL            = "photoUrl"
        const val BIO                  = "bio"
        const val COOKING_ROLE         = "cookingRole"
        const val FAVORITE_CUISINES    = "favoriteCuisines"
        const val DIETARY_RESTRICTIONS = "dietaryRestrictions"
        const val FAVORITE_INGREDIENTS = "favoriteIngredients"
        const val ALLERGIES            = "allergies"
        const val CREATED_AT           = "createdAt"
        const val UPDATED_AT           = "updatedAt"
    }


    object Recipe {
        const val ID                            = "id"
        const val OWNER_ID                      = "ownerId"
        const val OWNER_DISPLAY_NAME            = "ownerDisplayName"
        const val OWNER_PHOTO_URL               = "ownerPhotoUrl"
        const val TITLE                         = "title"
        const val DESCRIPTION                   = "description"
        const val IMAGE_URI                     = "imageUri"
        const val SERVINGS                      = "servings"
        const val PRICE_RANGE                   = "priceRange"
        const val DIFFICULTY                    = "difficulty"
        const val COOK_TIME_MINUTES             = "cookTimeMinutes"
        const val CALORIES                      = "calories"
        const val RECIPE_TYPE                   = "recipeType"
        const val CUISINE_TYPE                  = "cuisineType"
        const val DIETARY_RESTRICTION           = "dietaryRestriction"
        const val INGREDIENTS                   = "ingredients"
        const val STEPS                         = "steps"
        const val ORIGINAL_RECIPE_ID            = "originalRecipeId"
        const val ORIGINAL_RECIPE_TITLE         = "originalRecipeTitle"
        const val ORIGINAL_RECIPE_IMAGE_URI     = "originalRecipeImageUri"
        const val ORIGINAL_AUTHOR_ID            = "originalAuthorId"
        const val ORIGINAL_AUTHOR_DISPLAY_NAME  = "originalAuthorDisplayName"
        const val ORIGINAL_AUTHOR_PHOTO_URL     = "originalAuthorPhotoUrl"
        const val ADAPTATION_NOTE               = "adaptationNote"
        const val CREATED_AT                    = "createdAt"
        const val UPDATED_AT                    = "updatedAt"
        const val AVERAGE_RATING                = "averageRating"
        const val REVIEW_COUNT                  = "reviewCount"

    }


    object Review {
        const val ID                  = "id"
        const val RECIPE_ID           = "recipeId"
        const val RECIPE_TITLE        = "recipeTitle"
        const val RECIPE_OWNER_ID     = "recipeOwnerId"
        const val AUTHOR_ID           = "authorId"
        const val AUTHOR_DISPLAY_NAME = "authorDisplayName"
        const val AUTHOR_PHOTO_URL    = "authorPhotoUrl"
        const val RATING              = "rating"
        const val TITLE               = "title"
        const val TEXT                = "text"
        const val PHOTO_URI           = "photoUri"
        const val CREATED_AT          = "createdAt"
        const val UPDATED_AT          = "updatedAt"
    }

    object Tip {
        const val ID         = "id"
        const val RECIPE_ID  = "recipeId"
        const val AUTHOR_ID  = "authorId"
        const val TEXT       = "text"
        const val CREATED_AT = "createdAt"
        const val UPDATED_AT = "updatedAt"
    }

    object TriedRecipe {
        const val ID                = "id"
        const val RECIPE_ID         = "recipeId"
        const val USER_ID           = "userId"
        const val REVIEW_ID         = "reviewId"
        const val COOKED_AT         = "cookedAt"
        const val RESULT            = "result"
        const val MODIFICATIONS     = "modifications"
        const val PERSONAL_NOTE     = "personalNote"
        const val FINAL_PHOTO_URI   = "finalPhotoUri"
        const val WOULD_COOK_AGAIN  = "wouldCookAgain"
        const val RECIPE_TITLE      = "recipeTitle"
        const val RECIPE_IMAGE_URI  = "recipeImageUri"
        const val CREATED_AT        = "createdAt"
        const val UPDATED_AT        = "updatedAt"
        const val NOTES             = "notes"
    }


    object Favorite {
        const val ID               = "id"
        const val USER_ID          = "userId"
        const val RECIPE_ID        = "recipeId"
        const val RECIPE_TITLE     = "recipeTitle"
        const val RECIPE_IMAGE_URI = "recipeImageUri"
        const val RECIPE_OWNER_ID  = "recipeOwnerId"
        const val COLLECTION_ID    = "collectionId"
        const val COLLECTION_NAME  = "collectionName"
        const val SAVED_AT         = "savedAt"
    }

    object FavoriteCollection {
        const val ID         = "id"
        const val OWNER_ID   = "ownerId"
        const val NAME       = "name"
        const val CREATED_AT = "createdAt"
        const val UPDATED_AT = "updatedAt"
        const val IS_SYSTEM  = "isSystem"
    }

    object Notification {
        const val ID                  = "id"
        const val RECIPIENT_USER_ID   = "recipientUserId"
        const val ACTOR_USER_ID       = "actorUserId"
        const val ACTOR_DISPLAY_NAME  = "actorDisplayName"
        const val TYPE                = "type"
        const val TITLE               = "title"
        const val MESSAGE             = "message"
        const val RECIPE_ID           = "recipeId"
        const val RECIPE_TITLE        = "recipeTitle"
        const val REVIEW_ID           = "reviewId"
        const val READ                = "read"
        const val CREATED_AT          = "createdAt"
    }
}

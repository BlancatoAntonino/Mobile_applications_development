package it.polito.mad.cookbookcommunity.data.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import it.polito.mad.cookbookcommunity.data.firestore.FirestoreCollections
import it.polito.mad.cookbookcommunity.model.collection.Favorite
import it.polito.mad.cookbookcommunity.model.collection.FavoriteCollection
import it.polito.mad.cookbookcommunity.model.review.CookingResult
import kotlinx.coroutines.tasks.await

class FirestoreSeedRepository {

    private val db = Firebase.firestore

    suspend fun seedIfEmpty(
        loggedUserId: String,
        loggedUserDisplayName: String
    ) {
        val recipesSnap = db.collection(FirestoreCollections.RECIPES)
            .limit(1)
            .get()
            .await()

        if (recipesSnap.isEmpty) {
            val otherUserId = "2"
            val otherDisplayName = "Luca Ferretti"

            seedUsers(loggedUserId, loggedUserDisplayName)
            seedRecipes(
                loggedUserId = loggedUserId,
                loggedDisplayName = loggedUserDisplayName,
                otherUserId = otherUserId,
                otherDisplayName = otherDisplayName
            )
            seedReviews(
                loggedUserId = loggedUserId,
                loggedDisplayName = loggedUserDisplayName,
                otherUserId = otherUserId,
                otherDisplayName = otherDisplayName
            )
            seedFavorites(loggedUserId)
            seedFavoriteCollections(loggedUserId)
            seedNotifications(
                loggedUserId = loggedUserId,
                otherUserId = otherUserId,
                otherDisplayName = otherDisplayName
            )
        }

        seedDiaryEntriesIfMissing(loggedUserId)
    }

    private suspend fun seedUsers(loggedUserId: String, loggedUserDisplayName: String) {
        val users = listOf(
            userMap(
                id                   = loggedUserId,
                email                = "martini.michela02@gmail.com",
                firstName            = "Michela",
                lastName             = "Martini",
                nickname             = "michela",
                bio                  = "Passionate home cook who loves experimenting with Italian recipes.",
                cookingRole          = "HOME_COOK",
                favoriteCuisines     = listOf("ITALIAN", "MEDITERRANEAN"),
                dietaryRestrictions  = emptyList(),
                favoriteIngredients  = listOf("Pasta", "Olive oil", "Tomatoes"),
                allergies            = emptyList()
            ),
            userMap(
                id                   = "2",
                email                = "fabianafimiani@gmail.com",
                firstName            = "Fabiana",
                lastName             = "Fimiani",
                nickname             = "fabiana",
                bio                  = "Food enthusiast and amateur photographer. I cook, therefore I am.",
                cookingRole          = "FOOD_LOVER",
                favoriteCuisines     = listOf("ITALIAN", "ASIAN", "MEXICAN"),
                dietaryRestrictions  = emptyList(),
                favoriteIngredients  = listOf("Avocado", "Feta", "Lentils"),
                allergies            = emptyList()
            ),
            userMap(
                id                   = "3",
                email                = "riccardimattia02@gmail.com",
                firstName            = "Mattia",
                lastName             = "Riccardi",
                nickname             = "mattia",
                bio                  = "Pizza lover and weekend grill master.",
                cookingRole          = "HOME_COOK",
                favoriteCuisines     = listOf("ITALIAN", "AMERICAN"),
                dietaryRestrictions  = emptyList(),
                favoriteIngredients  = listOf("Mozzarella", "Beef", "Basil"),
                allergies            = emptyList()
            ),
            userMap(
                id                   = "4",
                email                = "antoninoblancat01@gmail.com",
                firstName            = "Antonino",
                lastName             = "Blancati",
                nickname             = "antonino",
                bio                  = "Professional chef with a passion for Mediterranean cuisine.",
                cookingRole          = "PROFESSIONAL_CHEF",
                favoriteCuisines     = listOf("MEDITERRANEAN", "ITALIAN", "MIDDLE_EASTERN"),
                dietaryRestrictions  = listOf("GLUTEN_FREE"),
                favoriteIngredients  = listOf("Olive oil", "Garlic", "Lemon"),
                allergies            = emptyList()
            )
        )

        for (user in users) {
            db.collection(FirestoreCollections.USERS)
                .document(user[FirestoreCollections.User.ID] as String)
                .set(user)
                .await()
        }
    }

    private fun userMap(
        id: String,
        email: String,
        firstName: String,
        lastName: String,
        nickname: String,
        bio: String,
        cookingRole: String,
        favoriteCuisines: List<String>,
        dietaryRestrictions: List<String>,
        favoriteIngredients: List<String>,
        allergies: List<String>
    ): Map<String, Any?> = mapOf(
        FirestoreCollections.User.ID                   to id,
        FirestoreCollections.User.EMAIL                to email,
        FirestoreCollections.User.DISPLAY_NAME         to "$firstName $lastName".trim(),
        FirestoreCollections.User.FIRST_NAME           to firstName,
        FirestoreCollections.User.LAST_NAME            to lastName,
        FirestoreCollections.User.NICKNAME             to nickname,
        FirestoreCollections.User.PHOTO_URL            to "",
        FirestoreCollections.User.BIO                  to bio,
        FirestoreCollections.User.COOKING_ROLE         to cookingRole,
        FirestoreCollections.User.FAVORITE_CUISINES    to favoriteCuisines,
        FirestoreCollections.User.DIETARY_RESTRICTIONS to dietaryRestrictions,
        FirestoreCollections.User.FAVORITE_INGREDIENTS to favoriteIngredients,
        FirestoreCollections.User.ALLERGIES            to allergies,
        FirestoreCollections.User.CREATED_AT           to System.currentTimeMillis(),
        FirestoreCollections.User.UPDATED_AT           to System.currentTimeMillis()
    )

    private suspend fun seedRecipes(
        loggedUserId: String,
        loggedDisplayName: String,
        otherUserId: String,
        otherDisplayName: String
    ) {
        val recipes = listOf(
            recipeMap(
                id = "1",
                ownerId = loggedUserId,
                ownerDisplayName = loggedDisplayName,
                title = "Pasta al pomodoro",
                description = "Simple Italian pasta with tomato sauce.",
                recipeType = "MAIN_COURSE",
                cuisineType = "ITALIAN",
                dietaryRestrictions = listOf("VEGETARIAN"),
                cookTimeMinutes = 20,
                priceRange = "LOW",
                difficulty = "EASY",
                servings = 2,
                calories = 520,
                averageRating = 4.75,
                reviewCount = 4,
                imageUri = "file:///android_asset/pasta.jpg",
                ingredients = listOf(
                    ingredientMap("1", "Pasta",          180.0, "G"),
                    ingredientMap("2", "Tomato sauce",   200.0, "ML"),
                    ingredientMap("3", "Olive oil",      2.0,   "TBSP"),
                    ingredientMap("4", "Garlic",         2.0,   "CLOVES"),
                    ingredientMap("5", "Salt",           null,  "TO_TASTE")
                ),
                steps = listOf(
                    stepMap("1", 1, "Boil salted water and cook pasta until al dente."),
                    stepMap("2", 2, "Sauté garlic in olive oil, add tomato sauce, simmer 10 min."),
                    stepMap("3", 3, "Drain pasta, toss with sauce, serve immediately.")
                )
            ),
            recipeMap(
                id = "2",
                ownerId = loggedUserId,
                ownerDisplayName = loggedDisplayName,
                title = "Tiramisu",
                description = "Classic Italian dessert with mascarpone and coffee.",
                recipeType = "DESSERT",
                cuisineType = "ITALIAN",
                dietaryRestrictions = emptyList(),
                cookTimeMinutes = 30,
                priceRange = "MEDIUM",
                difficulty = "MEDIUM",
                servings = 6,
                calories = 420,
                averageRating = 0.0,
                reviewCount = 0,
                ingredients = listOf(
                    ingredientMap("6", "Mascarpone",    500.0, "G"),
                    ingredientMap("7", "Eggs",          4.0,   "UNITS"),
                    ingredientMap("8", "Sugar",         100.0, "G"),
                    ingredientMap("9", "Espresso",      200.0, "ML"),
                    ingredientMap("10", "Ladyfingers",   200.0, "G"),
                    ingredientMap("11", "Cocoa powder",  null,  "TO_TASTE")
                ),
                steps = listOf(
                    stepMap("4", 1, "Whip egg yolks with sugar until pale."),
                    stepMap("5", 2, "Fold in mascarpone until smooth."),
                    stepMap("6", 3, "Dip ladyfingers in cold espresso, layer with cream."),
                    stepMap("7", 4, "Refrigerate at least 4 hours, dust with cocoa before serving.")
                )
            ),
            // ── 6 owned by other user ────────────────────────────────────────
            recipeMap(
                id = "3",
                ownerId = otherUserId,
                ownerDisplayName = otherDisplayName,
                title = "Greek Salad",
                description = "Fresh Mediterranean salad with feta and olives.",
                recipeType = "SALAD",
                cuisineType = "GREEK",
                dietaryRestrictions = listOf("VEGETARIAN", "GLUTEN_FREE"),
                cookTimeMinutes = 10,
                priceRange = "LOW",
                difficulty = "EASY",
                servings = 2,
                calories = 210,
                averageRating = 4.5,
                reviewCount = 4,
                ingredients = listOf(
                    ingredientMap("12", "Tomatoes",      3.0,   "UNITS"),
                    ingredientMap("13", "Cucumber",      1.0,   "UNITS"),
                    ingredientMap("14", "Feta cheese",   100.0, "G"),
                    ingredientMap("15", "Kalamata olives", 50.0, "G"),
                    ingredientMap("16", "Olive oil",     3.0,   "TBSP")
                ),
                steps = listOf(
                    stepMap("8", 1, "Chop vegetables into bite-sized chunks."),
                    stepMap("9", 2, "Toss with olive oil and season with salt and oregano."),
                    stepMap("10", 3, "Top with crumbled feta and olives.")
                )
            ),
            recipeMap(
                id = "4",
                ownerId = otherUserId,
                ownerDisplayName = otherDisplayName,
                title = "Avocado Toast",
                description = "Quick and healthy breakfast toast.",
                recipeType = "BREAKFAST",
                cuisineType = "AMERICAN",
                dietaryRestrictions = listOf("VEGAN"),
                cookTimeMinutes = 5,
                priceRange = "LOW",
                difficulty = "EASY",
                servings = 1,
                calories = 320,
                averageRating = 0.0,
                reviewCount = 0,
                ingredients = listOf(
                    ingredientMap("17", "Sourdough bread", 2.0, "SLICES"),
                    ingredientMap("18", "Avocado",         1.0, "UNITS"),
                    ingredientMap("19", "Lemon juice",     null,"TO_TASTE"),
                    ingredientMap("20", "Red pepper flakes", null, "TO_TASTE")
                ),
                steps = listOf(
                    stepMap("11", 1, "Toast the bread slices."),
                    stepMap("12", 2, "Mash avocado with lemon juice and salt."),
                    stepMap("13", 3, "Spread on toast, add red pepper flakes.")
                )
            ),
            recipeMap(
                id = "5",
                ownerId = otherUserId,
                ownerDisplayName = otherDisplayName,
                title = "Beef Tacos",
                description = "Classic Mexican street-style beef tacos.",
                recipeType = "MAIN_COURSE",
                cuisineType = "MEXICAN",
                dietaryRestrictions = emptyList(),
                cookTimeMinutes = 25,
                priceRange = "MEDIUM",
                difficulty = "EASY",
                servings = 4,
                calories = 480,
                averageRating = 0.0,
                reviewCount = 0,
                ingredients = listOf(
                    ingredientMap("21", "Ground beef",   400.0, "G"),
                    ingredientMap("22", "Taco shells",   8.0,   "UNITS"),
                    ingredientMap("23", "Onion",         1.0,   "UNITS"),
                    ingredientMap("24", "Cumin",         1.0,   "TSP")
                ),
                steps = listOf(
                    stepMap("14", 1, "Brown the ground beef with diced onion."),
                    stepMap("15", 2, "Season with cumin, salt, pepper."),
                    stepMap("16", 3, "Fill taco shells and top with salsa.")
                )
            ),
            recipeMap(
                id = "6",
                ownerId = otherUserId,
                ownerDisplayName = otherDisplayName,
                title = "Margherita Pizza",
                description = "Neapolitan pizza with tomato, mozzarella and basil.",
                recipeType = "MAIN_COURSE",
                cuisineType = "ITALIAN",
                dietaryRestrictions = listOf("VEGETARIAN"),
                cookTimeMinutes = 25,
                priceRange = "LOW",
                difficulty = "MEDIUM",
                servings = 2,
                calories = 700,
                averageRating = 0.0,
                reviewCount = 0,
                imageUri = "file:///android_asset/pizza.jpg",
                ingredients = listOf(
                    ingredientMap("25", "Pizza dough",     250.0, "G"),
                    ingredientMap("26", "Tomato sauce",    150.0, "ML"),
                    ingredientMap("27", "Mozzarella",      125.0, "G"),
                    ingredientMap("28", "Fresh basil",     null,  "TO_TASTE")
                ),
                steps = listOf(
                    stepMap("17", 1, "Preheat oven to 250 °C."),
                    stepMap("18", 2, "Stretch dough, spread sauce, add mozzarella."),
                    stepMap("19", 3, "Bake 10-12 min, finish with fresh basil.")
                )
            ),
            recipeMap(
                id = "7",
                ownerId = otherUserId,
                ownerDisplayName = otherDisplayName,
                title = "Lentil Soup",
                description = "Hearty and warming vegan lentil soup.",
                recipeType = "SOUP",
                cuisineType = "MEDITERRANEAN",
                dietaryRestrictions = listOf("VEGAN", "GLUTEN_FREE"),
                cookTimeMinutes = 40,
                priceRange = "LOW",
                difficulty = "EASY",
                servings = 4,
                calories = 280,
                averageRating = 0.0,
                reviewCount = 0,
                ingredients = listOf(
                    ingredientMap("29", "Red lentils",  200.0, "G"),
                    ingredientMap("30", "Carrot",       1.0,   "UNITS"),
                    ingredientMap("31", "Celery",       2.0,   "STALKS"),
                    ingredientMap("32", "Vegetable broth", 1.0, "L"),
                    ingredientMap("33", "Turmeric",     1.0,   "TSP")
                ),
                steps = listOf(
                    stepMap("20", 1, "Sauté carrot and celery in olive oil."),
                    stepMap("21", 2, "Add rinsed lentils and broth, bring to boil."),
                    stepMap("22", 3, "Simmer 30 min, season with turmeric and salt.")
                )
            ),
            recipeMap(
                id = "8",
                ownerId = otherUserId,
                ownerDisplayName = otherDisplayName,
                title = "Chocolate Chip Cookies",
                description = "Crispy on the edges, chewy in the middle.",
                recipeType = "DESSERT",
                cuisineType = "AMERICAN",
                dietaryRestrictions = listOf("VEGETARIAN"),
                cookTimeMinutes = 20,
                priceRange = "LOW",
                difficulty = "EASY",
                servings = 24,
                calories = 150,
                averageRating = 0.0,
                reviewCount = 0,
                ingredients = listOf(
                    ingredientMap("34", "Flour",           280.0, "G"),
                    ingredientMap("35", "Butter",          230.0, "G"),
                    ingredientMap("36", "Brown sugar",     200.0, "G"),
                    ingredientMap("37", "Egg",             2.0,   "UNITS"),
                    ingredientMap("38", "Chocolate chips", 200.0, "G")
                ),
                steps = listOf(
                    stepMap("23", 1, "Cream butter and sugar. Beat in eggs."),
                    stepMap("24", 2, "Mix in flour and chocolate chips."),
                    stepMap("25", 3, "Bake at 180 °C for 10-12 min.")
                )
            ),

            recipeMap(
                id = "9",
                ownerId = otherUserId,
                ownerDisplayName = otherDisplayName,
                title = "Whole wheat pasta al pomodoro",
                description = "A lighter adaptation of classic tomato pasta wheat pasta and fresh basil.",
                recipeType = "MAIN_COURSE",
                cuisineType = "ITALIAN",
                dietaryRestrictions = listOf("VEGETARIAN"),
                cookTimeMinutes = 22,
                priceRange = "LOW",
                difficulty = "EASY",
                servings = 2,
                calories = 500,
                averageRating = 0.0,
                reviewCount = 0,
                imageUri = "file:///android_asset/pasta.jpg",
                originalRecipeId = "1",
                originalRecipeTitle = "Pasta al pomodoro",
                originalRecipeImageUri = "file:///android_asset/pasta.jpg",
                originalAuthorId = loggedUserId,
                originalAuthorDisplayName = loggedDisplayName,
                adaptationNote = "I used whole wheat pasta and added fresh basil for a richer, higher-fiber version.",
                ingredients = listOf(
                    ingredientMap("39", "Whole wheat pasta", 180.0, "G"),
                    ingredientMap("40", "Tomato sauce", 200.0, "ML"),
                    ingredientMap("41", "Olive oil", 2.0, "TBSP"),
                    ingredientMap("42", "Garlic", 2.0, "CLOVES"),
                    ingredientMap("43", "Fresh basil", null, "TO_TASTE"),
                    ingredientMap("44", "Salt", null, "TO_TASTE")
                ),
                steps = listOf(
                    stepMap("26", 1, "Boil salted water and cook the whole wheat pasta until al dente."),
                    stepMap("27", 2, "Sauté garlic in olive oil, add tomato sauce and simmer for 10 minutes."),
                    stepMap("28", 3, "Toss pasta with the sauce and finish with fresh basil.")
                )
            ),

            recipeMap(
                id = "10",
                ownerId = loggedUserId,
                ownerDisplayName = loggedDisplayName,
                title = "Light Margherita pizza",
                description = "A homemade adaptation of Margherita pizza with less mozzarella and a crisper base.",
                recipeType = "MAIN_COURSE",
                cuisineType = "ITALIAN",
                dietaryRestrictions = listOf("VEGETARIAN"),
                cookTimeMinutes = 30,
                priceRange = "LOW",
                difficulty = "MEDIUM",
                servings = 2,
                calories = 610,
                averageRating = 0.0,
                reviewCount = 0,
                imageUri = "file:///android_asset/pizza.jpg",
                originalRecipeId = "6",
                originalRecipeTitle = "Margherita Pizza",
                originalRecipeImageUri = "file:///android_asset/pizza.jpg",
                originalAuthorId = otherUserId,
                originalAuthorDisplayName = otherDisplayName,
                adaptationNote = "I reduced the mozzarella and preheated the tray longer to get a crisper base.",
                ingredients = listOf(
                    ingredientMap("45", "Pizza dough", 250.0, "G"),
                    ingredientMap("46", "Tomato sauce", 150.0, "ML"),
                    ingredientMap("47", "Mozzarella", 80.0, "G"),
                    ingredientMap("48", "Fresh basil", null, "TO_TASTE"),
                    ingredientMap("49", "Olive oil", 1.0, "TBSP")
                ),
                steps = listOf(
                    stepMap("29", 1, "Preheat the oven and baking the tray to 250 °C."),
                    stepMap("30", 2, "Stretch the dough thinly and spread the tomato sauce."),
                    stepMap("31", 3, "Add a reduced amount of mozzarella and bake until crisp."),
                    stepMap("32", 4, "Finish with fresh basil and a drizzle of olive oil.")
                )
            )
        )

        for (recipe in recipes) {
            db.collection(FirestoreCollections.RECIPES)
                .document(recipe["id"] as String)
                .set(recipe)
                .await()
        }
    }
    private suspend fun seedReviews(
        loggedUserId: String,
        loggedDisplayName: String,
        otherUserId: String,
        otherDisplayName: String
    ) {
        val reviews = listOf(
            reviewMap("1", "1", "Pasta al pomodoro", loggedUserId,
                otherUserId, "Fabiana Fimiani", 5, "Perfect!", "Easy and delicious."),
            reviewMap("2", "1", "Pasta al pomodoro", loggedUserId,
                "3", "Mattia Riccardi", 5, "Family favourite", "My kids love this."),
            reviewMap("3", "1", "Pasta al pomodoro", loggedUserId,
                "4", "Antonino Blancati", 4, "Very good", "Simple and authentic."),
            reviewMap("4", "3", "Greek Salad", otherUserId,
                loggedUserId, "Michela Martini", 5, "So fresh!", "Perfect for summer."),
            reviewMap("5", "3", "Greek Salad", otherUserId,
                "3", "Mattia Riccardi", 4, "Loved it", "The feta makes it."),
            reviewMap("6", "3", "Greek Salad", otherUserId,
                "4", "Antonino Blancati", 5, "Top notch", "Restaurant quality at home.")
        )

        for (review in reviews) {
            db.collection(FirestoreCollections.REVIEWS)
                .document(review["id"] as String)
                .set(review)
                .await()
        }
    }

    private suspend fun seedDiaryEntriesIfMissing(
        loggedUserId: String
    ) {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24L * 60L * 60L * 1000L

        val entries = listOf(
            diaryEntryMap(
                id = "seed_diary_${loggedUserId}_recipe_3",
                recipeId = "3",
                userId = loggedUserId,
                reviewId = "4",
                cookedAt = now - 9L * oneDayMillis,
                result = CookingResult.EXCELLENT,
                modifications = "Added extra oregano and a squeeze of lemon before serving.",
                personalNote = "Fresh, quick and ideal for a warm evening. The feta was the best part.",
                finalPhotoUri = "file:///android_asset/greek_salad.jpg",
                wouldCookAgain = true,
                recipeTitle = "Greek Salad",
                recipeImageUri = "file:///android_asset/greek_salad.jpg"
            ),

            diaryEntryMap(
                id = "seed_diary_${loggedUserId}_recipe_4",
                recipeId = "4",
                userId = loggedUserId,
                reviewId = null,
                cookedAt = now - 6L * oneDayMillis,
                result = CookingResult.GOOD,
                modifications = "User wholegrain toast and added chili flakes.",
                personalNote = "A reliable breakfast when I need something fast before class.",
                finalPhotoUri = "file:///android_asset/avocado_toast.jpg",
                wouldCookAgain = true,
                recipeTitle = "Avocado Toast",
                recipeImageUri = "file:///android_asset/avocado_toast.jpg"
            ),

            diaryEntryMap(
                id = "seed_diary_${loggedUserId}_recipe_6",
                recipeId = "6",
                userId = loggedUserId,
                reviewId = null,
                cookedAt = now - 3L * oneDayMillis,
                result = CookingResult.FAILED,
                modifications = """
                    I used a baking tray that was not hot enough and rolled the dough too thick.
                    Next time I need to preheat the tray for longer, stretch the dough more carefully
                    and reduce the amount of mozzarella so the centre stays crisp.
                """.trimIndent(),
                personalNote = "The flavour was fine, but the base remained soft and the topping released too much water.",
                finalPhotoUri = "",
                wouldCookAgain = false,
                recipeTitle = "Margherita Pizza",
                recipeImageUri = "file:///android_asset/pizza.jpg"
            ),

            diaryEntryMap(
                id = "seed_diary_${loggedUserId}_recipe_8",
                recipeId = "8",
                userId = loggedUserId,
                reviewId = null,
                cookedAt = now - oneDayMillis,
                result = CookingResult.OK,
                modifications = "Reduced the sugar slightly and baked one minute less.",
                personalNote = "Nice texture, although I would use darker chocolate next time.",
                finalPhotoUri = "file:///android_asset/cookies.jpg",
                wouldCookAgain = true,
                recipeTitle = "Chocolate Chip Cookies",
                recipeImageUri = "file:///android_asset/cookies.jpg"
            )
        )

        for (entry in entries) {
            val recipeId = entry[FirestoreCollections.TriedRecipe.RECIPE_ID] as String

            val existingEntry = db.collection(FirestoreCollections.TRIED_RECIPES)
                .whereEqualTo(
                    FirestoreCollections.TriedRecipe.USER_ID,
                    loggedUserId
                )
                .whereEqualTo(
                    FirestoreCollections.TriedRecipe.RECIPE_ID,
                    recipeId
                )
                .limit(1)
                .get()
                .await()

            if (!existingEntry.isEmpty) {
                continue
            }

            val entryId = entry[FirestoreCollections.TriedRecipe.ID] as String

            db.collection(FirestoreCollections.TRIED_RECIPES)
                .document(entryId)
                .set(entry)
                .await()
        }
    }

    private suspend fun seedFavorites(loggedUserId: String) {
        val savedName = FavoriteCollection.SAVED_RECIPES_NAME

        val favorites = listOf(
            Favorite(
                id             = Favorite.idFor(loggedUserId, "3", savedName),
                userId         = loggedUserId,
                recipeId       = "3",
                recipeTitle    = "Greek Salad",
                recipeImageUri = "",
                recipeOwnerId  = "2",
                collectionId   = FavoriteCollection.savedRecipesId(loggedUserId),
                collectionName = savedName
            ),
            Favorite(
                id             = Favorite.idFor(loggedUserId, "4", savedName),
                userId         = loggedUserId,
                recipeId       = "4",
                recipeTitle    = "Avocado Toast",
                recipeImageUri = "",
                recipeOwnerId  = "2",
                collectionId   = FavoriteCollection.savedRecipesId(loggedUserId),
                collectionName = savedName
            ),
            Favorite(
                id             = Favorite.idFor(loggedUserId, "5", savedName),
                userId         = loggedUserId,
                recipeId       = "5",
                recipeTitle    = "Beef Tacos",
                recipeImageUri = "",
                recipeOwnerId  = "2",
                collectionId   = FavoriteCollection.savedRecipesId(loggedUserId),
                collectionName = savedName
            ),
            Favorite(
                id             = Favorite.idFor(loggedUserId, "6", savedName),
                userId         = loggedUserId,
                recipeId       = "6",
                recipeTitle    = "Margherita Pizza",
                recipeImageUri = "",
                recipeOwnerId  = "2",
                collectionId   = FavoriteCollection.savedRecipesId(loggedUserId),
                collectionName = savedName
            )
        )

        for (fav in favorites) {
            db.collection(FirestoreCollections.FAVORITES)
                .document(fav.id)
                .set(favToMap(fav))
                .await()
        }
    }

    private suspend fun seedFavoriteCollections(loggedUserId: String) {
        val savedId = FavoriteCollection.savedRecipesId(loggedUserId)
        val existing = db.collection(FirestoreCollections.FAVORITE_COLLECTIONS)
            .document(savedId).get().await()
        if (existing.exists()) return

        val systemCollection = FavoriteCollection(
            id       = savedId,
            ownerId  = loggedUserId,
            name     = FavoriteCollection.SAVED_RECIPES_NAME,
            isSystem = true
        )
        db.collection(FirestoreCollections.FAVORITE_COLLECTIONS)
            .document(savedId)
            .set(collectionToMap(systemCollection))
            .await()
    }

    private suspend fun seedNotifications(
        loggedUserId: String,
        otherUserId: String,
        otherDisplayName: String
    ) {
        val notifications = listOf(
            notificationMap(
                id              = "1",
                recipientId     = loggedUserId,
                actorId         = otherUserId,
                actorName       = otherDisplayName,
                type            = "REVIEW_RECEIVED",
                title           = "New review on your recipe",
                message         = "$otherDisplayName reviewed your recipe Pasta al pomodoro.",
                recipeId        = "1",
                recipeTitle     = "Pasta al pomodoro",
                reviewId        = "1",
                read            = false
            ),
            notificationMap(
                id              = "2",
                recipientId     = loggedUserId,
                actorId         = otherUserId,
                actorName       = otherDisplayName,
                type            = "RECOMMENDED_RECIPE",
                title           = "Recipe you might like",
                message         = "Check out the new recipe Greek Salad!",
                recipeId        = "3",
                recipeTitle     = "Greek Salad",
                reviewId        = null,
                read            = false
            )
        )

        for (notif in notifications) {
            db.collection(FirestoreCollections.NOTIFICATIONS)
                .document(notif["id"] as String)
                .set(notif)
                .await()
        }
    }

    private fun recipeMap(
        id: String,
        ownerId: String,
        ownerDisplayName: String,
        title: String,
        description: String,
        recipeType: String,
        cuisineType: String,
        dietaryRestrictions: List<String>,
        cookTimeMinutes: Int,
        priceRange: String,
        difficulty: String,
        servings: Int,
        calories: Int,
        averageRating: Double,
        reviewCount: Int,
        ingredients: List<Map<String, Any?>>,
        steps: List<Map<String, Any?>>,
        imageUri: String = "",
        originalRecipeId: String? = null,
        originalRecipeTitle: String = "",
        originalRecipeImageUri: String = "",
        originalAuthorId: String = "",
        originalAuthorDisplayName: String = "",
        originalAuthorPhotoUrl: String = "",
        adaptationNote: String = ""
    ): Map<String, Any?> = mapOf(
        "id"                        to id,
        "ownerId"                   to ownerId,
        "ownerDisplayName"          to ownerDisplayName,
        "ownerPhotoUrl"             to "",
        "title"                     to title,
        "description"               to description,
        "imageUri"                  to imageUri,
        "servings"                  to servings,
        "priceRange"                to priceRange,
        "difficulty"                to difficulty,
        "cookTimeMinutes"           to cookTimeMinutes,
        "calories"                  to calories,
        "recipeType"                to recipeType,
        "cuisineType"               to cuisineType,
        "dietaryRestrictions"       to dietaryRestrictions,
        "ingredients"               to ingredients,
        "steps"                     to steps,
        "originalRecipeId"          to originalRecipeId,
        "originalRecipeTitle"       to originalRecipeTitle,
        "originalRecipeImageUri"    to originalRecipeImageUri,
        "originalAuthorId"          to originalAuthorId,
        "originalAuthorDisplayName" to originalAuthorDisplayName,
        "originalAuthorPhotoUrl"    to originalAuthorPhotoUrl,
        "adaptationNote"            to adaptationNote,
        "createdAt"                 to System.currentTimeMillis(),
        "updatedAt"                 to System.currentTimeMillis(),
        "averageRating"             to averageRating,
        "reviewCount"               to reviewCount
    )

    private fun ingredientMap(
        id: String,
        name: String,
        quantity: Double?,
        unit: String
    ): Map<String, Any?> = mapOf(
        "id"       to id,
        "name"     to name,
        "quantity" to quantity,
        "unit"     to unit,
        "note"     to "",
        "optional" to false
    )

    private fun stepMap(id: String, stepNumber: Int, text: String): Map<String, Any?> = mapOf(
        "id"         to id,
        "stepNumber" to stepNumber,
        "text"       to text
    )

    private fun reviewMap(
        id: String,
        recipeId: String,
        recipeTitle: String,
        recipeOwnerId: String,
        authorId: String,
        authorDisplayName: String,
        rating: Int,
        title: String,
        text: String
    ): Map<String, Any?> = mapOf(
        "id"                to id,
        "recipeId"          to recipeId,
        "recipeTitle"       to recipeTitle,
        "recipeOwnerId"     to recipeOwnerId,
        "authorId"          to authorId,
        "authorDisplayName" to authorDisplayName,
        "authorPhotoUrl"    to "",
        "rating"            to rating,
        "title"             to title,
        "text"              to text,
        "photoUri"          to null,
        "createdAt"         to System.currentTimeMillis(),
        "updatedAt"         to System.currentTimeMillis()
    )

    private fun diaryEntryMap(
        id: String,
        recipeId: String,
        userId: String,
        reviewId: String?,
        cookedAt: Long,
        result: CookingResult,
        modifications: String,
        personalNote: String,
        finalPhotoUri: String,
        wouldCookAgain: Boolean,
        recipeTitle: String,
        recipeImageUri: String
    ): Map<String, Any?> = mapOf(
        FirestoreCollections.TriedRecipe.ID to id,
        FirestoreCollections.TriedRecipe.RECIPE_ID to recipeId,
        FirestoreCollections.TriedRecipe.USER_ID to userId,
        FirestoreCollections.TriedRecipe.REVIEW_ID to reviewId,
        FirestoreCollections.TriedRecipe.COOKED_AT to cookedAt,
        FirestoreCollections.TriedRecipe.RESULT to result,
        FirestoreCollections.TriedRecipe.MODIFICATIONS to modifications,
        FirestoreCollections.TriedRecipe.PERSONAL_NOTE to personalNote,
        FirestoreCollections.TriedRecipe.FINAL_PHOTO_URI to finalPhotoUri,
        FirestoreCollections.TriedRecipe.WOULD_COOK_AGAIN to wouldCookAgain,
        FirestoreCollections.TriedRecipe.RECIPE_TITLE to recipeTitle,
        FirestoreCollections.TriedRecipe.RECIPE_IMAGE_URI to recipeImageUri,
        FirestoreCollections.TriedRecipe.CREATED_AT to cookedAt,
        FirestoreCollections.TriedRecipe.UPDATED_AT to cookedAt,
        FirestoreCollections.TriedRecipe.NOTES to null
    )

    private fun favToMap(f: Favorite): Map<String, Any?> = mapOf(
        "id"             to f.id,
        "userId"         to f.userId,
        "recipeId"       to f.recipeId,
        "recipeTitle"    to f.recipeTitle,
        "recipeImageUri" to f.recipeImageUri,
        "recipeOwnerId"  to f.recipeOwnerId,
        "collectionId"   to f.collectionId,
        "collectionName" to f.collectionName,
        "savedAt"        to f.savedAt
    )

    private fun collectionToMap(c: FavoriteCollection): Map<String, Any?> = mapOf(
        "id"        to c.id,
        "ownerId"   to c.ownerId,
        "name"      to c.name,
        "createdAt" to c.createdAt,
        "updatedAt" to c.updatedAt,
        "isSystem"  to c.isSystem
    )

    private fun notificationMap(
        id: String,
        recipientId: String,
        actorId: String,
        actorName: String,
        type: String,
        title: String,
        message: String,
        recipeId: String?,
        recipeTitle: String?,
        reviewId: String?,
        read: Boolean
    ): Map<String, Any?> = mapOf(
        "id"              to id,
        "recipientUserId" to recipientId,
        "actorUserId"     to actorId,
        "actorDisplayName" to actorName,
        "type"            to type,
        "title"           to title,
        "message"         to message,
        "recipeId"        to recipeId,
        "recipeTitle"     to recipeTitle,
        "reviewId"        to reviewId,
        "read"            to read,
        "createdAt"       to System.currentTimeMillis()
    )
}
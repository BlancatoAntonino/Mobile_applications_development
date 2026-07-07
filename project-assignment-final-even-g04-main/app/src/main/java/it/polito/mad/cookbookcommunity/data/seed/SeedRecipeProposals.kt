package it.polito.mad.cookbookcommunity.data.seed

import it.polito.mad.cookbookcommunity.model.recipe.CuisineType
import it.polito.mad.cookbookcommunity.model.recipe.DietaryRestriction
import it.polito.mad.cookbookcommunity.model.recipe.DifficultyLevel
import it.polito.mad.cookbookcommunity.model.recipe.IngredientItem
import it.polito.mad.cookbookcommunity.model.recipe.IngredientUnit
import it.polito.mad.cookbookcommunity.model.recipe.InstructionStep
import it.polito.mad.cookbookcommunity.model.recipe.PriceRange
import it.polito.mad.cookbookcommunity.model.recipe.RecipeProposal
import it.polito.mad.cookbookcommunity.model.recipe.RecipeType

object SeedRecipeProposals {
    val items: List<RecipeProposal> = listOf(
        RecipeProposal(
            ownerId = "101",
            title = "Spaghetti al pomodoro",
            description = "A quick and easy classic.",
            imageUri = "file:///android_asset/pasta.jpg",
            servings = 2,
            priceRange = PriceRange.LOW.name,
            difficulty = DifficultyLevel.EASY.name,
            cookTimeMinutes = 20,
            calories = 520,
            recipeType = RecipeType.MAIN_COURSE.name,
            cuisineType = CuisineType.ITALIAN.name,
            dietaryRestrictions = listOf(DietaryRestriction.VEGETARIAN.name),
            ingredients = listOf(
                IngredientItem(name = "Spaghetti", quantity = 180.0, unit = IngredientUnit.G.name),
                IngredientItem(name = "Tomato sauce", quantity = 250.0, unit = IngredientUnit.G.name),
                IngredientItem(name = "Olive oil", quantity = 2.0, unit = IngredientUnit.TBSP.name),
                IngredientItem(name = "Salt", unit = IngredientUnit.TO_TASTE.name),
                IngredientItem(name = "Basil", quantity = 4.0, unit = IngredientUnit.UNIT.name)
            ),
            steps = listOf(
                InstructionStep(stepNumber = 1, text = "Bring the water to a boil and add salt."),
                InstructionStep(stepNumber = 2, text = "Cook the spaghetti."),
                InstructionStep(stepNumber = 3, text = "Heat the tomato sauce with oil and basil."),
                InstructionStep(stepNumber = 4, text = "Drain the pasta and stir it into the sauce.")
            )
        ),

        RecipeProposal(
            ownerId = "101",
            title = "Protein pancakes",
            description = "Perfect for breakfast.",
            imageUri = "file:///android_asset/pancakes.jpg",
            servings = 1,
            priceRange = PriceRange.LOW.name,
            difficulty = DifficultyLevel.EASY.name,
            cookTimeMinutes = 15,
            calories = 390,
            recipeType = RecipeType.BREAKFAST.name,
            cuisineType = CuisineType.AMERICAN.name,
            ingredients = listOf(
                IngredientItem(name = "Album", quantity = 150.0, unit = IngredientUnit.ML.name),
                IngredientItem(name = "Oat flour", quantity = 50.0, unit = IngredientUnit.G.name),
                IngredientItem(name = "Banana", quantity = 1.0, unit = IngredientUnit.UNIT.name)
            ),
            steps = listOf(
                InstructionStep(stepNumber = 1, text = "Blend all the ingredients."),
                InstructionStep(stepNumber = 2, text = "Pour the mixture into the pan."),
                InstructionStep(stepNumber = 3, text = "Cook on both sides.")
            )
        ),

        RecipeProposal(
            ownerId = "202",
            title = "Chickpea curry",
            description = "A spicy and filling dish.",
            imageUri = "file:///android_asset/curry.jpg",
            servings = 3,
            priceRange = PriceRange.MEDIUM.name,
            difficulty = DifficultyLevel.MEDIUM.name,
            cookTimeMinutes = 35,
            calories = 610,
            recipeType = RecipeType.MAIN_COURSE.name,
            cuisineType = CuisineType.INDIAN.name,
            dietaryRestrictions = listOf(DietaryRestriction.VEGAN.name, DietaryRestriction.DAIRY_FREE.name),
            ingredients = listOf(
                IngredientItem(name = "Chickpeas", quantity = 240.0, unit = IngredientUnit.G.name),
                IngredientItem(name = "Coconut milk", quantity = 200.0, unit = IngredientUnit.ML.name),
                IngredientItem(name = "Curry", quantity = 2.0, unit = IngredientUnit.TBSP.name)
            ),
            steps = listOf(
                InstructionStep(stepNumber = 1, text = "Toast the spices."),
                InstructionStep(stepNumber = 2, text = "Add the chickpeas and coconut milk."),
                InstructionStep(stepNumber = 3, text = "Cook until the sauce thickens.")
            )
        ),

        RecipeProposal(
            ownerId = "303",
            title = "Classic lasagna",
            description = "A hearty first course, perfect for Sunday lunch.",
            imageUri = "file:///android_asset/lasagna.jpg",
            servings = 6,
            priceRange = PriceRange.MEDIUM.name,
            difficulty = DifficultyLevel.HARD.name,
            cookTimeMinutes = 90,
            calories = 780,
            recipeType = RecipeType.MAIN_COURSE.name,
            cuisineType = CuisineType.ITALIAN.name,
            dietaryRestrictions = emptyList(),
            ingredients = listOf(
                IngredientItem(name = "Lasagna sheets", quantity = 250.0, unit = IngredientUnit.G.name),
                IngredientItem(name = "Ground meet", quantity = 500.0, unit = IngredientUnit.G.name),
                IngredientItem(name = "Tomato sauce", quantity = 700.0, unit = IngredientUnit.G.name),
                IngredientItem(name = "Onion", quantity = 1.0, unit = IngredientUnit.UNIT.name),
                IngredientItem(name = "Carrot", quantity = 1.0, unit = IngredientUnit.UNIT.name),
                IngredientItem(name = "Celery", quantity = 1.0, unit = IngredientUnit.UNIT.name),
                IngredientItem(name = "Béchamel sauce", quantity = 500.0, unit = IngredientUnit.ML.name),
                IngredientItem(name = "Grated Parmesan cheese", quantity = 120.0, unit = IngredientUnit.G.name),
                IngredientItem(name = "Olive oil", quantity = 2.0, unit = IngredientUnit.TBSP.name),
                IngredientItem(name = "Salt", unit = IngredientUnit.TO_TASTE.name),
                IngredientItem(name = "Pepper", unit = IngredientUnit.TO_TASTE.name)
            ),
            steps = listOf(
                InstructionStep(stepNumber = 1, text = "Sauté the onion, carrot, and celery."),
                InstructionStep(stepNumber = 2, text = "Add the ground meat and brown it thoroughly."),
                InstructionStep(stepNumber = 3, text = "Add the tomato sauce and salt, then cook the meat sauce."),
                InstructionStep(stepNumber = 4, text = "In a baking dish, layer the pasta sheets, meat sauce, béchamel sauce, and Parmesan cheese."),
                InstructionStep(stepNumber = 5, text = "Repeat the layers until you run out of ingredients."),
                InstructionStep(stepNumber = 6, text = "Bake in a preheated oven at 180°C for about 35-40 minutes.")
            )
        ),

        RecipeProposal(
            ownerId = "404",
            title = "Greek salad",
            description = "A fresh and quick recipe, perfect as an appetizer or a light meal.",
            imageUri = "file:///android_asset/greek_salad.jpg",
            servings = 2,
            priceRange = PriceRange.LOW.name,
            difficulty = DifficultyLevel.EASY.name,
            cookTimeMinutes = 10,
            calories = 320,
            recipeType = RecipeType.APPETIZER.name,
            cuisineType = CuisineType.MEDITERRANEAN.name,
            dietaryRestrictions = listOf(DietaryRestriction.VEGETARIAN.name, DietaryRestriction.GLUTEN_FREE.name),
            ingredients = listOf(
                IngredientItem(name = "Tomatoes", quantity = 200.0, unit = IngredientUnit.G.name),
                IngredientItem(name = "Cucumber", quantity = 1.0, unit = IngredientUnit.UNIT.name),
                IngredientItem(name = "Feta", quantity = 120.0, unit = IngredientUnit.G.name),
                IngredientItem(name = "Black olives", quantity = 60.0, unit = IngredientUnit.G.name),
                IngredientItem(name = "Red onion", quantity = 0.5, unit = IngredientUnit.UNIT.name),
                IngredientItem(name = "Olive oil", quantity = 2.0, unit = IngredientUnit.TBSP.name),
                IngredientItem(name = "Oregano", quantity = 1.0, unit = IngredientUnit.TSP.name),
                IngredientItem(name = "Salt", unit = IngredientUnit.TO_TASTE.name)
            ),
            steps = listOf(
                InstructionStep(stepNumber = 1, text = "Chop the tomatoes, cucumber, and red onion."),
                InstructionStep(stepNumber = 2, text = "Add the cubed feta and black olives."),
                InstructionStep(stepNumber = 3, text = "Season with oil, oregano, and salt."),
                InstructionStep(stepNumber = 4, text = "Gently stir and serve immediately.")
            )
        ),

        RecipeProposal(
            ownerId = "101",
            title = "Chocolate brownies",
            description = "A rich, indulgent dessert that's perfect for sharing.",
            imageUri = "file:///android_asset/brownies.jpg",
            servings = 9,
            priceRange = PriceRange.MEDIUM.name,
            difficulty = DifficultyLevel.EASY.name,
            cookTimeMinutes = 35,
            calories = 410,
            recipeType = RecipeType.DESSERT.name,
            cuisineType = CuisineType.AMERICAN.name,
            dietaryRestrictions = emptyList(),
            ingredients = listOf(
                IngredientItem(name = "Dark chocolate", quantity = 200.0, unit = IngredientUnit.G.name),
                IngredientItem(name = "Butter", quantity = 120.0, unit = IngredientUnit.G.name),
                IngredientItem(name = "Sugar", quantity = 150.0, unit = IngredientUnit.G.name),
                IngredientItem(name = "Eggs", quantity = 3.0, unit = IngredientUnit.UNIT.name),
                IngredientItem(name = "Flour", quantity = 90.0, unit = IngredientUnit.G.name),
                IngredientItem(name = "Unsweetened cocoa powder", quantity = 20.0, unit = IngredientUnit.G.name),
                IngredientItem(name = "Salt", quantity = 1.0, unit = IngredientUnit.PINCH.name)
            ),
            steps = listOf(
                InstructionStep(stepNumber = 1, text = "Melt the chocolate and butter in a double boiler."),
                InstructionStep(stepNumber = 2, text = "Add the sugar, then the eggs one at a time."),
                InstructionStep(stepNumber = 3, text = "Stir the flour, cocoa, and salt."),
                InstructionStep(stepNumber = 4, text = "Pour the mixture into a lined baking dish."),
                InstructionStep(stepNumber = 5, text = "Bake at 180°C for about 25 minutes."),
                InstructionStep(stepNumber = 6, text = "Let it cool, then cut it into squares.")
            )
        ),

        RecipeProposal(
            ownerId = "202",
            title = "Avocado toast with an egg.",
            description = "A simple, nutritious, and quick breakfast or brunch.",
            imageUri = "file:///android_asset/avocado_toast.jpg",
            servings = 1,
            priceRange = PriceRange.LOW.name,
            difficulty = DifficultyLevel.EASY.name,
            cookTimeMinutes = 12,
            calories = 430,
            recipeType = RecipeType.BREAKFAST.name,
            cuisineType = CuisineType.OTHER.name,
            dietaryRestrictions = listOf(DietaryRestriction.VEGETARIAN.name),
            ingredients = listOf(
                IngredientItem(name = "Whole-grain bread", quantity = 2.0, unit = IngredientUnit.UNIT.name),
                IngredientItem(name = "Avocado", quantity = 1.0, unit = IngredientUnit.UNIT.name),
                IngredientItem(name = "Egg", quantity = 1.0, unit = IngredientUnit.UNIT.name),
                IngredientItem(name = "Lemon juice", quantity = 1.0, unit = IngredientUnit.TSP.name),
                IngredientItem(name = "Salt", unit = IngredientUnit.TO_TASTE.name),
                IngredientItem(name = "Pepper", unit = IngredientUnit.TO_TASTE.name)
            ),
            steps = listOf(
                InstructionStep(stepNumber = 1, text = "Toast the slices of bread."),
                InstructionStep(stepNumber = 2, text = "Mash the avocado with lemon, salt and pepper."),
                InstructionStep(stepNumber = 3, text = "Cook the egg in a pan or poach it."),
                InstructionStep(stepNumber = 4, text = "Spread the avocado on the bread and top with the egg.")
            )
        ),

        RecipeProposal(
            ownerId = "101",
            title = "Chickpea hummus",
            description = "A Middle Eastern spread, perfect for appetizers and snacks.",
            imageUri = "file:///android_asset/hummus.jpg",
            servings = 4,
            priceRange = PriceRange.LOW.name,
            difficulty = DifficultyLevel.EASY.name,
            cookTimeMinutes = 10,
            calories = 210,
            recipeType = RecipeType.SNACK.name,
            cuisineType = CuisineType.MIDDLE_EASTERN.name,
            dietaryRestrictions = listOf(DietaryRestriction.VEGAN.name, DietaryRestriction.GLUTEN_FREE.name,
                DietaryRestriction.DAIRY_FREE.name),
            ingredients = listOf(
                IngredientItem(name = "Cooked chickpeas", quantity = 240.0, unit = IngredientUnit.G.name),
                IngredientItem(name = "Tahini", quantity = 2.0, unit = IngredientUnit.TBSP.name),
                IngredientItem(name = "Lemon juice", quantity = 2.0, unit = IngredientUnit.TBSP.name),
                IngredientItem(name = "Garlic", quantity = 1.0, unit = IngredientUnit.UNIT.name),
                IngredientItem(name = "Olive oil", quantity = 2.0, unit = IngredientUnit.TBSP.name),
                IngredientItem(name = "Bell pepper", quantity = 1.0, unit = IngredientUnit.TSP.name),
                IngredientItem(name = "Salt", unit = IngredientUnit.TO_TASTE.name)
            ),
            steps = listOf(
                InstructionStep(stepNumber = 1, text = "Blend the chickpeas, tahini, lemon, and garlic."),
                InstructionStep(stepNumber = 2, text = "Add oil and season with salt."),
                InstructionStep(stepNumber = 3, text = "Serve with paprika and a drizzle of oil.")
            )
        )
    )
}
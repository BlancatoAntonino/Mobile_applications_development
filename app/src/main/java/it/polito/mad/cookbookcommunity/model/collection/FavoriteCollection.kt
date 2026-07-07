package it.polito.mad.cookbookcommunity.model.collection

import java.util.UUID

data class FavoriteCollection(
    val id: String = UUID.randomUUID().toString(),
    val ownerId: String = "",
    val name: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSystem: Boolean = false
){
    companion object{

        fun savedRecipesId(ownerId: String): String = "saved_recipes_$ownerId"
        fun triedRecipesId(ownerId: String): String = "tried_recipes_$ownerId"

        const val SAVED_RECIPES_NAME = "Saved Recipes"
        const val TRIED_RECIPES_NAME = "Tried Recipes"
    }
}
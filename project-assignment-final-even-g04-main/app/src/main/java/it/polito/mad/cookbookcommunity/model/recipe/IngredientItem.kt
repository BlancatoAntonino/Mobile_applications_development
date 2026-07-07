package it.polito.mad.cookbookcommunity.model.recipe

import java.util.UUID

data class IngredientItem(
    var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var quantity: Double? = null,
    var unit: String = "UNIT",
    var note: String = "",
    var optional: Boolean = false
)

val IngredientItem.requiresQuantity: Boolean
    get() = unit != IngredientUnit.TO_TASTE.name

fun IngredientItem.normalizedForUnit(): IngredientItem {
    return if (unit == IngredientUnit.TO_TASTE.name && quantity != null) {
        copy(quantity = null)
    } else {
        this
    }
}
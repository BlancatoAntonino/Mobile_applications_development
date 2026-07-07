package it.polito.mad.cookbookcommunity.model.recipe

import java.util.UUID

data class InstructionStep(
    var id: String = UUID.randomUUID().toString(),
    var stepNumber: Int = 1,
    var text: String = ""
)
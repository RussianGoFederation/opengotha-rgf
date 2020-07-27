package ru.gofederation.gotha.model

import ru.gofederation.gotha.util.Serializable

/**
 * This class represents Russia Go Federation player's id
 */
class RgfId(
    val id: Int,
    val newPlayer: Boolean = false,
    val assessmentRating: Boolean = false
) : Serializable

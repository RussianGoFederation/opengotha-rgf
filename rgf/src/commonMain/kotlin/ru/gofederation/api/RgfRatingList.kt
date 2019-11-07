package ru.gofederation.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlin.jvm.JvmStatic

@Serializable
data class RgfRatingList(
    val data: Data
) {
    val players: List<Player>
        get() = data.players

    @Serializable
    data class Player (
        val id: Int,
        val firstName: String,
        val lastName: String,
        val firstNameLat: String,
        val lastNameLat: String,
        val townName: String?,
        val rating: Int,
        val ratingDrift: Int,
        val lastGame: Date
    )

    @Serializable
    data class Data(
        val players: List<Player>
    )

    @ExperimentalStdlibApi
    companion object {
        @JvmStatic
        fun parse(data: ByteArray): RgfRatingList {
            val json = Json(JsonConfiguration.Stable.copy(strictMode = false))
            return json.parse(serializer(), data.decodeToString())
        }
    }
}

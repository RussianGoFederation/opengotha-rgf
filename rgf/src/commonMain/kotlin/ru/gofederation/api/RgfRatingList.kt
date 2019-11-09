/*
 * This file is part of OpenGotha.
 *
 * OpenGotha is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * OpenGotha is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenGotha.  If not, see <http://www.gnu.org/licenses/>.
 */

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

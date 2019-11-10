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

import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

@Serializable
data class RgfTournament(
    val id: Int?,
    val tigrId: Int? = null,
    val name: String,
    val nameLat: String? = null,
    val location: String? = null,
    val description: String? = null,
    val descriptionLat: String? = null,
    val statute: String? = null,
    @Serializable(with = DateSerializer::class)
    val startDate: Date,
    @Serializable(with = DateSerializer::class)
    val endDate: Date,
    val system: String? = null,
    val roundCount: Int? = null,
    val komi_4: Int = 26,
    val state: State? = null,
    val category: Category,
    val timing: Timing? = null,
    val schedule: Schedule? = null,
    val applications: List<Application>? = null,
    val players: List<Player>? = null,
    val games: List<Game>? = null,
    @SerialName("playersCount")
    val participantsCount: Int? = null,
    val applicationsCount: Int? = null
) {
    enum class ImportMode {
        APPLICATIONS,
        PARTICIPANTS
    }

    enum class State {
        REGISTRATION,
        CONDUCTING,
        MODERATION,
        NON_RATING,
        RATED
    }

    enum class Category {
        NOT_SET,
        LOCAL,
        REGIONAL,
        FEDERAL,
        INTERNATIONAL,
        ONLINE
    }

    @Serializable
    data class Player (
        val id: Int,
        val playerId: Int? = null,
        val firstName: String,
        val lastName: String,
        val patronymic: String? = null,
        val firstNameLat: String? = null,
        val lastNameLat: String? = null,
        val townName: String? = null,
        val rating: Int,
        val ratingDrift: Int? = null,
        @Serializable(with = DateSerializer::class)
        val lastGame: Date? = null,
        val mm0_4: Int,
        val mmF_4: Int,
        val sos_4: Int,
        val sodos_4: Int,
        val place: Int
    )

    @Serializable
    data class Game (
        @SerialName("player1")
        val player1id: Int?,
        @SerialName("player2")
        val player2id: Int?,
        @Transient
        val player1: Player? = null,
        @Transient
        val player2: Player? = null,
        val color: Color,
        val result: Result,
        val round: Int,
        val board: Int,
        val komi_4: Int,
        val handicap: Int
    ) {
        enum class Color {
            UNKNOWN,
            PLAYER_1_BLACK
        }

        enum class Result {
            PLAYER_1_WIN,
            PLAYER_2_WIN,
            PLAYER_1_WIN_BYREF,
            PLAYER_2_WIN_BYREF,
            NOT_PLAYED,
            TIE,
            BOTH_LOST,
            UNKNOWN,
            TIE_BYREF;
        }
    }

    @Serializable(with = Timing.Serializer::class)
    sealed class Timing() {
        abstract val basicTime: Int

        data class SuddenDeath(
            override val basicTime: Int
        ) : Timing()

        data class Canadian(
            override val basicTime: Int,
            val boyomiTime: Int,
            val boyomiMoves: Int
        ) : Timing()

        data class Japanese(
            override val basicTime: Int,
            val boyomiTime: Int,
            val boyomiMoves: Int,
            val boyomiPeriods: Int
        ) : Timing()

        data class Fischer(
            override val basicTime: Int,
            val bonus: Int
        ) : Timing()

        object Serializer: KSerializer<Timing> {
            override val descriptor = object : SerialClassDescImpl("Timing") {
                init {
                    addElement("base")
                    addElement("boyomi_type")
                    addElement("boyomi")
                    addElement("boyomi_moves")
                    addElement("boyomi_periods")
                    addElement("bonus")
                }
            }

            override fun deserialize(decoder: Decoder): Timing {
                var basicTime = 0
                var boyomiType: String? = null
                var boyomi = 0
                var moves = 0
                var periods = 0

                val compositeDecoder = decoder.beginStructure(descriptor)
                loop@ while (true) {
                    when (val index = compositeDecoder.decodeElementIndex(descriptor)) {
                        CompositeDecoder.READ_ALL ->
                            break@loop
                        CompositeDecoder.READ_DONE ->
                            break@loop
                        0 -> basicTime = compositeDecoder.decodeIntElement(descriptor, 0)
                        1 -> boyomiType = compositeDecoder.decodeStringElement(descriptor, 1)
                        2 -> boyomi = compositeDecoder.decodeIntElement(descriptor, 2)
                        3 -> moves = compositeDecoder.decodeIntElement(descriptor, 3)
                        4 -> periods = compositeDecoder.decodeIntElement(descriptor, 4)
                        5 -> boyomi = compositeDecoder.decodeIntElement(descriptor, 5)
                        else -> println(index)
                    }
                }
                compositeDecoder.endStructure(descriptor)

                when (boyomiType) {
                    "abs" -> return SuddenDeath(basicTime)
                    "can" -> return Canadian(basicTime, boyomi, moves)
                    "jap" -> return Japanese(basicTime, boyomi, moves, periods)
                    "fischer" -> return Fischer(basicTime, boyomi)
                }

                throw Exception()
            }

            override fun serialize(encoder: Encoder, obj: Timing) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
    }

    @Serializable
    data class Application (
        val playerId: Int?,
        val firstName: String,
        val lastName: String,
        val townName: String,
        val rating: String
    )

    @Serializable
    data class Schedule (
        val tz: String,
        @SerialName("tz_offset")
        val tzOffset: String? = null,
        val rounds: List<String>?
    )

    @Serializer(forClass = Date::class)
    object DateSerializer : KSerializer<Date> {
        override val descriptor: SerialDescriptor = StringDescriptor.withName("Date")

        override fun deserialize(decoder: Decoder): Date =
            Date(decoder.decodeString())

        override fun serialize(encoder: Encoder, obj: Date) =
            encoder.encodeString(obj.toString())
    }

    @Serializable
    data class ApiResponse(val data: Data) {
        @Serializable
        data class Data(val tournament: RgfTournament)
    }
}

fun serializeTournament(tournament: RgfTournament) =
    Json(JsonConfiguration.Stable)
        .stringify(RgfTournament.serializer(), tournament)

fun parseApiResponse(json: String) =
    Json(JsonConfiguration.Stable.copy(strictMode = false))
        .parse(RgfTournament.ApiResponse.serializer(), json)

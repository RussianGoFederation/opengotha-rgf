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
 * along with OpenGotha. If not, see <http://www.gnu.org/licenses/>.
 */

package ru.gofederation.gotha.model

import info.vannier.gotha.ScoredPlayer
import info.vannier.gotha.TournamentParameterSet

data class HalfGame(
    val opponentNumber: Int,
    val result: Result,
    val type: Type,
    val color: Color,
    val handicap: Int
) {
    enum class Type {
        REGULAR, BY_DEF, NO_GAME;

        override fun toString(): String = when (this) {
            REGULAR -> "/"
            BY_DEF -> "!"
            NO_GAME -> " "
        }
    }

    enum class Color {
        NOT_SET, UNKNOWN, WHITE, BLACK;

        override fun toString() = when (this) {
            NOT_SET -> " "
            UNKNOWN -> "?"
            WHITE -> "w"
            BLACK -> "b"
        }
    }

    enum class Result {
        UNKNOWN, EQUAL, WIN, LOSE;

        override fun toString() = when (this) {
            UNKNOWN -> "?"
            EQUAL -> "="
            WIN -> "+"
            LOSE -> "-"
        }
    }

    fun toPaddedString(long: Boolean) =
        if (long) toPaddedStringLong()
        else toPaddedStringShort()


    fun toPaddedStringLong(): String =
        if (type == Type.NO_GAME) {
            "   0${result}"
        } else {
            opponentNumber.toString().padStart(4, ' ') +
                result.toString() +
                type.toString() +
                color.toString() +
                handicap.coerceIn(0, 9).toString()
        }

    fun toPaddedStringShort(): String =
        if (type == Type.NO_GAME) {
            "   0${result}"
        } else {
            opponentNumber.toString().padStart(4, ' ') +
                result.toString()
        }

    companion object {
        val EMPTY = HalfGame(0, Result.LOSE, Type.NO_GAME, Color.UNKNOWN, 0)

        operator fun invoke(tps: TournamentParameterSet, participation: Int): HalfGame {
            val gps = tps.generalParameterSet
            return HalfGame(
                opponentNumber = 0,
                result = when (when (participation) {
                    ScoredPlayer.NOT_ASSIGNED -> {
                        if (tps.tournamentType() == TournamentParameterSet.TYPE_MCMAHON) gps.genMMS2ValueAbsent
                        else gps.genNBW2ValueAbsent
                    }
                    ScoredPlayer.BYE -> {
                        if (tps.tournamentType() == TournamentParameterSet.TYPE_MCMAHON) gps.genMMS2ValueBye
                        else gps.genNBW2ValueBye
                    }
                    else -> {
                        0
                    }
                }) {
                    2 -> Result.WIN
                    1 -> Result.EQUAL
                    else -> Result.LOSE
                },
                color = Color.NOT_SET,
                type = Type.NO_GAME,
                handicap = 0
            )
        }

        operator fun invoke(opponentLookup: Map<String, Int>, player: ScoredPlayer, game: Game): HalfGame {
            val opponentNumber: Int
            val result: Result
            val color: Color

            if (game.whitePlayer.hasSameKeyString(player)) {
                opponentNumber = opponentLookup[game.blackPlayer.keyString] ?: 0
                color = if (game.knownColor) Color.WHITE else Color.UNKNOWN
                result = when (game.result.notByDef()) {
                    Game.Result.WHITEWINS, Game.Result.BOTHWIN -> Result.WIN
                    Game.Result.BLACKWINS, Game.Result.BOTHLOSE -> Result.LOSE
                    Game.Result.EQUAL -> Result.EQUAL
                    else -> Result.UNKNOWN
                }
            } else {
                opponentNumber = opponentLookup[game.whitePlayer.keyString] ?: 0
                color = if (game.knownColor) Color.BLACK else Color.UNKNOWN
                result = when (game.result.notByDef()) {
                    Game.Result.BLACKWINS, Game.Result.BOTHWIN -> Result.WIN
                    Game.Result.WHITEWINS, Game.Result.BOTHLOSE -> Result.LOSE
                    Game.Result.EQUAL -> Result.EQUAL
                    else -> Result.UNKNOWN
                }
            }

            return HalfGame(
                opponentNumber = opponentNumber,
                result = result,
                type = if (game.result.isByDef()) Type.BY_DEF else Type.REGULAR,
                color = color,
                handicap = game.handicap
            )
        }
    }
}

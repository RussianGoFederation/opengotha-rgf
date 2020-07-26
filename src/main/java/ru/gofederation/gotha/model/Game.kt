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

import info.vannier.gotha.Gotha
import info.vannier.gotha.Pairing
import info.vannier.gotha.ScoredPlayer
import info.vannier.gotha.TournamentInterface
import java.io.Serializable
import java.lang.Integer.min

/**
 * A game between two players.
 */
data class Game private constructor (
    /** 0-based round number */
    val round: Int,
    /** 0-based board number */
    val board: Int,
    val whitePlayer: Player,
    val blackPlayer: Player,
    /**
     * true if colors are known.
     * Note that, in some no-handicap tournaments, color is not published.
     * When it happens, players are randomly said to be white or black.
     * [knownColor] remembers whether color was actually known
     */
    val knownColor: Boolean,
    val handicap: Int,
    val result: Result
) : Serializable {
    /** [key] is used to address games in [info.vannier.gotha.Tournament.hmGames] */
    val key: Int
        get() = round * Gotha.MAX_NUMBER_OF_TABLES + board

    fun isWinner(player: Player) = when {
        player.hasSameKeyString(whitePlayer) -> {
            when(result) {
                Result.BOTHWIN,
                Result.BOTHWIN_BYDEF,
                Result.WHITEWINS,
                Result.WHITEWINS_BYDEF -> true
                else -> false
            }
        }
        player.hasSameKeyString(blackPlayer) -> {
            when(result) {
                Result.BOTHWIN,
                Result.BOTHWIN_BYDEF,
                Result.BLACKWINS,
                Result.BLACKWINS_BYDEF -> true
                else -> false
            }
        }
        else -> {
            false
        }
    }

    /**
     * Returns string representation of game result.
     * if [wb] is true puts white player first.
     */
    fun resultAsString(wb: Boolean): String = if (wb) {
        result.toString()
    } else {
        result.mirrored().toString()
    }

    /** Helper function to be used from Java code */
    fun withBoard(board: Int) = copy(board = board)

    /** Helper function to be used from Java code */
    fun withHandicap(handicap: Int) = copy(handicap = handicap.coerceHandicap())

    /** Helper function to be used from Java code */
    fun withResult(result: Result) = copy(result = result)

    /** Helper function to be used from Java code */
    fun withRound(round: Int) = copy(round = round)

    fun exchangeColors() = copy(
        blackPlayer = whitePlayer,
        whitePlayer = blackPlayer,
        result = result.mirrored()
    )

    fun builder(): Builder = Builder(
        round = round,
        board = board,
        whitePlayer = whitePlayer,
        blackPlayer = blackPlayer,
        knownColor = knownColor,
        handicap = handicap,
        result = result
    )

    /**
     * Helper class to be used from Java code
     */
    class Builder(
        var round: Int = 0,
        var board: Int = 0,
        var whitePlayer: Player? = null,
        var blackPlayer: Player? = null,
        var knownColor: Boolean = false,
        var handicap: Int = 0,
        var result: Result = Result.UNKNOWN
    ) {
        fun build(): Game = create(
            round = round,
            board = board,
            whitePlayer = whitePlayer ?: throw IllegalStateException("White player must be set"),
            blackPlayer = blackPlayer ?: throw IllegalStateException("Black player must be set"),
            knownColor = knownColor,
            handicap = handicap,
            result = result
        )
    }

    enum class Result(val code: Int, private val str: String) {
        /** Result unknown (usually means not yet input) */
        UNKNOWN(0, " - "),
        /** White player wins */
        WHITEWINS(17, "1-0"),
        WHITEWINS_BYDEF(WHITEWINS.code + 256, "1-0!"),
        /** Black player wins */
        BLACKWINS(18, "0-1"),
        BLACKWINS_BYDEF(BLACKWINS.code + 256, "0-1!"),
        /** Equal result (Jigo or any other reason for a draw). Means 1/2 - 1/2 */
        EQUAL(19, "½-½"),
        EQUAL_BYDEF(EQUAL.code + 256, "½-½!"),
        /** Both players lose. Means 0 - 0 */
        BOTHLOSE(32, "0-0"),
        BOTHLOSE_BYDEF(BOTHLOSE.code + 256, "0-0!"),
        /** Result : Both win. Means 1 - 1 */
        BOTHWIN(35, "1-1"),
        BOTHWIN_BYDEF(BOTHWIN.code + 256, "1-1!");

        fun gameWasPlayer(): Boolean = when(this) {
            UNKNOWN,
            WHITEWINS_BYDEF,
            BLACKWINS_BYDEF,
            EQUAL_BYDEF,
            BOTHLOSE_BYDEF,
            BOTHWIN_BYDEF -> false
            else -> true
        }

        fun mirrored(): Result = when(this) {
            WHITEWINS -> BLACKWINS
            WHITEWINS_BYDEF -> BLACKWINS_BYDEF
            BLACKWINS -> WHITEWINS
            BLACKWINS_BYDEF -> BLACKWINS_BYDEF
            else -> this
        }

        fun isByDef(): Boolean =
            this.code >= 256

        fun notByDef(): Result = when(this) {
            WHITEWINS_BYDEF -> WHITEWINS
            BLACKWINS_BYDEF -> BLACKWINS
            EQUAL_BYDEF -> EQUAL
            BOTHLOSE_BYDEF -> BOTHLOSE
            BOTHWIN_BYDEF -> BOTHWIN
            else -> this
        }

        fun byDef(): Result = when(this) {
            WHITEWINS -> WHITEWINS_BYDEF
            BLACKWINS -> BLACKWINS_BYDEF
            EQUAL -> EQUAL_BYDEF
            BOTHLOSE -> BOTHWIN_BYDEF
            BOTHWIN -> BOTHWIN_BYDEF
            else -> this
        }

        override fun toString(): String = this.str
    }

    companion object {
        fun create(
            round: Int,
            board: Int,
            whitePlayer: Player,
            blackPlayer: Player,
            knownColor: Boolean,
            handicap: Int,
            result: Result
        ) = Game(
            round = round,
            board = board,
            whitePlayer = whitePlayer,
            blackPlayer = blackPlayer,
            knownColor = knownColor,
            handicap = handicap,
            result = result
        )

        private fun Int.coerceHandicap() = this.coerceIn(0, 9)
    }
}

/**
 * Returns true if given game is between [p1] and [p2]
 */
fun Game.samePlayers(p1: ScoredPlayer, p2: ScoredPlayer): Boolean =
    (p1.hasSameKeyString(whitePlayer) && p2.hasSameKeyString(blackPlayer)) || (p1.hasSameKeyString(blackPlayer) && p2.hasSameKeyString(whitePlayer))

/**
 * Creates a new [Game.Builder] with everything defined except tableNumber
 */
fun TournamentInterface.gameBetween(sp1: ScoredPlayer, sp2: ScoredPlayer, round: Int): Game.Builder {
    val tps = this.tournamentParameterSet
    val hdPs = tps.handicapParameterSet
    val builder = Game.Builder(
        knownColor = true,
        result = Game.Result.UNKNOWN,
        round = round
    )

    var pseudoRank1 = sp1.rank.value
    var pseudoRank2 = sp2.rank.value
    if (hdPs.isHdBasedOnMMS) {
        pseudoRank1 = sp1.getCritValue(PlacementCriterion.MMS, round - 1) / 2 + Gotha.MIN_RANK
        pseudoRank2 = sp2.getCritValue(PlacementCriterion.MMS, round - 1) / 2 + Gotha.MIN_RANK
    }
    pseudoRank1 = min(pseudoRank1, hdPs.hdNoHdRankThreshold)
    pseudoRank2 = min(pseudoRank2, hdPs.hdNoHdRankThreshold)
    var hd = pseudoRank1 - pseudoRank2
    if (hd > 0) {
        hd -= hdPs.hdCorrection
        hd = hd.coerceAtLeast(0)
    }
    if (hd < 0) {
        hd += hdPs.hdCorrection
        hd = hd.coerceAtMost(0)
    }
    hd = hd.coerceIn(-hdPs.hdCeiling, hdPs.hdCeiling)

    val p1 = getPlayerByKeyString(sp1.keyString)
    val p2 = getPlayerByKeyString(sp2.keyString)

    when {
        (hd > 0) -> {
            builder.whitePlayer = p1
            builder.blackPlayer = p2
            builder.handicap = hd
        }
        (hd < 0) -> {
            builder.whitePlayer = p2
            builder.blackPlayer = p1
            builder.handicap = -hd
        }
        else -> {
            builder.handicap = 0
            val wbBalance = Pairing.wbBalance(sp1, round - 1) - Pairing.wbBalance(sp2, round - 1)
            when {
                wbBalance > 0 -> {
                    builder.whitePlayer = p2
                    builder.blackPlayer = p1
                }
                wbBalance < 0 -> {
                    builder.whitePlayer = p1
                    builder.blackPlayer = p2
                }
                else -> {
                    // choose color from a det random
                    if (Pairing.detRandom(1, sp1, sp2) == 0L) {
                        builder.whitePlayer = p1
                        builder.blackPlayer = p2
                    } else {
                        builder.whitePlayer = p2
                        builder.blackPlayer = p1
                    }
                }
            }
        }
    }

    return builder
}

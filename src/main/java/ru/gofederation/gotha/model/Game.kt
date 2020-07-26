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
import java.lang.Integer.min

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

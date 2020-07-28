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

package ru.gofederation.gotha.presenter

import info.vannier.gotha.TournamentInterface
import ru.gofederation.gotha.model.HalfGame
import ru.gofederation.gotha.model.ScoredPlayer

fun List<ScoredPlayer>.halfGames(roundNumber: Int, tournament: TournamentInterface): Array<Array<HalfGame>> {
    val tps = tournament.tournamentParameterSet
    val hmPos = this.mapIndexed { i, player -> player.keyString to i + 1 }.toMap()
    val hg = Array(roundNumber + 1) { Array(this.size) { HalfGame.EMPTY } }

    this.forEachIndexed { row, sp ->
        for (round in 0..roundNumber) {
            val g = sp.getGame(round)
            val participation = sp.getParticipation(round)
            hg[round][row] = if (g == null) {
                if (participation == ScoredPlayer.NOT_ASSIGNED) {
                    HalfGame.EMPTY
                } else {
                    HalfGame(tps, participation)
                }
            } else {
                HalfGame(tournament, hmPos, sp, g)
            }
        }
    }

    return hg
}

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

import ru.gofederation.gotha.model.Player

class PlayersQuickCheckTableModel(players: List<Player>, numberOfRounds: Int) : GothaTableModel<Player>(players, listOf(
    PlayerTableColumn.Registration(),
    PlayerTableColumn.LastName(),
    PlayerTableColumn.FirstName(),
    PlayerTableColumn.Country(),
    PlayerTableColumn.Club(),
    PlayerTableColumn.Rank(),
    PlayerTableColumn.Rating(),
    *(1..numberOfRounds).map { PlayerTableColumn.Participation(it) }.toTypedArray()
))

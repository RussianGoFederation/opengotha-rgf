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
import ru.gofederation.gotha.model.PlayerRegistrationStatus
import ru.gofederation.gotha.util.GothaLocale

sealed class PlayerTableColumn(header: String, prefWidth: Int) : GothaTableColumn<Player>(header, prefWidth) {

    class Registration() : PlayerTableColumn("R", 50) {
        override fun cellValue(obj: Player): Any = if (obj.registeringStatus == PlayerRegistrationStatus.FINAL) "F" else "P"
    }


    class FirstName() : PlayerTableColumn(GothaLocale.getCurrentLocale().tr("player.first_name"), 50) {
        override fun cellValue(obj: Player): Any = obj.firstName
    }

    class LastName() : PlayerTableColumn(GothaLocale.getCurrentLocale().tr("player.last_name"), 50) {
        override fun cellValue(obj: Player): Any = obj.name
    }

    class Country() : PlayerTableColumn(GothaLocale.getCurrentLocale().tr("player.country_s"), 50) {
        override fun cellValue(obj: Player): Any = obj.country
    }

    class Club() : PlayerTableColumn(GothaLocale.getCurrentLocale().tr("player.club"), 50) {
        override fun cellValue(obj: Player): Any = obj.club
    }

    class Rank() : PlayerTableColumn(GothaLocale.getCurrentLocale().tr("player.rank"), 50) {
        override fun cellValue(obj: Player): Any = obj.rank
    }

    class Rating() : PlayerTableColumn(GothaLocale.getCurrentLocale().tr("player.rating"), 50) {
        override fun cellValue(obj: Player): Any = obj.rating.value
    }

    class Participation(val round: Int) : PlayerTableColumn(round.toString(), 50) {
        override fun cellValue(obj: Player): Any = if (obj.isParticipating(round - 1)) "V" else ""
    }
}

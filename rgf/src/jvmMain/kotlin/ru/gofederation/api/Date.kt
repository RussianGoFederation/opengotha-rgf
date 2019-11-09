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

import java.util.*

actual fun currentDate(): Date {
    val cal = Calendar.getInstance()
    return Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH ) + 1, cal.get(Calendar.DATE))
}

fun Date.toJvmDate(): java.util.Date {
    return java.util.Date(this.year - 1900, this.month - 1, this.day)
}

fun java.util.Date.toApiDate(): Date {
    return Date(this.year + 1900, this.month + 1, this.date)
}

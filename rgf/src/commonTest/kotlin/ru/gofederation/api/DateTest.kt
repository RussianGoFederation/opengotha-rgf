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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class DateTest {
    @Test
    fun `test Date conversion from and to String`() {
        val testCases = listOf(
            Pair("2019-04-05", Date(2019, 4, 5))
        )

        for (case in testCases) {
            assertEquals(case.second, Date(case.first))
            assertEquals(case.first, Date(case.first).toString())
        }
    }
}

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

import org.junit.Test
import java.text.SimpleDateFormat
import kotlin.test.assertEquals

class DateJvmTest {
    val format = SimpleDateFormat("yyyy-MM-dd")

    @Test
    fun `Current date`() {
        assertEquals(format.format(java.util.Date()), Date().toString())
    }

    @Test
    fun `Api Date converts to JVM Date and back`() {
        val testCases = listOf(
            Pair("2019-10-05", Date(2019, 10, 5)),
            Pair("2013-07-28", Date(2013, 7, 28))
        )

        for ((s, date) in testCases) {
            val jvmDate = date.toJvmDate()

            assertEquals(format.parse(s), jvmDate)
            assertEquals(s, format.format(jvmDate))
            assertEquals(s, date.toString())
            assertEquals(s, format.format(date.toJvmDate()))
            assertEquals(s, jvmDate.toApiDate().toString())
        }
    }
}

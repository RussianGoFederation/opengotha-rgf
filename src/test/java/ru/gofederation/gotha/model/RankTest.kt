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

import kotlin.test.Test
import kotlin.test.assertEquals

class RankTest {
    @Test
    fun testRankRanges() {
        assertEquals(Rank.danRange.first, Rank.kyuRange.last + 1, "Kyu and dan ranges are not adjacent")
        assertEquals(Rank.proRange.first, Rank.danRange.last + 1, "Kyu and dan ranges are not adjacent")
        assertEquals(30, Rank.kyuRange.count(), "There must be exactly 30 kyu")
        assertEquals(9, Rank.danRange.count(), "There must be exactly 9 dan")
        assertEquals(9, Rank.proRange.count(), "There must be exactly 9 pro dan")

        assertEquals(Rank.kyuRange.first, Rank.fromString("30K").value)
        assertEquals(Rank.kyuRange.last, Rank.fromString("1K").value)
        assertEquals(Rank.danRange.first, Rank.fromString("1D").value)
        assertEquals(Rank.danRange.last, Rank.fromString("9D").value)
        assertEquals(Rank.proRange.first, Rank.fromString("1P").value)
        assertEquals(Rank.proRange.last, Rank.fromString("9P").value)
    }

    @Test
    fun testRankToStringAndBack() {
        val data = listOf(
            (-30 .. -1)
                .map { it to "${it * -1}K" },
            (0 .. 8)
                .map { it to "${it + 1}D"},
            (9 .. 17)
                .map { it to "${it - 8}P"})
            .flatten()

        data.forEach { (v, s) ->
            val rankFromV = Rank.fromInt(v)
            val rankFromS = Rank.fromString(s)
            assertEquals(s, rankFromV.toString())
            assertEquals(s, rankFromS.toString())
            assertEquals(v, rankFromV.value)
            assertEquals(v, rankFromS.value)
        }
    }
}

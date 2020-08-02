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

import info.vannier.gotha.TournamentParameterSet
import ru.gofederation.gotha.model.HalfGame
import ru.gofederation.gotha.model.HalfGame.Color
import ru.gofederation.gotha.model.HalfGame.Result
import ru.gofederation.gotha.model.HalfGame.Type
import ru.gofederation.gotha.model.HalfGame.UpDownStatus.NO_UPDOWN
import ru.gofederation.gotha.model.ScoredPlayer
import kotlin.test.Test
import kotlin.test.assertEquals

class HalfGameTest {
    private fun tps(mm: Boolean, participation: Int, pt: Int): TournamentParameterSet {
        if (pt < 0 || pt > 2) {
            throw IllegalArgumentException()
        }

        val tps = TournamentParameterSet()
        if (pt == 2) {
            tps.generalParameterSet.setGenMMS2ValueAbsent(0)
            tps.generalParameterSet.setGenNBW2ValueAbsent(0)
            tps.generalParameterSet.setGenMMS2ValueBye(0)
            tps.generalParameterSet.setGenNBW2ValueBye(0)
        } else {
            tps.generalParameterSet.setGenMMS2ValueAbsent(2)
            tps.generalParameterSet.setGenNBW2ValueAbsent(2)
            tps.generalParameterSet.setGenMMS2ValueBye(2)
            tps.generalParameterSet.setGenNBW2ValueBye(2)
        }

        if (mm) {
            tps.initForMM()
        } else {
            tps.initForSwiss()
        }

        if (participation == ScoredPlayer.ABSENT) {
            if (mm) tps.generalParameterSet.setGenMMS2ValueAbsent(pt)
            else tps.generalParameterSet.setGenNBW2ValueAbsent(pt)
        } else if (participation == ScoredPlayer.BYE) {
            if (mm) tps.generalParameterSet.setGenMMS2ValueBye(pt)
            else tps.generalParameterSet.setGenNBW2ValueBye(pt)
        }

        return tps
    }

    private fun testCase(mm: Boolean, participation: Int, pt: Int): HalfGame {
        val tps = tps(mm, participation, pt)
        return HalfGame(tps(mm, participation, pt), participation)
    }

    @Test
    fun testHalfGameToPaddedStringLong() {
        val data = listOf(
            HalfGame(5, Result.LOSE, Type.REGULAR, NO_UPDOWN, Color.WHITE, 4) to "   5-/w4",
            HalfGame(9999, Result.WIN, Type.BY_DEF, NO_UPDOWN, Color.BLACK, 0) to "9999+!b0",
            HalfGame(16, Result.UNKNOWN, Type.REGULAR, NO_UPDOWN, Color.WHITE, 0) to "  16?/w0",
            HalfGame(80, Result.UNKNOWN, Type.REGULAR, NO_UPDOWN, Color.UNKNOWN, 0) to "  80?/?0",

            testCase(true, ScoredPlayer.BYE, 2) to "   0+",
            testCase(true, ScoredPlayer.BYE, 1) to "   0=",
            testCase(false, ScoredPlayer.ABSENT, 0) to "   0-",

            HalfGame.EMPTY to "   0-"
        )

        data.forEach { (game, expected) ->
            assertEquals(expected, game.toPaddedStringLong())
        }
    }

    @Test
    fun testHalfGameToPaddedStringShort() {
        val data = listOf(
            HalfGame(5, Result.LOSE, Type.REGULAR, NO_UPDOWN, Color.WHITE, 4) to "   5-",
            HalfGame(9999, Result.WIN, Type.BY_DEF, NO_UPDOWN, Color.BLACK, 0) to "9999+",
            HalfGame(16, Result.UNKNOWN, Type.REGULAR, NO_UPDOWN, Color.WHITE, 0) to "  16?",
            HalfGame(80, Result.UNKNOWN, Type.REGULAR, NO_UPDOWN, Color.UNKNOWN, 0) to "  80?",

            testCase(true, ScoredPlayer.BYE, 2) to "   0+",
            testCase(true, ScoredPlayer.BYE, 1) to "   0=",
            testCase(false, ScoredPlayer.ABSENT, 0) to "   0-",

            HalfGame.EMPTY to "   0-"
        )

        data.forEach { (game, expected) ->
            assertEquals(expected, game.toPaddedStringShort())
        }
    }
}

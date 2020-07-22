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

        assertEquals(Rank.kyuRange.first.asRank(), Rank.fromString("30K"))
        assertEquals(Rank.kyuRange.last.asRank(), Rank.fromString("1K"))
        assertEquals(Rank.danRange.first.asRank(), Rank.fromString("1D"))
        assertEquals(Rank.danRange.last.asRank(), Rank.fromString("9D"))
        assertEquals(Rank.proRange.first.asRank(), Rank.fromString("1P"))
        assertEquals(Rank.proRange.last.asRank(), Rank.fromString("9P"))
    }

    @Test
    fun testRankToStringAndBack() {
        for (r in Rank.kyuRange.first..Rank.proRange.last) {
            val rank = r.asRank()
            val rankString = rank.toString()
            assertEquals(rank, rankString.asRank())
        }
    }
}

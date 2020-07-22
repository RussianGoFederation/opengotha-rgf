package ru.gofederation.gotha.model

import kotlin.test.Test
import kotlin.test.assertEquals

class RatingOriginTest {
    @Test
    fun `Test RGF rating to rank conversion`() {
        val data = sequence {
            var v = -30

            (0..599 step 60).map { r ->
                yieldAll((r..r+59).map { it to v })
                v++
            }

            (600..2099 step 75).map { r ->
                yieldAll((r..r+74).map { it to v })
                v++
            }

            (2100..2999 step 100).map { r ->
                yieldAll((r..r+99).map { it to v })
                v++
            }
        }

        data.forEach {  (rating, rankValue) ->
            val expectedRank = rankValue.asRank()
            val actualRank = RatingOrigin.RGF.ratingToRank(rating)
            assertEquals(expectedRank, actualRank)
        }
    }
}

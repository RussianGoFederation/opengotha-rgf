package ru.gofederation.gotha.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ScoreDisplayTest {
    data class TestCase(
        val score: Int,
        val ratio: Int,
        val result: String
    )

    @Test
    fun testScoreDisplay() {
        val data = listOf(
            TestCase(4, 4, "1"),
            TestCase(4, 2, "2"),
            TestCase(5, 2, "2½"),
            TestCase(41, 4, "10¼"),
            TestCase(42, 4, "10½"),
            TestCase(43, 4, "10¾"),

            TestCase(90, -1, "-89"),
            TestCase(-90, -1, "91")
        )

        data.forEach { (score, ratio, expected) ->
            val actual = score.formatScore(ratio)
            assertEquals(expected, actual)
        }
    }
}

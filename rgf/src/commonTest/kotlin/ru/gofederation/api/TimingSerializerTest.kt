package ru.gofederation.api

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals

class TimingSerializerTest {
    @Test
    fun testTimingSerializer() {
        val json = Json(JsonConfiguration.Stable)

        val testCases = listOf(
            Pair(
                """{"base":1800,"boyomi_type":"abs"}""",
                RgfTournament.Timing.SuddenDeath(1800)),
            Pair(
                """{"base":3600,"boyomi_type":"can","boyomi":600,"boyomi_moves":20}""",
                RgfTournament.Timing.Canadian(3600, boyomiTime = 600, boyomiMoves = 20)
            ),
            Pair(
                """{"base":2700,"boyomi_type":"jap","boyomi":30,"boyomi_moves":1,"boyomi_periods":2}""",
                RgfTournament.Timing.Japanese(2700, boyomiTime = 30, boyomiMoves =  1, boyomiPeriods =  2)
            ),
            Pair(
                """{"base":2400,"boyomi_type":"fischer","boyomi":15}""",
                RgfTournament.Timing.Fischer(2400, 15)
            )
        )

        for (testCase in testCases) {
            val timing = json.parse(RgfTournament.Timing.serializer(), testCase.first)
            assertEquals(testCase.second, timing)
        }

    }
}

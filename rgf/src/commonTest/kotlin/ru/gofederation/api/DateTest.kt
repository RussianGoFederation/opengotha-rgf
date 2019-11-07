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

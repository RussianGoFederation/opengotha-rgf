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
        val date = Date(2019, 10, 5)
        val jvmDate = date.toJvmDate()

        assertEquals(date.toString(), format.format(jvmDate))
        assertEquals(date, jvmDate.toApiDate())
    }
}

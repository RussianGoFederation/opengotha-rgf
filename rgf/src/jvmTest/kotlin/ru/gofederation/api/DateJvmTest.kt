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

        assertEquals(format.parse("2019-10-05"), jvmDate)
        assertEquals("2019-10-05", format.format(jvmDate))
        assertEquals("2019-10-05", date.toString())
        assertEquals("2019-10-05", format.format(date.toJvmDate()))
        assertEquals("2019-10-05", jvmDate.toApiDate().toString())
    }
}

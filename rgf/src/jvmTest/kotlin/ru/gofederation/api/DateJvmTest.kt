package ru.gofederation.api

import org.junit.Test
import ru.gofederation.api.Date
import ru.gofederation.api.toJvmDate
import java.text.SimpleDateFormat
import kotlin.test.assertEquals

class DateJvmTest {
    @Test
    fun `Api Date converts to JVM Date and back`() {
        val date = Date(2019, 10, 5)
        val jvmDate = date.toJvmDate()

        assertEquals(date.toString(), SimpleDateFormat("yyyy-MM-dd").format(jvmDate))
        assertEquals(date, jvmDate.toApiDate())
    }
}

package ru.ofederation.gotha.util

import com.soywiz.klock.Date
import org.junit.Test
import ru.gofederation.gotha.util.toJvmDate
import ru.gofederation.gotha.util.toKlockDate
import java.text.SimpleDateFormat
import kotlin.random.Random
import kotlin.test.assertEquals

@Throws
class DateTest {
    @Test
    fun testKlockDateConverters() {
        val testCases = sequence {
            yield(Pair("2019-12-08", Date(2019, 12, 8)))
            yield(Pair("1986-05-06", Date(1986, 5, 6)))

            repeat(30) {
                val year = Random.nextInt(1900, 2030)
                val month = Random.nextInt(1, 13)
                val day = Random.nextInt(1, 29)

                yield(Pair(
                    "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}",
                    Date(year, month, day)))
            }
        }

        for ((dateStr, date) in testCases) {
            val jvmDate = SimpleDateFormat("yyyy-MM-dd").parse(dateStr)
            assertEquals(jvmDate, date.toJvmDate())
            assertEquals(date, jvmDate.toKlockDate())
        }
    }
}

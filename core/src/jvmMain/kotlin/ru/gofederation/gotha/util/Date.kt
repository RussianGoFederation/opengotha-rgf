package ru.gofederation.gotha.util

import com.soywiz.klock.Date
import java.util.Calendar
import java.util.GregorianCalendar

fun Date.toJvmDate(): java.util.Date =
    java.util.Date.from(GregorianCalendar(year, month1 - 1, day).toInstant())

fun java.util.Date.toKlockDate(): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    return Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE))
}


package ru.gofederation.api

import java.util.*

actual fun currentDate(): Date {
    val cal = Calendar.getInstance()
    return Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH ) + 1, cal.get(Calendar.DATE))
}

fun Date.toJvmDate(): java.util.Date {
    return java.util.Date(this.year - 1900, this.month - 1, this.day)
}

fun java.util.Date.toApiDate(): Date {
    return Date.invoke(this.year, this.month, this.day)
}

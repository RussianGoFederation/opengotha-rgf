package ru.gofederation.api

fun Date.toJvmDate(): java.util.Date {
    return java.util.Date(this.year - 1900, this.month - 1, this.day)
}

fun java.util.Date.toApiDate(): Date {
    return Date.invoke(this.year, this.month, this.day)
}

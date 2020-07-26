package ru.gofederation.gotha.model

import ru.gofederation.gotha.util.toJvmDate
import ru.gofederation.gotha.util.toKlockDate
import java.util.Date

fun Player.getDateOfBirthJvm(): Date? =
    dateOfBirth?.toJvmDate()

fun Player.Builder.setDateOfBirthJvm(date: Date?) {
    dateOfBirth = date?.toKlockDate()
}

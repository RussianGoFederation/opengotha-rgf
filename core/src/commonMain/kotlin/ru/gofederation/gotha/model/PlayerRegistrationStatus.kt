package ru.gofederation.gotha.model

import kotlin.jvm.JvmStatic

enum class PlayerRegistrationStatus(private val code: String) {
    PRELIMINARY("PRE"), FINAL("FIN");

    override fun toString(): String = code

    companion object {
        @JvmStatic
        fun fromString(name: String): PlayerRegistrationStatus =
            values().firstOrNull { it.name == name || it.code == name } ?: PRELIMINARY
    }
}

package ru.gofederation.gotha.model

import kotlin.jvm.JvmStatic

fun Int.asRank(): Rank = Rank.fromInt(this)
fun String.asRank(): Rank = Rank.fromString(this)

class Rank internal constructor(value: Int) : Comparable<Rank> {
    // TODO: make value private once Rating class is rewritten
    val value = value.coerceIn(kyuRange.first, proRange.last)

    override fun compareTo(other: Rank): Int =
        this.value - other.value

    override fun hashCode(): Int = value

    override fun equals(other: Any?): Boolean =
        if (other is Rank) {
            this.value == other.value
        } else {
            false
        }

    override fun toString(): String = when {
        kyuRange.contains(value) -> "${-value}K"
        danRange.contains(value) -> "${value + 1}D"
        proRange.contains(value) -> "${value - 8}P"
        else -> throw IllegalStateException("Value $value is not valid rank")
    }

    companion object {
        val kyuRange = -30..-1
        val danRange = 0..8
        val proRange = 9..17

        @JvmStatic
        fun fromString(rank: String): Rank = rankFromString(rank)

        @JvmStatic
        fun fromInt(rank: Int): Rank = Rank(rank)
    }
}

internal expect fun rankFromString(rank: String): Rank

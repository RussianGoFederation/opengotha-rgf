/*
 * This file is part of OpenGotha.
 *
 * OpenGotha is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * OpenGotha is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenGotha. If not, see <http://www.gnu.org/licenses/>.
 */

package ru.gofederation.gotha.model

import java.util.regex.Pattern

class Rank private constructor(value: Int) : Comparable<Rank> {
    /** -30[30k]..-1[1k] 0[1d]..8[9d] 9[1p]..17[9p] */
    val value: Int = value.coerceIn(kyuRange.first, proRange.last)

    override fun compareTo(other: Rank): Int = this.value - other.value

    override fun hashCode(): Int = 31 + value

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
        else -> throw IllegalStateException("Value ${value} is not valid rank")
    }

    companion object {
        const val PATTERN = "(\\d{1,2})([KkDdPp])"
        private val pattern = Pattern.compile(PATTERN)

        val kyuRange = -30 .. -1
        val danRange = 0 .. 8
        val proRange = 9 .. 17
        private val valueRange = kyuRange.first .. proRange.last

        private val cache = valueRange
            .map { it to Rank(it) }
            .toMap()
            .withDefault { Rank(it.coerceIn(valueRange)) }

        @JvmStatic
        fun fromInt(rank: Int): Rank = cache.getValue(rank)

        @JvmStatic
        fun fromString(value: String): Rank {
            val matcher = pattern.matcher(value)
            if (!matcher.find() || matcher.groupCount() != 2) {
                throw IllegalArgumentException("$value does not match rank pattern")
            }
            return when {
                "k" == matcher.group(2).toLowerCase() ->
                    fromInt(0 - matcher.group(1).toInt())
                "d" == matcher.group(2).toLowerCase() ->
                    fromInt(matcher.group(1).toInt() - 1)
                "p" == matcher.group(2).toLowerCase() ->
                    fromInt(matcher.group(1).toInt() + 8)
                else ->fromInt(kyuRange.first)
            }
        }

        @JvmStatic
        fun fromRating(origin: RatingOrigin, rating: Int): Rank =
            fromInt(Rating.ratingToRank(origin, rating))
    }
}

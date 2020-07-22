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

import mu.KotlinLogging

class Rating(val origin: RatingOrigin, rating: Int) : Comparable<Rating> {
    val value = rating.coerceIn(origin.minRating, origin.maxRating)

    override fun compareTo(other: Rating): Int {
        if (this.origin != other.origin) {
            logger.warn { "Directly comparing ratings of different origins: $this and $other" }
        }
        return this.value - other.value
    }

    operator fun plus(value: Int): Rating =
        Rating(origin, (this.value + value).coerceIn(origin.minRating, origin.maxRating))

    override fun hashCode(): Int =
        7 + value * 31 + origin.hashCode()

    override fun equals(other: Any?): Boolean =
        if (other is Rating) {
            this.origin == other.origin && this.value == other.value
        } else {
            false
        }

    fun toRank(): Int {
        return ratingToRank(origin, value);
    }

    override fun toString(): String {
        val os = origin.toString()
        return if (os.isEmpty()) {
            value.toString()
        } else {
            "$os:$value"
        }
    }

    companion object {
        const val MIN_RATING = -900;
        const val MAX_RATING = 2949;
        private const val MIN_RGF_RATING = 0
        private const val MAX_RGF_RATING = 2999

        private val logger by lazy { KotlinLogging.logger("Rating") }

        @JvmStatic
        fun minRating(origin: RatingOrigin): Rating =
            Rating(origin, origin.minRating)

        @JvmStatic
        fun clampRating(origin: RatingOrigin, rating: Int): Int {
            if (origin == RatingOrigin.RGF) {
                return rating.coerceIn(MIN_RGF_RATING, MAX_RGF_RATING)
            }
            throw IllegalArgumentException("Not implemented")
        }

        @JvmStatic
        fun ratingToRank(origin: RatingOrigin, rating: Int): Int =
            origin.ratingToRank(rating).value

        @JvmStatic
        fun rankToRating(origin: RatingOrigin, rank: Int): Int {
            // TODO test limits
            if (origin == RatingOrigin.RGF) {
                if (rank <= -20) {
                    return (rank + 30) * 60
                }
                return if (rank <= 0) {
                    (rank + 20) * 75 + 600
                } else rank * 100 + 2100
            }
            throw IllegalArgumentException("Not implemented")
        }

    }
}

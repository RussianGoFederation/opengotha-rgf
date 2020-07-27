package ru.gofederation.gotha.model

import mu.KotlinLogging
import ru.gofederation.gotha.util.Serializable

class Rating(val origin: RatingOrigin, rating: Int) : Comparable<Rating>, Serializable {
    val value = rating.coerceIn(origin.minRating, origin.maxRating)

    operator fun plus(delta: Int): Rating = Rating(origin, value + delta)

    override fun compareTo(other: Rating): Int {
        if (this.origin != other.origin) {
            logger.warn { "Directly comparing ratings of different origins: $this and $other" }
        }
        return this.value - other.value
    }

    fun toRank(): Rank = origin.ratingToRank(value)

    override fun hashCode(): Int =
        7 + value * 31 + origin.hashCode()

    override fun equals(other: Any?): Boolean =
        if (other is Rating) {
            this.origin == other.origin && this.value == other.value
        } else {
            false
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
        private val logger by lazy { KotlinLogging.logger(Rating::class.toString()) }

        const val MIN_RATING = -900
        const val MAX_RATING = 2949
    }
}

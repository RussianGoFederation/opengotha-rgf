package ru.gofederation.gotha.model

fun String.asRatingOrigin(): RatingOrigin =
    RatingOrigin.values().firstOrNull { it.name == this } ?: RatingOrigin.UNDEF

enum class RatingOrigin(val minRating: Int, val maxRating: Int) {
    UNDEF(-900, 2949),

    /** Rating coming from European Go DataBase  */
    EGF(-900, 2949),

    /** Rating coming from FFG Rating list  */
    FFG(-900, 2949),

    /** Rating coming from American Go Association  */
    AGA(-900, 2949),

    /** Rating coming from Russian Go Federation  */
    RGF(0, 3000),

    /** Rating specified by the organiser or imported from vBar-separated file  */
    MAN(-900, 2949),

    /** Rating computed from rank  */
    INI(-900, 2949);

    fun rating(rating: Int): Rating = Rating(this, rating)

    fun clampRatingValue(rating: Int): Int = rating.coerceIn(minRating, maxRating)

    fun ratingToRank(rating: Int): Rank = when (this) {
        RGF -> {
            when (rating) {
                in RGF.minRating..599 -> rating / 60 - 30
                in 600..2099 -> rating / 75 - 28
                in 2100..RGF.maxRating -> rating / 100 - 21
                else -> throw IllegalArgumentException("Rating $rating out of range")
            }.asRank()
        }

        else -> ((rating + 950) / 100 - 30).coerceIn(-30, 8).asRank()
    }

    fun rankToRating(rank: Rank): Rating = when (this) {
        RGF -> {
            val rating = when (val value = rank.value) {
                in -30..-20 -> (value + 30) * 60
                in -19..0 -> (value + 20) * 75 + 600
                in 0..8 -> value * 100 + 2100
                else -> throw IllegalStateException()
            }
            Rating(this, rating)
        }

        else -> Rating(this, (rank.value + 30) * 100 - 900)
    }

    override fun toString(): String {
        return if (this == UNDEF) "" else name
    }
}

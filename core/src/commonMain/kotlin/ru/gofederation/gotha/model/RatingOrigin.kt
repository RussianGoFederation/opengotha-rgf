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

    override fun toString(): String {
        return if (this == UNDEF) "" else name
    }
}

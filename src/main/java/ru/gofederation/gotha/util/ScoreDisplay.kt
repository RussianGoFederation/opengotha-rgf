package ru.gofederation.gotha.util

/**
 * Converts an Int to a String representing score / [ratio]
 * fractional part will be formatted as : ½ ¼ ¾
*/
fun Int.formatScore(ratio: Int): String {
    if (ratio == -1) // only Cat
        return "" + (-this + 1)

    val fract: Int = this % ratio
    val ent: Int = this / ratio
    return if (this in 1 until ratio) "" else ent.toString() +
        when {
            fract == 1 && ratio == 2 -> "½"
            fract == 1 && ratio == 4 -> "¼"
            fract == 2 && ratio == 4 -> "½"
            fract == 3 && ratio == 4 -> "¾"
            else -> ""
        }
}

@file:JvmName("RankKtJvm") // See https://stackoverflow.com/questions/57762986/how-to-configure-build-gradle-kts-to-fix-error-duplicate-jvm-class-name-generat
package ru.gofederation.gotha.model

import java.util.regex.Pattern

private val rankPattern = Pattern.compile("(\\d{1,2})([KkDdPp])")
internal actual fun rankFromString(rank: String): Rank {
    val matcher = rankPattern.matcher(rank)
    if (!matcher.find() || matcher.groupCount() != 2) {
        throw IllegalArgumentException("$rank does not match rank pattern")
    }
    return when {
        "k" == matcher.group(2).toLowerCase() ->
            Rank.fromInt(0 - matcher.group(1).toInt())
        "d" == matcher.group(2).toLowerCase() ->
            Rank.fromInt(matcher.group(1).toInt() - 1)
        "p" == matcher.group(2).toLowerCase() ->
            Rank.fromInt(matcher.group(1).toInt() + 8)
        else -> Rank.fromInt(Rank.kyuRange.first)
    }
}

package ru.gofederation.gotha.model

import info.vannier.gotha.DPParameterSet
import kotlin.math.abs

fun Player.getStrRawRating(): String {
    var r = rating.value
    var strRR = "" + r
    if (rating.origin == RatingOrigin.FFG){
        strRR = "" + (r - 2050)
    }
    if (rating.origin == RatingOrigin.AGA){
        r -= 2050;
        if (r >= 0) r += 100;
        if (r < 0) r -= 100;

        // Generate a eeee.ff string
        val e = r / 100
        val f = abs(r % 100)
        val strF = if (f > 9) ".$f" else ".0$f"

        strRR = "" + e + strF
    }
    return "" + strRR
}

fun Player.augmentedPlayerName(dpps: DPParameterSet): String {
    val strNF = shortenedFullName()

    val bGr = dpps.isShowPlayerGrade
    val bCo = dpps.isShowPlayerCountry
    val bCl = dpps.isShowPlayerClub

    if (!bGr && !bCo && !bCl) return strNF
    val sb = StringBuilder()
    sb.append(strNF).append("(")
    var bFirst = true
    if (bGr) {
        sb.append(this.grade.toString())
        bFirst = false
    }
    if (bCo) {
        if (!bFirst) sb.append(",")
        sb.append(this.country.padStart(2).take(2))
        bFirst = false
    }
    if (bCl) {
        if (!bFirst) sb.append(",")
        sb.append(this.club.padStart(4).take(4))
    }
    sb.append(")")
    return sb.toString()
}

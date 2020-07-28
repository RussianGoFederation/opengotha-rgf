package ru.gofederation.gotha.model

import com.soywiz.klock.Date
import ru.gofederation.gotha.Limits
import ru.gofederation.gotha.model.tps.GeneralParameterSetInterface
import ru.gofederation.gotha.util.Serializable
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

open class Player private constructor(
    val name: String,
    val firstName: String,
    val patronymic: String?,
    val dateOfBirth: Date?,
    val country: String,
    val club: String,
    val agaId: AgaId?,
    val egfPin: EgfPin?,
    val ffgLicence: FfgLicence?,
    val rgfId: RgfId?,
    val rank: Rank,
    val rating: Rating,
    val grade: Rank,
    val smmsCorrection: Int,
    val smmsByHand: Int,
    private val participating: BooleanArray, // private to prevent external changes
    val registeringStatus: PlayerRegistrationStatus
) : Serializable {
    val keyString = computeKeyString("$name$firstName")

    constructor(player: Player) : this(player.toBuilder())

    constructor(block: Builder.() -> Unit) : this(Builder().apply(block))

    constructor(builder: Builder) : this(
        name = builder.name,
        firstName = builder.firstName,
        patronymic = builder.patronymic,
        dateOfBirth = builder.dateOfBirth,
        country = builder.country,
        club = builder.club,
        agaId = builder.agaId,
        egfPin = builder.egfPin,
        ffgLicence = builder.ffgLicence,
        rgfId = builder.rgfId,
        rank = builder.rank,
        rating = builder.rating,
        grade = builder.grade,
        smmsCorrection = builder.smmsCorrection,
        smmsByHand = builder.smmsByHand,
        participating = builder.participating,
        registeringStatus = builder.registeringStatus
    )

    @Deprecated("Use Player.Builder in new code")
    constructor (name: String, firstName: String, country: String, club: String, egfPin: String, ffgLicence: String, ffgLicenceStatus: String,
                 agaId: String, agaExpirationDate: String,
                 rank: Int, rating: Int, ratingOrigin: RatingOrigin, strGrade: String, smmsCorrection: Int,
                 registeringStatus: PlayerRegistrationStatus) : this(
        name = name,
        firstName = firstName,
        patronymic = null,
        dateOfBirth = null,
        country = country,
        club = club,
        egfPin = egfPin(egfPin),
        ffgLicence = ffgLicence(ffgLicence, ffgLicenceStatus),
        agaId = agaId(agaId, agaExpirationDate),
        rgfId = null,
        rank = rank.asRank(),
        rating = ratingOrigin.rating(rating),
        grade = strGrade.asRank(),
        smmsCorrection = smmsCorrection,
        smmsByHand = -1,
        participating = BooleanArray(Limits.MAX_NUMBER_OF_ROUNDS) { true },
        registeringStatus = registeringStatus
    )

    fun toBuilder(): Builder = Builder(
        name = name,
        firstName = firstName,
        patronymic = patronymic,
        dateOfBirth = dateOfBirth,
        country = country,
        club = club,
        agaId = agaId,
        egfPin = egfPin,
        ffgLicence = ffgLicence,
        rgfId = rgfId,
        rank = rank,
        rating = rating,
        grade = grade,
        smmsCorrection = smmsCorrection,
        smmsByHand = smmsByHand,
        participating = participating,
        registeringStatus = registeringStatus
    )

    /**
     * Is this player participating in given 0-based [round]?
     */
    fun isParticipating(round: Int): Boolean {
        require(0 <= round && round < Limits.MAX_NUMBER_OF_ROUNDS)
        return participating[round]
    }

    fun category(gps: GeneralParameterSetInterface): Int {
        if (gps.numberOfCategories <= 1) return 0

        val cat = gps.lowerCategoryLimits
        for (c in cat.indices) {
            if (rank.value >= cat[c]) return c
        }
        return cat.size
    }

    fun isSmmsByHand(): Boolean = smmsByHand >= 0

    fun smms(gps: GeneralParameterSetInterface): Int =
        if (smmsByHand >= 0) {
            smmsByHand
        } else {
            val zero = gps.genMMZero
            val smms = rank.value - zero
            val floor = gps.genMMFloor
            val bar = gps.genMMBar

            smms.coerceIn(floor - zero, bar - zero) + smmsCorrection
        }

    fun hasSameKeyString(other: Player?) =
        if (other != null) this.keyString == other.keyString
        else false

    fun fullName(): String = "$name $firstName"

    fun shortenedFullName(): String =
        (name.take(18) + " " + firstName).take(22)

    fun fullUnblankedName(): String =
        name.replace(" ", "_") + " " + firstName.replace(" ", "_")

    /**
     * builds an Id String : RGF if exists, else EGF if exists, else FFG if exists, else AGA if exists, else ""
     */
    fun getAnIdString(): String = when {
        rgfId != null -> "RGF ID : " + rgfId.id.toString()
        egfPin != null -> "EGF Pin : " + egfPin.pin
        ffgLicence != null -> "FFG Licence : " + ffgLicence.licence
        agaId != null -> "AGA Id : " + agaId.id
        else -> ""
    }
    /**
     * Generates a [round] characters String
     * with [pc] for participating and and [npc] for not participating
     */
    @JvmOverloads
    fun getParticipatingString(round: Int, pc: Char = '+', npc: Char = '-'): String =
        participating.toList()
            .subList(0, round)
            .map { if (it) pc else npc }
            .joinToString(separator = "")

    class Builder(
        var name: String = "",
        var firstName: String = "",
        var patronymic: String? = null,
        var dateOfBirth: Date? = null,
        var country: String = "",
        var club: String = "",
        var agaId: AgaId? = null,
        var egfPin: EgfPin? = null,
        var ffgLicence: FfgLicence? = null,
        var rgfId: RgfId? = null,
        var rank: Rank = Rank.kyuRange.first.asRank(),
        var rating: Rating = rank.toRating(RatingOrigin.UNDEF),
        var grade: Rank = rank,
        var smmsCorrection: Int = 0,
        var smmsByHand: Int = -1,
        internal val participating: BooleanArray = BooleanArray(Limits.MAX_NUMBER_OF_ROUNDS) { true },
        var registeringStatus: PlayerRegistrationStatus = PlayerRegistrationStatus.FINAL
    ) {
        constructor () : this(name = "")

        fun isParticipating(round: Int): Boolean {
            require(0 <= round && round < Limits.MAX_NUMBER_OF_ROUNDS)
            return this.participating[round]
        }

        fun setParticipating(participating: BooleanArray) {
            require(participating.size == Limits.MAX_NUMBER_OF_ROUNDS)
            participating.forEachIndexed { i, p -> this.participating[i] = p }
        }

        fun setParticipating(round: Int, participating: Boolean) {
            require(0 <= round && round < Limits.MAX_NUMBER_OF_ROUNDS)
            this.participating[round] = participating
        }

        /**
         * Switches player's participation in 0-based [round]
         * @return new participation status
         */
        fun switchParticipating(round: Int): Boolean {
            require(0 <= round && round < Limits.MAX_NUMBER_OF_ROUNDS)
            this.participating[round] = !this.participating[round]
            return this.participating[round]
        }

        fun build(): Player = Player(this)
    }

    companion object {
        /**
         * Converts an int rank value into a String rank representation
         */
        @Deprecated(
            message = "Use Rank.asRank() directly",
            replaceWith = ReplaceWith(
                expression = "rank.asRank().toString()",
                imports = ["ru.gofederation.gotha.model.Rank"]))
        @JvmStatic
        fun convertIntToKD(rank: Int): String = rank.asRank().toString()

        /**
         * Converts a String rank into an int rank value
         */
        @Deprecated(
            message = "Use Rank.asRank() directly",
            replaceWith = ReplaceWith(
                expression = "rank.asRank().toString()",
                imports = ["ru.gofederation.gotha.model.Rank"]))
        @JvmStatic
        fun convertKDPToInt(rank: String): Int = rank.asRank().value

        @JvmStatic
        fun computeKeyString(s: String): String = s.replace(" ", "").toUpperCase()
    }
}

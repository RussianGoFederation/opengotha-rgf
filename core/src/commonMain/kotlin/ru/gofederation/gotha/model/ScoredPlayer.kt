package ru.gofederation.gotha.model

import ru.gofederation.gotha.model.tps.GeneralParameterSetInterface
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

/**
 * ScoredPlayer represents a player and all useful scoring information (nbw, mms, ... dc, sdc)
 *
 * All datas except dc and sdc are updated by (and only by) fillBaseScoringInfo(), according to gps as defined in current tournament
 * dc and sdc are updated by (and only by) fillDirScoringInfo(), according to pps and round number  as defined in argument.
 *
 * ScoredPlayer does not contain any information about pairing
 */
class ScoredPlayer(private val gps: GeneralParameterSetInterface, player: Player) : Player(player) {
    private val numberOfRounds = gps.numberOfRounds

    /** for each round, participation can be : [ABSENT], [NOT_ASSIGNED], [BYE] or [PAIRED] */
    private val participation = IntArray(numberOfRounds)
    /** [Game]s played by this player  */
    private val gameArray = Array<Game?>(numberOfRounds) { null }

    // First level scores
    /** number of wins * 2 */
    private var nbwX2 = IntArray(numberOfRounds)
    /** mcmahon score * 2 */
    private val mmsX2 = IntArray(numberOfRounds)
    /** strasbourg score * 2 */
    private val stsX2 = IntArray(numberOfRounds)
    // Virtual scores : half points are given for not played games
    /** number of wins * 2 */
    private var nbwVirtualX2 = IntArray(numberOfRounds)
    /** mcmahon score * 2 */
    private val mmsVirtualX2 = IntArray(numberOfRounds)
    /** Strasbourg score * 2 */
    private val stsVirtualX2 = IntArray(numberOfRounds)

    // Second level scores
    /** Sum of successive nbw2 */
    private var cuswX2 = IntArray(numberOfRounds)
    /** Sum of successive mms2 */
    private val cusmX2 = IntArray(numberOfRounds)
    /** Sum of Opponents nbw2 */
    private val soswX2 = IntArray(numberOfRounds)
    /** Sum of (n-1) Opponents nbw2 */
    private val soswM1X2 = IntArray(numberOfRounds)
    /** Sum of (n-2) Opponents nbw2 */
    private val soswM2X2 = IntArray(numberOfRounds)
    /** Sum of Defeated Opponents nbw2 X2 */
    private val sdswX4 = IntArray(numberOfRounds)
    /** Sum of Opponents mms2 */
    private val sosmX2 = IntArray(numberOfRounds)
    /** Sum of (n-1) Opponents mms2 */
    private val sosmM1X2 = IntArray(numberOfRounds)
    /** Sum of (n-2) Opponents mms2 */
    private val sosmM2X2 = IntArray(numberOfRounds)
    /** Sum of Defeated Opponents mms2 X2 */
    private val sdsmX4 = IntArray(numberOfRounds)
    /** Sum of Opponents sts2 */
    private val sostsX2 = IntArray(numberOfRounds)

    /** Exploits tentes (based on nbw2, with a weight factor) */
    private val extX2 = IntArray(numberOfRounds)
    /** Exploits reussis(based on nbw2, with a weight factor) */
    private val exrX2 = IntArray(numberOfRounds)

    // Third level scores
    /** Sum of opponents sosw2 * 2 */
    private val ssswX2 = IntArray(numberOfRounds)
    /** Sum of opponents sosm2 * 2 */
    private val sssmX2 = IntArray(numberOfRounds)

    // Special Scores
    /** Direct Confrontation */
    private var dc = 0
    /** Simplified Direct Confrontation */
    private var sdc = 0

    // Pairing informations. Unlike preceeding data, these informations are computed for one round only : the current one
    @JvmField
    var numberOfGroups = 0 // Very redundant
    @JvmField
    var groupNumber = 0 //
    @JvmField
    var groupSize = 0 // Redundant
    @JvmField
    var innerPlacement = 0 // placement in homogeneous group (category and mainScore) beteen 0 and size(group) - 1
    @JvmField
    var nbDU = 0 // Number of Draw-ups
    @JvmField
    var nbDD = 0 // Number of Draw-downs

    fun getParticipation(rn: Int): Int {
        return if (isValidRoundNumber(rn)) participation[rn] else 0
    }

    fun setParticipation(rn: Int, participation: Int) {
        if (isValidRoundNumber(rn)) this.participation[rn] = participation
    }

    fun getGame(rn: Int): Game? {
        return if (isValidRoundNumber(rn)) gameArray[rn] else null
    }

    fun setGame(rn: Int, g: Game?) {
        if (isValidRoundNumber(rn)) gameArray[rn] = g
    }

    fun gameWasPlayed(rn: Int): Boolean {
        val (_, _, _, _, _, _, result) = getGame(rn) ?: return false
        // not paired
        return result.gameWasPlayer()
    }

    fun getNBWX2(rn: Int): Int {
        return if (isValidRoundNumber(rn)) nbwX2[rn] else 0
    }

    fun setNBWX2(rn: Int, value: Int) {
        if (isValidRoundNumber(rn)) nbwX2[rn] = value
    }

    fun getMMSX2(rn: Int): Int {
        return if (isValidRoundNumber(rn)) mmsX2[rn] else 0
    }

    fun setMMSX2(rn: Int, value: Int) {
        if (isValidRoundNumber(rn)) mmsX2[rn] = value
    }

    fun getSTSX2(rn: Int): Int {
        return if (isValidRoundNumber(rn)) stsX2[rn] else 0
    }

    fun setSTSX2(rn: Int, value: Int) {
        if (isValidRoundNumber(rn)) stsX2[rn] = value
    }

    fun getNBWVirtualX2(rn: Int): Int {
        return if (isValidRoundNumber(rn)) nbwVirtualX2[rn] else 0
    }

    fun setNBWVirtualX2(rn: Int, value: Int) {
        if (isValidRoundNumber(rn)) nbwVirtualX2[rn] = value
    }

    fun getMMSVirtualX2(rn: Int): Int {
        return if (isValidRoundNumber(rn)) mmsVirtualX2[rn] else 0
    }

    fun setMMSVirtualX2(rn: Int, value: Int) {
        if (isValidRoundNumber(rn)) mmsVirtualX2[rn] = value
    }

    fun getSTSVirtualX2(rn: Int): Int {
        return if (isValidRoundNumber(rn)) stsVirtualX2[rn] else 0
    }

    fun setSTSVirtualX2(rn: Int, value: Int) {
        if (isValidRoundNumber(rn)) stsVirtualX2[rn] = value
    }

    fun getCUSWX2(rn: Int): Int {
        return if (isValidRoundNumber(rn)) cuswX2[rn] else 0
    }

    fun setCUSWX2(rn: Int, value: Int) {
        if (isValidRoundNumber(rn)) cuswX2[rn] = value
    }

    fun getCUSMX2(rn: Int): Int {
        return if (isValidRoundNumber(rn)) cusmX2[rn] else 0
    }

    fun setCUSMX2(rn: Int, value: Int) {
        if (isValidRoundNumber(rn)) cusmX2[rn] = value
    }

    fun getSOSWX2(rn: Int): Int {
        return if (isValidRoundNumber(rn)) soswX2[rn] else 0
    }

    fun setSOSWX2(rn: Int, value: Int) {
        if (isValidRoundNumber(rn)) soswX2[rn] = value
    }

    fun getSOSWM1X2(rn: Int): Int {
        return if (isValidRoundNumber(rn)) soswM1X2[rn] else 0
    }

    fun setSOSWM1X2(rn: Int, value: Int) {
        if (isValidRoundNumber(rn)) soswM1X2[rn] = value
    }

    fun getSOSWM2X2(rn: Int): Int {
        return if (isValidRoundNumber(rn)) soswM2X2[rn] else 0
    }

    fun setSOSWM2X2(rn: Int, value: Int) {
        if (isValidRoundNumber(rn)) soswM2X2[rn] = value
    }

    fun getSDSWX4(rn: Int): Int {
        return if (isValidRoundNumber(rn)) sdswX4[rn] else 0
    }

    fun setSDSWX4(rn: Int, value: Int) {
        if (isValidRoundNumber(rn)) sdswX4[rn] = value
    }

    fun getSOSMX2(rn: Int): Int {
        return if (isValidRoundNumber(rn)) sosmX2[rn] else 0
    }

    fun setSOSMX2(rn: Int, value: Int) {
        if (isValidRoundNumber(rn)) sosmX2[rn] = value
    }

    fun getSOSMM1X2(rn: Int): Int {
        return if (isValidRoundNumber(rn)) sosmM1X2[rn] else 0
    }

    fun setSOSMM1X2(rn: Int, value: Int) {
        if (isValidRoundNumber(rn)) sosmM1X2[rn] = value
    }

    fun getSOSMM2X2(rn: Int): Int {
        return if (isValidRoundNumber(rn)) sosmM2X2[rn] else 0
    }

    fun setSOSMM2X2(rn: Int, value: Int) {
        if (isValidRoundNumber(rn)) sosmM2X2[rn] = value
    }

    fun getSDSMX4(rn: Int): Int {
        return if (isValidRoundNumber(rn)) sdsmX4[rn] else 0
    }

    fun setSDSMX4(rn: Int, value: Int) {
        if (isValidRoundNumber(rn)) sdsmX4[rn] = value
    }

    fun getSOSTSX2(rn: Int): Int {
        return if (isValidRoundNumber(rn)) sostsX2[rn] else 0
    }

    fun setSOSTSX2(rn: Int, value: Int) {
        if (isValidRoundNumber(rn)) sostsX2[rn] = value
    }

    fun getEXTX2(rn: Int): Int {
        return if (isValidRoundNumber(rn)) extX2[rn] else 0
    }

    fun setEXTX2(rn: Int, value: Int) {
        if (isValidRoundNumber(rn)) extX2[rn] = value
    }

    fun getEXRX2(rn: Int): Int {
        return if (isValidRoundNumber(rn)) exrX2[rn] else 0
    }

    fun setEXRX2(rn: Int, value: Int) {
        if (isValidRoundNumber(rn)) exrX2[rn] = value
    }

    fun getSSSWX2(rn: Int): Int {
        return if (isValidRoundNumber(rn)) ssswX2[rn] else 0
    }

    fun setSSSWX2(rn: Int, value: Int) {
        if (isValidRoundNumber(rn)) ssswX2[rn] = value
    }

    fun getSSSMX2(rn: Int): Int {
        return if (isValidRoundNumber(rn)) sssmX2[rn] else 0
    }

    fun setSSSMX2(rn: Int, value: Int) {
        if (isValidRoundNumber(rn)) sssmX2[rn] = value
    }

    fun getDC(): Int {
        return dc
    }

    fun setDC(value: Int) {
        dc = value
    }

    fun getSDC(): Int {
        return sdc
    }

    fun setSDC(value: Int) {
        sdc = value
    }

    fun getCritValue(criterion: PlacementCriterion, rn: Int): Int {
        return when (criterion) {
            PlacementCriterion.NUL -> 0 // Null criterion
            PlacementCriterion.CAT -> -category(gps) // Category
            PlacementCriterion.RANK -> rank.value // Rank
            PlacementCriterion.RATING -> rating.value // Rating
            PlacementCriterion.NBW -> if (rn >= 0) nbwX2[rn] else 0 // Number of Wins
            PlacementCriterion.MMS -> if (rn >= 0) mmsX2[rn] else 2 * smms(gps) // McMahon score
            PlacementCriterion.STS -> if (rn >= 0) stsX2[rn] else 2 * smms(gps) // STS score
            PlacementCriterion.SOSW -> if (rn >= 0) soswX2[rn] else 0 // Sum of Opponents McMahon scores
            PlacementCriterion.SOSWM1 -> if (rn >= 0) soswM1X2[rn] else 0
            PlacementCriterion.SOSWM2 -> if (rn >= 0) soswM2X2[rn] else 0
            PlacementCriterion.SODOSW -> if (rn >= 0) this.getSDSWX4(rn) else 0 // Sum of Defeated Opponents Scores
            PlacementCriterion.SOSOSW -> if (rn >= 0) ssswX2[rn] else 0 // Sum of opponents SOS
            PlacementCriterion.CUSSW -> if (rn >= 0) cuswX2[rn] else 0 // Cuss
            PlacementCriterion.SOSM -> if (rn >= 0) sosmX2[rn] else 0 // Sum of Opponents McMahon scores
            PlacementCriterion.SOSMM1 -> if (rn >= 0) sosmM1X2[rn] else 0
            PlacementCriterion.SOSMM2 -> if (rn >= 0) sosmM2X2[rn] else 0
            PlacementCriterion.SODOSM -> if (rn >= 0) this.getSDSMX4(rn) else 0 // Sum of Defeated Opponents Scores
            PlacementCriterion.SOSOSM -> if (rn >= 0) sssmX2[rn] else 0 // Sum of opponents SOS
            PlacementCriterion.CUSSM -> if (rn >= 0) cusmX2[rn] else 0 // Cuss
            PlacementCriterion.SOSTS -> if (rn >= 0) sostsX2[rn] else 0 // Sum of Opponents STS scores
            PlacementCriterion.EXT -> if (rn >= 0) extX2[rn] else 0 // Exploits tentes
            PlacementCriterion.EXR -> if (rn >= 0) exrX2[rn] else 0 // Exploits reussis
            PlacementCriterion.DC -> dc
            PlacementCriterion.SDC -> sdc
        }
    }


    private inline fun isValidRoundNumber(rn: Int) = rn in 0 until numberOfRounds

    companion object {
        /** For a given round and a given player, this status should not happen in actual round/actual player */
        const val UNKNOWN = 0
        /** For a given round and a given player, qualifies the fact that this player has been declared as not participating */
        const val ABSENT = -3
        /** For a given round and a given player, qualifies the fact that this player has been assigned as a neither as bye nor to a real game */
        const val NOT_ASSIGNED = -2
        /** For a given round and a given player, qualifies the fact that this player has been assigned as a Bye player */
        const val BYE = -1
        /** For a given round and a given player, qualifies the fact that this player has been assigned to a real game */
        const val PAIRED = 1
    }
}

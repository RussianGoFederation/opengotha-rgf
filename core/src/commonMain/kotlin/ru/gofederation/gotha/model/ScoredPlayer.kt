package ru.gofederation.gotha.model

import ru.gofederation.gotha.model.tps.GeneralParameterSetInterface
import ru.gofederation.gotha.util.intArrayWithPrevious
import ru.gofederation.gotha.util.min2sum
import ru.gofederation.gotha.util.sumByIndexed
import kotlin.jvm.JvmField

/**
 * ScoredPlayer represents a player and all useful scoring information (nbw, mms, ... dc, sdc)
 *
 * All datas except dc and sdc are updated by (and only by) fillBaseScoringInfo(), according to gps as defined in current tournament
 * dc and sdc are updated by (and only by) fillDirScoringInfo(), according to pps and round number  as defined in argument.
 *
 * ScoredPlayer does not contain any information about pairing
 */
class ScoredPlayer(private val gps: GeneralParameterSetInterface, player: Player, games: List<Game>, players: Map<String, ScoredPlayer>, byePlayers: Array<Player?>) : Player(player) {
    private val numberOfRounds = gps.numberOfRounds

    /** for each round, participation can be : [ABSENT], [NOT_ASSIGNED], [BYE] or [PAIRED] */
    private val participation by lazy {
        IntArray(numberOfRounds) { round ->
            when {
                !player.isParticipating(round) -> ABSENT
                getGame(round) != null -> PAIRED
                player.hasSameKeyString(byePlayers[round]) -> BYE
                else -> NOT_ASSIGNED
            }
        }
    }
    /** [Game]s played by this player  */
    private val gameArray by lazy {
        Array<Game?>(numberOfRounds) { round ->
            games.firstOrNull { game ->
                game.round == round && game.hasPlayer(player)
            }
        }
    }

    // First level scores
    /** number of wins * 2 */
    private val nbwX2 by lazy {
        var score = 0
        var nbPtsNBW2AbsentOrBye = 0
        IntArray(numberOfRounds) { round ->
            score += getGame(round)?.getWX2(player) ?: 0
            nbPtsNBW2AbsentOrBye += when (getParticipation(round)) {
                ABSENT -> gps.genNBW2ValueAbsent
                BYE -> gps.genNBW2ValueBye
                else -> 0
            }
            score +
                if (gps.isGenRoundDownNBWMMS) (nbPtsNBW2AbsentOrBye / 2) * 2
                else nbPtsNBW2AbsentOrBye
        }
    }
    /** mcmahon score * 2 */
    private val mmsX2 by lazy {
        var score = smms(gps) * 2
        var nbPtsMMS2AbsentOrBye = 0
        IntArray(numberOfRounds) { round ->
            score += getGame(round)?.getWX2(player) ?: 0
            nbPtsMMS2AbsentOrBye += when (getParticipation(round)) {
                ABSENT -> gps.genMMS2ValueAbsent
                BYE -> gps.genMMS2ValueBye
                else -> 0
            }
            score + if (gps.isGenRoundDownNBWMMS) (nbPtsMMS2AbsentOrBye / 2) * 2
            else nbPtsMMS2AbsentOrBye
        }
    }
    /** strasbourg score * 2 */
    private val stsX2 by lazy {
        val score = IntArray(numberOfRounds) { round -> getMMSX2(round) }
        val nbRounds = gps.numberOfRounds
        // If player is in topgroup and always winner up to quarterfinal, increase by 2 * 2
        if (score[nbRounds - 3] == 2 * (30 + gps.genMMBar + nbRounds - 2)) {
            score[nbRounds - 3] += 4
            score[nbRounds - 2] += 4
            score[nbRounds - 1] += 4
        }
        // if player is in topgroup and always winner up to semifinal,    increase by 2 * 2
        if (score[nbRounds - 2] == 2 * (30 + gps.genMMBar + nbRounds - 1)) {
            score[nbRounds - 2] += 4
            score[nbRounds - 1] += 4
        }
        score
    }
    // Virtual scores : half points are given for not played games
    /** number of wins * 2 */
    private val nbwVirtualX2 by lazy {
        var prevScore = 0
        var nbVPX2 = 0
        IntArray(numberOfRounds) { round ->
            if (!gameWasPlayed(round)) nbVPX2++
            prevScore += getGame(round)?.let { game ->
                when (game.result) {
                    Game.Result.WHITE_WINS -> if (game.whitePlayer.hasSameKeyString(player)) 2 else 0
                    Game.Result.BLACK_WINS -> if (game.blackPlayer.hasSameKeyString(player)) 2 else 0
                    Game.Result.EQUAL -> 1
                    Game.Result.BOTH_WIN -> 2
                    else -> 0
                }
            } ?: 0
            prevScore + nbVPX2
        }
    }
    /** mcmahon score * 2 */
    private val mmsVirtualX2 by lazy {
        val smms = smms(gps)
        IntArray(numberOfRounds) { round -> getNBWVirtualX2(round) + smms }
    }
    /** Strasbourg score * 2 */
    private val stsVirtualX2 = IntArray(numberOfRounds)

    // Second level scores
    /** Sum of successive nbw2 */
    private val cuswX2 by lazy {
        intArrayWithPrevious(numberOfRounds) { round, prevValue ->
            getNBWX2(round) + prevValue
        }
    }
    /** Sum of successive mms2 */
    private val cusmX2 by lazy {
        intArrayWithPrevious(numberOfRounds) { round, prevValue ->
            getMMSX2(round) + prevValue
        }
    }
    /** Sum of Opponents nbw2 */
    private val soswX2 by lazy {
        val virtual = gps.isGenCountNotPlayedGamesAsHalfPoint
        IntArray(numberOfRounds) { round ->
            oswX2(round, virtual).sum()
        }
    }
    /** Sum of (n-1) Opponents nbw2 */
    private val soswM1X2 by lazy {
        val virtual = gps.isGenCountNotPlayedGamesAsHalfPoint
        IntArray(numberOfRounds) { round ->
            val oswX2 = oswX2(round, virtual)
            getSOSWX2(round) - (oswX2.min() ?: 0)
        }
    }
    /** Sum of (n-2) Opponents nbw2 */
    private val soswM2X2 by lazy {
        val virtual = gps.isGenCountNotPlayedGamesAsHalfPoint
        IntArray(numberOfRounds) { round ->
            if (round <= 1) {
                0
            } else {
                val oswX2 = oswX2(round, virtual)
                getSOSWX2(round) - oswX2.min2sum()
            }
        }
    }
    /** Sum of Defeated Opponents nbw2 X2 */
    private val sdswX4 by lazy {
        val virtual = gps.isGenCountNotPlayedGamesAsHalfPoint
        IntArray(numberOfRounds) { round ->
            (0..round)
                .map { rr -> getGame(rr)?.getOpponent(player)?.keyString }
                .map { keyString -> players[keyString] }
                .sumByIndexed { r, player ->
                    if (null != player) {
                        val game = player.getGame(r)
                        game!!.getWX2(game.getOpponent(player)) *
                            if (virtual) player.getNBWVirtualX2(round)
                            else player.getNBWX2(round)
                    } else {
                        0
                    }
                }
        }
    }
    /** Sum of Opponents mms2 */
    private val sosmX2 by lazy {
        val virtual = gps.isGenCountNotPlayedGamesAsHalfPoint
        IntArray(numberOfRounds) { round ->
            osmX2(round, virtual).sum()
        }
    }
    /** Sum of (n-1) Opponents mms2 */
    private val sosmM1X2 by lazy {
        val virtual = gps.isGenCountNotPlayedGamesAsHalfPoint
        IntArray(numberOfRounds) { round ->
            if (round <= 1) {
                0
            } else {
                val osmX2 = osmX2(round, virtual)
                osmX2.sum() - (osmX2.min() ?: 0)
            }
        }
    }
    /** Sum of (n-2) Opponents mms2 */
    private val sosmM2X2 by lazy {
        val virtual = gps.isGenCountNotPlayedGamesAsHalfPoint
        IntArray(numberOfRounds) { round ->
            if (round <= 1) {
                0
            } else {
                val osmX2 = osmX2(round, virtual)
                osmX2.sum() - osmX2.min2sum()
            }
        }
    }
    /** Sum of Defeated Opponents mms2 X2 */
    private val sdsmX4 by lazy {
        val virtual = gps.isGenCountNotPlayedGamesAsHalfPoint
        IntArray(numberOfRounds) { round ->
            val osmX2 = osmX2(round, virtual)
            osmX2.mapIndexed { rr, osm ->
                val game = getGame(rr)
                osm * (game?.getWX2(this) ?: 0)
            }.sum()
        }
    }
    /** Sum of Opponents sts2 */
    private val sostsX2 by lazy {
        val virtual = gps.isGenCountNotPlayedGamesAsHalfPoint
        val smmsX2 = 2 * smms(gps)
        IntArray(numberOfRounds) { round ->
            (0..round)
                .map { rr ->
                    val opp = opponents[rr]
                    when {
                        null == opp -> smmsX2
                        virtual -> opp.getSTSVirtualX2(round)
                        else -> opp.getSTSX2(round)
                    }
                }
                .sum()
        }
    }

    /** Exploits tentes (based on nbw2, with a weight factor) */
    private val extX2 by lazy {
        IntArray(numberOfRounds) { round ->
            var extx2 = 0
            for (rr in 0..round) {
                val game = getGame(rr) ?: continue
                val opp = opponents[rr] ?: continue
                val realHd = game.handicap
                val naturalHd = rank - opp.rank
                val coef = when {
                    realHd - naturalHd < 0 -> 0
                    realHd - naturalHd == 0 -> 1
                    realHd - naturalHd == 1 -> 2
                    else -> 3
                }
                extx2 += opp.getNBWX2(round) * coef
            }
            extx2
        }
    }
    /** Exploits reussis (based on nbw2, with a weight factor) */
    private val exrX2 by lazy {
        IntArray(numberOfRounds) { round ->
            var exrx2 = 0
            for (rr in 0..round) {
                val game = getGame(rr) ?: continue
                val opp = opponents[rr] ?: continue
                val spWasWhite = game.whitePlayer.hasSameKeyString(this)
                val realHd = game.handicap
                val naturalHd = rank - opp.rank
                val coef = when {
                    realHd - naturalHd < 0 -> 0
                    realHd - naturalHd == 0 -> 1
                    realHd - naturalHd == 1 -> 2
                    else -> 3
                }
                var bwin = false
                if (spWasWhite && (game.result == Game.Result.WHITE_WINS ||
                        game.result == Game.Result.WHITE_WINS_BYDEF ||
                        game.result == Game.Result.BOTH_WIN ||
                        game.result == Game.Result.BOTH_WIN_BYDEF)) {
                    bwin = true
                }
                if (!spWasWhite && (game.result == Game.Result.BLACK_WINS ||
                        game.result == Game.Result.BLACK_WINS_BYDEF ||
                        game.result == Game.Result.BOTH_WIN ||
                        game.result == Game.Result.BOTH_WIN_BYDEF)) {
                    bwin = true
                }
                if (bwin) exrx2 += opp.getNBWX2(round) * coef
            }
            exrx2
        }
    }

    // Third level scores
    /** Sum of opponents sosw2 * 2 */
    private val ssswX2 by lazy {
        IntArray(numberOfRounds) { round ->
            (0..round).fold(0) { sososwX2, rr ->
                val opp = opponents[rr]
                sososwX2 + (opp?.getSOSWX2(round) ?: 0)
            }
        }
    }
    /** Sum of opponents sosm2 * 2 */
    private val sssmX2 by lazy {
        IntArray(numberOfRounds) { round ->
            (0..round).fold(0) { sososmX2, rr ->
                val opp = opponents[rr]
                sososmX2 + (opp?.getSOSMX2(round) ?: 2 * smms(gps) * (round + 1))
            }
        }
    }

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

    private val opponents by lazy {
        Array<ScoredPlayer?>(numberOfRounds) { round ->
            players[getGame(round)?.getOpponent(this)?.keyString]
        }
    }

    private inline fun oswX2(round: Int, virtual: Boolean) =
        IntArray(round + 1) { rr ->
            val opp = opponents[rr]
            when {
                opp == null -> 0
                virtual -> opp.getNBWVirtualX2(round)
                else -> opp.getNBWX2(round)
            }
        }

    private inline fun osmX2(round: Int, virtual: Boolean): IntArray {
        val smmsX2 = 2 * smms(gps)
        return IntArray(round + 1) { rr ->
            val game = getGame(rr)
            val opp = opponents[rr]
            when {
                opp == null -> smmsX2
                virtual -> opp.getMMSVirtualX2(round)
                else -> opp.getMMSX2(round)
            } + when {
                game == null -> 0
                game.whitePlayer.hasSameKeyString(this) -> 2 * game.handicap
                game.blackPlayer.hasSameKeyString(this) -> -2 * game.handicap
                else -> 0
            }
        }
    }

    fun getParticipation(round: Int): Int {
        return if (isValidRoundNumber(round)) participation[round] else 0
    }

    fun getGame(round: Int): Game? {
        return if (isValidRoundNumber(round)) gameArray[round] else null
    }

    private fun gameWasPlayed(round: Int): Boolean {
        val (_, _, _, _, _, _, result) = getGame(round) ?: return false
        // not paired
        return result.gameWasPlayer()
    }

    fun getNBWX2(round: Int): Int {
        return if (isValidRoundNumber(round)) nbwX2[round] else 0
    }

    fun getMMSX2(round: Int): Int {
        return if (isValidRoundNumber(round)) mmsX2[round] else 0
    }

    fun getSTSX2(round: Int): Int {
        return if (isValidRoundNumber(round)) stsX2[round] else 0
    }

    fun getNBWVirtualX2(round: Int): Int {
        return if (isValidRoundNumber(round)) nbwVirtualX2[round] else 0
    }

    fun getMMSVirtualX2(round: Int): Int {
        return if (isValidRoundNumber(round)) mmsVirtualX2[round] else 0
    }

    fun getSTSVirtualX2(round: Int): Int {
        return if (isValidRoundNumber(round)) stsVirtualX2[round] else 0
    }

    fun getCUSWX2(round: Int): Int {
        return if (isValidRoundNumber(round)) cuswX2[round] else 0
    }

    fun getCUSMX2(round: Int): Int {
        return if (isValidRoundNumber(round)) cusmX2[round] else 0
    }

    fun getSOSWX2(round: Int): Int {
        return if (isValidRoundNumber(round)) soswX2[round] else 0
    }

    fun getSOSWM1X2(round: Int): Int {
        return if (isValidRoundNumber(round)) soswM1X2[round] else 0
    }

    fun getSOSWM2X2(round: Int): Int {
        return if (isValidRoundNumber(round)) soswM2X2[round] else 0
    }

    fun getSDSWX4(round: Int): Int {
        return if (isValidRoundNumber(round)) sdswX4[round] else 0
    }

    fun getSOSMX2(round: Int): Int {
        return if (isValidRoundNumber(round)) sosmX2[round] else 0
    }

    fun getSOSMM1X2(round: Int): Int {
        return if (isValidRoundNumber(round)) sosmM1X2[round] else 0
    }

    fun getSOSMM2X2(round: Int): Int {
        return if (isValidRoundNumber(round)) sosmM2X2[round] else 0
    }

    fun getSDSMX4(round: Int): Int {
        return if (isValidRoundNumber(round)) sdsmX4[round] else 0
    }

    fun getSOSTSX2(round: Int): Int {
        return if (isValidRoundNumber(round)) sostsX2[round] else 0
    }

    fun getEXTX2(round: Int): Int {
        return if (isValidRoundNumber(round)) extX2[round] else 0
    }

    fun getEXRX2(round: Int): Int {
        return if (isValidRoundNumber(round)) exrX2[round] else 0
    }

    fun getSSSWX2(round: Int): Int {
        return if (isValidRoundNumber(round)) ssswX2[round] else 0
    }

    fun getSSSMX2(round: Int): Int {
        return if (isValidRoundNumber(round)) sssmX2[round] else 0
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

        /**
         * Returns 2 if [Game] was won (incl. "both won") by [player], 1 for equal result and 0 otherwise
         */
        private inline fun Game.getWX2(player: Player?): Int = when (result) {
            Game.Result.BOTH_LOSE,
            Game.Result.BOTH_LOSE_BYDEF,
            Game.Result.UNKNOWN ->
                0
            Game.Result.WHITE_WINS_BYDEF,
            Game.Result.WHITE_WINS ->
                if (whitePlayer.hasSameKeyString(player)) 2 else 0
            Game.Result.BLACK_WINS_BYDEF,
            Game.Result.BLACK_WINS ->
                if (blackPlayer.hasSameKeyString(player)) 2 else 0
            Game.Result.EQUAL_BYDEF,
            Game.Result.EQUAL ->
                1
            Game.Result.BOTH_WIN_BYDEF,
            Game.Result.BOTH_WIN ->
                2
        }
    }
}

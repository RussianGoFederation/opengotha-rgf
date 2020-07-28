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
 *
 */

package ru.gofederation.gotha.pairing

import info.vannier.gotha.Gotha
import info.vannier.gotha.Pairing
import info.vannier.gotha.PairingParameterSet
import info.vannier.gotha.PlacementParameterSet
import info.vannier.gotha.TournamentInterface
import info.vannier.gotha.TournamentParameterSet
import ru.gofederation.gotha.model.PlacementCriterion
import ru.gofederation.gotha.model.ScoredPlayer
import ru.gofederation.gotha.model.gameBetween
import ru.gofederation.gotha.model.samePlayers
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToLong

class PairingCosts(
    var avoidDupGames: Long = 0,
    var random: Long = 0,
    var bwCost: Long = 0,
    var catCost: Long = 0,
    var scoCost: Long = 0,
    var duddCost: Long = 0,
    var seedCost: Long = 0,
    var hdCost: Long = 0,
    var geoCost: Long = 0
) {
    fun sum() = 1L + avoidDupGames + random + bwCost + catCost + scoCost + duddCost + seedCost + hdCost + geoCost

    override fun toString() =
        "dup=$avoidDupGames, rnd=$random, bw=$bwCost, cat=$catCost, score=$scoCost, dudd=$duddCost, seed=$seedCost, hd=$hdCost, geo=$geoCost"

    class Factory(
        val tournament: TournamentInterface,
        val round: Int
    ) {
        private val tps = tournament.tournamentParameterSet
        private val gps = tps.generalParameterSet
        private val pps = tps.pairingParameterSet

        fun costs(sp1: ScoredPlayer, sp2: ScoredPlayer): PairingCosts {
            val costs = PairingCosts(
                avoidDupGames = avoidDupGames(sp1, sp2),
                random = random(sp1, sp2),
                bwCost = bwBalanceCost(sp1, sp2),
                catCost = catCost(sp1, sp2),
                scoCost = scoCost(sp1, sp2),
                duddCost = duddCost(sp1, sp2),
                seedCost = seedCost(sp1, sp2)
            )
            costs.setSecCrit(sp1, sp2)
            return costs
        }

        fun avoidDupGames(sp1: ScoredPlayer, sp2: ScoredPlayer): Long {
            var numberOfPreviousGamesP1P2 = 0

            for (r in 0 until round) {
                val game = sp1.getGame(r) ?: continue
                if (game.samePlayers(sp1, sp2)) numberOfPreviousGamesP1P2 += 1
            }

            return if (numberOfPreviousGamesP1P2 == 0) {
                pps.paiBaAvoidDuplGame
            } else {
                0
            }
        }

        /**
         * Random
         */
        fun random(sp1: ScoredPlayer, sp2: ScoredPlayer): Long =
            if (pps.isPaiBaDeterministic) {
                Pairing.detRandom(pps.paiBaRandom, sp1, sp2)
            } else {
                Pairing.nonDetRandom(pps.paiBaRandom)
            }

        /**
         * Balance B & W
         * This cost is never applied if potential handicap != 0
         * It is fully applied if wbBalance([sp1]) and wbBalance([sp2]) are strictly of different signs
         * It is half applied if one of wbBalance is 0 and the other is >=2
         */
        fun bwBalanceCost(sp1: ScoredPlayer, sp2: ScoredPlayer): Long {
            val g = tournament.gameBetween(sp1, sp2, round)
            val potHd = g.handicap
            return if (potHd == 0) {
                val wb1 = Pairing.wbBalance(sp1, round - 1)
                val wb2 = Pairing.wbBalance(sp2, round - 1)
                if (wb1 * wb2 < 0) {
                    pps.paiBaBalanceWB
                } else if (wb1 == 0 && abs(wb2) >= 2) {
                    pps.paiBaBalanceWB / 2
                } else if (wb2 == 0 && abs(wb1) >= 2) {
                    pps.paiBaBalanceWB / 2
                } else {
                    0
                }
            } else {
                0
            }
        }

        /**
         * Avoid mixing categories
         */
        fun catCost(sp1: ScoredPlayer, sp2: ScoredPlayer): Long {
            val numberOfCategories = gps.numberOfCategories
            return if (numberOfCategories > 1) {
                val x: Double = abs(sp1.category(gps) - sp2.category(gps)).toDouble() / numberOfCategories.toDouble()
                val k: Double = pps.paiStandardNX1Factor
                (pps.paiMaAvoidMixingCategories * (1.0 - x) * (1.0 + k * x)).roundToLong()
            } else {
                0
            }
        }

        /**
         * Minimize score difference
         */
        fun scoCost(sp1: ScoredPlayer, sp2: ScoredPlayer): Long {
            val scoRange = sp1.numberOfGroups
            return if (sp1.category(gps) == sp2.category(gps)) {
                val x = (sp1.groupNumber - sp2.groupNumber) / scoRange.toDouble()
                val k = pps.paiStandardNX1Factor
                (pps.paiMaMinimizeScoreDifference * (1.0 - x) * (1.0 + k * x)).roundToLong()
            } else {
                0
            }
        }

        /**
         * 5 possible scenarios:
         * * 0 : Both players have already been drawn in the same sense
         * * 1 : One of the players has already been drawn in the same sense
         * * 2 : Normal conditions (does not correct anything and no previous drawn in the same sense).
         *                This case also occurs if one DU/DD is increased, while one is compensated
         * * 3 : It corrects a previous DU/DD
         * * 4 : it corrects a previous DU/DD for both
         */
        fun duddScenario(sp1: ScoredPlayer, sp2: ScoredPlayer): Int {
            var scenario = 2

            if (sp1.nbDU > 0 && sp1.groupNumber > sp2.groupNumber) scenario--
            if (sp1.nbDD > 0 && sp1.groupNumber < sp2.groupNumber) scenario--
            if (sp2.nbDU > 0 && sp2.groupNumber > sp1.groupNumber) scenario--
            if (sp2.nbDD > 0 && sp2.groupNumber < sp1.groupNumber) scenario--

            if (scenario != 0 && sp1.nbDU > 0 && sp1.nbDD < sp1.nbDU && sp1.groupNumber < sp2.groupNumber) scenario++
            if (scenario != 0 && sp1.nbDD > 0 && sp1.nbDU < sp1.nbDD && sp1.groupNumber > sp2.groupNumber) scenario++
            if (scenario != 0 && sp2.nbDU > 0 && sp2.nbDD < sp2.nbDU && sp2.groupNumber < sp1.groupNumber) scenario++
            if (scenario != 0 && sp2.nbDD > 0 && sp2.nbDU < sp2.nbDD && sp2.groupNumber > sp1.groupNumber) scenario++

            return scenario
        }

        /**
         * Direct draw-up/draw-down cost
         */
        fun duddCost(sp1: ScoredPlayer, sp2: ScoredPlayer): Long {
            var cost = 0L
            val duddWeight = pps.paiMaDUDDWeight / 5
            val upperSP = if (sp1.groupNumber < sp2.groupNumber) sp1 else sp2
            val lowerSP = if (sp1.groupNumber < sp2.groupNumber) sp2 else sp1

            cost += when (pps.paiMaDUDDUpperMode) {
                PairingParameterSet.PAIMA_DUDD_TOP -> {
                    duddWeight / 2 * (upperSP.groupSize - 1 - upperSP.innerPlacement) / upperSP.groupSize
                }
                PairingParameterSet.PAIMA_DUDD_MID -> {
                    duddWeight / 2 * (upperSP.groupSize - 1 - abs(2 * upperSP.innerPlacement - upperSP.groupSize + 1)) / upperSP.groupSize
                }
                PairingParameterSet.PAIMA_DUDD_BOT -> {
                    duddWeight / 2 * upperSP.innerPlacement / upperSP.groupSize
                }
                else -> 0
            }
            cost += when (pps.paiMaDUDDLowerMode) {
                PairingParameterSet.PAIMA_DUDD_TOP -> {
                    duddWeight / 2 * (lowerSP.groupSize - 1 - lowerSP.innerPlacement) / lowerSP.groupSize
                }
                PairingParameterSet.PAIMA_DUDD_MID -> {
                    duddWeight / 2 * (lowerSP.groupSize - 1 - abs(2 * lowerSP.innerPlacement - lowerSP.groupSize + 1)) / lowerSP.groupSize
                }
                PairingParameterSet.PAIMA_DUDD_BOT -> {
                    duddWeight / 2 * lowerSP.innerPlacement / lowerSP.groupSize
                }
                else -> 0
            }

            val scenario = duddScenario(sp1, sp2)
            when {
                scenario == 1 -> cost += 1 * duddWeight
                scenario == 2 || (scenario > 2 && !pps.isPaiMaCompensateDUDD) ->
                    cost += 2 * duddWeight
                scenario == 3 -> cost += 3 * duddWeight
                scenario == 4 -> cost += 4 * duddWeight
            }

            // Decrease cost if players come from different categories
            val catGap = abs(sp1.category(gps) - sp2.category(gps))
            cost /= ((catGap + 1) * 4)

            return cost
        }

        /**
         * Seeding cost
         */
        fun seedCost(sp1: ScoredPlayer, sp2: ScoredPlayer): Long {
            return if (sp1.groupNumber == sp2.groupNumber) {
                val groupSize = sp1.groupSize
                val cla1 = sp1.innerPlacement
                val cla2 = sp2.innerPlacement
                val maxSeedingWeight = pps.paiMaMaximizeSeeding
                when (if (round < pps.paiMaLastRoundForSeedSystem1) pps.paiMaSeedSystem1 else pps.paiMaSeedSystem2) {
                    PairingParameterSet.PAIMA_SEED_SPLITANDRANDOM -> {
                        if (2 * cla1 < groupSize && 2 * cla2 >= groupSize || 2 * cla1 >= groupSize && 2 * cla2 < groupSize) {
                            val randRange = (pps.paiMaMaximizeSeeding * 0.2).roundToLong()
                            val rand = Pairing.detRandom(randRange, sp1, sp2)
                            maxSeedingWeight - rand
                        } else 0L
                    }
                    PairingParameterSet.PAIMA_SEED_SPLITANDFOLD -> {
                        // The best is to get cla1 + cla2 - (groupSize - 1) close to 0
                        val x = cla1 + cla2 - (groupSize - 1)
                        maxSeedingWeight - maxSeedingWeight * x / (groupSize - 1) * x / (groupSize - 1)
                    }
                    PairingParameterSet.PAIMA_SEED_SPLITANDSLIP -> {
                        // The best is to get 2 * |Cla1 - Cla2| - groupSize    close to 0
                        val x = 2 * abs(cla1 - cla2) - groupSize
                        maxSeedingWeight - maxSeedingWeight * x / groupSize * x / groupSize
                    }
                    else -> 0L
                }
            } else {
                0L
            }
        }

        /**
         * Sets [PairingCosts.hdCost] and [PairingCosts.geoCost].
         */
        fun PairingCosts.setSecCrit(sp1: ScoredPlayer, sp2: ScoredPlayer) {
            var secCase = 0
            val nbw2Threshold =
                if (pps.isPaiSeNbWinsThresholdActive) gps.numberOfRounds
                else 2 * gps.numberOfRounds

            val mmBar = gps.genMMBar - Gotha.MIN_RANK

            var pseudoMMSSP1 = sp1.getCritValue(PlacementCriterion.MMS, round - 1) / 2
            var pseudoMMSSP2 = sp2.getCritValue(PlacementCriterion.MMS, round - 1) / 2
            val maxMMS = gps.genMMBar + PlacementParameterSet.PLA_SMMS_CORR_MAX - Gotha.MIN_RANK + round

            val nbwSP1X2 = sp1.getCritValue(PlacementCriterion.NBW, round - 1)
            val nbwSP2X2 = sp2.getCritValue(PlacementCriterion.NBW, round - 1)

            var bStrongMMS = (2 * sp1.rank.value + sp1.getCritValue(PlacementCriterion.NBW, round - 1) >= 2 * pps.paiSeRankThreshold)
            var bManyWins = nbwSP1X2 >= nbw2Threshold
            var bAboveMMBar = (sp1.smms(gps) >= mmBar && pps.isPaiSeBarThresholdActive)
            if (bManyWins
                || bStrongMMS
                || bAboveMMBar) {
                secCase++
                pseudoMMSSP1 = maxMMS
            }

            bStrongMMS = (2 * sp2.rank.value + sp2.getCritValue(PlacementCriterion.NBW, round - 1) >= 2 * pps.paiSeRankThreshold)
            bManyWins = nbwSP2X2 >= nbw2Threshold
            bAboveMMBar = (sp2.smms(gps) >= mmBar && pps.isPaiSeBarThresholdActive)
            if (bManyWins
                || bStrongMMS
                || bAboveMMBar) {
                secCase++
                pseudoMMSSP2 = maxMMS
            }

            val k = pps.paiStandardNX1Factor
            val secRange = if (tournament.tournamentType() == TournamentParameterSet.TYPE_MCMAHON) {
                sp1.numberOfGroups
            } else {
                (gps.genMMBar - gps.genMMFloor + PlacementParameterSet.PLA_SMMS_CORR_MAX - PlacementParameterSet.PLA_SMMS_CORR_MIN) + round
            }.toDouble()

            this.hdCost = hdCost(pseudoMMSSP1, pseudoMMSSP2, secRange, k)
            this.geoCost = geoCost(sp1, sp2, secCase, secRange, k)
        }

        /**
         * Handocap cost
         */
        fun hdCost(pseudoMMSSP1: Int, pseudoMMSSP2: Int, secRange: Double, k: Double): Long {
            val x = abs(pseudoMMSSP1 - pseudoMMSSP2) / secRange
            return (pps.paiSeMinimizeHandicap * (1.0 - x) * (1.0 + k * x)).roundToLong()
        }

        fun geoCost(sp1: ScoredPlayer, sp2: ScoredPlayer, secCase: Int, secRange: Double, k: Double): Long {
            var malusGeo = 0.0

            if (sp1.country == sp2.country) {
                val countryFactor = pps.paiSePreferMMSDiffRatherThanSameCountry
                val xCountry = (abs(countryFactor + 0.99) / secRange).coerceAtMost(1.0)
                val malusCountry = (1.0 - k) * xCountry + k * xCountry * xCountry
                malusGeo = malusCountry
            }

            if (tournament.playersAreInCommonGroup(sp1, sp2)) {
                val clubsGroupFactor = pps.paiSePreferMMSDiffRatherThanSameClubsGroup
                val xClubsGroup = (abs(clubsGroupFactor + 0.99) / secRange).coerceAtMost(1.0)
                val malusClubsGroup = (1.0 - k) * xClubsGroup + k * xClubsGroup * xClubsGroup
                malusGeo = max(malusGeo, malusClubsGroup)
            }

            if (sp1.club == sp2.club) {
                val clubFactor = pps.paiSePreferMMSDiffRatherThanSameClub
                val xClub = (abs(clubFactor + 0.99) / secRange).coerceAtMost(1.0)
                val malusClub = (1.0 - k) * xClub + k * xClub * xClub
                malusGeo = max(malusGeo, malusClub)
            }

            val geoMaxCost = pps.paiSeAvoidSameGeo
            val geoNominalCost = (geoMaxCost * (1.0 - malusGeo)).roundToLong()

            return when (secCase) {
                2 -> geoMaxCost
                1 -> (geoMaxCost + geoNominalCost) / 2
                0 -> geoNominalCost
                else -> geoNominalCost
            }
        }
    }
}

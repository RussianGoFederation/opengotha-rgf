package ru.gofederation.gotha

import info.vannier.gotha.Tournament
import org.junit.jupiter.api.Test
import ru.gofederation.gotha.model.Game
import ru.gofederation.gotha.model.Player
import ru.gofederation.gotha.model.Rating
import ru.gofederation.gotha.model.RatingOrigin
import ru.gofederation.gotha.model.ScoredPlayer
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ScoredPlayerTest {
    private val lastBaseScoringInfoRefreshTimeField = Tournament::class.java.getDeclaredField("lastBaseScoringInfoRefreshTime")
        .apply { isAccessible = true }

    @Test
    fun `Test player scoring`() {
        // Suppress log messages
        Logger.getLogger("info.vannier.gotha.Tournament").level = Level.OFF

        sequence {
            repeat(10) {
                yield(Tournament().also { tournament ->
                    // Basic tournament setup
                    val tps = tournament.tournamentParameterSet
                    tps.initForMM()
                    tps.generalParameterSet.setNumberOfRounds(Random.nextInt(4, Limits.MAX_NUMBER_OF_ROUNDS))
                    tps.generalParameterSet.setGenCountNotPlayedGamesAsHalfPoint(Random.nextBoolean())
                    tournament.tournamentParameterSet = tps

                    // Fill tournament with random players
                    repeat(Random.nextInt(6, 1001)) { n ->
                        tournament.addPlayer(Player {
                            name = "a_$n"
                            firstName = "b_$n"
                            rating = RatingOrigin.MAN.random()
                            setParticipating(BooleanArray(Limits.MAX_NUMBER_OF_ROUNDS) {
                                it != Random.nextInt(Limits.MAX_NUMBER_OF_ROUNDS * 4)
                            })
                        })
                    }

                    // Make automatic pairing and generate random results
                    (0 until tournament.tournamentParameterSet.generalParameterSet.numberOfRounds).forEach { round ->
                        val players = tournament.playersList().filter { player ->
                            player.isParticipating(round)
                        }.toMutableList()
                        if (players.size % 2 == 1) {
                            tournament.chooseAByePlayer(ArrayList(players), round)
                            players.remove(tournament.getByePlayer(round))
                        }
                        val games = tournament.makeAutomaticPairing(ArrayList(players), round)
                        games.forEachIndexed { i, game ->
                            val b = game.builder()
                            b.board = i
                            b.result = Game.Result.values().random()
                            tournament.addGame(b.build())
                        }
                    }
                })
            }
        }.forEach { tournament ->
            (0 until tournament.tournamentParameterSet.generalParameterSet.numberOfRounds).forEach { round ->
                println("r: $round g: ${tournament.gamesList(round).size}")
                // Reset scoring info refresh time. In test scenario calls to orderedScoredPlayersList() happen too fast.
                lastBaseScoringInfoRefreshTimeField.set(tournament, 0)

                val scoredPlayers = tournament.orderedScoredPlayersList(round, tournament.tournamentParameterSet.placementParameterSet)
                val referenceScoredPlayers = ScoredPlayerReference.getOrderedScoredPlayerList(tournament, round)

                compare(scoredPlayers, referenceScoredPlayers, round, tournament)
            }
        }
    }

    private fun compare(
        scoredPlayers: List<ScoredPlayer>,
        referenceScoredPlayers: List<ScoredPlayerReference>,
        round: Int,
        tournament: Tournament
    ) {
        assertEquals(scoredPlayers.size, referenceScoredPlayers.size)
        referenceScoredPlayers.forEach { ref ->
            val key = ref.keyString
            val sp = scoredPlayers.firstOrNull { it.keyString == key }
            assertNotNull(sp)

            assertEquals(ref.smms(tournament.tournamentParameterSet.generalParameterSet),
            sp.smms(tournament.tournamentParameterSet.generalParameterSet))

            assertEquals(ref.getGame(round), sp.getGame(round))
            assertEquals(ref.getParticipation(round), sp.getParticipation(round))

            assertEquals(ref.getNBWX2(round), sp.getNBWX2(round))
            assertEquals(ref.getMMSX2(round), sp.getMMSX2(round))
            assertEquals(ref.getSTSX2(round), sp.getSTSX2(round))

            assertEquals(ref.getMMSVirtualX2(round), sp.getMMSVirtualX2(round))
            assertEquals(ref.getNBWVirtualX2(round), sp.getNBWVirtualX2(round))
            assertEquals(ref.getSTSVirtualX2(round), sp.getSTSVirtualX2(round))

            assertEquals(ref.getCUSWX2(round), sp.getCUSWX2(round))
            assertEquals(ref.getCUSMX2(round), sp.getCUSMX2(round))

            assertEquals(ref.getSOSWX2(round), sp.getSOSWX2(round))
            assertEquals(ref.getSOSWM1X2(round), sp.getSOSWM1X2(round))
            assertEquals(ref.getSOSWM2X2(round), sp.getSOSWM2X2(round))
            assertEquals(ref.getSDSWX4(round), sp.getSDSWX4(round))

            assertEquals(ref.getSOSMX2(round), sp.getSOSMX2(round))
            assertEquals(ref.getSOSMM1X2(round), sp.getSOSMM1X2(round))
            assertEquals(ref.getSOSMM2X2(round), sp.getSOSMM2X2(round))
            assertEquals(ref.getSDSMX4(round), sp.getSDSMX4(round))
            assertEquals(ref.getSOSTSX2(round), sp.getSOSTSX2(round))

            assertEquals(ref.getEXTX2(round), sp.getEXTX2(round))
            assertEquals(ref.getEXRX2(round), sp.getEXRX2(round))
            assertEquals(ref.getSSSWX2(round), sp.getSSSWX2(round))
            assertEquals(ref.getSSSMX2(round), sp.getSSSMX2(round))
        }
    }

    private fun RatingOrigin.random(): Rating {
        val max = this.maxRating - this.minRating
        return this.rating(Random.nextInt(max) + this.minRating)
    }
}

package ru.gofederation.gotha.model

import ru.gofederation.gotha.Limits
import ru.gofederation.gotha.util.Serializable

/**
 * A game between two [Player]s.
 */
data class Game internal constructor (
    /** 0-based round number */
    val round: Int,
    /** 0-based board number */
    val board: Int,
    val whitePlayer: Player,
    val blackPlayer: Player,
    /**
     * true if colors are known.
     * Note that, in some no-handicap tournaments, color is not published.
     * When it happens, players are randomly said to be white or black.
     * [knownColor] remembers whether color was actually known
     */
    val knownColor: Boolean,
    val handicap: Int,
    val result: Result
) : Serializable {
    init {
        require(board >= 0 && board < Limits.MAX_NUMBER_OF_BOARDS)
        require(round >= 0 && round < Limits.MAX_NUMBER_OF_ROUNDS)
    }

    /** [key] is used to address games in [Tournament.hmGames] */
    val key: Int
        get() = round * Limits.MAX_NUMBER_OF_BOARDS + board

    fun isWinner(player: Player) = when {
        player.hasSameKeyString(whitePlayer) -> {
            when(result) {
                Result.BOTH_WIN,
                Result.BOTH_WIN_BYDEF,
                Result.WHITE_WINS,
                Result.WHITE_WINS_BYDEF -> true
                else -> false
            }
        }
        player.hasSameKeyString(blackPlayer) -> {
            when(result) {
                Result.BOTH_WIN,
                Result.BOTH_WIN_BYDEF,
                Result.BLACK_WINS,
                Result.BLACK_WINS_BYDEF -> true
                else -> false
            }
        }
        else -> {
            false
        }
    }

    /**
     * Returns string representation of game result.
     * if [wb] is true puts white player first.
     */
    fun resultAsString(wb: Boolean): String = if (wb) {
        result.toString()
    } else {
        result.mirrored().toString()
    }

    /** Helper function to be used from Java code */
    fun withBoard(board: Int) = copy(board = board)

    /** Helper function to be used from Java code */
    fun withHandicap(handicap: Int) = copy(handicap = handicap.coerceHandicap())

    /** Helper function to be used from Java code */
    fun withResult(result: Result) = copy(result = result)

    /** Helper function to be used from Java code */
    fun withRound(round: Int) = copy(round = round)

    fun exchangeColors() = copy(
        blackPlayer = whitePlayer,
        whitePlayer = blackPlayer,
        result = result.mirrored()
    )

    fun builder(): Builder = Builder(
        round = round,
        board = board,
        whitePlayer = whitePlayer,
        blackPlayer = blackPlayer,
        knownColor = knownColor,
        handicap = handicap,
        result = result
    )

    /**
     * Helper class to be used from Java code
     */
    class Builder(
        var round: Int = 0,
        var board: Int = 0,
        var whitePlayer: Player? = null,
        var blackPlayer: Player? = null,
        var knownColor: Boolean = false,
        var handicap: Int = 0,
        var result: Result = Result.UNKNOWN
    ) {
        fun build(): Game = create(
            round = round,
            board = board,
            whitePlayer = whitePlayer ?: throw IllegalStateException("White player must be set"),
            blackPlayer = blackPlayer ?: throw IllegalStateException("Black player must be set"),
            knownColor = knownColor,
            handicap = handicap,
            result = result
        )
    }

    enum class Result(val code: Int, private val str: String) {
        /** Result unknown (usually means not yet input) */
        UNKNOWN(0, " - "),
        /** White player wins */
        WHITE_WINS(17, "1-0"),
        WHITE_WINS_BYDEF(WHITE_WINS.code + 256, "1-0!"),
        /** Black player wins */
        BLACK_WINS(18, "0-1"),
        BLACK_WINS_BYDEF(BLACK_WINS.code + 256, "0-1!"),
        /** Equal result (Jigo or any other reason for a draw). Means 1/2 - 1/2 */
        EQUAL(19, "½-½"),
        EQUAL_BYDEF(EQUAL.code + 256, "½-½!"),
        /** Both players lose. Means 0 - 0 */
        BOTH_LOSE(32, "0-0"),
        BOTH_LOSE_BYDEF(BOTH_LOSE.code + 256, "0-0!"),
        /** Result : Both win. Means 1 - 1 */
        BOTH_WIN(35, "1-1"),
        BOTH_WIN_BYDEF(BOTH_WIN.code + 256, "1-1!");

        fun gameWasPlayer(): Boolean = when(this) {
            UNKNOWN,
            WHITE_WINS_BYDEF,
            BLACK_WINS_BYDEF,
            EQUAL_BYDEF,
            BOTH_LOSE_BYDEF,
            BOTH_WIN_BYDEF -> false
            else -> true
        }

        fun mirrored(): Result = when(this) {
            WHITE_WINS -> BLACK_WINS
            WHITE_WINS_BYDEF -> BLACK_WINS_BYDEF
            BLACK_WINS -> WHITE_WINS
            BLACK_WINS_BYDEF -> BLACK_WINS_BYDEF
            else -> this
        }

        fun isByDef(): Boolean =
            this.code >= 256

        fun notByDef(): Result = when(this) {
            WHITE_WINS_BYDEF -> WHITE_WINS
            BLACK_WINS_BYDEF -> BLACK_WINS
            EQUAL_BYDEF -> EQUAL
            BOTH_LOSE_BYDEF -> BOTH_LOSE
            BOTH_WIN_BYDEF -> BOTH_WIN
            else -> this
        }

        fun byDef(): Result = when(this) {
            WHITE_WINS -> WHITE_WINS_BYDEF
            BLACK_WINS -> BLACK_WINS_BYDEF
            EQUAL -> EQUAL_BYDEF
            BOTH_LOSE -> BOTH_WIN_BYDEF
            BOTH_WIN -> BOTH_WIN_BYDEF
            else -> this
        }

        override fun toString(): String = this.str
    }

    companion object {
        fun create(
            round: Int,
            board: Int,
            whitePlayer: Player,
            blackPlayer: Player,
            knownColor: Boolean,
            handicap: Int,
            result: Result
        ) = Game(
            round = round,
            board = board,
            whitePlayer = whitePlayer,
            blackPlayer = blackPlayer,
            knownColor = knownColor,
            handicap = handicap,
            result = result
        )

        private fun Int.coerceHandicap() = this.coerceIn(0, 9)
    }
}

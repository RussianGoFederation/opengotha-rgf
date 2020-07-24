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
 */

package ru.gofederation.gotha.presenter

import info.vannier.gotha.DPParameterSet
import info.vannier.gotha.PlacementCriterion
import info.vannier.gotha.ScoredPlayer
import info.vannier.gotha.TournamentInterface
import info.vannier.gotha.TournamentParameterSet
import ru.gofederation.gotha.model.HalfGame
import ru.gofederation.gotha.util.GothaLocale
import javax.swing.table.AbstractTableModel

class StandingsTableModel(
    private val tournament: TournamentInterface, private val tps: TournamentParameterSet, private val displayedRoundNumber: Int
) : AbstractTableModel() {
    private lateinit var players: List<ScoredPlayer>
    private lateinit var games: Array<Array<HalfGame>>
    private lateinit var places: Array<String>
    private lateinit var columns: List<Column>

    fun getColumnIdentifiers(): List<Column> {
        return columns
    }

    init {
        invalidate()
    }

    private fun invalidate() {
        val dpps = tps.dpParameterSet
        players = tournament.orderedScoredPlayersList(displayedRoundNumber, tps.placementParameterSet).apply {
            // Eliminate non-players
            if (!dpps.isDisplayNPPlayers) { removeIf { !tournament.isPlayerImplied(it) } }
        }

        games = players.halfGames(displayedRoundNumber, tournament)
        places = ScoredPlayer.catPositionStrings(players, displayedRoundNumber, tps)

        columns = invalidateColumns()

        fireTableDataChanged()
    }

    private fun invalidateColumns(): List<Column> {
        return ArrayList<Column>().also { columns ->
            if (tps.dpParameterSet.isDisplayNumCol) {
                columns.add(Column(ColumnType.NUMBER, "Num"))
            }
            if (tps.dpParameterSet.isDisplayPlCol) {
                columns.add(Column(ColumnType.PLACE, "Pl"))
            }
            columns.add(Column(ColumnType.NAME, "Name"))
            columns.add(Column(ColumnType.GRADE, "Gr"))
            if (tps.dpParameterSet.isDisplayCoCol) {
                columns.add(Column(ColumnType.COUNTRY, "Co"))
            }

            if (tps.dpParameterSet.isDisplayClCol) {
                columns.add(Column(ColumnType.CLUB, "Cl"))
            }

            // TODO: make this configurable
            columns.add(Column(ColumnType.RATING, GothaLocale.getCurrentLocale().tr("player.rating")))

            // TODO: make this configurable
            columns.add(Column(ColumnType.MM0, "MM0"))

            columns.add(Column(ColumnType.NBW, "NBW"))

            for (r in 0..displayedRoundNumber) {
                columns.add(RoundColumn(r))
            }

            tps.placementParameterSet.plaCriteria
                .filter { crit -> crit != PlacementCriterion.NUL }
                .forEach { columns.add(PlaCritColumn(it)) }
        }
    }

    override fun getColumnCount(): Int {
        return columns.size
    }

    override fun getRowCount(): Int {
        return players.size
    }

    override fun getColumnName(col: Int): String {
        return columns[col].name
    }

    override fun getValueAt(row: Int, col: Int): Any {
        val column = getColumnIdentifiers()[col]
        return when (column.type) {
            ColumnType.NUMBER -> "${row + 1}"
            ColumnType.PLACE -> places[row]
            ColumnType.NAME -> players[row].fullName()
            ColumnType.GRADE -> players[row].strGrade
            ColumnType.COUNTRY -> players[row].country
            ColumnType.CLUB -> players[row].club
            ColumnType.RATING -> players[row].rating.value
            ColumnType.MM0 -> players[row].smms(tournament.tournamentParameterSet.generalParameterSet)
            ColumnType.NBW -> players[row].formatScore(PlacementCriterion.NBW, this.displayedRoundNumber)
            ColumnType.ROUND_RESULT -> games[(column as RoundColumn).round][row]
            ColumnType.CRITERION -> players[row].formatScore((column as PlaCritColumn).crit, this.displayedRoundNumber)
        }
    }

    fun getValueAt(row: Int, columnType: ColumnType): Any {
        val col = getColumnIdentifiers().find { it.type == columnType }
        if (null != col) {
            val colNum = getColumnIdentifiers().indexOf(col)
            return getValueAt(row, colNum)
        }
        return ""
    }

    enum class ColumnType(internal val prefWidth: Int) {
        NUMBER(45),
        PLACE(30),
        NAME(165),
        GRADE(45),
        COUNTRY(45),
        CLUB(45),
        RATING(45),
        MM0(45),
        NBW(30),
        ROUND_RESULT(52),
        CRITERION(60)

    }

    open inner class Column(val type: ColumnType, val name: String) : ITableColumn {
        override val prefWidth: Int
            get() {
                return if (type == ColumnType.ROUND_RESULT && this@StandingsTableModel.tps.dpParameterSet.gameFormat == DPParameterSet.DP_GAME_FORMAT_FULL) {
                    82
                } else {
                    type.prefWidth
                }
            }
    }

    private inner class RoundColumn internal constructor(val round: Int) : Column(ColumnType.ROUND_RESULT, "R" + (round + 1))

    private inner class PlaCritColumn internal constructor(val crit: PlacementCriterion) : Column(ColumnType.CRITERION, crit.shortName)
}

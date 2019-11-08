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

package ru.gofederation.gotha.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.miginfocom.swing.MigLayout
import ru.gofederation.api.Client
import ru.gofederation.api.RgfTournament
import ru.gofederation.api.TournamentList
import ru.gofederation.api.TournamentListError
import ru.gofederation.gotha.ui.component.watchProgress
import ru.gofederation.gotha.util.I18N
import java.awt.CardLayout
import java.awt.Component
import java.awt.Font
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.*
import javax.swing.event.MouseInputAdapter
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer
import kotlin.streams.toList

internal class RgfTournamentList(tournamentPickListener: TournamentPickListener) : Panel() {
    private val tournamentsTable: JTable
    private val progressBar: JProgressBar
    private val layout: CardLayout
    private val rgfApiClient = Client()
    private var updateJob: Job? = null

    init {
        val tableCellRenderer = object : DefaultTableCellRenderer() {
            override fun getTableCellRendererComponent(table: JTable?,
                                                       value: Any?, isSelected: Boolean, hasFocus: Boolean,
                                                       row: Int, column: Int): Component {
                val newValue = when (value) {
                    is Date ->
                        dateFormat.format(value)
                    is RgfTournament.State ->
                        when (value) {
                            RgfTournament.State.REGISTRATION -> tr("tournament.rgf.state.registration")
                            RgfTournament.State.CONDUCTING -> tr("tournament.rgf.state.conducting")
                            RgfTournament.State.MODERATION -> tr("tournament.rgf.state.moderation")
                            RgfTournament.State.NON_RATING -> tr("tournament.rgf.state.non_rating")
                            RgfTournament.State.RATED -> tr("tournament.rgf.state.rated")
                            else -> ""
                        }
                    else -> value
                }
                return super.getTableCellRendererComponent(table, newValue, isSelected, hasFocus, row, column)
            }
        }

        layout = CardLayout()
        setLayout(layout)

        tournamentsTable = JTable().apply {
            setDefaultRenderer(Date::class.java, tableCellRenderer)
            setDefaultRenderer(RgfTournament.State::class.java, tableCellRenderer)
            model = TableModel(this@RgfTournamentList)
            addMouseListener(object : MouseInputAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    super.mouseClicked(e)
                    val point = e!!.point
                    val row = rowAtPoint(point)
                    if (e.clickCount == 2 && selectedRow != -1) {
                        val model = model as TableModel
                        val tournament = model.getTournament(convertRowIndexToModel(row))
                        tournamentPickListener.onTournamentPicked(tournament)
                    }
                }
            })
            addKeyListener(object : KeyAdapter() {
                override fun keyPressed(event: KeyEvent?) {
                    if (event!!.keyCode == KeyEvent.VK_ENTER) {
                        event.consume()
                        val model = model as TableModel
                        val tournament = model.getTournament(
                            convertRowIndexToModel(selectedRow))
                        tournamentPickListener.onTournamentPicked(tournament)
                    }
                }
            })

        }

        progressBar = JProgressBar().apply {
            isStringPainted = true
        }

        val tablePanel = JPanel(MigLayout("insets 0, flowy", "[grow,fill]", "[grow,fill][]")).apply {
            add(JScrollPane(tournamentsTable))
            add(JLabel(tr("tournament.rgf.import.filter_applications")).also {
                it.font = it.font.deriveFont(Font.ITALIC)
            })
        }

        val progressPanel = JPanel(MigLayout("flowy", "push[]push", "push[]rel[]push")).apply {
            add(JLabel(tr("tournament.rgf.download_in_progress")))
            add(progressBar)
        }

        add(progressPanel, PROGRESS)
        add(tablePanel, LIST)

        update()
    }

    fun update() {
        updateJob?.cancel()
        updateJob = launch(Dispatchers.Main) {
            val tournamentsResult = withContext(Dispatchers.IO) {
                val progress = Channel<Pair<Long, Long>>(Channel.CONFLATED)
                launch(Dispatchers.Main) { progressBar.watchProgress(progress) }
                rgfApiClient.fetchTournaments(progress)
            }

            when (tournamentsResult) {
                is TournamentListError ->
                    ExceptionDialog("tournament.rgf.import.download_error", tournamentsResult.exception)
                        .show(this@RgfTournamentList)
                is TournamentList -> {
                    val model = (tournamentsTable.model as TableModel)
                    model.tournaments = tournamentsResult.tournaments
                        .stream()
                        .filter { it.applicationsCount?:0 > 0 }
                        .filter { it.endDate >= ru.gofederation.api.Date() }
                        .toList()
                    onListDownloaded(model)
                }
            }
        }
    }

    private fun onListDownloaded(model: TableModel) {
        tournamentsTable.model = model
        tournamentsTable.columnModel.getColumn(TableModel.NAME_COLUMN).preferredWidth = 400
        tournamentsTable.columnModel.getColumn(TableModel.LOCATION_COLUMN).preferredWidth = 150
        tournamentsTable.autoCreateRowSorter = true
        tournamentsTable.rowSorter.toggleSortOrder(TableModel.END_DATE_COLUMN)
        layout.show(this, LIST)
    }

    class TableModel(i18n: I18N) : AbstractTableModel(), I18N by i18n {
        var tournaments: List<RgfTournament> = ArrayList()

        internal fun getTournament(row: Int): RgfTournament {
            return tournaments[row]
        }

        override fun getColumnName(col: Int): String? {
            return when (col) {
                NAME_COLUMN -> tr("tournament.name")
                START_DATE_COLUMN -> tr("tournament.begin_date")
                END_DATE_COLUMN -> tr("tournament.end_date")
                LOCATION_COLUMN -> tr("tournament.location")
                STATE_COLUMN -> tr("tournament.rgf.state")
                APPLICATIONS_COLUMN -> tr("tournament.rgf.applications")
                PARTICIPANTS_COLUMN -> tr("tournament.rgf.participants")
                else -> null
            }
        }

        override fun getRowCount(): Int {
            return tournaments.size
        }

        override fun getColumnCount(): Int {
            return 7
        }

        override fun getValueAt(row: Int, column: Int): Any? {
            when (column) {
                NAME_COLUMN -> return tournaments[row].name
                START_DATE_COLUMN -> return tournaments[row].startDate
                END_DATE_COLUMN -> return tournaments[row].endDate
                LOCATION_COLUMN -> return tournaments[row].location
                STATE_COLUMN -> return tournaments[row].state
                APPLICATIONS_COLUMN -> return tournaments[row].applicationsCount
                PARTICIPANTS_COLUMN -> return tournaments[row].participantsCount
                else -> return null
            }
        }

        override fun getColumnClass(column: Int): Class<*> {
            when (column) {
                START_DATE_COLUMN, END_DATE_COLUMN -> return Date::class.java

                STATE_COLUMN -> return RgfTournament.State::class.java

                else -> return String::class.java
            }
        }

        companion object {
            const val NAME_COLUMN = 0
            const val START_DATE_COLUMN = 1
            const val END_DATE_COLUMN = 2
            const val LOCATION_COLUMN = 3
            const val STATE_COLUMN = 4
            const val APPLICATIONS_COLUMN = 5
            const val PARTICIPANTS_COLUMN = 6
        }
    }

    interface TournamentPickListener {
        fun onTournamentPicked(tournament: RgfTournament)
    }

    companion object {
        private const val PROGRESS = "progress"
        private const val LIST = "list"
    }
}

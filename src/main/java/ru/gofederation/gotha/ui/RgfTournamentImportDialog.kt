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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.miginfocom.swing.MigLayout
import ru.gofederation.api.*
import ru.gofederation.gotha.ui.component.addAll
import java.lang.StringBuilder
import javax.swing.*

class RgfTournamentImportDialog(private val tournamentOpener: TournamentOpener) : Panel(), RgfTournamentList.TournamentPickListener {
    private val rgfApiClient = Client()
    private var fetchJob: Job? = null

    private val importApplications = JRadioButton(tr("tournament.rgf.import.applications"))
    private val importParticipants = JRadioButton(tr("tournament.rgf.import.participants"))

    init {
        layout = MigLayout("insets dialog, flowy", "[grow,fill]", "[]unrel[grow,fill][]")

        add(JPanel(MigLayout("flowy, insets panel")).apply {
            border = BorderFactory.createTitledBorder(tr("tournament.rgf.import.options"))

            add(importApplications.apply {
                isSelected = true
            })

            add(importParticipants.apply {
                isEnabled = false
            })

            ButtonGroup().addAll(importApplications, importParticipants)
        })

        add(RgfTournamentList(this))

        add(JLabel(tr("tournament.rgf.import.help")))

//        val preferences = GothaPreferences.instance()
// TODO       preferences.persistWindowState(this, Dimension(JFrGotha.BIG_FRAME_WIDTH, JFrGotha.BIG_FRAME_HEIGHT))
    }

    private fun loadTournament(id: Int) {
        fetchJob?.cancel()
        fetchJob = launch {
            val importMode = if (importApplications.isSelected)
                RgfTournament.ImportMode.APPLICATIONS
            else
                RgfTournament.ImportMode.PARTICIPANTS

            val tournamentRes = withContext(Dispatchers.IO) {
                rgfApiClient.fetchTournament(id)
            }

            if (tournamentRes is TournamentErrorResult) {
                ExceptionDialog("tournament.rgf.import.download_error", tournamentRes.exception)
                    .show(this@RgfTournamentImportDialog)
            } else if (tournamentRes is TournamentResult) {
                val (tournament, report) = withContext(Dispatchers.Default) {
                    tournamentRes.tournament.rgf2gotha(importMode)
                }

                val reportPane = JOptionPane()
                if (report.hadError) {
                    reportPane.messageType = JOptionPane.WARNING_MESSAGE
                    reportPane.message = StringBuilder().also { sb ->
                        sb.append(tr("tournament.rgf.import.report.with_errors", report.players, report.games))
                        if (report.playerDoubles.size > 0) {
                            sb.append("\n\n")
                                .append(tr("tournament.rgf.import.error.player_doubles", report.playerDoubles.joinToString(", ")))
                        }
                        sb.toString()
                    }
                } else {
                    reportPane.messageType = JOptionPane.INFORMATION_MESSAGE
                    reportPane.message = tr("tournament.rgf.import.report.no_errors", report.players, report.games)
                }
                reportPane.createDialog(this@RgfTournamentImportDialog, tr("tournament.rgf.import.report.window_title")).isVisible = true

                tournamentOpener.openTournament(tournament)
            }

            closeWindow()
        }
    }

    override fun onTournamentPicked(tournament: RgfTournament) {
        tournament.id?.let { loadTournament(it) }
    }
}

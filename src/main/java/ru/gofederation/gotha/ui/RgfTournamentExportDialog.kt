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

import info.vannier.gotha.TournamentInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.miginfocom.swing.MigLayout
import ru.gofederation.api.Client
import ru.gofederation.api.TournamentErrorResult
import ru.gofederation.api.TournamentResult
import ru.gofederation.api.gotha2rgf
import ru.gofederation.gotha.ui.component.addAll
import javax.swing.*

class RgfTournamentExportDialog(tournament: TournamentInterface) : Panel() {
    private val rgfApiClient = Client()
    private var exportJob: Job? = null

    private val finishTournament: JCheckBox

    init {
        val gps = tournament.tournamentParameterSet.generalParameterSet

        layout = MigLayout("insets dialog, flowy", "[fill, grow]", "[]unrel:push[]")

        add(JPanel(MigLayout("flowy")).apply {
            border = BorderFactory.createTitledBorder(tr("tournament.rgf.publish.options"))

            val exportUpdate = add(JRadioButton(tr("tournament.rgf.publish.mode_update")).apply {
                isEnabled = gps.hasRgfId()
                isSelected = gps.hasRgfId()
            }) as JRadioButton

            val exportNew = add(JRadioButton(tr("tournament.rgf.publish.mode_new")).apply {
                isEnabled = !gps.hasRgfId()
                isSelected = !gps.hasRgfId()
            }) as JRadioButton

            ButtonGroup().addAll(exportUpdate, exportNew)

            finishTournament = add(JCheckBox(tr("tournament.rgf.publish.finish"))) as JCheckBox
        })

        add(JButton(tr("btn.ok")).apply {
            addActionListener { exportTournament(tournament) }
        }, "flowx, split, tag ok")

        add(JButton(tr("btn.cancel")).apply {
            addActionListener { closeWindow() }
        }, "tag cancel")
    }

    private fun exportTournament(tournament: TournamentInterface) {
        val authentication = RgfAuthentication().getAuthenticationHeader(this) ?: return

        exportJob?.cancel()
        exportJob = launch {
            val res = withContext(Dispatchers.IO) {
                rgfApiClient.postTournament(gotha2rgf(tournament), authentication)
            }

            when (res) {
                is TournamentResult -> {
                    InfoDialog(tr("tournament.rgf.publish.success"))
                        .show(this@RgfTournamentExportDialog)

                    tournament.tournamentParameterSet.generalParameterSet.rgfId = res.tournament.id ?: 0
                    tournament.setChangeSinceLastSaveAsFalse()
                }
                is TournamentErrorResult ->
                    ExceptionDialog("tournament.rgf.publish.error", res.exception)
                        .show(this@RgfTournamentExportDialog)
            }

            closeWindow()
        }
    }
}

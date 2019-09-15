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

import info.vannier.gotha.*
import net.miginfocom.layout.CC
import net.miginfocom.swing.MigLayout
import ru.gofederation.gotha.model.TournamentType
import javax.swing.BorderFactory.createTitledBorder
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JPanel

class NewTournamentPanel(
    private val tournamentOpener: TournamentOpener
) : Panel() {
    private val tournamentOptions: TournamentDetailsPanel = TournamentDetailsPanel(Tournament())
    private val tournamentSystem: TournamentSystemPanel = TournamentSystemPanel()

    init {
        layout = MigLayout("insets dialog, wrap 2", "[fill, sg]u[fill, sg]", "[fill]u[fill]")

        add(JPanel().also { panel ->
            panel.layout = MigLayout("insets panel")
            panel.border = createTitledBorder(tr("tournament.details"))
            panel.add(tournamentOptions)
        })

        add(JPanel().also { panel ->
            panel.layout = MigLayout("insets panel")
            panel.border = createTitledBorder(tr("tournament.system"))
            panel.add(tournamentSystem)
        })

        add(JButton(tr("btn.ok")).also {
            it.addActionListener {
                tournamentOpener.openTournament(buildTournament())
                closeWindow()
            }
        }, CC().split(3).span(2).tag("ok"))

        add(JButton(tr("btn.cancel")).also {
            it.addActionListener {
                closeWindow()
            }
        }, CC().tag("cancel"))

        add(JButton(tr("btn.help")).also {
            it.icon = ImageIcon(javaClass.getResource(HELP_ICON))
            it.addActionListener {
                Gotha.displayGothaHelp("Create a new tournament")
            }
        }, CC().tag("help"))
    }

    fun buildTournament(): TournamentInterface {
        return Tournament().also { tournament ->
            tournament.tournamentParameterSet = TournamentParameterSet().also { tps ->
                tps.initBase(tournamentOptions.shortName.text, tournamentOptions.name.text,
                    tournamentOptions.location.text, tournamentOptions.director.text,
                    tournamentOptions.beginDate.date, tournamentOptions.endDate.date,
                    tournamentOptions.numberOfRounds, 1)

                when (tournamentSystem.system) {
                    TournamentType.MCMAHON -> tps.initForMM()
                    TournamentType.SWISS -> tps.initForSwiss()
                    TournamentType.SWISS_CAT -> tps.initForSwissCat()
                    else -> {}
                }
            }
            tournament.teamTournamentParameterSet = TeamTournamentParameterSet().also { ttps ->
                ttps.init()
            }
        }
    }
}

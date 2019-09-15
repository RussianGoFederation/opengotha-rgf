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

import com.toedter.calendar.JDateChooser
import info.vannier.gotha.GeneralParameterSet
import info.vannier.gotha.Gotha
import info.vannier.gotha.JFrTournamentOptions
import info.vannier.gotha.TournamentInterface
import net.miginfocom.swing.MigLayout
import ru.gofederation.gotha.util.addFocusLostListener
import java.rmi.RemoteException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.JLabel
import javax.swing.JSpinner
import javax.swing.JTextField
import javax.swing.SpinnerNumberModel

class TournamentDetailsPanel(
    val tournament: TournamentInterface,
    mode: Mode = Mode.NEW
) : Panel() {
    val name: JTextField
    val shortName: JTextField
    val location: JTextField
    val director: JTextField
    val beginDate: JDateChooser
    val endDate: JDateChooser
    private val nor: JSpinner

    var numberOfRounds: Int
        get() {
            return nor.value as Int
        }
        set(value) {
            nor.value = when {
                value < 1 -> 1
                value > Gotha.MAX_NUMBER_OF_ROUNDS -> Gotha.MAX_NUMBER_OF_ROUNDS
                else -> value
            }
        }

    init {
        layout = MigLayout("insets 0, wrap 2", null, "[][][]u[]u[][]u[]")

        add(JLabel(tr("tournament.name")))
        name = JTextField().also { name ->
            name.toolTipText = tr("tournament.name.tooltip")
            name.addFocusLostListener {
                tournament.tournamentParameterSet.generalParameterSet.name = name.text
                tournament.lastTournamentModificationTime = tournament.currentTournamentTime
            }
        }
        add(name, inputLargeCC)

        add(JLabel(tr("tournament.short_name")))
        shortName = JTextField().also { shortName ->
            shortName.toolTipText = tr("tournament.short_name.tooltip")
            if (mode == Mode.EDIT && Gotha.runningMode == Gotha.RUNNING_MODE_CLI)
                shortName.isEditable = false
            shortName.addFocusLostListener {
                tournament.tournamentParameterSet.generalParameterSet.shortName =
                    Gotha.eliminateForbiddenCharacters(shortName.text) // TODO: probably should do this in setter
                tournament.isHasBeenSavedOnce = false
                tournament.lastTournamentModificationTime = tournament.currentTournamentTime
            }
        }
        add(shortName, inputLargeCC)

        add(JLabel(tr("tournament.location")))
        location = JTextField().also { location ->
            location.addFocusLostListener {
                tournament.tournamentParameterSet.generalParameterSet.location = location.text
                tournament.lastTournamentModificationTime = tournament.currentTournamentTime
            }
        }
        add(location, inputLargeCC)

        add(JLabel(tr("tournament.director")))
        director = JTextField().also { director ->
            director.addFocusLostListener {
                tournament.tournamentParameterSet.generalParameterSet.director = director.text
                tournament.lastTournamentModificationTime = tournament.currentTournamentTime
            }
        }
        add(director, inputMediumCC)

        add(JLabel(tr("tournament.begin_date")))
        beginDate = JDateChooser().also { beginDate ->
            beginDate.locale = locale
            beginDate.date = Date()
            beginDate.addFocusLostListener {
                tournament.tournamentParameterSet.generalParameterSet.beginDate = beginDate.date
                tournament.lastTournamentModificationTime = tournament.currentTournamentTime
            }
        }
        add(beginDate)

        add(JLabel(tr("tournament.end_date")))
        endDate = JDateChooser().also { endDate ->
            endDate.locale = locale
            endDate.date = Date()
            endDate.addFocusLostListener {
                tournament.tournamentParameterSet.generalParameterSet.endDate = endDate.date
                tournament.lastTournamentModificationTime = tournament.currentTournamentTime
            }
        }
        add(endDate)

        add(JLabel(tr("tournament.number_of_rounds")))

        nor = JSpinner().also {
            it.model = SpinnerNumberModel(5, 1, Gotha.MAX_NUMBER_OF_ROUNDS, 1)
            it.addChangeListener {
                // TODO: move this check to Tournament class

                val oldNbRounds = tournament.tournamentParameterSet.generalParameterSet.numberOfRounds
                var newNbRounds = numberOfRounds
                for (r in oldNbRounds - 1 downTo newNbRounds) {
                    try {
                        if (tournament.gamesList(r).size > 0 || tournament.getByePlayer(r) != null) {
                            newNbRounds = r + 1
                            break
                        }
                    } catch (ex: RemoteException) {
                        Logger.getLogger(JFrTournamentOptions::class.java.name).log(Level.SEVERE, null, ex)
                    }

                }
                if (newNbRounds == oldNbRounds) {
                    numberOfRounds = newNbRounds
                    return@addChangeListener
                }

                tournament.tournamentParameterSet.generalParameterSet.numberOfRounds = newNbRounds
                tournament.lastTournamentModificationTime = tournament.currentTournamentTime
            }
        }
        add(nor, inputXSmallCC)
    }

    fun updateForm(gps: GeneralParameterSet) {
        name.text = gps.name
        shortName.text = gps.shortName
        location.text = gps.location
        director.text = gps.director
        beginDate.date = gps.beginDate
        endDate.date = gps.endDate
    }

    fun getGeneralParameterSet(): GeneralParameterSet {
        return GeneralParameterSet().also {
            it.name = name.text
            it.shortName = shortName.text
            it.location = location.text
            it.director = director.text
            it.beginDate = beginDate.date
            it.endDate = endDate.date
        }
    }

    enum class Mode {
        EDIT, NEW
    }
}

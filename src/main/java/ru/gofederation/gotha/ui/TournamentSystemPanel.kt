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

import net.miginfocom.swing.MigLayout
import ru.gofederation.gotha.model.TournamentType
import javax.swing.ButtonGroup
import javax.swing.JLabel
import javax.swing.JRadioButton

class TournamentSystemPanel : Panel () {
    var system: TournamentType
        get() {
            return when {
                mcmahon.isSelected -> TournamentType.MCMAHON
                swiss.isSelected -> TournamentType.SWISS
                swissCat.isSelected -> TournamentType.SWISS_CAT
                else -> TournamentType.UNDEFINED
            }
        }
        set(value) {
            when (value) {
                TournamentType.MCMAHON -> mcmahon.isSelected = true
                TournamentType.SWISS -> swiss.isSelected = true
                TournamentType.SWISS_CAT -> swissCat.isSelected = true
                else -> {}
            }
        }

    private val mcmahon: JRadioButton
    private val swiss: JRadioButton
    private val swissCat: JRadioButton

    init {
        layout = MigLayout("insets 0, flowy", null, "[]u[]u[][]")

        val systemGroup = ButtonGroup()

        mcmahon = JRadioButton(tr("tournament.system.mcmahon")).also {
            systemGroup.add(it)
            it.toolTipText = tr("tournament.system.mcmahon_tooltip")
        }
        add(mcmahon)

        swiss = JRadioButton(tr("tournament.system.swiss")).also {
            systemGroup.add(it)
            it.toolTipText = tr("tournament.system.swiss_tooltip")
        }
        add(swiss)

        swissCat = JRadioButton(tr("tournament.system.swiss_cat")).also {
            systemGroup.add(it)
            it.toolTipText = tr("tournament.system.swiss_cat_tooltip")
        }
        add(swissCat)

        add(JLabel(tr("tournament.system.swiss_cat_recommended")), "gapleft indent")

        system = TournamentType.MCMAHON
    }
}

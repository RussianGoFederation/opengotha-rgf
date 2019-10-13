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
import ru.gofederation.gotha.model.RatingOrigin
import ru.gofederation.gotha.ui.component.ComboBoxDelegate
import ru.gofederation.gotha.ui.component.IntTextFieldDelegate
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JTextField

class RatingPanel : Panel() {
    private val label = JLabel(tr("player.enter_new_rating"))
    private val ratingInput = JTextField()
    private val originInput = JComboBox<RatingOrigin>().also {
        it.model = DefaultComboBoxModel<RatingOrigin>().also { model ->
            model.addElement(RatingOrigin.AGA)
            model.addElement(RatingOrigin.EGF)
            model.addElement(RatingOrigin.RGF)
            model.addElement(RatingOrigin.FFG)
        }
        it.selectedItem = RatingOrigin.RGF
        it.isEnabled = false
        it.addActionListener { originChanged() }
    }

    var rating by IntTextFieldDelegate(ratingInput)
    val origin by ComboBoxDelegate(originInput)

    init {
        layout = MigLayout("insets 0", "[][]u[][]")

        add(label, "spanx 4, wrap")

        add(JLabel(tr("player.rating")))
        add(ratingInput, inputMediumCC)
        add(JLabel(tr("player.rating_origin")))
        add(originInput, "wmin $INPUT_M, wrap")
    }

    init {
        originChanged()
    }

    private fun originChanged() {
        val newOrigin = originInput.selectedItem as RatingOrigin
        label.text = tr("player.enter_new_rating", newOrigin.minRating, newOrigin.maxRating)
    }
}

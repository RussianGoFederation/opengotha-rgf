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

package ru.gofederation.gotha.ui.component

import ru.gofederation.gotha.model.HalfGame
import java.awt.Color
import java.awt.Component
import java.awt.Font
import java.awt.Graphics
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

class StandingsTableCellRenderer(
    /** if true use full (123+/w4) game format, short (123+) otherwise */
    var roundCellFormatLong: Boolean
) : JLabel(), TableCellRenderer {
    private val markerUpColor = Color.RED
    private val markerDownColor = Color.BLUE

    private var marker = 0

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val markerSize = height / 3

        if (marker and MARKER_UP > 0) {
            g.color = markerUpColor
            g.fillPolygon(
                intArrayOf(width - markerSize, width, width),
                intArrayOf(0, 0, markerSize),
                3)
        }

        if (marker and MARKER_DOWN > 0) {
            g.color = markerDownColor
            g.fillPolygon(
                intArrayOf(width - markerSize-1, width, width),
                intArrayOf(height, height, height - markerSize - 1),
                3)
        }
    }

    override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
        marker = 0
        when (value) {
            is HalfGame -> {
                text = value.toPaddedString(roundCellFormatLong)
                if (value.upDownStatus == HalfGame.UpDownStatus.UP) marker = marker or MARKER_UP
                else if (value.upDownStatus == HalfGame.UpDownStatus.DOWN) marker = marker or MARKER_DOWN
            }
            else ->
                text = value.toString()
        }

        font = if (isSelected) font.deriveFont(Font.BOLD)
        else font.deriveFont(Font.PLAIN)

        return this
    }

    companion object {
        private const val MARKER_UP = 0b00000001
        private const val MARKER_DOWN = 0b00000010
    }
}

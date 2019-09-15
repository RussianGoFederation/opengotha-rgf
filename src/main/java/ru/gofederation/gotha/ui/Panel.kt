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

import net.miginfocom.layout.CC
import ru.gofederation.gotha.util.GothaLocale
import java.awt.Window
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.SwingUtilities

abstract class Panel(
    private val gothaLocale: GothaLocale = GothaLocale.getCurrentLocale()
) : JPanel() {

    init {
        locale = gothaLocale.locale
    }

    protected fun tr(key: String): String {
        return gothaLocale.getString(key)
    }

    protected fun closeWindow() {
        SwingUtilities.getWindowAncestor(this)?.dispose()
    }

    companion object {
        const val HELP_ICON = "/info/vannier/gotha/gothalogo16.jpg"

        private const val INPUT_L: String = "240lp"
        private const val INPUT_M: String = "180lp"
        private const val INPUT_XS: String = "42lp"

        val inputLargeCC: CC = CC().minWidth(INPUT_L)
        val inputMediumCC: CC = CC().minWidth(INPUT_M)
        val inputXSmallCC: CC = CC().minWidth(INPUT_XS)
    }
}

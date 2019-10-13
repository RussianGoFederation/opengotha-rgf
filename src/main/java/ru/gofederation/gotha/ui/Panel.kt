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

import info.vannier.gotha.Gotha
import net.miginfocom.layout.CC
import ru.gofederation.gotha.util.GothaLocale
import java.awt.Window
import java.util.logging.Logger
import java.util.prefs.Preferences
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.SwingUtilities

abstract class Panel(
    private val gothaLocale: GothaLocale = GothaLocale.getCurrentLocale()
) : JPanel() {
    open val preferencesNode: String = this.javaClass.name
    val preferences: Preferences by lazy(LazyThreadSafetyMode.NONE) {
        Preferences.userRoot().node(Gotha.strPreferences).node(preferencesNode)
    }

    init {
        locale = gothaLocale.locale
    }

    protected fun tr(key: String): String {
        return gothaLocale.getString(key)
    }

    protected fun tr(key: String, vararg args: Any): String {
        return gothaLocale.format(key, *args)
    }

    protected fun closeWindow() {
        SwingUtilities.getWindowAncestor(this)?.dispose()
    }

    companion object {
        val LOG = Logger.getLogger(this::class.java.simpleName)

        const val HELP_ICON = "/info/vannier/gotha/gothalogo16.jpg"

        const val INPUT_L: String = "240lp"
        const val INPUT_M: String = "180lp"
        const val INPUT_S: String = "58lp"
        private const val INPUT_XS: String = "42lp"

        val inputLargeCC: CC = CC().minWidth(INPUT_L)
        val inputMediumCC: CC = CC().minWidth(INPUT_M)
        val inputSmallCC: CC = CC().minWidth(INPUT_S)
        val inputXSmallCC: CC = CC().minWidth(INPUT_XS)
    }
}

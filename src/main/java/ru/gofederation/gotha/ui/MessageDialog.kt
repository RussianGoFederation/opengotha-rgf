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

import kotlinx.coroutines.channels.Channel
import net.miginfocom.swing.MigLayout
import ru.gofederation.gotha.ui.component.watchProgress
import ru.gofederation.gotha.util.GothaLocale
import ru.gofederation.gotha.util.I18N
import java.awt.Component
import javax.swing.*

sealed class MessageDialog(
    val type: Type,
    gothaLocale: GothaLocale = GothaLocale.getCurrentLocale()
) : I18N by gothaLocale {
    abstract fun message(): Message

    fun show(parent: Component, title_: String? = null) {
        val title = title_ ?: this.tr(type.titleKey)
        when (val message = message()) {
            is StringMessage ->
                JOptionPane(message.toString(), type.type)
                    .createDialog(parent, title).also {
                        it.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
                    }
                    .isVisible = true
            is ComponentMessage ->
                JOptionPane(message, type.type)
                    .createDialog(parent, title).also {
                        it.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
                    }
                    .isVisible = true
        }
    }

    enum class Type(val titleKey: String, val type: Int) {
        ERROR("alert.error", JOptionPane.ERROR_MESSAGE),
        INFORMATION("alert.message", JOptionPane.INFORMATION_MESSAGE)
    }
}

class InfoDialog(private val message: String) : MessageDialog(Type.INFORMATION) {
    override fun message(): StringMessage = StringMessage(message)
}

class ExceptionDialog(private val messageKey: String, val exception: Exception) : MessageDialog(Type.ERROR) {
    override fun message(): StringMessage = StringMessage(tr(messageKey, exception))
}

sealed class Message
class StringMessage(private val message: String) : Message() {
    override fun toString() = message
}
class ComponentMessage(val component: Component) : Message()

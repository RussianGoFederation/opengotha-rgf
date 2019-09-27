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

package ru.gofederation.gotha.util

import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document
import javax.swing.text.JTextComponent

fun JTextComponent.addChangeListener(listener: (String) -> Unit) {
    var documentListener = object : DocumentListener {
        private var lastChange = 0
        private var lastNotifiedChange = 0

        override fun changedUpdate(evt: DocumentEvent?) {
            onChange()
        }

        override fun insertUpdate(evt: DocumentEvent?) {
            onChange()
        }

        override fun removeUpdate(evt: DocumentEvent?) {
            onChange()
        }

        private fun onChange() {
            lastChange ++
            SwingUtilities.invokeLater {
                if (lastNotifiedChange != lastChange) {
                    lastNotifiedChange = lastChange
                    listener(text.toString())
                }
            }
        }
    }

    this.addPropertyChangeListener("document") {
        val d1 = it.oldValue as Document?
        val d2 = it.newValue as Document?
        d1?.removeDocumentListener(documentListener)
        d2?.addDocumentListener(documentListener)
        documentListener.changedUpdate(null)
    }

    this.document?.addDocumentListener(documentListener)
}

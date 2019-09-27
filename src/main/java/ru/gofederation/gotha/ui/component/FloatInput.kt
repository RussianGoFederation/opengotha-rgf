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

import java.text.NumberFormat
import javax.swing.text.JTextComponent
import kotlin.reflect.KProperty

class FloatInput(
    val input: JTextComponent,
    val format: NumberFormat
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Float {
        return format.parse(input.text).toFloat()
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
        input.text = format.format(value)
    }
}

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

import kotlinx.coroutines.channels.Channel
import javax.swing.JProgressBar

suspend fun JProgressBar.watchProgress(progress: Channel<Pair<Long, Long>>) {
    isIndeterminate = true

    for (step in progress) {
        if (step.second > 0) {
            isIndeterminate = false
            maximum = step.second.toInt()
            value = step.first.toInt()
        } else {
            isIndeterminate = true
        }
    }

    isIndeterminate = false
    value = 100
    maximum = 100
}

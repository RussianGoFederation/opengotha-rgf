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

package ru.gofederation.gotha.presenter

import javax.swing.table.AbstractTableModel

abstract class GothaTableModel<T>(
    private val items: List<T>,
    private val columns: List<TableColumn<T>>
) : AbstractTableModel() {
    override fun getRowCount(): Int {
        return items.size
    }

    override fun getColumnName(col: Int): String {
        return columns[col].header
    }

    override fun getColumnCount(): Int {
        return columns.size
    }

    override fun getValueAt(row: Int, col: Int): Any {
        return columns[col].cellValue(items[row])
    }
}

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

package ru.gofederation.gotha.printing;

import javax.swing.table.TableModel;

public abstract class TableModelPrinter extends TablePrinter {
    private final TableModel model;

    TableModelPrinter(TableModel model) {
        this.model = model;
    }

    @Override
    int getColumnCount() {
        return model.getColumnCount();
    }

    @Override
    int getRowCount() {
        return model.getRowCount();
    }

    @Override
    String getHeader(int column) {
        return model.getColumnName(column);
    }

    @Override
    String getCell(int row, int column) {
        return model.getValueAt(row, column).toString();
    }
}

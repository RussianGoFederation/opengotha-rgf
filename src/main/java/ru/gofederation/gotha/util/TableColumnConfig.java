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

package ru.gofederation.gotha.util;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public interface TableColumnConfig {
    default int minWidth() {
        return 10;
    }

    default int prefWidth() {
        return 80;
    }

    default int horizontalAlignment() {
        return SwingConstants.LEFT;
    }

    default TableCellRenderer tableCellRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(horizontalAlignment());
        return renderer;
    }

    static void configure(TableColumn column, TableColumnConfig config) {
        column.setMinWidth(config.minWidth());
        column.setPreferredWidth(config.prefWidth());
        column.setCellRenderer(config.tableCellRenderer());
    }
}

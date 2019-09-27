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

import java.awt.Font;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.text.DateFormat;
import java.util.Date;

import info.vannier.gotha.Gotha;

public abstract class TablePrinter extends Printer {
    private boolean didInit = false;
    private int[] columnWidths;
    private int[] columnPositions;
    private int page;
    private int totalPages;
    private String timestamp;

    @Override
    protected void init(Graphics graphics, PageFormat pageFormat) {
        super.init(graphics, pageFormat);
        if (!didInit) {
            this.columnWidths = new int[getColumnCount()];
            this.columnPositions = new int[getColumnCount()];
            for (int i = 0; i < getColumnCount(); i++) {
                this.columnWidths[i] = measureMaxColumnWidth(i);
                if (i > 0) {
                    columnPositions[i] = columnPositions[i - 1] + columnWidths[i - 1] + fontMetrics.stringWidth("  ");
                } else {
                    columnPositions[i] = 0;
                }
            }
            timestamp = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale.getLocale()).format(new Date());
            didInit = true;
        }
    }

    @Override
    public final int print(Graphics graphics, PageFormat pf, int page) {
        this.init(graphics, pf);
        this.page = page;

        int rowCount = getRowCount();
        int workHeight = (int) (pf.getImageableHeight() - headerHeight() - footerHeight());
        int rowsPerPage = (workHeight - fontMetrics.getHeight() - 1) / fontMetrics.getHeight();
        this.totalPages = rowCount / rowsPerPage;

        if (page > totalPages) {
            return NO_SUCH_PAGE;
        }

        int startingRow = page * rowsPerPage;
        int printColumnCount = getColumnCount();
        while (columnPositions[printColumnCount - 1] > pageFormat.getImageableWidth()) {
            printColumnCount --;
        }
        for (int column = 0; column < printColumnCount; column ++) {
            graphics.drawString(getHeader(column), columnPositions[column], fontMetrics.getHeight() + headerHeight());
            for (int row = startingRow; row < startingRow + rowsPerPage && row < rowCount; row++) {
                int y = fontMetrics.getHeight() * (row - startingRow + 2) + headerHeight();
                getCellPrinter(column).printCell(graphics, columnPositions[column], y, getCell(row, column));
            }
        }

        printHeader();
        printFooter();

        return PAGE_EXISTS;
    }

    @Override
    public void setFont(Font font) {
        didInit = false;
        super.setFont(font);
    }

    private int measureMaxColumnWidth(int column) {
        int width = fontMetrics.stringWidth(getHeader(column));
        for (int i = 0; i < getRowCount(); i++) {
            width = Math.max(width, fontMetrics.stringWidth(getCell(i, column)));
        }
        return width;
    }

    @Override
    protected int footerHeight() {
        return (int) (fontMetrics.getHeight() * 1.5);
    }

    @Override
    protected void printFooter() {
        int y = (int) (pageFormat.getImageableHeight() - fontMetrics.getMaxDescent());
        graphics.drawString(Gotha.getGothaReleaseVersion(), 0, y);
        String pageNumber = locale.format("printing.page_number", page + 1, totalPages + 1);
        int w = fontMetrics.stringWidth(pageNumber);
        graphics.drawString(pageNumber, (int) (pageFormat.getImageableWidth() / 2 - w / 2), y);
        w = fontMetrics.stringWidth(timestamp);
        graphics.drawString(timestamp, (int) (pageFormat.getImageableWidth() - w), y);
    }

    abstract int getColumnCount();
    abstract int getRowCount();
    abstract String getHeader(int column);
    abstract String getCell(int row, int column);

    protected CellPrinter getCellPrinter(int column) {
        return (g, x, y, s) -> g.drawString(s, x, y);
    }

    interface CellPrinter {
        void printCell(Graphics g, int x, int y, String s);
    }
}

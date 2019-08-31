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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSize;

import ru.gofederation.gotha.util.GothaLocale;

public abstract class Printer implements Printable {
    private static final String MEDIA_SIZE = "media_size";

    protected Graphics graphics;
    protected PageFormat pageFormat;
    protected Font font;
    protected FontMetrics fontMetrics;
    protected GothaLocale locale;

    protected void init(Graphics graphics, PageFormat pageFormat) {
        this.graphics = graphics;
        this.pageFormat = pageFormat;
        this.font =  graphics.getFont().deriveFont(8f);
        this.fontMetrics = graphics.getFontMetrics(this.font);
        this.locale = GothaLocale.getCurrentLocale();
        graphics.setFont(this.font);

        graphics.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());
    }

    static PrintRequestAttributeSet getPrintRequestAttributeSet() {
        PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
        attributes.add(MediaSize.ISO.A4.getMediaSizeName());
        return attributes;
    }

    abstract protected int headerHeight();
    abstract protected int footerHeight();
    abstract protected void printHeader();
    abstract protected void printFooter();

    protected void drawString(String s, int x, int y) {
        graphics.drawString(s, x, y);
    }
}

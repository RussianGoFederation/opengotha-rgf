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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;

import info.vannier.gotha.Gotha;
import ru.gofederation.gotha.util.GothaLocale;

public abstract class Printer implements Printable {
    private static final String MEDIA_PRINTABLE_AREA = "media_printable_area";
    private static final String PRINT_SERVICE = "print_service";

    private final Logger logger = Logger.getGlobal();

    protected Graphics graphics;
    protected PageFormat pageFormat;
    protected Font font;
    protected FontMetrics fontMetrics;
    protected GothaLocale locale;

    protected void init(Graphics graphics, PageFormat pageFormat) {
        this.graphics = graphics;
        this.pageFormat = pageFormat;
        if (null == this.font) this.font = graphics.getFont().deriveFont(10f);
        graphics.setFont(this.font);
        this.fontMetrics = graphics.getFontMetrics(this.font);
        this.locale = GothaLocale.getCurrentLocale();
        graphics.setColor(Color.BLACK);

        graphics.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());
    }

    public void setFont(Font font) {
        this.font = font;
    }

    private Preferences getPreferences() {
        return Preferences.userRoot().node(Gotha.strPreferences).node("printing");
    }

    private PrintRequestAttributeSet getPrintRequestAttributeSet() {
        Preferences preferences = getPreferences();
        PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();

        attributes.add(MediaSize.ISO.A4.getMediaSizeName());

        String areaStr = preferences.get(MEDIA_PRINTABLE_AREA, null);
        if (null != areaStr) {
            String[] areaArr = areaStr.split("_");
            if (areaArr.length == 4) {
                attributes.add(new MediaPrintableArea(
                    Integer.parseInt(areaArr[0]),
                    Integer.parseInt(areaArr[1]),
                    Integer.parseInt(areaArr[2]),
                    Integer.parseInt(areaArr[3]),
                    1
                ));
            }
        }

        return attributes;
    }

    private void savePrintRequestAttributeSet(PrintRequestAttributeSet attributes, PrinterJob printerJob) {
        Preferences preferences = getPreferences();

        preferences.put(PRINT_SERVICE, printerJob.getPrintService().getName());

        if (attributes.containsKey(MediaPrintableArea.class)) {
            MediaPrintableArea area = (MediaPrintableArea) attributes.get(MediaPrintableArea.class);
            String areaStr = "" + Math.round(area.getX(1)) + "_"
                                + Math.round(area.getY(1)) + "_"
                                + Math.round(area.getWidth(1)) + "_"
                                + Math.round(area.getHeight(1));
            preferences.put(MEDIA_PRINTABLE_AREA, areaStr);
        }
    }

    protected void print() {
        PrinterJob printerJob = PrinterJob.getPrinterJob();

        try {
            PrintService printService = findPrintService(getPreferences().get(PRINT_SERVICE, ""));
            if (null != printService) printerJob.setPrintService(printService);
        } catch (PrinterException e) {
            logger.log(Level.WARNING, "Failed to set PrintService", e);
        }

        PrintRequestAttributeSet attributes = getPrintRequestAttributeSet();
        if (printerJob.printDialog(attributes)) {
            savePrintRequestAttributeSet(attributes, printerJob);
            try {
                printerJob.setPrintable(this);
                printerJob.print(attributes);
            } catch (PrinterException e) {
                e.printStackTrace();
            }
        }

    }

    private PrintService findPrintService(String name) {
        PrintService[] services = PrinterJob.lookupPrintServices();
        for (PrintService service : services) {
            if (service.getName().equals(name)) return service;
        }
        return null;
    }

    abstract protected int headerHeight();
    abstract protected int footerHeight();
    abstract protected void printHeader();
    abstract protected void printFooter();
}

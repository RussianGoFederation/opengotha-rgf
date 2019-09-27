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

package ru.gofederation.gotha.ui;

import net.miginfocom.layout.LC;
import net.miginfocom.layout.UnitValue;
import net.miginfocom.swing.MigLayout;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;

/**
 * Based on <a href="https://github.com/barteksc/java-printable-view/blob/master/PrintPreview.java">https://github.com/barteksc/java-printable-view/blob/master/PrintPreview.java</a>
 */
public class PrintPreview extends JScrollPane {

    private static final long serialVersionUID = 1L;
    private Pageable pageable = null;
    private double scale = 1.0;
    private final List<Page> pages = new ArrayList<>();
    private JPanel mMainPanel = new JPanel(new MigLayout(new LC()));

    private JPanel mControls = null;
    private JSlider mZoomSlider = null;

    public PrintPreview() {
        setPreferredSize(new Dimension(200, 200));
    }

    public PrintPreview(Pageable pg) {
        this();

        setPageable(pg);
    }

    public PrintPreview(final Printable pr, final PageFormat p) {
        this();

        setPrintable(pr, p);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                onResize();
            }
        });
    }

    private void onResize() {
        if (pages.size() == 0) return;

        int pageWidth = pages.get(0).getMinimumSize().width;
        if (pageWidth == 0) return;

        MigLayout l = (MigLayout) mMainPanel.getLayout();
        LC lc = (LC) l.getLayoutConstraints();
        int wrapAfter = Math.max(1, (this.getWidth() - this.verticalScrollBar.getWidth()) / pageWidth);
        lc.setWrapAfter(wrapAfter);
        l.setLayoutConstraints(lc);
        mMainPanel.invalidate();
        validate();
    }

    public void setPrintable(Printable printable, PageFormat pageFormat) {
        setPageable(new PageablePrintable(printable, pageFormat));
    }

    public void setPageable(Pageable pageable) {
        this.pageable = pageable;
        updatePreview();
    }

    private void updatePreview() {
        int n = pageable.getNumberOfPages();
        pages.clear();
        mMainPanel.removeAll();


        for (int i = 0; i < n; i++) {
            Page page = new Page(pageable, i);
            pages.add(page);
            mMainPanel.add(page);
        }
        setViewportView(mMainPanel);
    }

    private void setupControls(){

        mControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        mZoomSlider = new JSlider();

        mZoomSlider.addChangeListener(val -> {
            double v = (double)mZoomSlider.getValue() / 10.0;
            zoom(v);
        });

        mZoomSlider.setPaintTicks(true);
        mZoomSlider.setPaintLabels(true);
        mZoomSlider.setMinimum(0);
        mZoomSlider.setMaximum(70);
        mZoomSlider.setValue(10);
        mZoomSlider.setSnapToTicks(true);
        mZoomSlider.setMinorTickSpacing(5);
        mZoomSlider.setMajorTickSpacing(10);

        mControls.add(mZoomSlider);
    }

    /**
     * Method lazy initializes JPanel with controls (if not initialized yet) and returns it
     * @return panel with controls
     */
    public JPanel getControls(){
        if(mControls == null)
            setupControls();

        return mControls;
    }

    /**
     * Method checks if next page exists before processing
     *
     * @return number of pages
     */
    public int pages() {
        return pages.size();
    }

    /**
     * Prints whole document
     */
    public void print(){
        try {
            PrinterJob pj = PrinterJob.getPrinterJob();
            pj.defaultPage(pageable.getPageFormat(0));
            pj.setPageable(pageable);
            if (pj.printDialog())
                pj.print();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.toString(),
                "Error in Printing", 1);
        }
    }

    /**
     *
     *
     * @param zoom double value greater than 0
     */
    public void zoom(double zoom) {
        double temp = zoom;
        if (temp == scale)
            return;
        if (temp == 0)
            temp = 0.01;


        scale = temp;
        for (Page p : pages) {
            p.refreshScale();
        }

        onResize();
    }

    public void zoomFitWidth() {
        zoom((getViewport().getWidth() - verticalScrollBar.getWidth()) / pages.get(0).mPageFormat.getWidth());
    }

    class Page extends JComponent {
        private final int mPageNum;
        private final PageFormat mPageFormat;
        private final Dimension size;

        @Override
        public void paint(Graphics gg) {
            Graphics2D g = (Graphics2D) gg;
            Color c = g.getColor();
            g.setColor(Color.white);
            g.scale(scale, scale);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.fillRect(0, 0, (int) mPageFormat.getWidth(), (int) mPageFormat.getHeight());
            g.setColor(c);
            Stroke s = g.getStroke();
            Shape clip = g.getClip();
            try {
                g.clipRect((int) mPageFormat.getImageableX(),
                    (int) mPageFormat.getImageableY(),
                    (int) mPageFormat.getImageableWidth(),
                    (int) mPageFormat.getImageableHeight());
                pageable.getPrintable(mPageNum).print(g, mPageFormat, mPageNum);

                g.setColor(new Color(128, 128, 128, 128));
                g.setStroke(new BasicStroke(2.0f,
                    BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER,
                    10.0f,
                    new float[] {5.0f, 5.0f},
                    0.0f));
                g.translate(0, 0);
                g.clipRect(0, 0, (int) mPageFormat.getWidth(), (int) mPageFormat.getHeight());
                g.drawRect(0, 0, (int) mPageFormat.getImageableWidth() - 1, (int) mPageFormat.getImageableHeight() - 1);
            } catch (Exception ex) {
            } finally {
                g.setColor(c);
                g.setStroke(s);
                g.setClip(clip);
            }
        }

        Page(Pageable pageable, int pageNumber) {
            mPageNum = pageNumber;
            mPageFormat = pageable.getPageFormat(mPageNum);

            PageFormat pf = pageable.getPageFormat(0);
            Dimension size = new Dimension((int) pf.getPaper().getWidth(), (int) pf
                .getPaper().getHeight());
            if (pf.getOrientation() != PageFormat.PORTRAIT)
                size = new Dimension(size.height, size.width);

            this.size = size;
            refreshScale();
        }

        public void refreshScale() {
            Dimension newSize = new Dimension((int) (size.width * scale), (int) (size.height * scale));
            setMinimumSize(newSize);
            invalidate();
            repaint();
        }
    }

    private static class PageablePrintable implements Pageable {
        private final Printable printable;
        private final PageFormat pageFormat;

        PageablePrintable(Printable printable, PageFormat pageFormat) {
            this.printable = printable;
            this.pageFormat = pageFormat;
        }

        @Override
        public int getNumberOfPages() {
            Graphics g = new java.awt.image.BufferedImage(2, 2,
                java.awt.image.BufferedImage.TYPE_INT_RGB)
                .getGraphics();
            int n = 0;
            try {
                while (printable.print(g, pageFormat, n) == Printable.PAGE_EXISTS)
                    n++;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return n;
        }

        @Override
        public PageFormat getPageFormat(int x) {
            return pageFormat;
        }

        @Override
        public Printable getPrintable(int x) {
            return printable;
        }
    }
}

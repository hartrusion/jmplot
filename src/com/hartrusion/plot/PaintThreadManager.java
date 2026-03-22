/*
 * The MIT License
 *
 * Copyright 2026 Viktor Alexander Hartung.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.hartrusion.plot;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Generates and manages a separate thread that generates the image that is to
 * be drawn which represents the plot you see on screen. This is used for live
 * plot views where a line plot is continuously updated. That update would be a
 * massive performance drop when handling large lines as the whole Event
 * Dispatch Thread would be busy re-calculating pixel positions all the time.
 * This class pre-renders the image used in the figure in a separate thread, the
 * image also can be re-used in case of repaint() is requested.
 *
 * @author Viktor Alexander Hartung
 */
public class PaintThreadManager {

    private final FigureJPane figure;
    private volatile BufferedImage plotImage = null;
    private volatile boolean dirty = true;
    private volatile boolean running = false;
    private volatile int targetIntervalMs = 200;
    private int lastWidth = -1;
    private int lastHeight = -1;
    private Thread thread;

    PaintThreadManager(FigureJPane owner) {
        this.figure = owner;

        // Start thread on construction of this object
        running = true;
        dirty = true;
        thread = new Thread(this::renderLoop,
                "jmplot-renderer-"
                + Integer.toHexString(
                        System.identityHashCode(figure)));
        thread.setDaemon(true);
        thread.start();
    }

    BufferedImage getPlotImage() {
        return plotImage;
    }

    public void markDirty() {
        dirty = true;
        Thread t = thread;
        if (t != null) {
            t.interrupt();
        }
    }

    public void setTargetIntervalMs(int ms) {
        if (ms < 1) {
            throw new IllegalArgumentException("Interval must be >= 1");
        }
        targetIntervalMs = ms;
    }

    public void stop() {
        running = false;
        Thread t = thread;
        if (t != null) {
            t.interrupt();
            try {
                t.join(targetIntervalMs * 2L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        thread = null;
    }

    /**
     * The render loop. Runs on the dedicated render thread. This is a private
     * method, not an overridden Runnable.run().
     */
    private void renderLoop() {
        while (running) {
            try {
                int w = figure.getWidth();
                int h = figure.getHeight();
                boolean resized
                        = (w != lastWidth || h != lastHeight);

                if ((dirty || resized) && w > 0 && h > 0) {
                    renderFrame(w, h);
                    lastWidth = w;
                    lastHeight = h;
                    dirty = false;
                    figure.repaint();
                }
                Thread.sleep(targetIntervalMs);
            } catch (InterruptedException e) {
                // markDirty() woke us up – loop back
            } catch (Exception e) {
                System.err.println(
                        "PlotRenderer: " + e.getMessage());
            }
        }
    }

    /**
     * Renders one complete frame into a new BufferedImage. No locking is
     * performed – see class Javadoc for the rationale.
     */
    private void renderFrame(int w, int h) {
        BufferedImage img = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (figure.isOpaque()) {
            g2.setColor(figure.getBackground());
            g2.fillRect(0, 0, w, h);
        }

        float pw = (float) w - 1;
        float ph = (float) h - 1;

        figure.paintFigureContent(g2, pw, ph);

        g2.dispose();
        plotImage = img;  // volatile write → sofort sichtbar für EDT
    }
}

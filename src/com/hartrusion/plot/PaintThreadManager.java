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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

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

    /**
     * The pre-rendered plot image. Written by the render thread, read by the
     * EDT. Volatile ensures visibility without locking.
     */
    private volatile BufferedImage plotImage = null;

    /**
     * Guards against submitting multiple render tasks concurrently.
     * true = a render task is currently queued or executing.
     */
    private final AtomicBoolean rendering = new AtomicBoolean(false);

    /**
     * Single-thread executor – one thread is kept alive and ready in the pool.
     * When a render task is submitted it executes immediately (no thread
     * creation delay). When idle, the thread simply waits (no CPU usage,
     * no polling). Uses a daemon thread so it won't prevent JVM shutdown.
     */
    private final ExecutorService executor;

    /**
     * Dimensions of the last rendered image, used to detect resize.
     */
    private volatile int lastWidth = -1;
    private volatile int lastHeight = -1;

    PaintThreadManager(FigureJPane owner) {
        this.figure = owner;
        // A single-thread executor with a daemon thread factory.
        // The thread sits idle when no task is submitted (zero CPU).
        // When a task is submitted, it starts immediately.
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r,
                    "jmplot-renderer-"
                    + Integer.toHexString(
                            System.identityHashCode(figure)));
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Returns the last rendered image, or null if none exists yet.
     */
    BufferedImage getPlotImage() {
        return plotImage;
    }

    /**
     * Submits a render task if none is currently running. Called from
     * paintComponent on the EDT when isDrawingDeprecated() is true or
     * no plotImage exists. Returns immediately – the EDT is not blocked.
     * <p>
     * If a render task is already in progress, this call is a no-op
     * (the running task will produce a fresh image anyway).
     */
    public void requestRender() {
        if (rendering.compareAndSet(false, true)) {
            executor.submit(this::renderTask);
        }
    }

    /**
     * The actual render work. Runs on the executor thread (off-EDT).
     * Builds a new BufferedImage, paints the figure content into it,
     * then triggers repaint() so the EDT picks up the new image.
     */
    private void renderTask() {
        try {
            int w = figure.getWidth();
            int h = figure.getHeight();

            if (w <= 0 || h <= 0) {
                return; // component not yet laid out
            }

            renderFrame(w, h);
            lastWidth = w;
            lastHeight = h;

            // Trigger a repaint on the EDT so the new image gets displayed.
            // This is a lightweight call – it just schedules a paint event.
            figure.repaint();
        } catch (Exception e) {
            System.err.println("PlotRenderer: " + e.getMessage());
        } finally {
            // Allow the next render request to be submitted
            rendering.set(false);
        }
    }

    /**
     * Renders one complete frame into a new BufferedImage.
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
        plotImage = img;  // volatile write → immediately visible to EDT
    }

    /**
     * Checks if the current image dimensions match the figure size.
     * Used by FigureJPane to detect resize as a deprecation reason.
     */
    boolean isSizeMatching() {
        return lastWidth == figure.getWidth()
                && lastHeight == figure.getHeight();
    }
}

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
     * Set by each render request and cleared by the render task right before
     * it renders. A request which arrives while a rendering is in progress
     * therefore leads to one more rendering afterwards, because the running
     * rendering may already have read the data before the change that
     * caused the request.
     */
    private final AtomicBoolean renderRequested = new AtomicBoolean(false);

    /**
     * Single-thread executor – one thread is kept alive and ready in the pool
     * as long as the figure is displayable. When a render task is submitted
     * it executes immediately (no thread creation delay). When idle, the
     * thread simply waits (no CPU usage, no polling). Uses a daemon thread so
     * it won't prevent JVM shutdown.
     * <p>
     * The executor is created with the first render request and shut down by
     * release when the figure is removed from a displayable container, for
     * example because the figures of a window are replaced or because the
     * window or internal frame that contains the figure is disposed. A thread
     * which waits for tasks is a garbage collection root, so an executor that
     * is never shut down would keep its thread and everything reachable from
     * it alive forever. The next render request after the figure was added
     * again creates a new executor. Null while no executor exists. Guarded by
     * the monitor of this object, because release is called on the EDT while
     * the render thread may submit a follow-up task.
     */
    private ExecutorService executor = null;

    /**
     * Name of the render thread, which contains the identity hash of the
     * figure to tell the threads of several figures apart in a debugger. It
     * is determined once in the constructor, so that the thread factory only
     * captures this string and not the figure.
     */
    private final String threadName;

    /**
     * Dimensions of the last rendered image, used to detect resize.
     */
    private volatile int lastWidth = -1;
    private volatile int lastHeight = -1;

    PaintThreadManager(FigureJPane owner) {
        this.figure = owner;
        this.threadName = "jmplot-renderer-"
                + Integer.toHexString(System.identityHashCode(owner));
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
     * <p>
     * A figure which is not displayable is not rendered, because nothing of
     * it is visible and because a render task would start the thread again
     * that release has just ended. Since release also discards the image,
     * the first paint after the figure was added again finds no image and
     * requests the rendering that was skipped here.
     */
    public void requestRender() {
        if (!figure.isDisplayable()) {
            return;
        }
        renderRequested.set(true);
        if (rendering.compareAndSet(false, true)) {
            submitRenderTask();
        }
    }

    /**
     * Submits the render task to the executor and creates the executor if
     * none exists, which is the case before the first rendering and after
     * release. The caller must have set the rendering flag.
     */
    private synchronized void submitRenderTask() {
        if (executor == null) {
            // The factory captures only the name and not this object, so
            // that the thread can never keep the figure alive by itself.
            String name = threadName;
            executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, name);
                t.setDaemon(true);
                return t;
            });
        }
        executor.submit(this::renderTask);
    }

    /**
     * Ends the render thread and discards the rendered image, so that the
     * figure and its data are no longer referenced by a running thread and
     * can be garbage collected once the application has dropped the figure.
     * A render task which is just running is completed. Called from
     * FigureJPane.removeNotify when the figure is removed from a displayable
     * container, and the figure can be shown again afterwards, because the
     * next render request creates a new executor.
     */
    synchronized void release() {
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
        plotImage = null;
        lastWidth = -1;
        lastHeight = -1;
    }

    /**
     * The actual render work. Runs on the executor thread (off-EDT).
     * Builds a new BufferedImage, paints the figure content into it,
     * then triggers repaint() so the EDT picks up the new image.
     */
    private void renderTask() {
        try {
            // Render again as long as requests arrived during the rendering.
            while (renderRequested.getAndSet(false)) {
                int w = figure.getWidth();
                int h = figure.getHeight();

                if (w <= 0 || h <= 0) {
                    return; // component not yet laid out
                }

                plotImage = renderImage(w, h); // volatile write
                lastWidth = w;
                lastHeight = h;

                // Trigger a repaint on the EDT so the new image gets
                // displayed. This is a lightweight call – it just schedules a
                // paint event.
                figure.repaint();
            }
        } catch (Exception e) {
            System.err.println("PlotRenderer: " + e.getMessage());
        } finally {
            // Allow the next render request to be submitted
            rendering.set(false);
            // A request that arrived between the last check of the loop and
            // the reset above found the task still running and did not
            // submit a new one, so it is submitted here. A figure which was
            // released in the meantime is not rendered anymore.
            if (renderRequested.get() && figure.isDisplayable()
                    && rendering.compareAndSet(false, true)) {
                submitRenderTask();
            }
        }
    }

    /**
     * Renders one complete frame of the figure into a new image. Besides the
     * render thread, the figure calls this on the EDT for zooming and
     * panning with the mouse, so that the image which is painted at once and
     * the image of the render thread which replaces it afterwards are painted
     * with the same font and rendering hints and look exactly the same.
     *
     * @param w width of the image in pixels, greater than zero
     * @param h height of the image in pixels, greater than zero
     * @return the rendered image
     */
    BufferedImage renderImage(int w, int h) {
        BufferedImage img = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        figure.prepareGraphics(g2);

        if (figure.isOpaque()) {
            g2.setColor(figure.getBackground());
            g2.fillRect(0, 0, w, h);
        }

        float pw = (float) w - 1;
        float ph = (float) h - 1;

        figure.paintFigureContent(g2, pw, ph);

        g2.dispose();
        return img;
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

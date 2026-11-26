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

/**
 * Receives the changes which the user makes to the x cursors of an axes
 * object with the mouse, which are the insertion of an x cursor by a click
 * and the movement of an x cursor by dragging its handle.
 * <p>
 * The listener is the hook for applications that keep the x cursors of
 * several axes in sync, for example several figures which show signals of
 * the same time base with linked x axes. Unlike XAxisLink for the x limits,
 * the library does not contain a ready-made link for x cursors, because such
 * a synchronization is a special application, and the application usually
 * knows best which axes belong together and which figures have to be painted
 * again. A synchronization is built from three parts:
 * <ol>
 * <li>One listener is registered with Axes.addXCursorListener at every axes
 * that takes part.</li>
 * <li>When the listener is called, it reads the cursor values of the source
 * axes with getXCursorValues and applies them to every other axes with
 * setXCursorValues.</li>
 * <li>It repaints the figures of these other axes, because the axes objects
 * do not know their figures. The figure of the source axes is painted again
 * by its own mouse handling.</li>
 * </ol>
 * The listener is only notified about changes by the mouse, but not about
 * changes by addXCursor, setXCursorValues or clearInteractiveCursors. A
 * listener that applies the values to other axes therefore does not notify
 * itself again, so that no guard against an endless loop is needed. If
 * cursors are cleared or restored by the application, the application has
 * to do this for all synchronized axes itself.
 * <pre>
 * XCursorListener sync = source -&gt; {
 *     float[] values = source.getXCursorValues();
 *     for (int idx = 0; idx &lt; axes.length; idx++) {
 *         if (axes[idx] != source) {
 *             axes[idx].setXCursorValues(values);
 *             figures[idx].repaint();
 *         }
 *     }
 * };
 * for (Axes a : axes) {
 *     a.setCursorMode(CursorMode.X_CURSOR_DUAL);
 *     a.addXCursorListener(sync);
 * }
 * </pre>
 * The listener is called on the event dispatch thread, on which the mouse
 * interaction of the figures takes place. While a cursor is dragged, it is
 * called for every mouse movement, which is cheap as long as the listener
 * only sets values and repaints, because the cursors are painted as an
 * overlay on top of the rendered lines.
 *
 * @author Viktor Alexander Hartung
 */
public interface XCursorListener {

    /**
     * Called after the user inserted or moved an x cursor of an axes object
     * with the mouse.
     *
     * @param source axes object whose x cursors were changed, which provides
     * the new values with getXCursorValues
     */
    public void xCursorsChanged(Axes source);
}

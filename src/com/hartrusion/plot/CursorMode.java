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
 * Behaviour of an axes object when the user clicks into its box with the left
 * mouse button, which is set with Axes.setCursorMode.
 * <p>
 * A click is a press and a release of the left mouse button at nearly the
 * same position, so that dragging a zoom rectangle is not affected. The
 * cursors that a click creates store their position as values of the axis
 * rulers, which is the reason why they keep their place on the data when
 * the axes are zoomed or panned.
 *
 * @author Viktor Alexander Hartung
 */
public enum CursorMode {

    /**
     * Clicks do not create cursors, which is the default of every axes.
     */
    NONE,
    /**
     * A click moves a single crosshair cursor to the data point of all lines
     * that lies closest to the click on the screen, and a box right above the
     * cursor shows the x and y value of this data point.
     */
    POINT_CURSOR,
    /**
     * The first click inserts a vertical x cursor, which marks the data point
     * closest to its x value on every line. The cursor can be moved by
     * dragging the triangle handle at its top.
     */
    X_CURSOR,
    /**
     * Like X_CURSOR, but the second click inserts a second x cursor, and the
     * distance between both cursors is shown as delta x.
     */
    X_CURSOR_DUAL,
    /**
     * Horizontal cursor which is planned in analogy to X_CURSOR but not
     * implemented yet, so that Axes.setCursorMode rejects it.
     */
    Y_CURSOR
}

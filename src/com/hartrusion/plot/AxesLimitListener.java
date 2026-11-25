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
 * Receives the changes of the x axis limits of an axes object. The axes
 * object calls the listener from xLim, which is used by zooming and panning
 * with the mouse as well as by autoscaling and by programmatic calls, so
 * that a listener learns about every new x range regardless of its origin.
 * XAxisLink implements this interface to apply the x range of one axes to
 * other axes, so that several plots of the same time base can be zoomed
 * together.
 *
 * <p>
 * LLM code generated with Claude Opus 5.5
 *
 * @author Viktor Alexander Hartung
 */
public interface AxesLimitListener {

    /**
     * Called after the x axis limits of an axes object were set.
     *
     * @param source Axes object whose limits were changed
     * @param lower New lower limit of the x axis
     * @param upper New upper limit of the x axis
     */
    public void xLimChanged(Axes source, float lower, float upper);
}

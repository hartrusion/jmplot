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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Links the x axes of several axes objects, so that zooming or panning in one
 * of them applies the same x range to all others, like linkaxes with the
 * option 'x' does in MATLAB. The y axes stay independent, which means that a
 * zoom rectangle changes the y range only in the plot in which it was drawn.
 *
 * <p>
 * The linked axes can be placed in different figures, for example in several
 * FigureJPane objects which are stacked in one window, or in the same figure
 * as subplots. Each axes is therefore added together with the figure that
 * displays it, because a changed x range of an axes is only visible after
 * its figure was painted again, and the axes object itself does not know its
 * figure. The link registers itself as AxesLimitListener at each axes and is
 * notified from xLim, so that it also follows autoscaling and programmatic
 * limit changes of any linked axes.
 *
 * <p>
 * The link is not thread safe and is meant to be used on the event dispatch
 * thread, on which the mouse interaction of the figures changes the limits.
 *
 * <p>
 * LLM code generated with Claude Opus 5.5
 *
 * @author Viktor Alexander Hartung
 */
public class XAxisLink implements AxesLimitListener {

    /**
     * Linked axes objects with the figure that displays each of them, in the
     * order in which they were added.
     */
    private final Map<Axes, Figure> linkedAxes = new LinkedHashMap<>();

    /**
     * True while the link applies a new x range to the linked axes. Setting
     * the limits of a linked axes notifies this link again, and these nested
     * notifications are ignored so that the range is applied only once.
     */
    private boolean propagating = false;

    /**
     * Adds an axes object to the link. The x range of the axes is not changed
     * when it is added, but only with the next change of any linked axes.
     *
     * @param figure Figure which displays the axes and which is painted again
     * after the x range of the axes was changed by the link.
     * @param axes Axes object to link
     */
    public void add(Figure figure, Axes axes) {
        if (linkedAxes.containsKey(axes)) {
            return;
        }
        linkedAxes.put(axes, figure);
        axes.addLimitListener(this);
    }

    /**
     * Removes an axes object from the link, so that it keeps its x range
     * when other axes are zoomed and does not change them anymore.
     *
     * @param axes Axes object to remove
     */
    public void remove(Axes axes) {
        if (linkedAxes.remove(axes) != null) {
            axes.removeLimitListener(this);
        }
    }

    /**
     * Removes all axes objects from the link.
     */
    public void clear() {
        for (Axes axes : linkedAxes.keySet()) {
            axes.removeLimitListener(this);
        }
        linkedAxes.clear();
    }

    /**
     * Applies the new x range of one linked axes to all other linked axes and
     * repaints their figures. The figure of the source axes is not repainted
     * here, because the code that changed its limits is responsible for that,
     * like the mouse interaction of the figure is.
     */
    @Override
    public void xLimChanged(Axes source, float lower, float upper) {
        if (propagating) {
            return;
        }
        propagating = true;
        try {
            for (Map.Entry<Axes, Figure> entry : linkedAxes.entrySet()) {
                if (entry.getKey() == source) {
                    continue;
                }
                entry.getKey().xLim(lower, upper);
                entry.getValue().repaint();
            }
        } finally {
            propagating = false;
        }
    }
}

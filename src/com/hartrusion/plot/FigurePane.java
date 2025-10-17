/*
 * The MIT License
 *
 * Copyright 2025 Viktor Alexander Hartung.
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

import java.awt.Graphics;
import java.awt.Panel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An AWT Panel class that is used to hold and draw multiple axes objects. It
 * can hold manually added axes (the simplest version is one axes here) or a set
 * of axes for a subplot arrangement.
 * <p>
 * Contrary to the matlab figure, this extends a panel and can therefore used as
 * a drawing area on any guy. If you use NetBeans GUI builder, just drag and
 * drop the class onto your GUI.
 *
 * @author Viktor Alexander Hartung
 */
public class FigurePane extends Panel implements Figure {

    /**
     * A list containing all axes that are included in this figure panel.
     */
    private List<Axes> axes = new ArrayList<>();

    /**
     * Reference to a subplot manager, holding some information about the axes
     * placements and so on.
     */
    private SubPlot subPlot;

    @Override
    public void addAxes(Axes a) {
        axes.add(a);
    }

    @Override
    public Axes getLastAxes() {
        if (axes.isEmpty()) {
            return null;
        }
        return axes.get(0);
    }

    @Override
    public void addSubPlot(SubPlot sp) {
        subPlot = sp;
    }

    @Override
    public SubPlot getSubPlot() {
        return subPlot;
    }

    @Override
    public void clear() {
        subPlot = null;
        axes.clear();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        for (Axes a : axes) {
            a.awtPaintComponents(
                    g, (float) getWidth() - 1, (float) getHeight() - 1);
        }
        if (subPlot != null) {
            Iterator<Axes> axIterator = subPlot.getAxesIterator();
            while (axIterator.hasNext()) {
                Axes a = axIterator.next();
                a.awtPaintComponents(
                        g, (float) getWidth() - 1, (float) getHeight() - 1);
            }
        }
    }
}

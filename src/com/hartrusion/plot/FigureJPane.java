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
import java.beans.BeanProperty;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;

/**
 * A Swing JPanel class that is used to hold multiple axes objects. It can hold
 * manually added axes (the simplest version is one axes here) or a set of axes
 * for a subplot arrangement.
 * <p>
 * Contrary to the matlab figure, this extends a panel and can therefore used as
 * a drawing area on any guy. If you use NetBeans GUI builder, just drag and
 * drop the class onto your GUI.
 *
 * @author Viktor Alexander Hartung
 */
public class FigureJPane extends JComponent implements Figure {

    /**
     * A list containing all axes that are included in this figure panel.
     */
    private final List<Axes> axes = new ArrayList<>();

    /**
     * Reference to a subplot manager, holding some information about the axes
     * placements and so on.
     */
    private SubPlot subPlot;

    private int yRulers = 1;
    
    private int[] subplotLayout = {0, 0};

    public FigureJPane() {
        axes.add(new Axes()); // construct the default axes
    }

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
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
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

    public int getYRulers() {
        return yRulers;
    }

    @BeanProperty(preferred = true, visualUpdate = true, description
            = "Number of Y Rulers")
    public void setYRulers(int yRulers) {
        if (yRulers < 0) {
            throw new IllegalArgumentException("Illegal value.");
        }
        int old = this.yRulers;
        if (old != yRulers) {
            axes.clear();
            switch (yRulers) {
                case 0:
                    break; // nothing and no default
                case 1:
                    axes.add(new Axes());
                    break;
                case 2:
                    axes.add(new YYAxes());
                    break;
                default:
                    axes.add(new MYAxes());
                    break;
            }
            this.yRulers = yRulers;
            firePropertyChange("yRulers", old, yRulers);
        }
    }
    
    public int[] getSubplotLayout() {
        return subplotLayout;
    }

    @BeanProperty(preferred = true, visualUpdate = true, description
            = "Subplot Layout")
    public void setSubplotLayout(int[] subplotLayout) {
        if (subplotLayout.length != 2) {
            throw new IllegalArgumentException("Must be array with 2 values.");
        }
        if (subplotLayout[0] < 0 || subplotLayout[1] < 0) {
            throw new IllegalArgumentException("Values must not be negative.");
        }     
        if (!java.util.Arrays.equals(this.subplotLayout, subplotLayout)) {
            
            int[] old = new int[2]; // remember previous
            System.arraycopy(this.subplotLayout, 0, old, 0, 2);
            this.subplotLayout = subplotLayout;
           
            // Apply: Create new subplot
            subPlot = null; // dump
            SubPlot sp = new SubPlot();
            sp.initAxes(subplotLayout[0], subplotLayout[1]);
            addSubPlot(sp);
            
            firePropertyChange("subplotLayout", old, subplotLayout);
        }
    }
}

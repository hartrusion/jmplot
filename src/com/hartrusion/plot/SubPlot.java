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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages multiple axes objects arranged to a uniform grid.
 *
 * @author Viktor Alexander Hartung
 */
public class SubPlot {

    /**
     * The default position array which describes the placement of the axes box
     * relative to the border of the embedding figure. This is used to calculate
     * the new positions when splitting the frame for multiple axes boxes.
     */
    private float[] axesPositions
            // = new float[]{0.13F, 0.11F, 0.775F, 0.815F};
            = new float[]{0.22F, 0.2F, 0.67F, 0.7F};
    
    private int sizeX, sizeY;

    public float[] getAxesPositions() {
        return axesPositions;
    }

    public void setAxesPositions(float[] axesPositions) {
        this.axesPositions = axesPositions;
        updateAxesPositions();
    }

    private List<Axes> axes = new ArrayList<>();

    /**
     * To access and work through the contained axes, an iterator can be used.
     *
     * @return iterator of axes list.
     */
    public Iterator<Axes> getAxesIterator() {
        return axes.iterator();
    }

    /**
     * Initializes and creates the axes for the subplot with the proper position
     * values, making them the subplot arrangement.
     *
     * @param sizeX
     * @param sizeY
     */
    public void initAxes(int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        for (int idx = 0; idx < sizeY; idx++) {
            for (int jdx = 0; jdx < sizeX; jdx++) {
                Axes a = new Axes();
                axes.add(a);
                a.setSubPlot(this);
            }
        }
        updateAxesPositions();
    }
    
    /**
     * Updates the axes positions according to the set axesPosition values.
     */
    private void updateAxesPositions() {
        /*        x=2        axes.get( )
        *      0 0   0 1   ->   0   1
        * y=3  1 0   1 1        2   3
        *      2 0   2 1        4   5
        */
        float xSpacing = 1 / ((float) sizeX);
        float ySpacing = 1 / ((float) sizeY);
        for (int idx = 0; idx < sizeY; idx++) {
            for (int jdx = 0; jdx < sizeX; jdx++) {
                System.out.println(idx * sizeX + jdx);
                axes.get(idx * sizeX + jdx).setPosition(
                        xSpacing * ((float) jdx)
                        + axesPositions[0] / ((float) sizeX),
                        ySpacing * ((float) (sizeY - idx - 1))
                        + axesPositions[1] / ((float) sizeY),
                        axesPositions[2] / ((float) sizeX),
                        axesPositions[3] / ((float) sizeY));
            }
        }
    }

    /**
     * Returns the nth axes given by a number starting from one, while 1
     * represents the upper left axes system.
     *
     * @param idx 1..nrOfAxes
     * @return Axes object
     */
    public Axes getAxes(int idx) {
        return axes.get(idx - 1);
    }

}

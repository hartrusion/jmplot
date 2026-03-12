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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

/**
 * A cursor that can be drawn on an axes object. Its usage is similar to the 2D
 * line object but instead of a series of data points it is just one pair of X
 * and Y values. It is also attached to X and Y axes ruler for positioning so
 * its coordinates are given in data coordinates, not screen coordinates.
 *
 * @author Viktor Alexander Hartung
 */
public class Cursor {

    private float x;
    private float y;

    private AxisRuler xaxis;
    private AxisRuler yaxis;

    // 1: Cursor draw ends excatly before it overwrites the box lines.
    // 0: Cursor draw can be drawn exactly on the box border line
    // -1: Cursor draw will be drawn 1 px over the box border line.
    private static final int BOX_PADDING = -3;

    /**
     * Width of the crosshair styled cursor
     */
    private static final int CROSSHAIR_WIDTH = 8;

    /**
     * Called from the axes object when adding the cursor to the axes. Creates a
     * link between axes and the cursor by making the rulers known to the
     * instance.
     */
    @SuppressWarnings("NonPublicExported")
    public void initComponent(AxisRuler xaxis, AxisRuler yaxis) {
        this.xaxis = xaxis;
        this.yaxis = yaxis;
    }

    /**
     * Sets the position where this cursor is to be displayed.
     *
     * @param x
     * @param y
     */
    public void setPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Paint the line onto an awt panel object. Intended to be called from the
     * axes awtPaintComponents method.
     *
     * @param g Graphics object for drawing.
     */
    public void awtPaintComponents(Graphics g) {
        if (!Float.isFinite(x) || !Float.isFinite(y)) {
            return; // nothing to draw if one value is invalid. Acceptable.
        }

        // Set clipping area to only draw inside the axes box area
        Shape previousClip = g.getClip(); // remember current setting

        // x line start and end is something like 50 and 450, while the y line
        // is reversed so we have end-start for x with start-end for y height.
        // + and - 1 is to not draw over the box and ruler lines.
        Rectangle boxArea = new Rectangle(
                xaxis.getCoordinateLineStart() + BOX_PADDING,
                yaxis.getCoordinateLineEnd() + BOX_PADDING,
                xaxis.getCoordinateLineEnd()
                - xaxis.getCoordinateLineStart() - 2 * BOX_PADDING + 1,
                yaxis.getCoordinateLineStart()
                - yaxis.getCoordinateLineEnd() - 2 * BOX_PADDING + 1);
        g.setClip(boxArea);

        // Draw a crosshair styled cursor - so far the only option
        g.setColor(Color.BLACK);

        int xMiddle = xaxis.getCoordinateValue(x);
        int yMiddle = yaxis.getCoordinateValue(y);

        // horizontal lines left
        g.drawLine(xMiddle - 2, yMiddle - 1,
                xMiddle - CROSSHAIR_WIDTH + 1, yMiddle - 1);
        g.drawLine(xMiddle - 2, yMiddle + 1,
                xMiddle - CROSSHAIR_WIDTH + 1, yMiddle + 1);
        // horizontal lines right
        g.drawLine(xMiddle + 2, yMiddle - 1,
                xMiddle + CROSSHAIR_WIDTH - 1, yMiddle - 1);
        g.drawLine(xMiddle + 2, yMiddle + 1,
                xMiddle + CROSSHAIR_WIDTH - 1, yMiddle + 1);
        // vertical lines top
        g.drawLine(xMiddle - 1, yMiddle - CROSSHAIR_WIDTH + 1,
                xMiddle - 1, yMiddle - 2);
        g.drawLine(xMiddle + 1, yMiddle - CROSSHAIR_WIDTH + 1,
                xMiddle + 1, yMiddle - 2);
        // vertical lines bottom
        g.drawLine(xMiddle - 1, yMiddle + CROSSHAIR_WIDTH - 1,
                xMiddle - 1, yMiddle + 2);
        g.drawLine(xMiddle + 1, yMiddle + CROSSHAIR_WIDTH - 1,
                xMiddle + 1, yMiddle + 2);
        // outer points
        g.drawLine(xMiddle, yMiddle + CROSSHAIR_WIDTH, 
                xMiddle, yMiddle + CROSSHAIR_WIDTH);
        g.drawLine(xMiddle, yMiddle - CROSSHAIR_WIDTH, 
                xMiddle, yMiddle - CROSSHAIR_WIDTH);
        g.drawLine(xMiddle + CROSSHAIR_WIDTH, yMiddle, 
                xMiddle + CROSSHAIR_WIDTH, yMiddle);
        g.drawLine(xMiddle - CROSSHAIR_WIDTH, yMiddle, 
                xMiddle - CROSSHAIR_WIDTH, yMiddle);
        
        g.setClip(previousClip); // restore previous clipping area
    }
    
    /**
     * Returns the currently assigned y axis ruler object to which this line
     * refers to.
     * 
     * @return YAxisRuler
     */
    public YAxisRuler getYAxis() {
        return (YAxisRuler) yaxis;
    }
}

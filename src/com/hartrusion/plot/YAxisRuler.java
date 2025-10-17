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

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 *
 * @author Viktor Alexander Hartung
 */
class YAxisRuler extends AxisRuler {

    YAxisRuler() {
        location = "left";
    }

    /**
     * Holds the position border in X direction for Y Tick labels. If ticks are
     * on the left side, this is the most left position of the ticks, if they
     * are on the right side, this is the end position of the longest tick.
     */
    private int outerYTickLabelPosition;

    /**
     * Updates the draw coordinates for the axes, will be called from paint
     * component of the parent Axes object containing this one.
     * 
     * @param xaxis Reference to the X-Axis ruler object to get the X-Position
     *              where the axes starts and ends.
     */
    public void updatePlacement(XAxisRuler xaxis) {
        switch (location) {
            case "left":
                placement = xaxis.getCoordinateLineStart();
                break;
            case "right":
                placement = xaxis.getCoordinateLineEnd();
                break;
            case "origin":
                placement = xaxis.getCoordinateOrigin();
                break;
        }
    }

    /**
     * Manually sets the placement in X direction where this Y ruler will be
     * placed on the drawing area. Used to set the placement manually.
     * 
     * @param placement
     */
    public void setPlacement(int placement) {
        this.placement = placement;
    }

    public void awtPaintComponents(Graphics g) {
        setGraphics((Graphics2D) g);
        FontMetrics fm = g.getFontMetrics();

        g.drawLine(placement, coordinates[0],
                placement, coordinates[1]);
        updateTickCoordinates();
        // draw Y tick marks
        for (int idx = 0; idx < tickCoordinates.length; idx++) {
            if (tickCoordinates[idx] < coordinates[1]
                    || tickCoordinates[idx] > coordinates[0]) {
                continue; // ticks out of range
            }
            if (tickDir.equals("in") && location.equals("left")
                    || tickDir.equals("out") && location.equals("right")
                    || location.equals("origin")) {
                g.drawLine(placement, tickCoordinates[idx],
                        placement + tickLength, tickCoordinates[idx]);
            } else if (tickDir.equals("in") && location.equals("right")
                    || tickDir.equals("out") && location.equals("left")) {
                g.drawLine(placement, tickCoordinates[idx],
                        placement - tickLength, tickCoordinates[idx]);
            }

        }

        // draw Y tick labels - also track their outer position
        // initialize outerYTickLabelPosition variable
        if (location.equals("right")) {
            outerYTickLabelPosition = placement + 5;
        } else {
            outerYTickLabelPosition = placement - 5;
        }
        for (int idx = 0; idx < tickLabels.length; idx++) {
            if (tickCoordinates[idx] < coordinates[1]
                    || tickCoordinates[idx] > coordinates[0]) {
                continue; // ticks out of range
            }
            if (location.equals("right")) {
                g.drawString(tickLabels[idx],
                        placement + 5,
                        tickCoordinates[idx] + 5); // y: a bit down
                // Track the string withs for end positon
                outerYTickLabelPosition = Math.max(outerYTickLabelPosition,
                        placement + 5 
                            + fm.getMaxAscent()
                            + fm.stringWidth(tickLabels[idx]));
            } else {
                // align them right by using the string width
                g.drawString(tickLabels[idx],
                        placement - fm.stringWidth(tickLabels[idx]) - 5,
                        tickCoordinates[idx] + 5); // y: a bit down
                outerYTickLabelPosition = Math.min(outerYTickLabelPosition,
                        placement - 5 - fm.stringWidth(tickLabels[idx]));
            }
        }
        // draw Y-Label, which is rotated
        if (labelVisible) {
            int xPosition, yPosition;
            yPosition = (coordinates[0] + (coordinates[1] - coordinates[0]) / 2)
                    + fm.stringWidth(label) / 2;
            if (location.equals("right")) {
                // Y-Label on the right side:
                xPosition = Math.max(
                        outerYTickLabelPosition + 10,
                        placement + 0);
            } else {
                // old: 40 px away from Y-Axes line with:
                // xPosition = placement - 40;
                // new X-Position: 1/3rd between both, but not more far
                // away than 40 px away from the axes in case of subplots
                // xPosition = Math.max(
                //         (placement + fm.getMaxAscent()) / 3,
                //        placement - 40);
                // Even newer: Place at the end of the tick labels
                xPosition = Math.min(
                    outerYTickLabelPosition + 3,
                    placement - 0);
            }

            AffineTransform previousTransform = ((Graphics2D) g).getTransform();
            AffineTransform rotTrans = AffineTransform.getQuadrantRotateInstance(3);
            ((Graphics2D) g).setTransform(rotTrans);
            // Rotation is done by coordinate system rotation so the argument
            // are a bit weird: first number is Y from top as negative number,
            // second is X from the left side.
            g.drawString(label, -yPosition, xPosition);
            ((Graphics2D) g).setTransform(previousTransform); // undo transform
        }
    }
}

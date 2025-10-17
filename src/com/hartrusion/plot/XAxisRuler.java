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

/**
 *
 * @author Viktor Alexander Hartung
 */
class XAxisRuler extends AxisRuler {

    XAxisRuler() {
        location = "bottom";
    }

    public void updatePlacement(YAxisRuler yaxis) {
        switch (location) {
            case "bottom":
                placement = yaxis.getCoordinateLineStart();
                break;
            case "top":
                placement = yaxis.getCoordinateLineEnd();
                break;
            case "origin":
                placement = yaxis.getCoordinateOrigin();
                break;
        }
    }

    public void awtPaintComponents(Graphics g) {
        setGraphics((Graphics2D) g);
        FontMetrics fm = g.getFontMetrics();

        g.drawLine(coordinates[0], placement,
                coordinates[1], placement);
        updateTickCoordinates();
        // draw X tick marks
        for (int idx = 0; idx < tickCoordinates.length; idx++) {
            if (tickCoordinates[idx] > coordinates[1]
                    || tickCoordinates[idx] < coordinates[0]) {
                continue; // ticks out of range 
            }
            if (tickDir.equals("in") && location.equals("bottom")
                    || tickDir.equals("out") && location.equals("top")
                    || location.equals("origin")) {
                g.drawLine(tickCoordinates[idx], placement,
                        tickCoordinates[idx], placement - tickLength);
            } else {
                g.drawLine(tickCoordinates[idx], placement,
                        tickCoordinates[idx], placement + tickLength);
            }

        }
        // draw X tick labels
        for (int idx = 0; idx < tickLabels.length; idx++) {
            if (tickCoordinates[idx] > coordinates[1]
                    || tickCoordinates[idx] < coordinates[0]) {
                continue; // ticks out of range
            }
            if (location.equals("top")) {
                g.drawString(tickLabels[idx],
                        tickCoordinates[idx]
                        - fm.stringWidth(tickLabels[idx]) / 2,
                        placement - 2);
            } else {
                g.drawString(tickLabels[idx],
                        tickCoordinates[idx]
                        - fm.stringWidth(tickLabels[idx]) / 2,
                        placement + 14);
            }
        }
        // draw xlabel if visible
        if (labelVisible) {
            // X-Position: Middle position between axes start and end in x
            // direction, minus the labels half-length to center it.
            // Y-Position: placement (Y-coordinate of the X-axes) and more
            // 32 px down.
            g.drawString(label,
                    coordinates[0] + (coordinates[1] - coordinates[0]) / 2
                    - fm.stringWidth(label) / 2,
                    placement + 32);
        }
    }
}

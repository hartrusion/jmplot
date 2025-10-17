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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Contains some properties for the background box. The drawing is done in the
 * Axes class so there is not that much to see here.
 *
 * @author Viktor Alexander Hartung
 */
class Box extends AxisElementProperties {

    private Color lineColor = Color.LIGHT_GRAY;

    Box() {
        color = Color.WHITE;
        stroke = new BasicStroke(1F);
    }

    @Override
    public void setGraphics(Graphics2D g2) {
        g2.setColor(lineColor); // we use the line color here
        g2.setStroke(stroke);
    }

    // box has also a background with different parameters
    public void setGraphicsBackground(Graphics2D g2) {
        g2.setColor(color);
    }

    public void setLineColor(Color c) {
        lineColor = c;
    }

}

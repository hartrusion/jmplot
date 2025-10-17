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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * Holds some totally general properties that are common for elements of an axis
 * and provides methods for changing them. Also contains a prototype for
 * setGraphics which is intended to be called to prepare the drawing process
 *
 * @author Viktor Alexander Hartung
 */
abstract class AxisElementProperties {

    protected boolean visible = true;
    protected Color color;
    protected Stroke stroke;

    /**
     * Applies set properties (Color, Linewidth, ...) to a provided graphics
     * element. Intended to be called before the drawing to set the properties
     * on how the elements shall be drawn.
     *
     * @param g2
     */
    public abstract void setGraphics(Graphics2D g2);

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Primary color of the element.
     *
     * @return
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the primary color of the element.
     *
     * @param color
     */
    public void setColor(Color color) {
        this.color = color;
    }
}

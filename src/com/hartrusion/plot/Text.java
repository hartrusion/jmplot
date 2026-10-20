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

import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Represents a text string drawn onto an Axes object with given x and y
 * coordinates from the axes rulers. It is supposed to be used as an annotation
 * object on line plots and therefor uses the same coordinates as the data of
 * the Line object.
 * <p>
 * The x and y coordinates describe the horizontal left side and vertical center
 * of the drawn text object. Setting a rotation will rotate the text around that
 * point.
 *
 * @author Viktor Alexander Hartung
 */
public class Text extends CoordinateObject {

    private double rotation;
    private String text;
    
    private float x, y;

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    public void setCoordinates(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
        /**
     * Paint the line onto an awt panel object. Intended to be called from the
     * axes awtPaintComponents method.
     *
     * @param g Graphics object for drawing.
     */
    public void paintContent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        // Todo...
    }
}

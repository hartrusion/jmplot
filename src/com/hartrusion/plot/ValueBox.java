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
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Small box with a text that shows values next to a cursor, similar to a
 * tooltip.
 * <p>
 * The box uses the background, border and text colors of the legend, so that
 * cursor values and legend look alike and both follow the light and dark
 * theme. Each cursor owns its own value box, because the theme is applied to
 * the cursors one by one, and the axes owns one more for the delta x of two
 * x cursors.
 *
 * @author Viktor Alexander Hartung
 */
final class ValueBox {

    /**
     * Distance between the border of the box and its text in pixels.
     */
    static final int PADDING = 2;

    private Color backgroundColor = Legend.THEME_LIGHT_BACKGROUND_COLOR;
    private Color borderColor = Legend.THEME_LIGHT_BORDER_COLOR;
    private Color textColor = Legend.THEME_LIGHT_TEXT_COLOR;

    void setThemeDark() {
        backgroundColor = Legend.THEME_DARK_BACKGROUND_COLOR;
        borderColor = Legend.THEME_DARK_BORDER_COLOR;
        textColor = Legend.THEME_DARK_TEXT_COLOR;
    }

    void setThemeLight() {
        backgroundColor = Legend.THEME_LIGHT_BACKGROUND_COLOR;
        borderColor = Legend.THEME_LIGHT_BORDER_COLOR;
        textColor = Legend.THEME_LIGHT_TEXT_COLOR;
    }

    /**
     * Returns the size of the box for a text with the current font of the
     * graphics object, which callers need to place the box.
     */
    Dimension getSize(Graphics g, String text) {
        FontMetrics fm = g.getFontMetrics();
        return new Dimension(fm.stringWidth(text) + 2 * PADDING,
                fm.getHeight() + 2 * PADDING);
    }

    /**
     * Paints the box with its upper left corner at the given position.
     *
     * @param g graphics object to paint into
     * @param text text inside the box
     * @param x left edge of the box in pixels
     * @param y upper edge of the box in pixels
     */
    void paint(Graphics g, String text, int x, int y) {
        Color previousColor = g.getColor();
        FontMetrics fm = g.getFontMetrics();
        Dimension size = getSize(g, text);
        g.setColor(backgroundColor);
        g.fillRect(x, y, size.width, size.height);
        g.setColor(borderColor);
        g.drawRect(x, y, size.width, size.height);
        g.setColor(textColor);
        g.drawString(text, x + PADDING, y + PADDING + fm.getAscent());
        g.setColor(previousColor);
    }

    /**
     * Paints the box right above a point, so that the point itself stays
     * visible. The box is moved to the left side of the point if it would
     * leave the area on the right, and below the point if it would leave the
     * area at the top, which keeps it readable at the edges of the axes box.
     *
     * @param g graphics object to paint into
     * @param text text inside the box
     * @param px horizontal pixel position of the point
     * @param py vertical pixel position of the point
     * @param gap distance between the point and the box in both directions
     * @param area area in which the box shall stay, usually the axes box
     */
    void paintRightAbove(Graphics g, String text, int px, int py, int gap,
            Rectangle area) {
        Dimension size = getSize(g, text);
        int x = px + gap;
        if (x + size.width > area.x + area.width) {
            x = px - gap - size.width;
        }
        int y = py - gap - size.height;
        if (y < area.y) {
            y = py + gap;
        }
        paint(g, text, x, y);
    }
}

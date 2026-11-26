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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.List;

/**
 * A vertical cursor with a fixed x value, which marks the data point of each
 * line that lies closest to this x value.
 * <p>
 * The cursor is created by a click into an axes in the cursor modes X_CURSOR
 * and X_CURSOR_DUAL and is moved by dragging the triangle handle at its top,
 * which the figure pane detects with isHandleHit. The data points are not
 * interpolated, so that each mark sits on a data point that really exists
 * and may lie left or right of the cursor line, which makes the marks jump
 * from point to point while the cursor is dragged. The x value is stored as
 * a value of the x axis ruler, so that the cursor keeps its place on the
 * data when the axes are zoomed or panned. The x value is shown in a value
 * box on the x axis, which covers the tick labels there, and the y value of
 * each marked point is shown in a value box next to the point. Since the
 * axes paints the x cursors as an overlay on top of the rendered lines, the
 * lines do not have to be rendered again while a cursor is dragged.
 *
 * @author Viktor Alexander Hartung
 */
public class XCursor extends CoordinateObject {

    /**
     * Width of the triangle handle at the top of the cursor line in pixels.
     */
    static final int HANDLE_WIDTH = 13;

    /**
     * Height of the triangle handle in pixels, measured downwards from the
     * upper edge of the axes box.
     */
    static final int HANDLE_HEIGHT = 8;

    /**
     * Additional distance around the triangle handle in pixels in which a
     * mouse press still grabs the cursor, because the triangle itself is a
     * small target.
     */
    private static final int HANDLE_HIT_MARGIN = 3;

    /**
     * Diameter of the circle which marks a data point of a line without
     * marker symbol.
     */
    private static final int POINT_CIRCLE_DIAMETER = 7;

    /**
     * Distance between the x axis line and the value box with the x value.
     */
    private static final int X_VALUE_BOX_OFFSET = 2;

    /**
     * Value on the x axis at which the cursor line is drawn.
     */
    private float x;

    /**
     * Color of the cursor line and the handle, which is changed by the theme
     * methods and uses the colors of the crosshair cursor.
     */
    private Color color = Cursor.THEME_LIGHT_COLOR;

    private final ValueBox valueBox = new ValueBox();

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setThemeDark() {
        color = Cursor.THEME_DARK_COLOR;
        valueBox.setThemeDark();
    }

    public void setThemeLight() {
        color = Cursor.THEME_LIGHT_COLOR;
        valueBox.setThemeLight();
    }

    /**
     * Tells whether the x value lies within the current limits of the x axis,
     * because the cursor is only drawn and can only be grabbed there.
     */
    boolean isInsideLimits() {
        return Float.isFinite(x) && x >= xaxis.lim[0] && x <= xaxis.lim[1];
    }

    /**
     * Returns the horizontal pixel position of the cursor line, which is only
     * valid after the axes was painted at least once.
     */
    int getPixelX() {
        return xaxis.getCoordinateValue(x);
    }

    /**
     * Tells whether a pixel lies on the triangle handle including a margin
     * around it. Called from the figure pane on a mouse press to decide
     * whether the press starts dragging this cursor instead of a zoom
     * rectangle.
     *
     * @param px horizontal pixel position of the mouse
     * @param py vertical pixel position of the mouse
     * @return true if the handle is hit
     */
    boolean isHandleHit(int px, int py) {
        if (!isInsideLimits()) {
            return false;
        }
        int top = yaxis.getCoordinateLineEnd();
        return Math.abs(px - getPixelX())
                <= HANDLE_WIDTH / 2 + HANDLE_HIT_MARGIN
                && py >= top - HANDLE_HIT_MARGIN
                && py <= top + HANDLE_HEIGHT + HANDLE_HIT_MARGIN;
    }

    /**
     * Paints the cursor line, the handle, the marks of the closest data
     * points with their y values and the x value box. Called from the axes
     * when it paints its interactive cursors on top of the rendered figure.
     *
     * @param g graphics object to paint into
     * @param lines all lines of the axes, including lines that belong to
     * additional y axes, because each line converts its y value with its own
     * y axis ruler
     */
    void paintContent(Graphics g, List<Line> lines) {
        if (!isInsideLimits()) {
            return;
        }
        int left = xaxis.getCoordinateLineStart();
        int right = xaxis.getCoordinateLineEnd();
        int top = yaxis.getCoordinateLineEnd();
        int bottom = yaxis.getCoordinateLineStart();
        int pixelX = getPixelX();
        Rectangle boxArea = new Rectangle(left, top, right - left + 1,
                bottom - top + 1);
        Color previousColor = g.getColor();
        Font previousFont = g.getFont();
        Shape previousClip = g.getClip();
        g.setClip(boxArea);

        g.setColor(color);
        g.drawLine(pixelX, top, pixelX, bottom);
        g.fillPolygon(
                new int[]{pixelX - HANDLE_WIDTH / 2,
                    pixelX + HANDLE_WIDTH / 2 + 1, pixelX},
                new int[]{top, top, top + HANDLE_HEIGHT}, 3);

        for (Line l : lines) {
            paintClosestPoint(g, l, boxArea, previousFont);
        }
        g.setFont(previousFont);

        // The x value box lies on the x axis below the box and covers the
        // tick labels there, so it is painted without the box clipping.
        g.setClip(previousClip);
        String text = xaxis.formatCursorValue(x);
        Dimension size = valueBox.getSize(g, text);
        valueBox.paint(g, text, pixelX - size.width / 2,
                bottom + X_VALUE_BOX_OFFSET);
        g.setColor(previousColor);
    }

    /**
     * Marks the data point of one line which lies closest to the x value of
     * the cursor and shows its y value. The point is marked with the marker
     * symbol of the line if the line has one, and with a circle otherwise,
     * both in the color of the line.
     *
     * @param regularFont font of the value box, which has to be restored
     * after a marker glyph was drawn with its own font
     */
    private void paintClosestPoint(Graphics g, Line l, Rectangle boxArea,
            Font regularFont) {
        int index = l.findNearestIndexByX(x);
        if (index < 0) {
            return;
        }
        float yValue = l.getYValue(index);
        if (!Float.isFinite(yValue)) {
            return;
        }
        YAxisRuler lineYAxis = l.getYAxis();
        int px = xaxis.getCoordinateValue(l.getXValue(index));
        int py = lineYAxis.getCoordinateValue(yValue);
        Color lineColor = l.getLineColor();
        if (lineColor == null) {
            lineColor = Color.BLUE;
        }
        g.setColor(lineColor);
        if (l.getMarker() != '\0') {
            new MarkerGlyph(l.getMarker(), l.getMarkerSize(),
                    ((Graphics2D) g).getFontRenderContext()).draw(g, px, py);
            // The marker font is left set by the glyph, but the value box
            // shall use the regular font.
            g.setFont(regularFont);
        } else {
            g.drawOval(px - POINT_CIRCLE_DIAMETER / 2,
                    py - POINT_CIRCLE_DIAMETER / 2,
                    POINT_CIRCLE_DIAMETER - 1, POINT_CIRCLE_DIAMETER - 1);
        }
        valueBox.paintRightAbove(g, lineYAxis.formatCursorValue(yValue),
                px, py, POINT_CIRCLE_DIAMETER / 2 + 1, boxArea);
    }
}

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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 * Represents a text string drawn onto an Axes object with given x and y
 * coordinates from the axes rulers. It is supposed to be used as an annotation
 * object on line plots and therefor uses the same coordinates as the data of
 * the Line object.
 * <p>
 * The x and y coordinates describe the horizontal left side and vertical center
 * of the drawn text object. Setting a rotation will rotate the text around that
 * point.
 * <p>
 * AI transparency note: paintContent is completely vibe coded using Claude 
 * Opus 5.5
 *
 * @author Viktor Alexander Hartung
 */
public class Text extends CoordinateObject {

    /**
     * Color of the text in the light theme, which is also the default color
     * of the text and equals the axis label color of the light axes theme.
     */
    public static final Color THEME_LIGHT_COLOR
            = Axes.THEME_LIGHT_AXIS_RULER_COLOR;

    /**
     * Color of the text in the dark theme, which equals the axis ruler color
     * of the dark axes theme so that annotation texts and axis labels look
     * alike, just as they do in the light theme.
     */
    public static final Color THEME_DARK_COLOR
            = Axes.THEME_DARK_AXIS_RULER_COLOR;

    /**
     * Color of the text which is used for drawing. It is changed by the theme
     * methods.
     */
    private Color color = THEME_LIGHT_COLOR;

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
     * Applies the dark theme to this text, which draws it in the light gray
     * of the dark axes theme instead of black. The change becomes visible
     * with the next rendering of the figure.
     *
     * <p>
     * LLM code generated with Claude Opus 5.5
     */
    public void setThemeDark() {
        color = THEME_DARK_COLOR;
    }

    /**
     * Applies the light theme to this text, which restores the default black
     * text color. This is used to revert a previously applied dark theme.
     *
     * <p>
     * LLM code generated with Claude Opus 5.5
     */
    public void setThemeLight() {
        color = THEME_LIGHT_COLOR;
    }
    
    /**
     * Paint the text onto an awt panel object. Intended to be called from the
     * axes awtPaintComponents method.
     *
     * @param g Graphics object for drawing.
     */
    public void paintContent(Graphics g) {
        if (text == null || text.isEmpty()) {
            return;
        }
        if (!Float.isFinite(x) || !Float.isFinite(y)) {
            return; // nothing to draw if one value is invalid. Acceptable.
        }
        Graphics2D g2 = (Graphics2D) g;
        // Same appearance as the axis labels with the default font and the
        // color of the current theme.
        g2.setColor(color);
        FontMetrics fm = g2.getFontMetrics();

        int xPosition = xaxis.getCoordinateValue(x);
        int yPosition = yaxis.getCoordinateValue(y);
        // drawString uses the baseline as y coordinate, shift it down so the
        // given y coordinate is the vertical center of the text.
        int yBaseline = yPosition + (fm.getAscent() - fm.getDescent()) / 2;

        if (rotation == 0.0) {
            g2.drawString(text, xPosition, yBaseline);
            return;
        }

        // Rotate coordinate system around the anchor point but keep parent
        // transformation which shifted the coordinates already in parent
        // element. Rotation is given counterclockwise in degrees, as the
        // y direction of the screen is pointing down, the sign is reversed.
        AffineTransform previousTransform = g2.getTransform();
        AffineTransform withRot = new AffineTransform(previousTransform);
        withRot.concatenate(AffineTransform.getRotateInstance(
                -Math.toRadians(rotation), xPosition, yPosition));
        g2.setTransform(withRot);
        g2.drawString(text, xPosition, yBaseline);
        g2.setTransform(previousTransform); // undo transform
    }
}

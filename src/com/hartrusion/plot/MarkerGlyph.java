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

import java.awt.Font;
import java.awt.Graphics;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

/**
 * A marker character of a line, prepared to be drawn centered on a pixel
 * position.
 * <p>
 * Markers are drawn as characters of a monospaced font, but the position that
 * drawString expects is the left end of the baseline and not the middle of
 * the visible glyph. This class measures the visual bounds of the glyph once
 * and keeps the offset from the baseline origin to the glyph center, so that
 * the many markers of a line are placed exactly on their data points without
 * measuring the glyph for each of them. The line uses it for its markers and
 * its legend sample, and the x cursor uses it to mark the data point of a
 * line with the same symbol that the line itself shows.
 *
 * @author Viktor Alexander Hartung
 */
final class MarkerGlyph {

    private final Font font;

    private final String text;

    /**
     * Horizontal distance from the baseline origin to the center of the
     * visible glyph in pixels.
     */
    private final double offsetX;

    /**
     * Vertical distance from the baseline origin to the center of the
     * visible glyph in pixels, which is negative because the glyph lies
     * above the baseline.
     */
    private final double offsetY;

    /**
     * Measures the marker glyph for the given render context.
     *
     * @param marker character which is drawn as marker, e.g. 'o' or 'x'
     * @param size font size of the marker in points
     * @param frc render context of the graphics object that draws the
     * markers, because the visual bounds depend on it
     */
    MarkerGlyph(char marker, float size, FontRenderContext frc) {
        font = new Font(Font.MONOSPACED, Font.PLAIN, Math.round(size));
        text = String.valueOf(marker);
        // The visual bounds are the bounding box of the rendered glyph
        // pixels relative to the baseline origin (0, 0), so their x and y
        // values are usually negative.
        Rectangle2D visualBounds = font.createGlyphVector(frc, text)
                .getVisualBounds();
        offsetX = visualBounds.getX() + visualBounds.getWidth() / 2.0;
        offsetY = visualBounds.getY() + visualBounds.getHeight() / 2.0;
    }

    /**
     * Draws the marker with its visual center on the given pixel position in
     * the current color of the graphics object. The marker font is set on
     * the graphics object if it is not set yet, and it is left set, so that
     * many markers are drawn without switching the font each time. The
     * caller has to restore its previous font afterwards.
     *
     * @param g graphics object to draw into
     * @param px horizontal pixel position of the glyph center
     * @param py vertical pixel position of the glyph center
     */
    void draw(Graphics g, int px, int py) {
        if (g.getFont() != font) {
            g.setFont(font);
        }
        g.drawString(text,
                (int) Math.round(px - offsetX),
                (int) Math.round(py - offsetY) + 1);
    }
}

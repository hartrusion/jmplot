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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines and manages a legend box that can be drawn. It has a context to an
 * Axes object to be able to be drawn in context to that Axes but the legend is
 * not part of the axes, making it possible to draw it basically anywhere.
 *
 * @author Viktor Alexander Hartung
 */
public class Legend {

    /**
     * Background color of the legend box.
     */
    public static final Color BACKGROUND_COLOR = Color.WHITE;

    /**
     * Border color of the legend box.
     */
    public static final Color BORDER_COLOR = Color.BLACK;

    /**
     * Color used for the label text.
     */
    public static final Color TEXT_COLOR = Color.BLACK;

    /**
     * Font used for legend label text.
     */
    // public static final Font LEGEND_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    /**
     * Padding in pixels between the legend box edge and its content.
     */
    public static final int PADDING = 2;

    /**
     * Vertical spacing in pixels between two legend entries.
     */
    public static final int ENTRY_SPACING = 1;

    /**
     * Length of the exemplary sample line in pixels.
     */
    public static final int SAMPLE_LINE_LENGTH = 20;

    /**
     * Horizontal gap between the end of the sample line and the label text.
     */
    public static final int TEXT_OFFSET = 5;

    /**
     * List of line elements that shall be referenced in this legend display.
     */
    private final List<Line> lines = new ArrayList<>();

    private int xPosition, yPosition;

    /**
     * Optional reference to an Axes object. If set, the legend will be
     * positioned relative to the axes box (top-right corner with inset).
     */
    private Axes axes = null;

    /**
     * Inset in pixels from the axes box edge when bound to an Axes.
     */
    public static final int AXES_INSET = 8;

    /**
     * Adds a line object that shall be displayed in this Legend
     *
     * @param line Line object
     */
    public void addLine(Line line) {
        lines.add(line);
    }

    /**
     * Binds this legend to an Axes object. When bound, the legend will
     * automatically position itself inside the axes box (top-right corner) and
     * ignore the manually set xPosition/yPosition.
     *
     * @param axes Axes to bind to, or null to unbind.
     */
    public void setLocationInsideAxes(Axes axes) {
        this.axes = axes;
    }

    /**
     * Draws the complete legend box with all entries onto the given Graphics
     * context. Call this from {@code FigureJPane.paintComponent}.
     *
     * <p>
     * The method computes the required box size dynamically based on the number
     * of lines and the width of the longest label text.</p>
     *
     * @param g Graphics object from the paint method
     */
    public void awtPaintComponents(Graphics g) {
        if (lines.isEmpty()) {
            return; // nothing to draw
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Figure out how big the box will be, this depends on the text that
        // will be drawn afterwards-
        // Font previousFont = g.getFont(); - use default so far
        //g.setFont(LEGEND_FONT);
        FontMetrics fm = g.getFontMetrics();

        int entryHeight = fm.getHeight(); // height of one text line
        int maxLabelWidth = 0;

        for (Line line : lines) {
            String label = line.getLabel();
            if (label != null) {
                int w = fm.stringWidth(label);
                if (w > maxLabelWidth) {
                    maxLabelWidth = w;
                }
            }
        }

        // Box width: PADDING + sampleLine + textOffset + longestLabel + PADDING
        int boxWidth = PADDING
                + SAMPLE_LINE_LENGTH
                + TEXT_OFFSET
                + maxLabelWidth
                + PADDING;

        // Box height: PADDING + n * entryHeight + (n-1) * entrySpacing + PADDING
        int n = lines.size();
        int boxHeight = PADDING
                + n * entryHeight
                + (n - 1) * ENTRY_SPACING
                + PADDING;

        // After sizes are known, draw the legend box.
        Color previousColor = g.getColor();
        
        int drawX;
        int drawY;

        if (axes != null) {
            // boxCoordinates: [upper-left X, upper-left Y, lower-right X, lower-right Y]
            // Place legend at top-right inside the axes box with AXES_INSET margin.
            // drawX = axes.boxCoordinates[2] - AXES_INSET - boxWidth;
            drawX = axes.boxCoordinates[0] + AXES_INSET; // top-left
            drawY = axes.boxCoordinates[1] + AXES_INSET;
        } else {
            drawX = xPosition;
            drawY = yPosition;
        }

        // Fill background
        g.setColor(BACKGROUND_COLOR);
        g.fillRect(drawX, drawY, boxWidth, boxHeight);

        // Draw border
        g.setColor(BORDER_COLOR);
        g.drawRect(drawX, drawY, boxWidth, boxHeight);

        // Draw each entry of the legend, this is not done here but in the
        // Line object itself as the line object knows best how it looks like, 
        // this prevens unnecessary passing of data.
        for (int i = 0; i < n; i++) {
            // X start of the sample line inside the box
            int entryX = drawX + PADDING;

            // Y center of this entry: top padding + half an entry height
            // for the first entry, then offset by (entryHeight + spacing)
            // for each subsequent entry.
            int entryYCenter = drawY + PADDING
                    + i * (entryHeight + ENTRY_SPACING)
                    + entryHeight / 2;

            lines.get(i).drawToLegend(g, entryX, entryYCenter,
                    SAMPLE_LINE_LENGTH);
        }

        // g.setFont(previousFont);
        g.setColor(previousColor);
    }

}

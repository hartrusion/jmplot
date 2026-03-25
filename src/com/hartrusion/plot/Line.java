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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;

/**
 * 2D line object to be drawn on a axes object. The line has references to the
 * axis ruler object from the axes where it is supposed to be drawn into (or
 * onto) and uses the ruler object to calulate the proper positions of the
 * coordinates where the line will be placed.
 * <p>
 * The line also holds an array of x and y data, while this array is only a
 * reference at first, it can either be created here and filled with data or set
 * to an external data source.
 *
 * @author Viktor Alexander Hartung
 */
public class Line {

    private float[] xdata;
    private float[] ydata;

    private boolean noXData;
    private boolean noYData;

    private Color lineColor = null;
    private Stroke lineStroke = new BasicStroke(1F);

    private AxisRuler xaxis;
    private AxisRuler yaxis;

    private float xMin, xMax, yMin, yMax;

    private boolean drawingDeprecated;

    /**
     * To determine whether the class holds the data to plot or if a reference
     * to external data is used. If setData is used, the data will be copied to
     * this line object and min and max values will be only calculated once. If
     * external data is used, min and max will be calculated each time the
     * values are requested.
     */
    private boolean externalDataSource = false;

    /**
     * The marker character, e.g. 'o', 'x', '*'. '\0' means no marker.
     */
    private char marker = '\0';
    private float markerSize = 12.0F;
    private int markerInterval = 1;

    private String label = null;

    // 1: Line ends excatly before it overwrites the box lines.
    // 0: Line can be drawn exactly on the box border line
    // -1: Line will be drawn 1 px over the box border line.
    private static int BOX_PADDING = 0;

    /**
     * Called from the axes object when adding the line to the axes. Creates the
     * link between axes and the line by making the rulers known to the line.
     */
    @SuppressWarnings("NonPublicExported")
    public void initComponent(AxisRuler xaxis, AxisRuler yaxis) {
        this.xaxis = xaxis;
        this.yaxis = yaxis;
    }

    /**
     * Copies data to plot into the line object. Note that this will not set a
     * reference to the data. This is mainly done to keep compatibility with the
     * matlab usage.
     *
     * @param x
     * @param y
     */
    public void setData(float[] x, float[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("Length mismatch");
        }
        externalDataSource = false;
        if (xdata == null || ydata == null) {
            xdata = new float[x.length];
            ydata = new float[y.length];
        } else if (xdata.length != x.length) {
            xdata = new float[x.length];
            ydata = new float[y.length];
        }

        System.arraycopy(x, 0, xdata, 0, x.length);
        System.arraycopy(y, 0, ydata, 0, y.length);

        updateNoXDataProperty();
        if (!noXData) {
            updateXMinProperty();
            updateXMaxProperty();
        } else {
            xMin = 0;
            xMax = 0;
        }

        updateNoYDataProperty();
        if (!noYData) {
            updateYMinProperty();
            updateYMaxProperty();
        } else {
            yMin = 0;
            yMax = 0;
        }
    }

    /**
     * The line will be generated from the given arrays. This method sets the
     * line data to an external reference, the line plot will display what is
     * stored inside those arrays.
     *
     * @param x Reference to array of float
     * @param y Reference to array of float
     */
    public void setDataSource(float[] x, float[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("Length mismatch");
        }
        externalDataSource = true;
        xdata = x;
        ydata = y;
    }

    public void setDrawingDeprecated() {
        drawingDeprecated = true;
    }
    
    public boolean isDrawingDeprecated() {
        return drawingDeprecated;
    }

    /**
     * Paint the line onto an awt panel object. Intended to be called from the
     * axes awtPaintComponents method.
     *
     * @param g Graphics object for drawing.
     */
    public void paintContent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        if (lineColor == null) {
            lineColor = Color.BLUE; // assign default if its still not done
        }
        Color previousColor = g.getColor();
        g.setColor(lineColor);
        g2.setStroke(lineStroke);

        // Set clipping area to only draw inside the axes box area
        Shape previousClip = g.getClip(); // remember current setting

        // x line start and end is something like 50 and 450, while the y line
        // is reversed so we have end-start for x with start-end for y height.
        // + and - 1 is to not draw over the box and ruler lines.
        Rectangle boxArea = new Rectangle(
                xaxis.getCoordinateLineStart() + BOX_PADDING,
                yaxis.getCoordinateLineEnd() + BOX_PADDING,
                xaxis.getCoordinateLineEnd()
                - xaxis.getCoordinateLineStart() - 2 * BOX_PADDING + 1,
                yaxis.getCoordinateLineStart()
                - yaxis.getCoordinateLineEnd() - 2 * BOX_PADDING + 1);
        g.setClip(boxArea);
        // Plot lines between xdata points
        for (int idx = 0; idx < xdata.length - 1; idx++) {
            if (!Float.isFinite(xdata[idx])
                    || !Float.isFinite(xdata[idx + 1])
                    || !Float.isFinite(ydata[idx])
                    || !Float.isFinite(ydata[idx + 1])) {
                // No lines between points if any value is NaN or Infinity
                continue;
            }
            g.drawLine(xaxis.getCoordinateValue(xdata[idx]),
                    yaxis.getCoordinateValue(ydata[idx]),
                    xaxis.getCoordinateValue(xdata[idx + 1]),
                    yaxis.getCoordinateValue(ydata[idx + 1]));
        }

        if (marker != '\0') {
            Font previousFont = g.getFont();

            // Set up marker font and color
            Font markerFont = new Font(Font.MONOSPACED, Font.PLAIN,
                    Math.round(markerSize));
            g.setFont(markerFont);

            // Create a GlyphVector for the marker character once.
            // getVisualBounds() gives the bounding box of the actual
            // rendered glyph pixels – this is what makes centering exact.
            String markerStr = String.valueOf(marker);
            GlyphVector gv = markerFont.createGlyphVector(
                    g2.getFontRenderContext(), markerStr);
            Rectangle2D visualBounds = gv.getVisualBounds();

            // The visual bounds are relative to the baseline origin (0,0).
            // To center the glyph on a point (px, py), we need to shift:
            //   drawX = px - (visualBounds.x + visualBounds.width / 2)
            //   drawY = py - (visualBounds.y + visualBounds.height / 2)
            // where visualBounds.x/y are typically negative (left of / above baseline).
            double offsetX = visualBounds.getX()
                    + visualBounds.getWidth() / 2.0;
            double offsetY = visualBounds.getY()
                    + visualBounds.getHeight() / 2.0;

            for (int idx = 0; idx < xdata.length; idx += markerInterval) {
                if (!Float.isFinite(xdata[idx]) || !Float.isFinite(ydata[idx])) {
                    continue;
                }
                int px = xaxis.getCoordinateValue(xdata[idx]);
                int py = yaxis.getCoordinateValue(ydata[idx]);

                // Draw the string so that the visual center of the glyph
                // lands exactly on (px, py).
                g.drawString(markerStr,
                        (int) Math.round(px - offsetX),
                        (int) Math.round(py - offsetY) + 1);
            }
            g.setFont(previousFont);
        }

        g.setColor(previousColor);
        g.setClip(previousClip); // restore previous clipping area
        
        drawingDeprecated = false;
    }

    /**
     * Gets the minimum X number of the data that is stored in this line object.
     * Note that the variable is initialized with Float.MAX_VALUE so if there is
     * nothing in the data array than NaN, it will return MAX_VALUE.
     *
     * @return least number
     */
    public float getXMin() {
        if (externalDataSource) {
            updateXMinProperty();
        }
        return xMin;
    }

    /**
     * Gets the largest X number of the data that is stored in this line object.
     * Note that the variable is initialized with Float.MIN_VALUE so if there is
     * nothing in the data array than NaN, it will return MIN_VALUE.
     *
     * @return largest number
     */
    public float getXMax() {
        if (externalDataSource) {
            updateXMaxProperty();
        }
        return xMax;
    }

    /**
     * Gets the minimum Y number of the data that is stored in this line object.
     * Note that the variable is initialized with Float.MAX_VALUE so if there is
     * nothing in the data array than NaN, it will return MAX_VALUE.
     *
     * @return least number
     */
    public float getYMin() {
        if (externalDataSource) {
            updateYMinProperty();
        }
        return yMin;
    }

    /**
     * Gets the largest Y number of the data that is stored in this line object.
     * Note that the variable is initialized with Float.MIN_VALUE so if there is
     * nothing in the data array than NaN, it will return MIN_VALUE.
     *
     * @return largest number
     */
    public float getYMax() {
        if (externalDataSource) {
            updateYMaxProperty();
        }
        return yMax;
    }

    /**
     * To determine if the whole line data is NaN.
     *
     * @return false if any finite values are present.
     */
    public boolean hasXValues() {
        if (externalDataSource) {
            updateNoXDataProperty();
        }
        return !noXData;
    }

    /**
     * To determine if the whole line data is NaN.
     *
     * @return false if any finite values are present.
     */
    public boolean hasYValues() {
        if (externalDataSource) {
            updateNoYDataProperty();
        }
        return !noYData;
    }

    /**
     * Get the current color for this line object. Can return null if the color
     * is not yet set, which is default for new created line objects.
     *
     * @return Color or null if undefined.
     */
    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    /**
     * Sets the marker character.
     *
     * @param marker Exactly one character, e.g. 'o', 'x', '*', '+'
     */
    public void setMarker(char marker) {
        this.marker = marker;
    }

    public void setMarkerDisable() {
        marker = '\0';
    }

    public char getMarker() {
        return marker;
    }

    /**
     * Sets the font size (in points) used to draw the marker character.
     *
     * @param size Font size, e.g. 12.0F
     */
    public void setMarkerSize(float size) {
        this.markerSize = size;
    }

    public float getMarkerSize() {
        return markerSize;
    }

    /**
     * Sets the interval at which markers are drawn. 1 = every point, 10 = every
     * 10th point.
     *
     * @param interval Must be >= 1
     */
    public void setMarkerInterval(int interval) {
        if (interval < 1) {
            throw new IllegalArgumentException("Marker interval must be >= 1");
        }
        this.markerInterval = interval;
    }

    public int getMarkerInterval() {
        return markerInterval;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Sets a label that describes how this line object is named. Used for
     * legend objects.
     *
     * @param label String to describe the Line
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Returns the currently assigned y axis ruler object to which this line
     * refers to.
     *
     * @return YAxisRuler
     */
    public YAxisRuler getYAxis() {
        return (YAxisRuler) yaxis;
    }

    private synchronized void updateXMinProperty() {
        boolean xDataFinite;
        xMin = Float.MAX_VALUE;
        for (int idx = 0; idx < xdata.length; idx++) {
            xDataFinite = Double.isFinite(xdata[idx]);
            if (xdata[idx] < xMin && xDataFinite) {
                xMin = xdata[idx];
            }
        }
    }

    private synchronized void updateXMaxProperty() {
        boolean xDataFinite;
        xMax = Float.MIN_VALUE;
        for (int idx = 0; idx < xdata.length; idx++) {
            xDataFinite = Double.isFinite(xdata[idx]);
            if (xdata[idx] > xMax && xDataFinite) {
                xMax = xdata[idx];
            }
        }
    }

    private synchronized void updateYMinProperty() {
        boolean yDataFinite;
        yMin = Float.MAX_VALUE;
        for (int idx = 0; idx < ydata.length; idx++) {
            yDataFinite = Double.isFinite(ydata[idx]);
            if (ydata[idx] < yMin && yDataFinite) {
                yMin = ydata[idx];
            }
        }
    }

    private synchronized void updateYMaxProperty() {
        boolean yDataFinite;
        yMax = Float.MIN_VALUE;
        for (int idx = 0; idx < ydata.length; idx++) {
            yDataFinite = Double.isFinite(ydata[idx]);
            if (ydata[idx] > yMax && yDataFinite) {
                yMax = ydata[idx];
            }
        }
    }

    private synchronized void updateNoXDataProperty() {
        noXData = true;
        for (int idx = 0; idx < xdata.length; idx++) {
            if (Double.isFinite(xdata[idx])) {
                noXData = false;
                return;
            }
        }
    }

    private synchronized void updateNoYDataProperty() {
        noYData = true;
        for (int idx = 0; idx < ydata.length; idx++) {
            if (Double.isFinite(ydata[idx])) {
                noYData = false;
                return;
            }
        }
    }

    /**
     * Draws a legend entry for this line at the given coordinates. This draws a
     * short exemplary line (with marker in the middle if set) and the label
     * text next to it.
     *
     * @param g Graphics object for drawing
     * @param xCoord X coordinate where the legend sample line starts
     * @param yCoord Y coordinate (vertical center of the entry)
     * @param sampleLineLength Length of the exemplary line in pixels
     */
    public void drawToLegend(Graphics g,
            int xCoord, int yCoord,
            int sampleLineLength) {
        Graphics2D g2 = (Graphics2D) g;
        Color previousColor = g.getColor();
        Stroke previousStroke = g2.getStroke();

        Font previousFont = g.getFont();

        Color color = (lineColor != null) ? lineColor : Color.BLUE;
        g.setColor(color);
        g2.setStroke(lineStroke);

        // Draw the exemplary line from left to right
        g.drawLine(xCoord, yCoord, xCoord + sampleLineLength, yCoord);

        // Draw marker in the center of the sample line if a marker is set
        if (marker != '\0') {
            Font markerFont = new Font(Font.MONOSPACED, Font.PLAIN,
                    Math.round(markerSize));
            g.setFont(markerFont);

            String markerStr = String.valueOf(marker);
            GlyphVector gv = markerFont.createGlyphVector(
                    g2.getFontRenderContext(), markerStr);
            Rectangle2D visualBounds = gv.getVisualBounds();

            double offsetX = visualBounds.getX()
                    + visualBounds.getWidth() / 2.0;
            double offsetY = visualBounds.getY()
                    + visualBounds.getHeight() / 2.0;

            int cx = xCoord + sampleLineLength / 2;
            g.drawString(markerStr,
                    (int) Math.round(cx - offsetX),
                    (int) Math.round(yCoord - offsetY) + 1);
            g.setFont(previousFont);
        }

        // Draw the label text to the right of the sample line
        if (label != null && !label.isEmpty()) {
            // g.setFont(Legend.LEGEND_FONT);
            g.setColor(Legend.TEXT_COLOR);
            FontMetrics fm = g.getFontMetrics();
            int textX = xCoord + sampleLineLength + Legend.TEXT_OFFSET;
            // Vertically center the text on the yCoord line
            int textY = yCoord + (fm.getAscent() - fm.getDescent()) / 2;
            g.drawString(label, textX, textY);
        }

        // Restore previous state
        g2.setStroke(previousStroke);
        g.setColor(previousColor);
    }
}

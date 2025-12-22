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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;

/**
 * 2D line object to be drawn on a axes object. The line has references to the
 * axis ruler object from the axes where it is supposed to be drawn into (or
 * onto) and uses the ruler object to calulate the proper positions of the
 * coordinates where the line will be placed.
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

    /**
     * To determine whether the class holds the data to plot or if a reference
     * to external data is used. If setData is used, the data will be copied to
     * this line object and min and max values will be only calculated once. If
     * external data is used, min and max will be calculated each time the
     * values are requested.
     */
    private boolean externalDataSource = false;

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

    /**
     * Paint the line onto an awt panel object. Intended to be called from the
     * axes awtPaintComponents method.
     *
     * @param g Graphics object for drawing.
     */
    public void awtPaintComponents(Graphics g) {
        setGraphics((Graphics2D) g);

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

        g.setClip(previousClip); // restore previous clipping area
    }

    private void setGraphics(Graphics2D g2) {
        if (lineColor == null) {
            lineColor = Color.BLUE; // assign default if its still not done
        }
        g2.setColor(lineColor);
        g2.setStroke(lineStroke);
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
}

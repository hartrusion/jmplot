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
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Represents the axis line and has some common values and methods like scaling,
 * position, limits. This abstract class will be used for both X and Y axis
 * lines, therefore some components are not documented in terms of what exactly
 * they will be at the ent.
 *
 * @author Viktor Alexander Hartung
 */
abstract class AxisRuler extends AxisElementProperties {

    /**
     * Limits of the axis, described as the same value as the line plot. Per
     * default, a plot ranges from 0 to 10.
     */
    protected float[] lim;

    /**
     * Describes where to place this axis.
     */
    protected String location;

    /**
     * The position of the scaled origin as a value between 0..1. If the axis is
     * limited from -2 to 8, the origin will be at 0.25.
     */
    protected float origin = 0F;

    /**
     * Two-dimensional array containing the coordinates where the ruler starts
     * and where it ends on the drawing area. Those values will be either X or Y
     * values.
     */
    protected int[] coordinates = new int[2];

    /**
     * Placement of the axis as awt element coordinates. For the X-Axis, this is
     * the Y value where the axis will be placed and for Y it is the X value.
     */
    protected int placement;

    /**
     * Values where the ruler ticks are to be placed, given as target float
     * value of the lineplot.
     */
    protected float[] tick;

    /**
     * Defines where the ticks are directed to. Possible values: in, out, both.
     */
    protected String tickDir = "in";

    /**
     * Holds values where the tick dash will be placed in the direction of the
     * axis.
     */
    protected int[] tickCoordinates;

    /**
     * Length of the tick mark in pixels
     */
    protected int tickLength = 5;

    /**
     * Holds the labels for this axis ruler which will be displayed.
     */
    protected String[] tickLabels;

    private NumberFormat numberFormat = DecimalFormat.getInstance();

    protected boolean labelVisible = false;
    protected String label;

    AxisRuler() {
        color = Color.BLACK;
        stroke = new BasicStroke(1F);

        // Construct the default axis from 0 to 10. This is moved here
        // to maybe parametrize it later.
        lim = new float[]{0F, 1F};
        tick = new float[6];
        for (int idx = 0; idx < 6; idx++) {
            tick[idx] = 0.2F * (float) idx;
        }
        // these arrays will be filled later, but will be initialized here
        // to avoid having to check for null each time.
        tickCoordinates = new int[6];
        tickLabels = new String[6];
        generateTickLabels();
    }

    @Override
    public void setGraphics(Graphics2D g2) {
        g2.setColor(color);
        g2.setStroke(stroke);
    }

    /**
     * Set the limit of this axis. This describes the beginning and the end of
     * the axis in value dimensions. Does not trigger any other action besides
     * stting the limit. No ticks will be applied.
     *
     * @param lower
     * @param upper
     */
    public void setLim(float lower, float upper) {
        lim[0] = lower;
        lim[1] = upper;
    }

    /**
     * Sets tick values given as array
     *
     * @param tick Array containing tick values
     */
    public void setTicks(float[] tick) {
        if (this.tick.length != tick.length) {
            this.tick = new float[tick.length];
        }
        System.arraycopy(tick, 0, this.tick, 0, tick.length);
    }

    /**
     * Generates ticks to display at the axes. 0, 2, 10 will create ticks on 0,
     * 2, 4, 6, 8, 10. Note that this does not change the limits, only which
     * values will be marked on the axis.
     *
     * @param lower First value
     * @param increment increment
     * @param upper last value
     */
    public void setTicks(float lower, float increment, float upper) {
        if (!Float.isFinite(lower)
                || !Float.isFinite(increment)
                || !Float.isFinite(upper)) {
            return; // ignore command for NaN-values, which are valid.
        }
        if (lower == Float.MIN_VALUE
                && upper == Float.MAX_VALUE) {
            return; // an empty line will return these values, ignore them.
        }
        if (increment <= 0.0F) {
            return; // invalid increment
        }
        int length = Math.round((upper - lower) / increment) + 1;
        if (tick.length != length) {
            tick = new float[length];
        }
        tick[0] = lower;
        for (int idx = 1; idx < length; idx++) {
            tick[idx] = tick[idx - 1] + increment;
        }
        generateTickLabels();
    }

    /**
     * Set the coordinates where this axis begins and where it ends as values of
     * the awt container that contians this axis. For Y-Axis, the value end will
     * be smaller than start, as the directions are reversed.
     *
     * @param start coordinate on the awt draw area
     * @param end coordinate on the awt draw area
     */
    public void setCoordinates(int start, int end) {
        coordinates[0] = start;
        coordinates[1] = end;
    }

    public int getCoordinateLineStart() {
        return coordinates[0];
    }

    public int getCoordinateLineEnd() {
        return coordinates[1];
    }

    public int getCoordinateOrigin() {
        return (int) (((float) (coordinates[0] - coordinates[1])) * origin);
    }

    /**
     * Calculates the position of a float value in reference to this axis'
     * limits. This is heavily used by elements that plot into the axes to
     * transfer the plot values to pixel values for drawing.
     *
     * @param value A value, likely betweel lim[0] and lim[1]
     * @return pixel position in the direction of this axis.
     */
    public int getCoordinateValue(float value) {
        return coordinates[0]
                + (int) (((float) (coordinates[1] - coordinates[0]))
                * (value - lim[0])
                / (lim[1] - lim[0]));
    }

    /**
     * Set the location of this ruler.
     *
     * @param s For x-Axis: top, bottom, origin. Y-Axis: left, right, origin
     */
    public void setLocation(String s) {
        location = s;
    }

    public String getLocation() {
        return location;
    }

    /**
     * Updates the tickCoordinates array and recalulates the tick positions on
     * the axis. Gets called on each paint of the ui.
     */
    protected final void updateTickCoordinates() {
        if (tickCoordinates.length != tick.length) {
            tickCoordinates = new int[tick.length];
        }
        for (int idx = 0; idx < tickCoordinates.length; idx++) {
            tickCoordinates[idx] = getCoordinateValue(tick[idx]);
        }
    }

    /**
     * Generates the tickLabels (String) from tick (Float) array. The
     */
    protected final void generateTickLabels() {
        // try to calculate a proper number of digits
        int digits = -2 + (int) (Math.log10(lim[1] - lim[0]));
        if (digits < 0) {
            numberFormat.setMaximumFractionDigits(-digits);
        } else {
            numberFormat.setMaximumFractionDigits(0);
        }
        if (tickLabels.length != tick.length) {
            tickLabels = new String[tick.length];
        }
        for (int idx = 0; idx < tickLabels.length; idx++) {
            tickLabels[idx] = numberFormat.format(tick[idx]);
        }
    }

    public void setLabel(String s) {
        if (s == null) {
            labelVisible = false;
        } else {
            label = s;
            labelVisible = true;
        }
    }
}

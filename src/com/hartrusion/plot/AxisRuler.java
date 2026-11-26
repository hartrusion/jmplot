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

    /**
     * Number of fraction digits of the current tick labels, which is the
     * base for the precision of cursor values.
     */
    private int tickFractionDigits = 0;

    /**
     * Number of fraction digits that cursor values show in addition to the
     * tick labels, because a cursor value lies between the ticks and is
     * read more precisely than the axis scale.
     */
    private static final int CURSOR_VALUE_EXTRA_DIGITS = 2;

    /**
     * Format of the cursor values, which uses the locale of the system like
     * the tick labels. It is separate from numberFormat, because it has a
     * fixed number of fraction digits whereas the tick labels have a maximum
     * number only.
     */
    private final NumberFormat cursorValueFormat = DecimalFormat.getInstance();

    /**
     * Counts the changes of the limits and ticks of this ruler. The counter is
     * compared with paintedModificationCount to find out whether the ruler
     * changed after it was painted the last time, because a figure which
     * renders into a buffered image has to render again in that case.
     */
    private volatile int modificationCount = 0;

    /**
     * Value of modificationCount when the painting of the ruler began the
     * last time. A change during a painting therefore leaves the two values
     * different, so that the next check requests a new painting. The
     * initial value differs from modificationCount, because the ruler has
     * not been painted yet.
     */
    private volatile int paintedModificationCount = -1;

    /**
     * Maximum number of tick intervals which the axes objects request with
     * setTicks(int) whenever they set new limits.
     */
    static final int DEFAULT_TICK_NUMBER = 10;

    /**
     * Mantissas of the steps between round tick values in ascending order.
     * The step is one of these values multiplied with a power of ten, and the
     * last value 10 is the fallback that always fits.
     */
    private static final double[] ROUND_TICK_MANTISSAS = {1.0, 2.0, 2.5, 5.0,
        10.0};

    /**
     * Mantissa which needs one fraction digit more than its power of ten,
     * since 2.5 has a digit behind the 2 whereas 1, 2 and 5 have none.
     */
    private static final double FRACTIONAL_MANTISSA = 2.5;

    protected boolean labelVisible = false;
    protected String label;

    AxisRuler() {
        color = Axes.THEME_LIGHT_AXIS_RULER_COLOR;
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
        modificationCount++;
    }

    /**
     * Remembers the current state of limits and ticks as painted. Called by
     * the axes object at the beginning of its painting, before the ruler and
     * the lines read the limits.
     */
    void markPainted() {
        paintedModificationCount = modificationCount;
    }

    /**
     * Tells whether limits or ticks were changed after the painting began the
     * last time, which is used by the axes object to decide whether the
     * figure has to be rendered again.
     *
     * @return true if the painted state is outdated
     */
    boolean isModifiedSincePaint() {
        return modificationCount != paintedModificationCount;
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
        modificationCount++;
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
     * Generates ticks with round values between the current limits, so that
     * the labels show values like 0, 0.5, 1, 1.5 instead of the plain tenth
     * parts of an arbitrary range. The step between the ticks is the
     * smallest value of 1, 2, 2.5 or 5 multiplied with a power of ten which
     * does not result in more than the given number of intervals. The ticks
     * start at the first multiple of the step which is not below the lower
     * limit, so that the limits themselves are usually not marked, because
     * the limits are not changed by this method. The tick values are
     * calculated by multiplication instead of summing up the step, which
     * would accumulate the rounding errors of the float values.
     *
     * <p>
     * LLM code generated with Claude Opus 5.5
     *
     * @param number Maximum number of intervals between the ticks within the
     * limits, which must be at least 1.
     */
    public void setTicks(int number) {
        double lower = lim[0];
        double upper = lim[1];
        if (!Double.isFinite(lower) || !Double.isFinite(upper)
                || upper <= lower || number < 1) {
            return; // no range to place ticks into
        }
        double rawStep = (upper - lower) / number;
        double magnitude = Math.pow(10.0, Math.floor(Math.log10(rawStep)));
        double mantissa
                = ROUND_TICK_MANTISSAS[ROUND_TICK_MANTISSAS.length - 1];
        for (double candidate : ROUND_TICK_MANTISSAS) {
            // The tolerance accepts a step that equals rawStep except for
            // floating point noise, for example 0.1 for a range of 1.
            if (candidate * magnitude >= rawStep * (1.0 - 1e-9)) {
                mantissa = candidate;
                break;
            }
        }
        double step = mantissa * magnitude;
        long firstIndex = (long) Math.ceil(lower / step - 1e-9);
        long lastIndex = (long) Math.floor(upper / step + 1e-9);
        int length = (int) (lastIndex - firstIndex + 1);
        if (tick.length != length) {
            tick = new float[length];
        }
        for (int idx = 0; idx < length; idx++) {
            tick[idx] = (float) ((firstIndex + idx) * step);
        }
        // A step of 10^n needs -n fraction digits, which are none for n >= 0.
        int digits = -(int) Math.round(Math.log10(magnitude));
        if (mantissa == FRACTIONAL_MANTISSA) {
            digits++;
        }
        generateTickLabels(Math.max(0, digits));
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
     * Calculates the value that is assigned to a certain pixel value.
     *
     * @param pixel
     * @return
     */
    public float getValueForCoordinate(int pixel) {
        if (coordinates[0] == coordinates[1]) {
            return lim[0];
        }
        return lim[0] + (float) (pixel - coordinates[0])
                * (lim[1] - lim[0])
                / (float) (coordinates[1] - coordinates[0]);
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
     * Updates the tickCoordinates array and recalculates the tick positions on
     * the axis. Gets called on each paint of the UI.
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
            generateTickLabels(-digits);
        } else {
            generateTickLabels(0);
        }
    }

    /**
     * Generates the tickLabels from the tick array with a given maximum
     * number of fraction digits. Used by setTicks(int), which knows the
     * number of digits that its round tick values need.
     *
     * @param fractionDigits Maximum number of digits behind the decimal
     * separator.
     */
    private void generateTickLabels(int fractionDigits) {
        // All methods which set ticks from values end here.
        modificationCount++;
        tickFractionDigits = fractionDigits;
        numberFormat.setMaximumFractionDigits(fractionDigits);
        if (tickLabels.length != tick.length) {
            tickLabels = new String[tick.length];
        }
        for (int idx = 0; idx < tickLabels.length; idx++) {
            tickLabels[idx] = numberFormat.format(tick[idx]);
        }
    }

    /**
     * Formats a value of this axis for the value box of a cursor. The value
     * is written in the locale of the system like the tick labels and with
     * two fraction digits more than the current tick labels, so that the
     * precision follows the zoom of the axis.
     *
     * @param value value on this axis
     * @return formatted value
     */
    public String formatCursorValue(float value) {
        int digits = tickFractionDigits + CURSOR_VALUE_EXTRA_DIGITS;
        cursorValueFormat.setMinimumFractionDigits(digits);
        cursorValueFormat.setMaximumFractionDigits(digits);
        return cursorValueFormat.format(value);
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

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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Viktor Alexander Hartung
 */
public class Axes {

    /**
     * Background color of the box in the light theme, which is also the
     * default background color of the box.
     */
    public static final Color THEME_LIGHT_BOX_BACKGROUND_COLOR = Color.WHITE;

    /**
     * Frame line color of the box in the light theme, which is also the
     * default frame line color of the box.
     */
    public static final Color THEME_LIGHT_BOX_LINE_COLOR = Color.LIGHT_GRAY;

    /**
     * Color of the axis rulers, which includes the ruler lines, ticks, tick
     * labels and axis labels, in the light theme. This is also the default
     * color of the axis rulers.
     */
    public static final Color THEME_LIGHT_AXIS_RULER_COLOR = Color.BLACK;

    /**
     * Background color of the box in the dark theme.
     */
    public static final Color THEME_DARK_BOX_BACKGROUND_COLOR = Color.BLACK;

    /**
     * Frame line color of the box in the dark theme, which is the dark gray
     * (128, 128, 128) of the classic 16 color palette so that the frame stays
     * visible but less prominent than the axis rulers.
     */
    public static final Color THEME_DARK_BOX_LINE_COLOR
            = new Color(128, 128, 128);

    /**
     * Color of the axis rulers in the dark theme, which is the light gray
     * (192, 192, 192) of the classic 16 color palette to keep the retro look.
     */
    public static final Color THEME_DARK_AXIS_RULER_COLOR
            = new Color(192, 192, 192);

    /**
     * Contains double values between 0 and 1 which describe the lower left
     * position of the axis system (first two elements), the width and the
     * height of the axes object.
     */
    private final float[] position = new float[]{0.13F, 0.11F, 0.775F, 0.815F};

    /**
     * Contains calculated int values where the box and axes will be placed in
     * the parent awt object.
     *
     * <p>
     * [upper left X, upper left Y, lower right X, lower right Y]
     */
    protected final int[] boxCoordinates = new int[4];

    /**
     * Stores the box coordinates that were used the last time the axes content
     * was drawn. Used to compare with the current box coordinates so it can be
     * decided if a redraw is needed, mostly due to resizing the container.
     */
    protected final int[] oldBoxCoordinates = new int[4];

    private final Box box = new Box(); // there is only ONE box per axes.

    protected final XAxisRuler xaxis = new XAxisRuler();
    protected final YAxisRuler yaxis = new YAxisRuler();

    /**
     * Holds references to all lines which will be drawn into this axes
     */
    protected final List<Line> lines = new ArrayList<>();

    /**
     * Holds references to all cursor objects which will be drawn into this axes
     */
    protected final List<Cursor> cursors = new ArrayList<>();
    
    /**
     * Holds references to all text objects that are drawn into this axes.
     */
    protected final List<Text> texts = new ArrayList<>();

    /**
     * When hold is off, adding a new line will replace the old line plot.
     */
    protected boolean hold = false;

    /**
     * True if the dark theme was applied to this axes. It is used to apply the
     * same theme to cursors and texts which are added after the theme was set,
     * since cursors are usually created on demand by setCursor and texts can
     * be added at any time.
     */
    private boolean themeDark = false;

    /**
     * The subplot this axes is assigned to, if it is existing. Can be null if
     * the axes is not being managed by a subplot.
     */
    private SubPlot parentSubPlot;

//    Axes() {
//    }
    /**
     * Adds a line object to this Axis. Depending if hold is on or off, the
     * previous line or lines might be replaced.
     *
     * @param l Line object that will be drawn within this axis.
     */
    public void addLine(Line l) {
        if (!hold) {
            lines.clear();
            cursors.clear();
            texts.clear();
        }
        lines.add(l);
        // Auto-Assign line colors when adding if no line color is defined yet
        if (l.getLineColor() == null) {
            switch (lines.indexOf(l)) {
                case 0:
                    l.setLineColor(Color.BLUE);
                    break;
                case 1: // 2nd line
                    l.setLineColor(new Color(0, 127, 0)); // dark green
                    break;
                case 2: // 3rd line
                    l.setLineColor(Color.RED);
                    break;
                case 3:
                    l.setLineColor(new Color(128, 128, 0)); // dark yellow
                    break;
                case 4:
                    l.setLineColor(new Color(128, 0, 128)); // violet
                    break;
            }
        }
        l.initComponent(xaxis, yaxis);
        // Adding a line will set this axes to the last current axes.
        VisualizeData.setCurrentAxes(this);
        if (!hold) { // trigger autoscale
            autoX();
            autoY();

            if (l.hasXValues()) {
                if ((l.getXMax() - l.getXMin()) < 1e-40) { // all X vals zero
                    xaxis.setTicks(
                            l.getXMin() - 0.5F, 0.5F, l.getXMax() + 0.5F);
                } else {
                    xaxis.setTicks(l.getXMin(),
                            (l.getXMax() - l.getXMin()) / 5F,
                            l.getXMax());
                }
            }

            if (l.hasYValues()) {
                if ((l.getYMax() - l.getYMin()) < 1e-40) { // all Y vals zero
                    yaxis.setTicks(
                            l.getYMin() - 0.5F, 0.5F, l.getYMax() + 0.5F);
                } else {
                    yaxis.setTicks(l.getYMin(),
                            (l.getYMax() - l.getYMin()) / 10F,
                            l.getYMax());
                }
            }
        }
    }
    
    /**
     * Adds a text object to be drawn on this axes.
     * 
     * @param t Text object to draw
     */
    public void addText(Text t) {
        if (texts.contains(t)) {
            return;
        }
        texts.add(t);
        t.initComponent(xaxis, yaxis);
        if (themeDark) {
            t.setThemeDark();
        } else {
            t.setThemeLight();
        }
    }

    /**
     * Returns the line object with a given line number.
     *
     * @param number Line Number (1..n)
     * @return Line Object
     */
    public Line getLine(int number) {
        return lines.get(number - 1);
    }

    /**
     * Returns the line object that is drawn. If there is more than one line
     * object, use getLine(number) to access lines.
     *
     * @return Line Object
     */
    public Line getLine() {
        return lines.get(0);
    }

    /**
     * Adds a new cursor object to the axes. Used to add more than one cursor
     *
     * @param c Cursor object
     */
    public void addCursor(Cursor c) {
        cursors.add(c);
        c.initComponent(xaxis, yaxis);
        if (themeDark) {
            c.setThemeDark();
        } else {
            c.setThemeLight();
        }
    }

    /**
     * Sets a cursor to the specified coordinates. Internally, this either uses
     * the one existing cursor or generates a new cursor if no cursor is
     * present. Requires only one or no cursor to be present.
     *
     * @param x
     * @param y
     */
    public void setCursor(float x, float y) {
        if (cursors.size() >= 2) {
            throw new UnsupportedOperationException("Setting the cursor with "
                    + "this method does not support more than 1 cursor.");
        }
        if (cursors.size() == 0) { // if none is present, generate one.
            addCursor(new Cursor());
        }
        Cursor c = cursors.get(0);
        c.setPoint(x, y);
    }

    /**
     * Removes all cursors from the axes.
     */
    public void clearCursors() {
        cursors.clear();
    }

    /**
     * Checks if the drawing of this axes or any of its contained elements
     * (lines, cursors) is deprecated and needs to be redrawn. This can be: Box
     * coordinates changed (component resized) or any line reports
     * isDrawingDeprecated().
     *
     * @return true if a redraw is needed
     */
    public boolean isDrawingDeprecated() {
        // Check if box coordinates changed (resize)
        if (!java.util.Arrays.equals(boxCoordinates, oldBoxCoordinates)) {
            return true;
        }
        // Check all lines
        for (Line l : lines) {
            if (l.isDrawingDeprecated()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Paints this axes class and all contained elements into a FigurePanel
     * container. Intended to be called in the paint method from the awt panel
     * object where the axes system is drawn into.
     *
     * @param g Graphics object from paint method
     * @param parentWidth pixels, as float value, (float) getWidth() - 1
     * @param parentHeight pixels, as float value, (float) getHeight() - 1
     */
    public void paintContent(Graphics g,
            float parentWidth, float parentHeight) {
        // Recalculate the coordinates where the box and lines shall be drawn.
        // This is done like this to prevent rounding issues and keep the pixel
        // values consistent.
        boxCoordinates[0] // upper left X
                = (int) (parentWidth * position[0]);
        boxCoordinates[1] // upper left Y
                = (int) (parentHeight * (1F - position[1] - position[3]));
        boxCoordinates[2]
                = // lower right X
                (int) (parentWidth * (position[0] + position[2]));
        boxCoordinates[3]
                = // lower right Y
                (int) (parentHeight * (1F - position[1]));
        if (box.isVisible()) { // Draw the box
            if (boxCoordinates[3] - boxCoordinates[1] > 2
                    && boxCoordinates[2] - boxCoordinates[0] > 2) {
                // fill will only be painted if height and width is there
                box.setGraphicsBackground((Graphics2D) g);
                g.fillRect(boxCoordinates[0] + 1,
                        boxCoordinates[1] + 2,
                        boxCoordinates[2] - boxCoordinates[0] - 2,
                        boxCoordinates[3] - boxCoordinates[1] - 2);
            }
            box.setGraphics((Graphics2D) g);
            g.drawRect(boxCoordinates[0],
                    boxCoordinates[1],
                    boxCoordinates[2] - boxCoordinates[0],
                    boxCoordinates[3] - boxCoordinates[1]);
        }
        // tell both rulers where they shall start and end
        xaxis.setCoordinates(boxCoordinates[0], boxCoordinates[2]);
        yaxis.setCoordinates(boxCoordinates[3], boxCoordinates[1]);
        // Read those coordinates from each others axes to get the position
        // where to draw the ruler.
        xaxis.updatePlacement(yaxis);
        yaxis.updatePlacement(xaxis);

        if (xaxis.isVisible()) {
            xaxis.paintContent(g);
        }
        if (yaxis.isVisible()) {
            yaxis.paintContent(g);
        }

        // Plot all known lines
        for (Line l : lines) {
            if (l.getYAxis() != yaxis) {
                continue; // skip foreign lines (only for extensions of Axes)
            }
            l.paintContent(g);
        }

        // Draw all cursors
        for (Cursor c : cursors) {
            if (c.getYAxis() != yaxis) {
                continue; // skip cursors not assigned to the y axis of this
            }
            c.paintContent(g);
        }
        
        // Draw all text objects
        for (Text t : texts) {
            if (t.getYAxis() != yaxis) {
                continue; // skip cursors not assigned to the y axis of this
            }
            t.paintContent(g);
        }

        // Save the used box coordinates for comparison.
        System.arraycopy(boxCoordinates, 0, oldBoxCoordinates, 0, 4);
    }

    public void xLim(float x1, float x2) {
        xaxis.setLim(x1, x2);
        xaxis.setTicks(x1, (x2 - x1) / 10F, x2);
    }

    public void yLim(float y1, float y2) {
        yaxis.setLim(y1, y2);
        yaxis.setTicks(y1, (y2 - y1) / 10F, y2);
    }

    public void xlabel(String s) {
        xaxis.setLabel(s);
    }

    public void ylabel(String s) {
        yaxis.setLabel(s);
    }

    /**
     * Autoscale the x-Axes
     */
    public void autoX() {
        boolean valueFound = false;
        // Float.MIN_VALUE is the smallest positive number and not the most
        // negative one, so the maximum has to start at negative infinity to
        // be found for data that lies completely below zero.
        float xMax = Float.NEGATIVE_INFINITY;
        float xMin = Float.MAX_VALUE;
        for (Line l : lines) {
            if (!l.hasXValues()) {
                continue; // Only NaN values, which have no range.
            }
            if (l.getXMin() < xMin) {
                xMin = l.getXMin();
            }
            if (l.getXMax() > xMax) {

                xMax = l.getXMax();
            }
            valueFound = true;
        }
        if (valueFound) {
            xLim(xMin, xMax);
        }
    }

    /**
     * Autoscale the y-Axes
     */
    public void autoY() {
        float[] limits = calculateAutoYLimits(yaxis);
        if (limits != null) {
            yLim(limits[0], limits[1]);
        }
    }

    /**
     * Calculates the limits which autoscaling applies to a y axis ruler. The
     * limits enclose the finite values of all lines that are assigned to this
     * ruler, whereas lines with only NaN values are skipped because they
     * have no range. Extensions of this class with more than one y axis call
     * this method for each of their rulers, so that all rulers are scaled in
     * the same way.
     *
     * @param ruler y axis ruler for which the lines are evaluated
     * @return {lower, upper}, or null if no line of the ruler has a finite
     * value. Identical values are padded by 1 in both directions, so that
     * the line is placed in the middle of the axis.
     */
    protected float[] calculateAutoYLimits(YAxisRuler ruler) {
        boolean valueFound = false;
        // Negative infinity as start value, see autoX.
        float yMax = Float.NEGATIVE_INFINITY;
        float yMin = Float.MAX_VALUE;
        for (Line l : lines) {
            if (l.getYAxis() != ruler) {
                continue; // This line is assigned to a different ruler.
            }
            if (!l.hasYValues()) {
                continue; // Only NaN values, which have no range.
            }
            if (l.getYMin() < yMin) {
                yMin = l.getYMin();
            }
            if (l.getYMax() > yMax) {
                yMax = l.getYMax();
            }
            valueFound = true;
        }
        if (!valueFound) {
            return null;
        }
        // Having identical values: Place line in the middle by padding with 1
        if (yMin == yMax) {
            yMin -= 1.0F;
            yMax += 1.0F;
        }
        return new float[]{yMin, yMax};
    }

    /**
     * Set the hold state. If hold is on, addLine will add the line as an
     * additional line and no autoscaling is used. . If hold is off, each line
     * add will be the only line that's in the plot and trigger autoscale.
     *
     * @param value true - on, false - off
     */
    public void setHold(boolean value) {
        hold = value;
    }

    public boolean getHold() {
        return hold;
    }

    /**
     * Sets the position of the axes box relative to the containing figure.
     * Position is described with float values between 0 and 1 which describe
     * the lower left position of the axis system (first two values), the width
     * and the height of the axes object.
     *
     * <p>
     * Defaults: 0.13F, 0.11F, 0.775F, 0.815F
     *
     * @param position 0..3 array with x, y, width, height
     */
    public void setPosition(float[] position) {
        System.arraycopy(position, 0, this.position, 0, position.length);
    }

    /**
     * Sets the position of the axes box relative to the containing figure.
     * Position is described with float values between 0 and 1 which describe
     * the lower left position of the axis system (first two values), the width
     * and the height of the axes object.
     *
     * <p>
     * Defaults: 0.13F, 0.11F, 0.775F, 0.815F
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void setPosition(float x, float y, float width, float height) {
        position[0] = x;
        position[1] = y;
        position[2] = width;
        position[3] = height;
    }

    /**
     * Applies the dark theme to this axes by setting a black box background
     * and light gray axis rulers. The cursors of this axes are drawn in white
     * and the texts in the light gray of the axis rulers, which also applies
     * to cursors and texts that are added later. Lines and the surrounding
     * figure keep their colors. The change becomes visible with the next
     * rendering of the figure.
     *
     * <p>
     * LLM code generated with Claude Opus 5.5
     */
    public void setThemeDark() {
        box.setColor(THEME_DARK_BOX_BACKGROUND_COLOR);
        box.setLineColor(THEME_DARK_BOX_LINE_COLOR);
        xaxis.setColor(THEME_DARK_AXIS_RULER_COLOR);
        yaxis.setColor(THEME_DARK_AXIS_RULER_COLOR);
        themeDark = true;
        for (Cursor c : cursors) {
            c.setThemeDark();
        }
        for (Text t : texts) {
            t.setThemeDark();
        }
    }

    /**
     * Applies the light theme to this axes, which restores the default colors
     * of the box, the axis rulers, the cursors and the texts. This is used to
     * revert a previously applied dark theme.
     *
     * <p>
     * LLM code generated with Claude Opus 5.5
     */
    public void setThemeLight() {
        box.setColor(THEME_LIGHT_BOX_BACKGROUND_COLOR);
        box.setLineColor(THEME_LIGHT_BOX_LINE_COLOR);
        xaxis.setColor(THEME_LIGHT_AXIS_RULER_COLOR);
        yaxis.setColor(THEME_LIGHT_AXIS_RULER_COLOR);
        themeDark = false;
        for (Cursor c : cursors) {
            c.setThemeLight();
        }
        for (Text t : texts) {
            t.setThemeLight();
        }
    }

    public SubPlot getSubPlot() {
        return parentSubPlot;
    }

    public void setSubPlot(SubPlot parent) {
        this.parentSubPlot = parent;
    }

    /**
     * To identify if an axes object is part of the
     *
     * @param x
     * @param y
     * @return
     */
    public boolean containsPoint(int x, int y) {
        return x >= boxCoordinates[0] && x <= boxCoordinates[2]
                && y >= boxCoordinates[1] && y <= boxCoordinates[3];
    }

    /**
     * Zooms the axes by setting the limits to a box that is described by two
     * points, used for zooming when selecting a box rectangle.
     *
     * @param startX Screen coordinate (pixels)
     * @param startY Screen coordinate (pixels)
     * @param endX Screen coordinate (pixels)
     * @param endY Screen coordinate (pixels)
     */
    public void applyZoomBox(int startX, int startY, int endX, int endY) {
        float x1 = xaxis.getValueForCoordinate(startX);
        float x2 = xaxis.getValueForCoordinate(endX);
        float minX = Math.min(x1, x2);
        float maxX = Math.max(x1, x2);
        if (minX < maxX) {
            xLim(minX, maxX);
        }

        float y1 = yaxis.getValueForCoordinate(startY);
        float y2 = yaxis.getValueForCoordinate(endY);
        float minY = Math.min(y1, y2);
        float maxY = Math.max(y1, y2);
        if (minY < maxY) {
            yLim(minY, maxY);
        }
    }

    /**
     * Zooms the axes in around a given point.
     *
     * @param x Screen coordinate (pixels)
     * @param y Screen coordinate (pixels)
     * @param factor Zoom factor
     */
    public void applyZoomPoint(int x, int y, float factor) {
        float valX = xaxis.getValueForCoordinate(x);
        float valY = yaxis.getValueForCoordinate(y);
        float rangeX = (xaxis.lim[1] - xaxis.lim[0]) * factor;
        float rangeY = (yaxis.lim[1] - yaxis.lim[0]) * factor;

        float ratioX = (valX - xaxis.lim[0]) / (xaxis.lim[1] - xaxis.lim[0]);
        float ratioY = (valY - yaxis.lim[0]) / (yaxis.lim[1] - yaxis.lim[0]);

        float minX = valX - rangeX * ratioX;
        float maxX = valX + rangeX * (1 - ratioX);
        float minY = valY - rangeY * ratioY;
        float maxY = valY + rangeY * (1 - ratioY);

        if (minX < maxX) {
            xLim(minX, maxX);
        }
        if (minY < maxY) {
            yLim(minY, maxY);
        }
    }

    /**
     * Moves the drawn line plot by the given delta values by manipulating x and
     * y limits.
     *
     * @param dx
     * @param dy
     */
    public void applyPan(int dx, int dy) {
        float valDx = (float) dx * (xaxis.lim[1] - xaxis.lim[0])
                / (float) (xaxis.coordinates[1] - xaxis.coordinates[0]);
        float valY1 = yaxis.getValueForCoordinate(boxCoordinates[1]);
        float valY2 = yaxis.getValueForCoordinate(boxCoordinates[1] + dy);
        float valDy = valY2 - valY1;

        float minX = xaxis.lim[0] - valDx;
        float maxX = xaxis.lim[1] - valDx;
        float minY = yaxis.lim[0] - valDy;
        float maxY = yaxis.lim[1] - valDy;

        if (minX < maxX) {
            xLim(minX, maxX);
        }
        if (minY < maxY) {
            yLim(minY, maxY);
        }
    }
}

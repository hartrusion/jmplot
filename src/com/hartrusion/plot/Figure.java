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

/**
 * Describes the methods to handle axes objects inside an element. This is
 * implemented by objects that are the parent objects of the axes object,
 * representing the figure that holds the axes objects.
 *
 * @author Viktor Alexander Hartung
 */
public interface Figure {

    /**
     * Background color which is applied by setThemeLight if the current
     * background is too dark for the light theme. This is the dark gray
     * #808080 of the classic 16 color palette.
     */
    public static final Color THEME_LIGHT_BACKGROUND_COLOR
            = new Color(0x80, 0x80, 0x80);

    /**
     * Background color which is applied by setThemeDark if the current
     * background is too bright for the dark theme. This is the gray #444444
     * (color 238) of the 256 color palette.
     */
    public static final Color THEME_DARK_BACKGROUND_COLOR
            = new Color(0x44, 0x44, 0x44);

    /**
     * Perceived brightness on a scale from 0 to 255 below which a background
     * color is considered to be dark. The gray #808080 has exactly this value
     * and therefore counts as a light background.
     */
    public static final int DARK_BACKGROUND_BRIGHTNESS_LIMIT = 128;

    /**
     * Returns the background color of the figure. This method is already
     * implemented by java.awt.Component, which all figure implementations
     * extend, and is declared here so that the theme methods can use it.
     *
     * @return Background color, can be null if none is set or inherited.
     */
    public Color getBackground();

    /**
     * Sets the background color of the figure. This method is already
     * implemented by java.awt.Component, which all figure implementations
     * extend, and is declared here so that the theme methods can use it.
     *
     * @param c Background color
     */
    public void setBackground(Color c);

    /**
     * Requests a new painting of the figure. This method is already
     * implemented by java.awt.Component, which all figure implementations
     * extend, and is declared here so that XAxisLink can repaint the figures
     * of the axes whose limits it changed.
     */
    public void repaint();

    /**
     * Adapts the figure background to the dark theme. Since the background is
     * usually provided by the swing look and feel, it is only replaced by
     * THEME_DARK_BACKGROUND_COLOR if it is not already dark or if no
     * background is set at all, so that a dark look and feel keeps its own
     * background. The axes inside the figure are not affected and have to be
     * themed separately.
     *
     * <p>
     * LLM code generated with Claude Opus 5.5
     */
    public default void setThemeDark() {
        Color background = getBackground();
        if (background == null || !isColorDark(background)) {
            setBackground(THEME_DARK_BACKGROUND_COLOR);
        }
    }

    /**
     * Adapts the figure background to the light theme. The background is only
     * replaced by THEME_LIGHT_BACKGROUND_COLOR if it is currently dark or if
     * no background is set at all, so that a light look and feel keeps its
     * own background. The axes inside the figure are not affected and have to
     * be themed separately.
     *
     * <p>
     * LLM code generated with Claude Opus 5.5
     */
    public default void setThemeLight() {
        Color background = getBackground();
        if (background == null || isColorDark(background)) {
            setBackground(THEME_LIGHT_BACKGROUND_COLOR);
        }
    }

    /**
     * Checks whether a color is dark by calculating its perceived brightness
     * with the weighting of the ITU-R BT.601 luma, which accounts for the
     * different sensitivity of the eye to red, green and blue, and comparing
     * it with DARK_BACKGROUND_BRIGHTNESS_LIMIT.
     *
     * <p>
     * LLM code generated with Claude Opus 5.5
     *
     * @param c Color to check, must not be null.
     * @return true if the color is dark.
     */
    private static boolean isColorDark(Color c) {
        // Integer weights in per mille avoid float rounding, which would
        // otherwise let the gray #808080 fall slightly below the limit.
        int brightness = (299 * c.getRed()
                + 587 * c.getGreen()
                + 114 * c.getBlue()) / 1000;
        return brightness < DARK_BACKGROUND_BRIGHTNESS_LIMIT;
    }

    /**
     * Adds a new axes to this FigurePanel. A figure can display multiple axes
     * systems.
     *
     * @param a
     */
    public void addAxes(Axes a);
    
    /**
     * Adds a legend object that will be displayed on this Figure. A figure can
     * display multiple legends (or just one).
     * 
     * @param l Legend Object
     */
    public void addLegend(Legend l);
    
    /**
     * Returns the last legend that was added to this figure.
     * @return 
     */
    public Legend getLastLegend();

    /**
     * Add an instance of a subplot manager to be displayed on this figure. A
     * subplot manager is used to display multiple axes on one figure in a grid
     * layout that can be easily accessed and managed. However, it is not
     * necessary to use a subplot to have multiple axes displayed.
     * <p>
     * Remember that adding a subplot manager does not remove the default added
     * axes system so you might want to call clear on the figure.
     *
     * @param sp Instance of subplot manager
     */
    public void addSubPlot(SubPlot sp);

    /**
     * Returns the instance of the used subplot manager for this figure.
     *
     * @return instance or null.
     */
    public SubPlot getSubPlot();

    /**
     * Get the last axes that was used inside this Figure.
     *
     * @return Axes reference.
     */
    public Axes getLastAxes();

    public void clear();
}

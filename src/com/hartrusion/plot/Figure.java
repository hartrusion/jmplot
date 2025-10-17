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

/**
 * Describes the methods to handle axes objects inside an element. This is
 * implemented by objects that are the parent objects of the axes object,
 * representing the figure that holds the axes objects.
 *
 * @author Viktor Alexander Hartung
 */
public interface Figure {

    /**
     * Adds a new axes to this FigurePanel. A figure can display multiple axes
     * systems.
     *
     * @param a
     */
    public void addAxes(Axes a);

    /**
     * Add an instance of a subplot manager to be displayed on this figure. A
     * subplot manager is used to display multiple axes on one figure in a grid
     * layout that can be easily accessed and managed. However, it is not
     * necessary to use a subplot to have multiple axes displayed.
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
     * @return Axes referece.
     */
    public Axes getLastAxes();

    public void clear();
}

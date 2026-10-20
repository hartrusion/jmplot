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

/**
 * Common base class that describes a relation to X and Y values described by an
 * axis ruler object.
 *
 * @author Viktor Alexander Hartung
 */
public abstract class CoordinateObject {

    protected AxisRuler xaxis;
    protected AxisRuler yaxis;

    /**
     * Called from the axes object when adding the object to the axes. Creates
     * the link between axes and the object by making the rulers known to the
     * object.
     */
    @SuppressWarnings("NonPublicExported")
    public void initComponent(AxisRuler xaxis, AxisRuler yaxis) {
        this.xaxis = xaxis;
        this.yaxis = yaxis;
    }
    
    /**
     * Returns the currently assigned y axis ruler object to which this object
     * refers to.
     * 
     * @return YAxisRuler
     */
    public YAxisRuler getYAxis() {
        return (YAxisRuler) yaxis;
    }
}

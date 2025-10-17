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

import static com.hartrusion.plot.VisualizeData.plot;
import static com.hartrusion.plot.VisualizeData.xlabel;
import static com.hartrusion.plot.VisualizeData.ylabel;

/**
 *
 * @author Viktor Alexander Hartung
 */
public class ExamplePlot {
    public static void main(String[] args) {
        float[] xdata = new float[50];
        float[] ydata = new float[50];
        for (int idx = 0; idx < xdata.length; idx++) {
            xdata[idx] = 0.1F * (float) idx;
            ydata[idx] = (float) Math.sin((double) xdata[idx]);
        }

        plot(xdata, ydata);
        xlabel("X label");
        ylabel("Y label");
    }
}

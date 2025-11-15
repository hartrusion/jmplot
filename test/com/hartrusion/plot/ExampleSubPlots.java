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

import static com.hartrusion.plot.VisualizeData.*;

/**
 * Creates a 2x2 sub-plot layout and adds some lines to it.
 *
 * @author Viktor Alexander Hartung
 */
public class ExampleSubPlots {

    public static void main(String[] args) {
        float[] xdata = new float[]{0.1F, 0.2F, 0.8F};
        float[] ydata = new float[]{7F, 3F, 1F};
        float[] ydata2 = new float[]{12F, 8F, -3F};

        subplot(2,2,1);
        plot(xdata, ydata);
        xlabel("x label 1");
        ylabel("y label 1");
        hold("on");
        plot(xdata, ydata2);
        axis(0F, 1F, -5F, 15F);
        
        subplot(2,2,2);
        xlabel("x label 2");
        ylabel("y label 2");
        
        subplot(2,2,3);
        xlabel("x label 3");
        ylabel("y label 3");
        
        subplot(2,2,4);
        plot(xdata, ydata2);
        xlabel("x label 4");
        ylabel("y label 4");
    
    }
}

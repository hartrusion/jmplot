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

import static com.hartrusion.plot.VisualizeData.gca;
import static com.hartrusion.plot.VisualizeData.plot;
import static com.hartrusion.plot.VisualizeData.text;
import static com.hartrusion.plot.VisualizeData.xlabel;
import static com.hartrusion.plot.VisualizeData.ylabel;

/**
 * Creates a simple XY line plot and annotates it with text objects, including
 * rotated text.
 *
 * @author Viktor Alexander Hartung
 */
public class ExampleText {
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

        // Simple text on the current axes using the static method
        text(1.6F, 1.0F, "Maximum");

        // Rotated text objects, rotation is counterclockwise in degrees.
        Text t = new Text();
        t.setText("Rotated by 90°");
        t.setCoordinates(0.5F, -0.2F);
        t.setRotation(90.0);
        gca().addText(t);

        t = new Text();
        t.setText("Rotated by -45°");
        t.setCoordinates(3.3F, -0.1F);
        t.setRotation(-45.0);
        gca().addText(t);

        // Mark the anchor point of the last text with a cursor
        gca().setCursor(3.3F, -0.1F);
    }
}

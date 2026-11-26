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

import static com.hartrusion.plot.VisualizeData.*;

/**
 * Shows the cursor modes in three axes below each other, so that the
 * behaviour of clicks, the dragging of x cursors and the value boxes can be
 * checked by hand. The upper axes uses POINT_CURSOR, the middle one X_CURSOR
 * with a line that has a marker symbol and one without, and the lower one
 * X_CURSOR_DUAL with one million points per line to check that a dragged x
 * cursor follows the mouse without delay. The program argument "dark"
 * applies the dark theme to all axes.
 *
 * @author Viktor Alexander Hartung
 */
public class ExampleCursorModes {

    public static void main(String[] args) {
        boolean dark = args.length > 0 && args[0].equals("dark");

        float[] x = new float[200];
        float[] sine = new float[x.length];
        float[] cosine = new float[x.length];
        for (int idx = 0; idx < x.length; idx++) {
            x[idx] = 0.05F * (float) idx;
            sine[idx] = (float) Math.sin((double) x[idx]);
            cosine[idx] = 3F * (float) Math.cos((double) x[idx]);
        }

        subplot(1, 3, 1);
        plot(x, sine);
        hold("on");
        plot(x, cosine);
        ylabel("POINT_CURSOR");
        gca().setCursorMode(CursorMode.POINT_CURSOR);
        applyTheme(dark);

        float[] xCoarse = new float[25];
        float[] sineCoarse = new float[xCoarse.length];
        for (int idx = 0; idx < xCoarse.length; idx++) {
            xCoarse[idx] = 0.4F * (float) idx;
            sineCoarse[idx] = (float) Math.sin((double) xCoarse[idx]);
        }
        subplot(1, 3, 2);
        plot(xCoarse, sineCoarse);
        hold("on");
        plot(x, cosine);
        gca().getLine(1).setMarker('o');
        ylabel("X_CURSOR");
        gca().setCursorMode(CursorMode.X_CURSOR);
        applyTheme(dark);

        float[] xLarge = new float[1_000_000];
        float[] noisy = new float[xLarge.length];
        float[] ramp = new float[xLarge.length];
        for (int idx = 0; idx < xLarge.length; idx++) {
            xLarge[idx] = 1e-5F * (float) idx;
            noisy[idx] = (float) (Math.sin((double) xLarge[idx])
                    + 0.1 * Math.random());
            ramp[idx] = 0.1F * xLarge[idx] - 0.5F;
        }
        subplot(1, 3, 3);
        plot(xLarge, noisy);
        hold("on");
        plot(xLarge, ramp);
        xlabel("Time (s)");
        ylabel("X_CURSOR_DUAL");
        gca().setCursorMode(CursorMode.X_CURSOR_DUAL);
        applyTheme(dark);
    }

    private static void applyTheme(boolean dark) {
        if (dark) {
            theme("dark");
        }
    }
}

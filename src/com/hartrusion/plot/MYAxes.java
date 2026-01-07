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
import java.util.ArrayList;
import java.util.List;

/**
 * Holds multiple y-axes (more than 2) with the first axes on the right, second
 * on the left and more axes, beginning with 3, on the left side left of the
 * first y axes.
 */
public class MYAxes extends YYAxes {

    /**
     * Holds the third axes on index 0 and more additional axes, gets
     * initialized by the constructor with one additional axes
     */
    private final List<YAxisRuler> myaxes = new ArrayList<>();

    /**
     * Additional space between added Y Axes rulers on the left side.
     */
    private float addYSpacing = 0.12F;

    /**
     * Describes the position of the axes construct as if the outer left axes
     * would be the outside of the box. The visible box will be smaller but
     * aligned to the right.
     */
    private final float[] outerPosition
            = new float[]{0.13F, 0.11F, 0.775F, 0.815F};

    public MYAxes() {
        // at least one additional axes will be added when creating the
        // instance, more will be added later if needed.
        YAxisRuler thirdAxis = new YAxisRuler();
        thirdAxis.setColor(new Color(255, 0, 0));
        myaxes.add(thirdAxis);
    }

    /**
     * Each time a target axes nr is called it will be checked if it is existing
     * and if not it will be created.
     *
     * @param target
     */
    private void checkAndCreateAxes(int target) {
        if (target > myaxes.size() + 2) {
            for (int idx = myaxes.size(); idx <= target - 3; idx++) {
                YAxisRuler addAxes = new YAxisRuler();
                // assign default colors
                switch (idx) {
                    case 2: // 4th axes (3rd on the left) dark yellow
                        addAxes.setColor(new Color(128, 128, 0));
                        break;
                    case 3: // 5th axes, violet
                        addAxes.setColor(new Color(128, 0, 128));
                        break;
                    default:
                        break;
                }
                myaxes.add(addAxes);
            }
        }
    }

    @Override
    public void addLine(int target, Line l) {
        if (target <= 2) {
            super.addLine(target, l);
            return;
        }
        // Check if the axes nr does exist, if not, create all of them
        checkAndCreateAxes(target);
        lines.add(l);
        if (l.getLineColor() == null) {
            switch (target) {
                case 3:
                    l.setLineColor(new Color(255, 0, 0));
                    break;
                case 4: // 4th axes (3rd on the left) dark yellow
                    l.setLineColor(new Color(128, 128, 0));
                    break;
                case 5: // 5th axes, violet
                    l.setLineColor(new Color(128, 0, 128));
                    break;
                default:
                    break;
            }
        }
        l.initComponent(xaxis, myaxes.get(target - 3));
        VisualizeData.setCurrentAxes(this);
    }

    @Override
    public void yLim(int target, float y1, float y2) {
        if (target <= 2) {
            super.yLim(target, y1, y2);
            return;
        }
        checkAndCreateAxes(target);
        myaxes.get(target - 3).setLim(y1, y2);
        myaxes.get(target - 3).setTicks(y1, (y2 - y1) / 10F, y2);
    }

    @Override
    public void ylabel(int target, String s) {
        if (target <= 2) {
            super.ylabel(target, s);
            return;
        }
        checkAndCreateAxes(target);
        myaxes.get(target - 3).setLabel(s);
    }

    @Override
    public void autoY() {
        super.autoY();

    }

    @Override
    public void setPosition(float[] position) {
        // Redirect this to other variable:
        System.arraycopy(position, 0, this.outerPosition, 0, position.length);
        setSuperPosition();
    }

    @Override
    public void setPosition(float x, float y, float width, float height) {
        // Redirect this to other variable
        outerPosition[0] = x;
        outerPosition[1] = y;
        outerPosition[2] = width;
        outerPosition[3] = height;

        setSuperPosition();
    }

    /**
     * Updates the parent position variable accordingly depending on how many y
     * axes are being added to the left and how they are spaced.
     */
    private void setSuperPosition() {
        float xDiff = addYSpacing * (float) myaxes.size();
        super.setPosition(outerPosition[0] + xDiff,
                outerPosition[1],
                outerPosition[2] - xDiff,
                outerPosition[3]);
    }

    @Override
    public void awtPaintComponents(Graphics g,
            float parentWidth, float parentHeight) {
        setSuperPosition(); // Manipulate the box coordinates first,
        // and paint all the super stuff afterwards.
        super.awtPaintComponents(g, parentWidth, parentHeight);
        // Prepare the additional Y axes by supplying the coordinates.
        for (int idx = 0; idx < myaxes.size(); idx++) {
            // This has to be known to get the correct scaling of the lines
            // that are assigned to the axes.
            myaxes.get(idx).setCoordinates(
                    boxCoordinates[3], boxCoordinates[1]);
            // Calculate the X position (pixels) where this axes will be 
            // placed, can only be done after X axes has been drawn.
            myaxes.get(idx).setPlacement(xaxis.getCoordinateLineStart()
                    - (int) (parentWidth * (addYSpacing * (float) (idx + 1))));
            if (myaxes.get(idx).isVisible()) {
                myaxes.get(idx).awtPaintComponents(g);
            }
        }
        // Plot all lines assigned to the additional axes
        for (Line l : lines) {
            if (!myaxes.contains(l.getYAxis())) {
                continue; // skip lines from other axes
            }
            l.awtPaintComponents(g);
        }
    }
}

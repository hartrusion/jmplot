package com.hartrusion.plot;

import java.awt.Color;
import java.awt.Graphics;

/**
 * An extension of the default X/Y axes system featuring two Y, each on one
 * side. The line can be added to the a
 */
public class YYAxes extends Axes {

    private final YAxisRuler secondaryYaxis = new YAxisRuler();

    public YYAxes() {
        secondaryYaxis.setLocation("right");
        secondaryYaxis.setColor(new Color(0, 127, 0));
        yaxis.setColor(new Color (0,0,255));
    }

    /**
     * Adds a line object to the specified axes number.
     * 
     * @param l
     * @param axes Axes number, with 1 being the left primary axes and 2
     *             being the right secondary axes.
     */
    public void addLine(int axes, Line l) {
        if (!hold) {
            lines.clear();
        }
        if (axes == 2) {
            // Mostly the same as the super method but line gets assigned with
            // the secondary y-axis.
            lines.add(l);
            if (l.getLineColor() == null) {
                l.setLineColor(new Color(0, 127, 0));
            }
            l.initComponent(xaxis, secondaryYaxis);
            VisualizeData.setCurrentAxes(this);
        } else if (axes == 1) {
            super.addLine(l);
        } else {
            throw new IllegalArgumentException("Invalid axes number.");
        }
    }

    public void yLim(int target, float y1, float y2) {
        if (target == 2) {
            secondaryYaxis.setLim(y1, y2);
            secondaryYaxis.setTicks(y1, (y2 - y1) / 10F, y2);
        } else if (target == 1) {
            super.yLim(y1, y2);
        } else {
            throw new IllegalArgumentException("Invalid axes number.");
        }
    }

    /**
     * Sets the y Label for axes systems with more than 1 y axes. 
     * @param target Axes number, with 1 being the primary (left), 2 being
     * the secondary (right). 3 and more are added on the left side left to 
     * the primary axes.
     * @param s String to write.
     */
    public void ylabel(int target, String s) {
        if (target == 2) {
            secondaryYaxis.setLabel(s);
        } else if (target == 1) {
            super.ylabel(s);
        } else {
            throw new IllegalArgumentException("Invalid axes number.");
        }
    }

    @Override
    public void autoY() {
        super.autoY();
        this.autoY(2);
    }

    /**
     * Autoscale the given y-Axes.
     * 
     * @param target Axes nr (1: primary left, 2: secondary right, 3 and more
     * are additional axes on the left).
     */
    public void autoY(int target) {
        if (target == 1) {
            super.autoY();
        } else if (target == 2) {
            boolean valueFound = false;
            float yMax = Float.MIN_VALUE;
            float yMin = Float.MAX_VALUE;
            for (Line l : lines) {
                if (l.getYAxis() != secondaryYaxis) {
                    continue; // This line is not assigned to the secondary Y
                }
                if (l.getYMin() == Float.MAX_VALUE) {
                    continue; // probably just an array of NaN.
                }
                if (l.getYMin() == Float.MIN_VALUE) {
                    continue; // probably just an array of NaN.
                }
                if (l.getYMin() < yMin) {
                    yMin = l.getYMin();
                }
                if (l.getYMax() > yMax) {
                    yMax = l.getYMax();
                }
                valueFound = true;
            }
            // Having identical values: Place line in the middle by padding with 1
            if (yMin == yMax) {
                yMin -= 1.0F;
                yMax += 1.0F;
            }
            if (valueFound) {
                yLim(2, yMin, yMax);
            }
        }
    }

    @Override
    public void awtPaintComponents(Graphics g,
            float parentWidth, float parentHeight) {
        super.awtPaintComponents(g, parentWidth, parentHeight);

        // Paint the additional axes afterward. Lines were already drawn in
        // the super call.
        secondaryYaxis.setCoordinates(boxCoordinates[3], boxCoordinates[1]);
        secondaryYaxis.updatePlacement(xaxis);
        if (secondaryYaxis.isVisible()) {
            secondaryYaxis.awtPaintComponents(g);
        }

        // Plot lines assigned to the secondary y axes
        for (Line l : lines) {
            if (l.getYAxis() != secondaryYaxis) {
                continue; // skip foreign lines (only for extensions of Axes)
            }
            l.awtPaintComponents(g);
        }
    }

}

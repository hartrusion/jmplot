package com.hartrusion.plot;

import java.awt.Color;
import java.awt.Graphics;

/**
 * An extension of the default X/Y axes system featuring two Y, each on one
 * side. The line can be added to the a
 */
public class YYAxes extends Axes {

    /**
     * Color of the primary (left) y axis ruler, which matches the default
     * color of the first line so that the line can be related to its axis.
     * This color is kept regardless of the applied theme.
     */
    private static final Color PRIMARY_Y_AXIS_COLOR = new Color(0, 0, 255);

    private final YAxisRuler secondaryYaxis = new YAxisRuler();

    public YYAxes() {
        secondaryYaxis.setLocation("right");
        secondaryYaxis.setColor(new Color(0, 127, 0));
        yaxis.setColor(PRIMARY_Y_AXIS_COLOR);
    }

    /**
     * Adds a line object to the specified axes number.
     *
     * @param l
     * @param axes Axes number, with 1 being the left primary axes and 2 being
     * the right secondary axes.
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
     *
     * @param target Axes number, with 1 being the primary (left), 2 being the
     * secondary (right). 3 and more are added on the left side left to the
     * primary axes.
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

    /**
     * Applies the dark theme like the parent class does, but restores the
     * color of the primary y axis afterwards because the y axis rulers of this
     * axes are colored to identify their assigned lines. The secondary y axis
     * is not touched by the parent class and keeps its color as well.
     *
     * <p>
     * LLM code generated with Claude Opus 5.5
     */
    @Override
    public void setThemeDark() {
        super.setThemeDark();
        yaxis.setColor(PRIMARY_Y_AXIS_COLOR);
    }

    /**
     * Applies the light theme like the parent class does, but restores the
     * color of the primary y axis afterwards for the same reason as described
     * in setThemeDark.
     *
     * <p>
     * LLM code generated with Claude Opus 5.5
     */
    @Override
    public void setThemeLight() {
        super.setThemeLight();
        yaxis.setColor(PRIMARY_Y_AXIS_COLOR);
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
            float[] limits = calculateAutoYLimits(secondaryYaxis);
            if (limits != null) {
                yLim(2, limits[0], limits[1]);
            }
        }
    }

    @Override
    public void paintContent(Graphics g,
            float parentWidth, float parentHeight) {
        super.paintContent(g, parentWidth, parentHeight);

        // Paint the additional axes afterward. Lines were already drawn in
        // the super call.
        secondaryYaxis.setCoordinates(boxCoordinates[3], boxCoordinates[1]);
        secondaryYaxis.updatePlacement(xaxis);
        if (secondaryYaxis.isVisible()) {
            secondaryYaxis.paintContent(g);
        }

        // Plot lines assigned to the secondary y axes
        for (Line l : lines) {
            if (l.getYAxis() != secondaryYaxis) {
                continue; // skip foreign lines (only for extensions of Axes)
            }
            l.paintContent(g);
        }
    }

    @Override
    public void applyZoomBox(int startX, int startY, int endX, int endY) {
        super.applyZoomBox(startX, startY, endX, endY);
        float y1 = secondaryYaxis.getValueForCoordinate(startY);
        float y2 = secondaryYaxis.getValueForCoordinate(endY);
        float minY = Math.min(y1, y2);
        float maxY = Math.max(y1, y2);
        if (minY < maxY) {
            yLim(2, minY, maxY);
        }
    }

    @Override
    public void applyZoomPoint(int x, int y, float factor) {
        super.applyZoomPoint(x, y, factor);
        float valY = secondaryYaxis.getValueForCoordinate(y);
        float rangeY = (secondaryYaxis.lim[1] - secondaryYaxis.lim[0]) * factor;
        float ratioY = (valY - secondaryYaxis.lim[0]) / (secondaryYaxis.lim[1] - secondaryYaxis.lim[0]);
        float minY = valY - rangeY * ratioY;
        float maxY = valY + rangeY * (1 - ratioY);
        if (minY < maxY) {
            yLim(2, minY, maxY);
        }
    }

    @Override
    public void applyPan(int dx, int dy) {
        super.applyPan(dx, dy);
        float valY1 = secondaryYaxis.getValueForCoordinate(boxCoordinates[1]);
        float valY2 = secondaryYaxis.getValueForCoordinate(boxCoordinates[1] + dy);
        float valDy = valY2 - valY1;
        float minY = secondaryYaxis.lim[0] - valDy;
        float maxY = secondaryYaxis.lim[1] - valDy;
        if (minY < maxY) {
            yLim(2, minY, maxY);
        }
    }

}

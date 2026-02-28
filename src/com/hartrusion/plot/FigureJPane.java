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

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.beans.BeanProperty;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;

/**
 * A Swing JPanel class that is used to hold multiple axes objects. It can hold
 * manually added axes (the simplest version is one axes here) or a set of axes
 * for a subplot arrangement.
 * <p>
 * Contrary to the matlab figure, this extends a panel and can therefore be used
 * as a drawing area on any GUI. If you use NetBeans GUI builder, just drag and
 * drop the class onto your GUI.
 *
 * @author Viktor Alexander Hartung
 */
public class FigureJPane extends JComponent implements Figure {

    /**
     * A list containing all axes that are included in this figure panel.
     */
    private final List<Axes> axes = new ArrayList<>();

    /**
     * Reference to a subplot manager, holding some information about the axes
     * placements and so on.
     */
    private SubPlot subPlot;

    private int yRulers = 1;

    private int[] subplotLayout = {0, 0};

    private Rectangle selectionRect = null;
    private Point leftDragStart = null;
    private Point rightDragStart = null;

    private Axes activeAxes = null;

    public FigureJPane() {
        axes.add(new Axes()); // construct the default axes

        // Add a mouse adapter for interacting with the mouse.
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                activeAxes = getAxes(e.getX(), e.getY());
                if (activeAxes == null) {
                    return;
                }

                // left mouse button: zoom rectangle start
                // right mouse button: start dragging from this point
                if (e.getButton() == MouseEvent.BUTTON1) {
                    // Linke Taste => Zoom-Rechteck Start
                    leftDragStart = e.getPoint();
                    selectionRect = new Rectangle(e.getX(), e.getY(), 0, 0);
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    rightDragStart = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (activeAxes == null) {
                    leftDragStart = null;
                    rightDragStart = null;
                    selectionRect = null;
                    return;
                }

                if (e.getButton() == MouseEvent.BUTTON1) {
                    // Apply the zoom for the selected rectange on mouse release
                    if (selectionRect != null 
                            && selectionRect.width > 5 
                            && selectionRect.height > 5) {
                        activeAxes.applyZoomBox(
                                selectionRect.x,
                                selectionRect.y,
                                selectionRect.x + selectionRect.width,
                                selectionRect.y + selectionRect.height
                        );
                    }
                    selectionRect = null;
                    leftDragStart = null;
                    repaint();
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    // stop dragging
                    rightDragStart = null;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (activeAxes == null) {
                    return;
                }

                // Linke Taste gedrückt? => Rechteck ziehen
                if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) 
                        != 0 && leftDragStart != null) {
                    // Limit the zoom rectange to the axes box so no zoom is 
                    // possible outside the axes object. This might be nice to 
                    // have but feels weird as it is not visible yet what is 
                    // there to zoom into. In such cases, the lines should
                    // be dragged first.
                    int x1 = Math.max(activeAxes.boxCoordinates[0], 
                            Math.min(leftDragStart.x, e.getX()));
                    int y1 = Math.max(activeAxes.boxCoordinates[1], 
                            Math.min(leftDragStart.y, e.getY()));
                    int x2 = Math.min(activeAxes.boxCoordinates[2], 
                            Math.max(leftDragStart.x, e.getX()));
                    int y2 = Math.min(activeAxes.boxCoordinates[3], 
                            Math.max(leftDragStart.y, e.getY()));

                    selectionRect = new Rectangle(x1, y1, x2 - x1, y2 - y1);
                    repaint();
                    return;
                }

                // Pan with right mouse
                if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0 
                        && rightDragStart != null) {
                    int dx = e.getX() - rightDragStart.x;
                    int dy = e.getY() - rightDragStart.y;

                    activeAxes.applyPan(dx, dy);
                    rightDragStart = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                Axes targetAxes = getAxes(e.getX(), e.getY());
                if (targetAxes == null) {
                    return;
                }
                // Use mouse wheel to zoom, generate a factor depending 
                // on the rotation direction and apply it to a point zoom.
                float factor = e.getWheelRotation() < 0 ? 0.8f : 1.25f;
                targetAxes.applyZoomPoint(e.getX(), e.getY(), factor);
                repaint();
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
    }

    @Override
    public void addAxes(Axes a) {
        axes.add(a);
    }

    @Override
    public Axes getLastAxes() {
        if (axes.isEmpty()) {
            return null;
        }
        return axes.get(0);
    }

    @Override
    public void addSubPlot(SubPlot sp) {
        subPlot = sp;
    }

    @Override
    public SubPlot getSubPlot() {
        return subPlot;
    }

    @Override
    public void clear() {
        subPlot = null;
        axes.clear();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        for (Axes a : axes) {
            a.awtPaintComponents(
                    g, (float) getWidth() - 1, (float) getHeight() - 1);
        }
        if (subPlot != null) {
            Iterator<Axes> axIterator = subPlot.getAxesIterator();
            while (axIterator.hasNext()) {
                Axes a = axIterator.next();
                a.awtPaintComponents(
                        g, (float) getWidth() - 1, (float) getHeight() - 1);
            }
        }
        if (selectionRect != null) {
            g.setColor(new Color(0, 120, 215)); // Z.B. klassisches Explorer Blau
            g.drawRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
        }
    }

    public int getYRulers() {
        return yRulers;
    }

    @BeanProperty(preferred = true, visualUpdate = true, description
            = "Number of Y Rulers. 0 for no main plot.")
    public void setYRulers(int yRulers) {
        if (yRulers < 0) {
            throw new IllegalArgumentException("Illegal value.");
        }
        int old = this.yRulers;
        if (old != yRulers) {
            axes.clear();
            switch (yRulers) {
                case 0:
                    break; // nothing and no default
                case 1:
                    axes.add(new Axes());
                    break;
                case 2:
                    axes.add(new YYAxes());
                    break;
                default:
                    axes.add(new MYAxes());
                    break;
            }
            this.yRulers = yRulers;
            firePropertyChange("yRulers", old, yRulers);
        }
    }

    public int[] getSubplotLayout() {
        return subplotLayout;
    }

    @BeanProperty(preferred = true, visualUpdate = true, description
            = "Subplot Layout")
    public void setSubplotLayout(int[] subplotLayout) {
        if (subplotLayout.length != 2) {
            throw new IllegalArgumentException("Must be array with 2 values.");
        }
        if (subplotLayout[0] < 0 || subplotLayout[1] < 0) {
            throw new IllegalArgumentException("Values must not be negative.");
        }
        if (!java.util.Arrays.equals(this.subplotLayout, subplotLayout)) {

            int[] old = new int[2]; // remember previous
            System.arraycopy(this.subplotLayout, 0, old, 0, 2);
            this.subplotLayout = subplotLayout;

            // Apply: Create new subplot
            subPlot = null; // dump
            SubPlot sp = new SubPlot();
            sp.initAxes(subplotLayout[0], subplotLayout[1]);
            addSubPlot(sp);

            firePropertyChange("subplotLayout", old, subplotLayout);
        }
    }

    public float[] getSubplotPosition() {
        if (subPlot == null) {
            return null;
        }
        return subPlot.getAxesPositions();
    }

    @BeanProperty(preferred = true, visualUpdate = true, description
            = "Subplot position values")
    public void setSubplotPosition(float[] subplotPosition) {
        if (subplotPosition.length != 4) {
            throw new IllegalArgumentException("Must be array with 4 values.");
        }
        if (subplotPosition[2] < 0 || subplotPosition[3] < 0) {
            throw new IllegalArgumentException("Size values must be positive.");
        }
        if (subPlot != null) {
            float[] old = new float[4]; // remember previous
            System.arraycopy(subPlot.getAxesPositions(), 0, old, 0, 2);
            subPlot.setAxesPositions(subplotPosition);
            firePropertyChange("subplotPosition", old, subplotPosition);
        }
    }

    /**
     * Get the axes at the specified coordinates.
     *
     * @param x
     * @param y
     * @return Axes object or null, if nothing exists at the given coodrinates.
     */
    private Axes getAxes(int x, int y) {
        for (Axes a : axes) {
            if (a.containsPoint(x, y)) {
                return a;
            }
        }
        if (subPlot != null) {
            Iterator<Axes> it = subPlot.getAxesIterator();
            while (it.hasNext()) {
                Axes a = it.next();
                if (a.containsPoint(x, y)) {
                    return a;
                }
            }
        }
        return null;
    }
}

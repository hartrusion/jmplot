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
 * Forbidden class which allows to use static methods for quick access to plot
 * functionality. This provides matlab like code style. Do not use if you know
 * how to do things. It provides some super shady thing that is called "global
 * variables" in other languages and this is not a way things should be done.
 * But, for very short time coding issues, this allows easy plotting from
 * everywhere and handles the ui thread stuff and so on. I personally use it for
 * trying short code snippets and simply visualize the output once. To use,
 * simply make an import of all static methods wherever you want to use it:
 * <p>
 * import static com.hartrusion.plot.VisualizeData.*;
 * <p>
 * Matlab provides convenient ways of writing scripts for analyzing numeric math
 * problems where you do not have to mess around on creating windows and objects
 * to plot data. A call of the "plot" function simply opens a new windows and
 * displays data that was provided within the functions arguments. This class
 * provides such ways for this plot package. If you want just to have a quick
 * visualization on your data for development or debugging purposes, it is fine
 * to use those things here as you would also use System.out.println() to
 * display things you want to know on console output. However, when releasing a
 * java application, you will (hopefully) use a logger to log such things
 * instead of sysout. The same goes for this class here, it should never, under
 * no circumstances, be used for application releases. Use a clean, organized
 * way and organize your panels, axes, threads and so on in a proper thread safe
 * manner. There is no way to safely organize which axes goes to which panel and
 * which panel is used in which window using the commands provided in this
 * class. Do not copy and paste matlab code. And by the way, the same goes for
 * matlab. If you do large projects, you will come to the point where you do not
 * just call plot, instead, you will organize the windows and axes in a proper
 * manner using handles, which are basically references.
 * <p>
 * There are some restrictions and rules on how this works: Each time a new axes
 * gets created, it registers itself here as the last created axes system.
 * Calling the plot function will either use this one or it will create a new
 * axes system in a new FigureJFrame containing a FramePanel. As long as the
 * functions of this class are not called, they must not interfere with any
 * other parts of your program.
 *
 * @author Viktor Alexander Hartung
 */
public final class VisualizeData {

    private VisualizeData() {// prevent instance
    }

    private static Axes currentAxes;
    private static Figure currentFigure;

    public static void setCurrentAxes(Axes a) {
        currentAxes = a;
    }

    public static Axes gca() {
        return currentAxes;
    }

    public static void setCurrentFigure(FigurePane fp) {
        currentFigure = fp;
    }

    public static Figure gcf() {
        return currentFigure;
    }

    public static void plot(float[] xdata, float[] ydata) {
        if (xdata.length != ydata.length) {
            throw new IllegalArgumentException("Length mismatch");
        }
        // weather we use an already existing plot or create a new one or use
        // a still existing window totally depends on what is already existing.
        // This is the desired behaviour of this command.
        if (currentAxes == null) {
            if (currentFigure == null) {
                // if there is no axes known, create one in a new window.
                FigureJFrame root = new FigureJFrame();
                root.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent evt) {
                        evt.getWindow().dispose(); // kill window
                    }
                });
                currentFigure = root.getFigure(); // set as figure
                java.awt.EventQueue.invokeLater(() -> {
                    root.setVisible(true);
                });
            }
            // we might have a currentFigure or just created one - try to get
            // the currentAxes.
            currentAxes = currentFigure.getLastAxes();
            if (currentAxes == null) { // if it's not there,
                currentAxes = new Axes(); // create and add it
                currentFigure.addAxes(currentAxes);
            }
        }
        // now we have a currentAxes as the axes registered itself with
        // the static method. we can now add a new line, add the data to the
        // line and add it to the axes.
        Line l = new Line();
        l.setData(xdata, ydata);
        currentAxes.addLine(l);
    }

    public static void plot(double[] xdata, double[] ydata) {
        if (xdata.length != ydata.length) {
            throw new IllegalArgumentException("Length mismatch");
        }
        float[] x = new float[xdata.length];
        float[] y = new float[ydata.length];
        for (int idx = 0; idx < xdata.length; idx++) {
            x[idx] = (float) xdata[idx];
            y[idx] = (float) ydata[idx];
        }
        plot(x, y);
    }

    public static void plotyy(float[] x1data, float[] y1data, 
        float[] x2data, float[] y2data) {
        if (x1data.length != y1data.length) {
            throw new IllegalArgumentException("Length mismatch");
        }
        if (x2data.length != y2data.length) {
            throw new IllegalArgumentException("Length mismatch");
        }
        // This is almost the same as the normal plot command but here we also
        // need to check if the axes system is of the correct type. Only re-use
        // a proper YYAxes instead of an axes system.
        if (currentAxes == null || !(currentAxes instanceof YYAxes)) {
            if (currentFigure == null) { // is there even an active figure?
                // if there is no axes known, create one in a new window.
                FigureJFrame root = new FigureJFrame();
                root.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent evt) {
                        evt.getWindow().dispose(); // kill window
                    }
                });
                currentFigure = root.getFigure(); // set as figure
                java.awt.EventQueue.invokeLater(() -> {
                    root.setVisible(true);
                });
            }
            // we might have a currentFigure or just created one - try to get
            // the currentAxes.
            currentAxes = currentFigure.getLastAxes();
            if (!(currentAxes instanceof YYAxes)) {
                currentAxes = null; // this is an old axes of wrong type.
                currentFigure.clear();
            }
            if (currentAxes == null) { // if it's not there,
                currentAxes = new YYAxes(); // create the YYAxes
                currentFigure.addAxes(currentAxes);
            }
        }
        YYAxes yyAxes = (YYAxes) currentAxes; // get access to YY methods
        boolean prevHold = yyAxes.getHold();
        if (!prevHold) {
            yyAxes.lines.clear();
        }
        yyAxes.setHold(true); // hold has to be true during 2nd add.
        Line l = new Line();
        l.setData(x1data, y1data);
        yyAxes.addLine(1, l);
        l = new Line();
        l.setData(x2data, y2data);
        yyAxes.addLine(2, l);
        if (!prevHold) {
            yyAxes.autoX();
            yyAxes.autoY();
        }
        yyAxes.setHold(prevHold);
    }

    public static void plot3y(float[] x1data, float[] y1data, 
    float[] x2data, float[] y2data, float[] x3data, float[] y3data) {
    if (x1data.length != y1data.length) {
        throw new IllegalArgumentException("Length mismatch");
    }
    if (x2data.length != y2data.length) {
        throw new IllegalArgumentException("Length mismatch");
    }
    if (x3data.length != y3data.length) {
        throw new IllegalArgumentException("Length mismatch");
    }
    // This is almost the same as the normal plot command but here we also
    // need to check if the axes system is of the correct type. Only re-use
    // a proper MYAxes instead of an axes system.
    if (currentAxes == null || !(currentAxes instanceof MYAxes)) {
        if (currentFigure == null) { // is there even an active figure?
            // if there is no axes known, create one in a new window.
            FigureJFrame root = new FigureJFrame();
            root.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    evt.getWindow().dispose(); // kill window
                }
            });
            currentFigure = root.getFigure(); // set as figure
            java.awt.EventQueue.invokeLater(() -> {
                root.setVisible(true);
            });
        }
        // we might have a currentFigure or just created one - try to get
        // the currentAxes.
        currentAxes = currentFigure.getLastAxes();
        if (!(currentAxes instanceof MYAxes)) {
            currentAxes = null; // this is an old axes of wrong type.
            currentFigure.clear();
        }
        if (currentAxes == null) { // if it's not there,
            currentAxes = new MYAxes(); // create the YYAxes
            currentFigure.addAxes(currentAxes);
        }
    }
    MYAxes myAxes = (MYAxes) currentAxes; // get access to MY methods
    boolean prevHold = myAxes.getHold();
    if (!prevHold) {
        myAxes.lines.clear();
    }
    myAxes.setHold(true); // hold has to be true while adding lines
    Line l = new Line();
    l.setData(x1data, y1data);
    myAxes.addLine(1, l);
    l = new Line();
    l.setData(x2data, y2data);
    myAxes.addLine(2, l);
    l = new Line();
    l.setData(x3data, y3data);
    myAxes.addLine(3, l);
    if (!prevHold) {
        myAxes.autoX();
        myAxes.autoY();
    }
    myAxes.setHold(prevHold);
}

    /**
     * Adds a description label to the x-axis of the current axes. Using "null"
     * as an argument will remove the label.
     *
     * @param s
     */
    public static void xlabel(String s) {
        if (currentAxes != null) {
            currentAxes.xlabel(s);
        }
    }

    /**
     * Adds a description label to the y-axis of the current axes. Using "null"
     * as an argument will remove the label.
     *
     * @param s
     */
    public static void ylabel(String s) {
        if (currentAxes != null) {
            currentAxes.ylabel(s);
        }
    }

    public static void ylabel(int target, String s) {
        if (currentAxes != null) {
            if (currentAxes instanceof YYAxes) {
                ((YYAxes) currentAxes).ylabel(target, s);
            } else if (target != 1) {
                throw new IllegalArgumentException("Invalid target.");
            } else { // target "1" is valid for standard Axes class.
                currentAxes.ylabel(s);
            }
        }
    }

    /**
     * Sets the limit for the current axis. Requires a current axes system to be
     * present.
     *
     * @param x1 Lower x limit
     * @param x2 Upper x limit
     * @param y1 Lower y limit
     * @param y2 Upper x limit
     */
    public static void axis(float x1, float x2, float y1, float y2) {
        if (currentAxes == null) {
            return;
        }
        currentAxes.xLim(x1, x2);
        currentAxes.yLim(y1, y2);
    }

    /**
     * Sets the limit for the current axis. Requires a current axes system to be
     * present.
     *
     * <p>
     * Allows the call of axis which has float arguments using doubles so you
     * don't need to write the "F" at the end of the number. Can be more
     * convenient. I think it's okay to let a machine do this if it's our
     * desire.
     *
     * @param x1 Lower x limit
     * @param x2 Upper x limit
     * @param y1 Lower y limit
     * @param y2 Upper x limit
     */
    public static void axis(double x1, double x2, double y1, double y2) {
        axis((float) x1, (float) x2, (float) y1, (float) y2);
    }

    /**
     * Shortcut for command "hold on". Has to be used as hold("on"), which is
     * also possible in matlab.
     *
     * @param state "on" or "off"
     */
    public static void hold(String state) {
        if (currentAxes == null) {
            return;
        }
        if (state.equals("on")) {
            currentAxes.setHold(true);
        } else if (state.equals("off")) {
            currentAxes.setHold(false);
        }
    }

    /**
     *
     * @param sizeX
     * @param sizeY
     * @param number
     */
    public static void subplot(int sizeX, int sizeY, int number) {
        SubPlot sp = null;
        if (currentFigure == null) {
            // if there is no figure known, create a new one.
            FigureJFrame root = new FigureJFrame();
            root.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    evt.getWindow().dispose(); // kill window
                }
            });
            currentFigure = root.getFigure(); // set as figure
            java.awt.EventQueue.invokeLater(() -> {
                root.setVisible(true);
            });
        }
        // try to get the instance of the last used subplot, first, from axes:
        if (currentAxes != null) {
            if (currentAxes.getSubPlot() == null) {
                currentFigure.clear();
            } else {
                sp = currentAxes.getSubPlot();
            }
        }
        // if not found, try to get it from current figure:
        if (sp == null) {
            sp = currentFigure.getSubPlot();
        }
        if (sp != null) {
            ; // todo: check dimensions, remove and renew if different
        }
        if (sp == null) {
            sp = new SubPlot();
            sp.initAxes(sizeX, sizeY);
            currentFigure.addSubPlot(sp);
        }
        // Main thing: Set the selected number as current Axes for upcoming plot
        // commands.
        currentAxes = sp.getAxes(number);
    }

    /**
     * Generates a new figure window, this is a Frame containing the figurePanel
     * object that represents the draw area. It also resets the global static
     * currentAxes reference, any following plot command will then use this
     * figure as a target to draw into.
     *
     * <p>
     * The figure does not contain an axes object.
     *
     * @return Reference to the FigurePane object
     */
    public static Figure figure() {
        currentAxes = null;
        FigureJFrame root = new FigureJFrame();
        root.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                evt.getWindow().dispose(); // kill window
            }
        });
        Figure fp = root.getFigure();
        currentFigure = fp; // set as global currentFigure
        java.awt.EventQueue.invokeLater(() -> {
            root.setVisible(true);
        });
        return fp;
    }
}

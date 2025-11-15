package com.hartrusion.plot;

import java.awt.*;

public class FigurePaneTest {
    public static void main(String[] args) {
        Frame frame = new Frame("AWT Frame");

        FigurePane figurePane = new FigurePane();
        Axes axes = new Axes();
        axes.ylabel("y label");
        axes.xlabel("x label");
        figurePane.addAxes(axes);
        figurePane.setPreferredSize(new Dimension(548, 418));
        frame.add(figurePane);
        frame.pack();
        frame.setVisible(true);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                frame.dispose();
            }
        });
    }
}
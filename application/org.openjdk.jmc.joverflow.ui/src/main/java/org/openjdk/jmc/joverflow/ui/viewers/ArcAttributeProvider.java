package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

class ArcAttributeProvider implements IArcAttributeProvider {
    private static final Color[] COLORS = {new Color(Display.getDefault(), 250, 206, 210), // red
            new Color(Display.getDefault(), 185, 214, 255), // blue
            new Color(Display.getDefault(), 229, 229, 229), // grey
            new Color(Display.getDefault(), 255, 231, 199), // orange
            new Color(Display.getDefault(), 171, 235, 238), // aqua
            new Color(Display.getDefault(), 228, 209, 252), // purple
            new Color(Display.getDefault(), 255, 255, 255), // white
            new Color(Display.getDefault(), 205, 249, 212), // green
    };

    int i = 0;

    @Override
    public int getWeight(Object element) {
        return 1;
    }

    @Override
    public Color getColor(Object element) {
        return COLORS[i++ % COLORS.length];
    }
}
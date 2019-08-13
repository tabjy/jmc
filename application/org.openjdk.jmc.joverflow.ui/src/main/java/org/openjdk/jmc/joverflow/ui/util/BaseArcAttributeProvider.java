package org.openjdk.jmc.joverflow.ui.util;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class BaseArcAttributeProvider implements IArcAttributeProvider {
    private ListenerList<IArcAttributeProviderListener> mListenerList = new ListenerList<>();

    protected final Color[] COLORS = {
            new Color(Display.getDefault(), 250, 206, 210), // red
            new Color(Display.getDefault(), 185, 214, 255), // blue
            new Color(Display.getDefault(), 229, 229, 229), // grey
            new Color(Display.getDefault(), 255, 231, 199), // orange
            new Color(Display.getDefault(), 171, 235, 238), // aqua
            new Color(Display.getDefault(), 228, 209, 252), // purple
            new Color(Display.getDefault(), 205, 249, 212), // green
    };

    private int i = 0;

    public BaseArcAttributeProvider() {
        addListener((event) -> i = 0);
    }

    public int getWeight(Object element) {
        return 1;
    }

    public Color getColor(Object element) {
        return COLORS[i++ % COLORS.length];
    }

    public void addListener(IArcAttributeProviderListener listener) {
        mListenerList.add(listener);
    }

    public void dispose() {
        for (Color c : COLORS) {
            c.dispose();
        }
    }

    public void removeListener(IArcAttributeProviderListener listener) {
        mListenerList.remove(listener);
    }

    public ListenerList<IArcAttributeProviderListener> getListenerList() {
        return mListenerList;
    }
}
package org.openjdk.jmc.joverflow.ui.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorIndexedArcAttributeProvider extends BaseArcAttributeProvider {
    private Color COLOR_GRAY = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);

    private int totalWeight = 0;
    private int minimumAngle = 0;

    // cache
    private Map<Object, Color> colors = new HashMap<>();
    public ColorIndexedArcAttributeProvider() {
        super();

        addListener((event) -> {
        	disposeColors();
        	
            totalWeight = 0;
            for (Object e : event.getElements()) {
                totalWeight += getWeight(e);
            }
        });
    }

    public void setMinimumArcAngle(int angle) {
        minimumAngle = angle;
    }

    @Override
    public Color getColor(Object element) {
        if ((double) getWeight(element) / (double) totalWeight * 360f < minimumAngle) {
            return COLOR_GRAY;
        }

        return colors.computeIfAbsent(element, (obj) -> {
            RGB rgb = new RGB((float)(obj.hashCode() % 361), 0.8f, 0.9f);
            return new Color(Display.getCurrent(), rgb);
        });
    }

    @Override
    public void dispose() {
    	disposeColors();
        super.dispose();
    }
    
    private void disposeColors() {
    	for (Color c: colors.values()) {
    	    c.dispose();
        }

    	colors.clear();
    }
}

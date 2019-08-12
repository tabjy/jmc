package org.openjdk.jmc.joverflow.ui.viewers;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class ColorIndexedArcAttributeProvider extends BaseArcAttributeProvider {
    private Color COLOR_GRAY = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);

    private int totalWeight = 0;

    private int minimumAngle = 0;

    public ColorIndexedArcAttributeProvider() {
        super();

        addListener((event) -> {
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
        return COLORS[element.hashCode() % COLORS.length];
    }
}

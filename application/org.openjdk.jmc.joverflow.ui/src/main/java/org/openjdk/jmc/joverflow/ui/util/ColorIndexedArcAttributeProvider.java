package org.openjdk.jmc.joverflow.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ColorIndexedArcAttributeProvider extends BaseArcAttributeProvider {
	private final Color COLOR_GRAY = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);

	private int totalWeight = 0;
	private int minimumAngle = 0;

	private Map<Object, Color> colors = new HashMap<>();

	public ColorIndexedArcAttributeProvider() {
		super();

		addListener((event) -> {
			totalWeight = 0;
			colors.clear();

			for (Object e : event.getElements()) {
				totalWeight += getWeight(e);
			}

			Arrays.sort(event.getElements(), (o1, o2) -> getWeight(o2) - getWeight(o1));
			for (Object e : event.getElements()) {
				getColor(e);
			}
		});
	}

	public void setMinimumArcAngle(int angle) {
		minimumAngle = angle;
	}

	@Override
	public Color getColor(Object element) {
		Color color = colors.get(element);
		if (color != null) {
			return color;
		}

		if ((double) getWeight(element) / (double) totalWeight * 360f < minimumAngle) {
			color = COLOR_GRAY;
		} else {
			color =  super.getColor(element);
		}

		colors.put(element, color);
		return color;
	}
}

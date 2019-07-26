package org.openjdk.jmc.joverflow.ui.util;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class BaseArcAttributeProvider implements IArcAttributeProvider {
	private final ListenerList<IArcAttributeProviderListener> mListenerList = new ListenerList<>();

	private final Color[] COLORS = {new Color(Display.getDefault(), 169, 226, 0),
			new Color(Display.getDefault(), 249, 217, 0),
			new Color(Display.getDefault(), 34, 186, 217),
			new Color(Display.getDefault(), 1, 129, 226),
			new Color(Display.getDefault(), 47, 53, 127),
			new Color(Display.getDefault(), 134, 0, 97),
			new Color(Display.getDefault(), 198, 43, 0),
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

package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Color;

public interface IArcAttributeProvider {
    int getWeight(Object element);

    Color getColor(Object element);

    void addListener(IArcAttributeProviderListener listener);

    void dispose();

    void removeListener(IArcAttributeProviderListener listener);

    ListenerList<IArcAttributeProviderListener> getListenerList();
}

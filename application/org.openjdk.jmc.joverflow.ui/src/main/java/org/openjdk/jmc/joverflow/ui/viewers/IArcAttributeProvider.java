package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.swt.graphics.Color;

public interface IArcAttributeProvider {
    int getWeight(Object element);

    Color getColor(Object element);
}

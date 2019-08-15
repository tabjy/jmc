package org.openjdk.jmc.joverflow.ui.util;

import org.openjdk.jmc.joverflow.ui.viewers.BaseViewer;

public interface FilterChangedListener {
    void onFilterChanged(BaseViewer viewer);
}

package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ContentViewer;
import org.openjdk.jmc.joverflow.support.RefChainElement;
import org.openjdk.jmc.joverflow.ui.model.ModelListener;
import org.openjdk.jmc.joverflow.ui.model.ObjectCluster;
import org.openjdk.jmc.joverflow.ui.util.FilterChangedListener;

abstract public class BaseViewer extends ContentViewer implements ModelListener {
    private ListenerList<FilterChangedListener> mListeners = new ListenerList<>();
    private long mHeapSize;

    public void addFilterChangedListener(FilterChangedListener listener) {
        mListeners.add(listener);
    }

    public void removeFilterChangedListener(FilterChangedListener listener) {
        mListeners.remove(listener);
    }

    protected void notifyFilterChangedListeners() {
        for (FilterChangedListener l : mListeners) {
            l.onFilterChanged(this);
        }
    }

    public void setHeapSize(long size) {
        mHeapSize = size;
    }

    public long getHeapSize() {
        return mHeapSize;
    }

    public boolean filter(ObjectCluster oc) {
        return true;
    }

    public boolean filter(RefChainElement rce) {
        return true;
    }

    abstract public void reset();
}

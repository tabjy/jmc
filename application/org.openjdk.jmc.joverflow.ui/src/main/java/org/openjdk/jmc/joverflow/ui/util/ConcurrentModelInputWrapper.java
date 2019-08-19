package org.openjdk.jmc.joverflow.ui.util;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.deferred.IConcurrentModel;
import org.eclipse.jface.viewers.deferred.IConcurrentModelListener;

import java.util.Collection;

public class ConcurrentModelInputWrapper implements IConcurrentModel {
    private final ListenerList<IConcurrentModelListener> mModelListeners = new ListenerList<>();
    private Object[] mElements;

    public void setInput(Object input) {
        Object[] elements;
        if (input instanceof Object[]) {
            elements = (Object[]) input;
        } else if (input instanceof Collection) {
            elements = ((Collection) input).toArray();
        } else {
            elements = new Object[0];
        }

        mElements = elements;

        for (IConcurrentModelListener l : mModelListeners) {
            l.setContents(mElements);
        }
    }

    @Override
    public void requestUpdate(IConcurrentModelListener listener) {
        listener.setContents(mElements);
    }

    @Override
    public void addListener(IConcurrentModelListener listener) {
        mModelListeners.add(listener);
    }

    @Override
    public void removeListener(IConcurrentModelListener listener) {
        mModelListeners.remove(listener);
    }
}

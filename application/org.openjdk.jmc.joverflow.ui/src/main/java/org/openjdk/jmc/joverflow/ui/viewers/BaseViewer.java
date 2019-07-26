package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ContentViewer;
import org.openjdk.jmc.joverflow.support.RefChainElement;
import org.openjdk.jmc.joverflow.ui.model.ModelListener;
import org.openjdk.jmc.joverflow.ui.model.ObjectCluster;
import org.openjdk.jmc.joverflow.ui.util.FilterChangedListener;

// Base class for 4 main viewers
abstract public class BaseViewer extends ContentViewer implements ModelListener {
	private final ListenerList<FilterChangedListener> mListeners = new ListenerList<>();

	public void addFilterChangedListener(FilterChangedListener listener) {
		mListeners.add(listener);
	}

	public void removeFilterChangedListener(FilterChangedListener listener) {
		mListeners.remove(listener);
	}

	void notifyFilterChangedListeners() {
		for (FilterChangedListener l : mListeners) {
			l.onFilterChanged();
		}
	}

	abstract public void setHeapSize(long size);

	public boolean filter(ObjectCluster oc) {
		return true;
	}

	public boolean filter(RefChainElement rce) {
		return true;
	}

	abstract public void reset();
}

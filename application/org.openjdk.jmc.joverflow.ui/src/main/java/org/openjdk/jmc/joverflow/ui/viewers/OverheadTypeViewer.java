package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.openjdk.jmc.joverflow.support.RefChainElement;
import org.openjdk.jmc.joverflow.ui.model.ClusterType;
import org.openjdk.jmc.joverflow.ui.model.MemoryStatisticsItem;
import org.openjdk.jmc.joverflow.ui.model.ObjectCluster;

public class OverheadTypeViewer extends BaseViewer {

	private final MemoryStatisticsTableViewer mTableViewer;

	private final MemoryStatisticsItem[] mItems = new MemoryStatisticsItem[ClusterType.values().length];
	private ClusterType mCurrentType = ClusterType.ALL_OBJECTS;

	private boolean mAllIncluded = false;

	public OverheadTypeViewer(Composite parent, int style) {
		for (ClusterType t : ClusterType.values()) {
			mItems[t.ordinal()] = new MemoryStatisticsItem(t, 0, 0, 0);
		}

		mTableViewer = new MemoryStatisticsTableViewer(parent, style | SWT.FULL_SELECTION, null);
		mTableViewer.setPrimaryColumnText("Object Selection");

		mTableViewer.addSelectionChangedListener(event -> setCurrentType(getSelectedType()));
	}

	@Override
	public Control getControl() {
		return mTableViewer.getControl();
	}

	@Override
	public void refresh() {
		mTableViewer.refresh();
	}

	@Override
	public ISelection getSelection() {
		return mTableViewer.getSelection();
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		mTableViewer.setSelection(selection, reveal);
	}

	@Override
	public void include(ObjectCluster oc, RefChainElement ref) {
		if (mAllIncluded) {
			for (MemoryStatisticsItem item : mItems) {
				item.reset();
			}
			mAllIncluded = false;
		}

		if (oc.getType() != null) {
			mItems[oc.getType().ordinal()].addObjectCluster(oc);
		}
	}

	@Override
	public void allIncluded() {
		((MemoryStatisticsTableViewer.MemoryStatisticsContentProvider) mTableViewer.getContentProvider())
				.setInput(mItems);
		mAllIncluded = true;
	}

	@Override
	public void setHeapSize(long size) {
		mTableViewer.setHeapSize(size);
	}

	public ClusterType getCurrentType() {
		return mCurrentType;
	}

	public void setCurrentType(ClusterType type) {
		ClusterType oldType = mCurrentType;
		mCurrentType = type;

		if (oldType != mCurrentType) {
			notifyFilterChangedListeners();
		}
	}

	private ClusterType getSelectedType() {
		ClusterType type = ClusterType.ALL_OBJECTS;
		if (!getSelection().isEmpty()) {
			if (getSelection() instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) getSelection();
				MemoryStatisticsItem item = ((MemoryStatisticsItem) selection.getFirstElement());
				if (item != null && item.getId() != null) {
					type = (ClusterType) item.getId();
				}
			}
		}

		return type;
	}

	@Override
	public boolean filter(ObjectCluster oc) {
		return getCurrentType() == oc.getType();
	}

	@Override
	public void reset() {
		setCurrentType(ClusterType.ALL_OBJECTS);
	}
}

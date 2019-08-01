package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;
import org.openjdk.jmc.joverflow.support.RefChainElement;
import org.openjdk.jmc.joverflow.ui.model.ClusterType;
import org.openjdk.jmc.joverflow.ui.model.MemoryStatisticsItem;
import org.openjdk.jmc.joverflow.ui.model.ModelListener;
import org.openjdk.jmc.joverflow.ui.model.ObjectCluster;

public class OverheadTypeViewer extends ContentViewer implements ModelListener {

    private final MemoryStatisticsTableViewer<MemoryStatisticsItem> mTableViewer;
    private MemoryStatisticsItem[] mItems = new MemoryStatisticsItem[ClusterType.values().length];
    private boolean mAllIncluded = false;

    public OverheadTypeViewer(Composite parent, int style) {
        for (ClusterType t : ClusterType.values()) {
            mItems[t.ordinal()] = new MemoryStatisticsItem(t, 0, 0, 0);
        }

        mTableViewer = new MemoryStatisticsTableViewer<>(parent, SWT.BORDER | SWT.FULL_SELECTION);
        mTableViewer.setPrimaryColumnText("Object Selection");
    }

    @Override
    public Control getControl() {
        return mTableViewer.getControl();
    }

    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        mTableViewer.addSelectionChangedListener(listener);
    }

    @Override
    public ISelection getSelection() {
        return mTableViewer.getSelection();
    }

    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        mTableViewer.removeSelectionChangedListener(listener);
    }

    @Override
    public void setSelection(ISelection selection) {
        mTableViewer.setSelection(selection);
    }

    @Override
    public void refresh() {
        mTableViewer.refresh();
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
        mTableViewer.setInput(mItems);
        mAllIncluded = true;
    }

    public ClusterType getCurrentType() {
        ISelection selection = getSelection();
        if (selection.isEmpty() || !(selection instanceof StructuredSelection)) {
            return ClusterType.ALL_OBJECTS;
        }
        ClusterType type = (ClusterType) ((MemoryStatisticsItem) ((StructuredSelection) getSelection()).getFirstElement()).getId();
        if (type == null) {
            return ClusterType.ALL_OBJECTS;
        }

        return type;
    }

    public void setTotalMemory(long memory) {
        mTableViewer.setTotalMemory(memory);
    }

    public void reset() {
        for (TableItem item : mTableViewer.getTable().getItems()) {
            if (ClusterType.ALL_OBJECTS == ((MemoryStatisticsItem) item.getData()).getId()) {
                mTableViewer.getTable().setSelection(item);
            }
        }
    }
}

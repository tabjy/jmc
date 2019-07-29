package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.openjdk.jmc.joverflow.support.RefChainElement;
import org.openjdk.jmc.joverflow.ui.model.ClusterType;
import org.openjdk.jmc.joverflow.ui.model.ModelListener;
import org.openjdk.jmc.joverflow.ui.model.ObjectCluster;

public class OverheadTypeViewer extends ContentViewer implements ModelListener {

    private final MemoryStatisticsTableViewer<MemoryStatisticsItem> mTableViewer;
    private MemoryStatisticsItem[] mItems = new MemoryStatisticsItem[ClusterType.values().length];

    public OverheadTypeViewer(Composite parent, int style) {
        initItems();

        mTableViewer = new MemoryStatisticsTableViewer<>(parent, SWT.BORDER | SWT.FULL_SELECTION);
        mTableViewer.addSelectionChangedListener(event -> {
          // TODO: notify selection changed
        });
    }

    private void initItems() {
        mItems = new MemoryStatisticsItem[ClusterType.values().length];

        for (ClusterType t : ClusterType.values()) {
            mItems[t.ordinal()] = new MemoryStatisticsItem(t, 0, 0, 0);
        }
    }

    @Override
    public Control getControl() {
        return mTableViewer.getControl();
    }

    @Override
    public ISelection getSelection() {
        return mTableViewer.getSelection();
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
        if (oc.getType() != null) {
            mItems[oc.getType().ordinal()].addObjectCluster(oc);
        }
    }

    @Override
    public void allIncluded() {
        mTableViewer.setInput(mItems);
        initItems();
    }

    public ClusterType getCurrentType() {
        ISelection selection = getSelection();
        if (selection.isEmpty()) {
            return null;
        }
        if (!(selection instanceof StructuredSelection)) {
            return null;
        }
        return (ClusterType) ((StructuredSelection) getSelection()).getFirstElement();
    }

    public void setTotalMemory(long memory) {
        mTableViewer.setTotalMemory(memory);
    }
}

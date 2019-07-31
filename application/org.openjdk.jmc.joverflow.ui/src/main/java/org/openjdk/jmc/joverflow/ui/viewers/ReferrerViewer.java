package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.openjdk.jmc.joverflow.support.RefChainElement;
import org.openjdk.jmc.joverflow.ui.model.ModelListener;
import org.openjdk.jmc.joverflow.ui.model.ObjectCluster;

import java.util.ArrayList;
import java.util.function.Predicate;

public class ReferrerViewer extends ContentViewer implements ModelListener {

    private ReferrerTreeViewer<ReferrerItem> mTreeViewer;
    private ReferrerItemBuilder mItemBuilder;
    private Predicate<RefChainElement> mFilter = refChainElement -> true;

    public ReferrerViewer(Composite parent, int style) {
        mTreeViewer = new ReferrerTreeViewer<>(parent, SWT.BORDER | SWT.FULL_SELECTION);
    }

    @Override
    public void allIncluded() {
        if (mItemBuilder == null) {
            mTreeViewer.setInput(new ArrayList<ReferrerItem>());
        } else {
            mTreeViewer.setInput(mItemBuilder.buildReferrerList());
            mTreeViewer.expandAll();
            mItemBuilder = null;
        }
    }

    @Override
    public void include(ObjectCluster oc, RefChainElement ref) {
        if (mItemBuilder == null) {
            mItemBuilder = new ReferrerItemBuilder(oc, ref);
        } else {
            mItemBuilder.addCluster(oc, ref);
        }
    }

    public void reset() {
        mFilter = refChainElement -> true;
    }

    @Override
    public Control getControl() {
        return mTreeViewer.getControl();
    }

    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        mTreeViewer.addSelectionChangedListener(listener);
    }

    @Override
    public ISelection getSelection() {
        return mTreeViewer.getSelection();
    }

    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        mTreeViewer.removeSelectionChangedListener(listener);
    }

    @Override
    public void refresh() {
        mTreeViewer.refresh();
    }

    @Override
    public void setSelection(ISelection selection, boolean reveal) {
        mTreeViewer.setSelection(selection, reveal);
    }

    public Predicate<RefChainElement> getFilter() {
        ISelection selection = getSelection();
        if (selection.isEmpty() || !(selection instanceof StructuredSelection)) {
            return mFilter;
        }

        ReferrerItem item = (ReferrerItem) ((StructuredSelection) getSelection()).getFirstElement();
        mFilter = item::check;
        return mFilter;
    }
}
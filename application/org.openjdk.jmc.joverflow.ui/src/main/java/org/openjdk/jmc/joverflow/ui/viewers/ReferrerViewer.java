package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.openjdk.jmc.joverflow.support.RefChainElement;
import org.openjdk.jmc.joverflow.ui.model.ModelListener;
import org.openjdk.jmc.joverflow.ui.model.ObjectCluster;

import java.util.ArrayList;

public class ReferrerViewer extends ContentViewer implements ModelListener {

    //    private final ReferrerTreeViewer ui;
    private ReferrerTreeViewer<ReferrerItem> mTreeViewer;
    private ReferrerItemBuilder builder;

    public ReferrerViewer(Composite parent, int style) {
        mTreeViewer = new ReferrerTreeViewer<>(parent, SWT.BORDER | SWT.FULL_SELECTION);
    }

    @Override
    public void allIncluded() {
        if (builder == null) {
//            ui.getItems().clear();
//            TODO
            mTreeViewer.setInput(new ArrayList<ReferrerItem>());
        } else {
//            ui.set(builder.buildReferrerList());
            mTreeViewer.setInput(builder.buildReferrerList());
            builder = null;
        }
    }

    @Override
    public void resetItems() {
        // TODO
    }

    @Override
    public void include(ObjectCluster oc, RefChainElement ref) {
        if (builder == null) {
            builder = new ReferrerItemBuilder(oc, ref);
        } else {
            builder.addCluster(oc, ref);
        }
    }

    public void reset() {
//        ui.selectedItem = null;
        // TODO
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
}
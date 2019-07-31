package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.openjdk.jmc.joverflow.support.RefChainElement;
import org.openjdk.jmc.joverflow.ui.model.ModelListener;
import org.openjdk.jmc.joverflow.ui.model.ObjectCluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;

public class ReferrerViewer extends ContentViewer implements ModelListener {

    private ReferrerTreeViewer<ReferrerItem> mTreeViewer;
    private ReferrerItemBuilder mItemBuilder;

    private Collection<ISelectionChangedListener> mListeners = new HashSet<>();
    private ReferrerItem mSelectedItem;

    public ReferrerViewer(Composite parent, int style) {
        mTreeViewer = new ReferrerTreeViewer<>(parent, SWT.BORDER | SWT.FULL_SELECTION);

        mTreeViewer.addSelectionChangedListener(event -> {
            if (event.getStructuredSelection().isEmpty()) {
                return;
            }
            mSelectedItem = (ReferrerItem) event.getStructuredSelection().getFirstElement();

            notifySelectionChanged();
        });

        mTreeViewer.getControl().addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                // intentionally empty
            }

            @Override
            public void mouseDown(MouseEvent e) {
                if (e.button == 3) { // right button
                    reset();
                }
            }

            @Override
            public void mouseUp(MouseEvent e) {
                // intentionally empty
            }
        });
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
        mSelectedItem = null;
        notifySelectionChanged();
    }

    @Override
    public Control getControl() {
        return mTreeViewer.getControl();
    }

    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        mListeners.add(listener);
    }

    @Override
    public ISelection getSelection() {
        if (mSelectedItem == null) {
            return StructuredSelection.EMPTY;
        }

        return new StructuredSelection(mSelectedItem);
    }

    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public void refresh() {
        mTreeViewer.refresh();
    }

    @Override
    public void setSelection(ISelection selection, boolean reveal) {
        mSelectedItem = (ReferrerItem) ((StructuredSelection) getSelection()).getFirstElement();
        mTreeViewer.setSelection(selection, reveal);

        notifySelectionChanged();
    }

    private void notifySelectionChanged() {
        SelectionChangedEvent e = new SelectionChangedEvent(this, getSelection());
        for (ISelectionChangedListener l : mListeners) {
            l.selectionChanged(e);
        }
    }

    public Predicate<RefChainElement> getFilter() {
        ISelection selection = getSelection();
        if (selection.isEmpty()) {
            return refChainElement -> true;
        }

        ReferrerItem item = (ReferrerItem) ((StructuredSelection) getSelection()).getFirstElement();
        return item::check;
    }
}
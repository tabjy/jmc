package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.openjdk.jmc.joverflow.support.RefChainElement;
import org.openjdk.jmc.joverflow.ui.model.ModelListener;
import org.openjdk.jmc.joverflow.ui.model.ObjectCluster;

public class ReferrerViewer extends ContentViewer implements ModelListener {

    //    private final ReferrerTable ui;
    private ReferrerItemBuilder builder;

    public ReferrerViewer(Composite parent, int style) {
        // TODO: init table
        new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
    }

    @Override
    public void allIncluded() {
        if (builder == null) {
//            ui.getItems().clear();
        } else {
//            ui.set(builder.buildReferrerList());
            builder = null;
        }
    }

    @Override
    public void resetItems() {

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
    }

    @Override
    public Control getControl() {
        return null;
    }

    @Override
    public ISelection getSelection() {
        return null;
    }

    @Override
    public void refresh() {

    }

    @Override
    public void setSelection(ISelection selection, boolean reveal) {

    }
}
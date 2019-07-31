package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.openjdk.jmc.joverflow.support.RefChainElement;
import org.openjdk.jmc.joverflow.ui.model.ModelListener;
import org.openjdk.jmc.joverflow.ui.model.ObjectCluster;

public class AncestorViewer extends ContentViewer implements ModelListener {
    public AncestorViewer(Composite parent, int style) {
        SashForm bottomRightSash = new SashForm(parent, SWT.NONE);
        Group ancestorReferrerPieChartContainer = new Group(bottomRightSash, SWT.NONE);
        ancestorReferrerPieChartContainer.setLayout(new FillLayout(SWT.VERTICAL));

        Label ancestorReferrerLabel = new Label(ancestorReferrerPieChartContainer, SWT.NONE);
        ancestorReferrerLabel.setText("Ancestor referrer");

        Button ancestorReferrerPieChart = new Button(ancestorReferrerPieChartContainer, SWT.NONE);
        ancestorReferrerPieChart.setText("[Pie Chart]");

        Group ancestorReferrerTableContainer = new Group(bottomRightSash, SWT.NONE);
        ancestorReferrerTableContainer.setLayout(new FillLayout(SWT.HORIZONTAL));

        // TODO: init mAncestorViewer
        new TableViewer(ancestorReferrerTableContainer, SWT.BORDER | SWT.FULL_SELECTION);
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

    @Override
    public void include(ObjectCluster cluster, RefChainElement referenceChain) {

    }

    @Override
    public void allIncluded() {

    }
}

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

public class ClusterGroupViewer extends ContentViewer implements ModelListener {

    private String mQualifierName = "Class";

    private Label mTitle;

    public ClusterGroupViewer(Composite parent, int style) {
        SashForm bottomLeftSash = new SashForm(parent, SWT.NONE);
        Group classPieChartContainer = new Group(bottomLeftSash, SWT.NONE);
        classPieChartContainer.setLayout(new FillLayout(SWT.VERTICAL));

        mTitle = new Label(classPieChartContainer, SWT.NONE);
        mTitle.setText(mQualifierName);

        Button classPieChart = new Button(classPieChartContainer, SWT.NONE);
        classPieChart.setText("[Pie Chart]");

        Group classTableContainer = new Group(bottomLeftSash, SWT.NONE);
        classTableContainer.setLayout(new FillLayout(SWT.HORIZONTAL));

        // TODO: init table
        new TableViewer(classTableContainer, SWT.BORDER | SWT.FULL_SELECTION);
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
    
    public void setQualifierName(String qualifierName) {
        mQualifierName = qualifierName;
        mTitle.setText(mQualifierName);
    }
}

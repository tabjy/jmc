package org.openjdk.jmc.joverflow.ui;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.openjdk.jmc.joverflow.support.RefChainElement;
import org.openjdk.jmc.joverflow.ui.model.ClusterType;
import org.openjdk.jmc.joverflow.ui.model.ObjectCluster;
import org.openjdk.jmc.joverflow.ui.model.ReferenceChain;
import org.openjdk.jmc.joverflow.ui.viewers.OverheadTypeViewer;

import java.util.Collection;

public class JOverflowUi extends Composite {

    private Collection<ReferenceChain> mModel;
    private long mTotalMemory;

    private final OverheadTypeViewer mOverheadTypeViewer; // left-top viewer
    private final TableViewer mClusterGroupViewer; // left-bottom viewer
    private final TableViewer mReferrerViewer; // right-top viewer
    private final TableViewer mAncestorViewer; // right-bottom viewer

    public JOverflowUi(Composite parent, int style) {
        super(parent, style);
        this.setLayout(new FillLayout());

        SashForm hSash = new SashForm(this, SWT.NONE);

        {
            SashForm vSashLeft = new SashForm(hSash, SWT.VERTICAL);
            // Type Viewer
            {
                Group topLeftContainer = new Group(vSashLeft, SWT.NONE);
                topLeftContainer.setLayout(new FillLayout(SWT.HORIZONTAL));

                mOverheadTypeViewer = new OverheadTypeViewer(topLeftContainer, SWT.BORDER | SWT.FULL_SELECTION);
            }

            // Cluster Group Viewer
            {
                Group bottomLeftContainer = new Group(vSashLeft, SWT.NONE);
                bottomLeftContainer.setLayout(new FillLayout(SWT.HORIZONTAL));


                {
                    // TODO: abstract to ClusterGroupViewer
                    SashForm bottomLeftSash = new SashForm(bottomLeftContainer, SWT.NONE);
                    Group classPieChartContainer = new Group(bottomLeftSash, SWT.NONE);
                    classPieChartContainer.setLayout(new FillLayout(SWT.VERTICAL));

                    Label classLabel = new Label(classPieChartContainer, SWT.NONE);
                    classLabel.setText("Class");

                    Button classPieChart = new Button(classPieChartContainer, SWT.NONE);
                    classPieChart.setText("[Pie Chart]");

                    Group classTableContainer = new Group(bottomLeftSash, SWT.NONE);
                    classTableContainer.setLayout(new FillLayout(SWT.HORIZONTAL));

                    // TODO: init mClusterGroupViewer
                    mClusterGroupViewer = new TableViewer(classTableContainer, SWT.BORDER | SWT.FULL_SELECTION);
                }
            }
            vSashLeft.setWeights(new int[]{1, 1});
        }

        {
            SashForm vSashRight = new SashForm(hSash, SWT.VERTICAL);
            // Referrer Viewer
            {
                Group topRightContainer = new Group(vSashRight, SWT.NONE);
                topRightContainer.setLayout(new FillLayout(SWT.HORIZONTAL));

                // TODO: init mReferrerViewer
                mReferrerViewer = new TableViewer(topRightContainer, SWT.BORDER | SWT.FULL_SELECTION);
            }

            // AncestorViewer
            {
                Group bottomRightContainer = new Group(vSashRight, SWT.NONE);
                bottomRightContainer.setLayout(new FillLayout(SWT.HORIZONTAL));

                {
                    // TODO: abstract to AncestorViewer
                    SashForm bottomRightSash = new SashForm(bottomRightContainer, SWT.NONE);
                    Group ancestorReferrerPieChartContainer = new Group(bottomRightSash, SWT.NONE);
                    ancestorReferrerPieChartContainer.setLayout(new FillLayout(SWT.VERTICAL));

                    Label ancestorReferrerLabel = new Label(ancestorReferrerPieChartContainer, SWT.NONE);
                    ancestorReferrerLabel.setText("Ancestor referrer");

                    Button ancestorReferrerPieChart = new Button(ancestorReferrerPieChartContainer, SWT.NONE);
                    ancestorReferrerPieChart.setText("[Pie Chart]");

                    Group ancestorReferrerTableContainer = new Group(bottomRightSash, SWT.NONE);
                    ancestorReferrerTableContainer.setLayout(new FillLayout(SWT.HORIZONTAL));

                    // TODO: init mAncestorViewer
                    mAncestorViewer = new TableViewer(ancestorReferrerTableContainer, SWT.BORDER | SWT.FULL_SELECTION);
                }
            }
            vSashRight.setWeights(new int[]{1, 1});
        }

        hSash.setWeights(new int[]{1, 1});
    }

    public void setModel(Collection<ReferenceChain> model) {
        mModel = model;
        long heapSize = 0;
        for (ReferenceChain rc : model) {
            for (ObjectCluster oc : rc) {
                if (oc.getType() == ClusterType.ALL_OBJECTS) {
                    heapSize += oc.getMemory();
                }
            }
        }
        mTotalMemory = heapSize;
        updateModel();
//      modelUpdater.run();
    }

    private void updateModel() {
        ClusterType currentType = mOverheadTypeViewer.getCurrentType();
//        mClusterGroupViewer.setQualifierName(currentType == ClusterType.DUPLICATE_STRING || currentType == ClusterType.DUPLICATE_ARRAY ? "Duplicate" : null);
        // Loop all reference chains
        for (ReferenceChain chain : mModel) {
            RefChainElement rce = chain.getReferenceChain();
            // Check filters for reference chains
//            if (mReferrerViewer.getFilter().call(rce) && checkFilter(mAncestorViewer.getFilters(), rce)) {
            if (true) {
                // Loop all object clusters
                for (ObjectCluster oc : chain) {
                    // Check filters for object clusters
//                    if (checkFilter(clusterGroupViewer.getFilters(), oc)) {
                    if (true) {
                        // Add object cluster to type-viewer regardless of type
                        mOverheadTypeViewer.include(oc, rce);
                        // Add type object cluster matches current type and add to all other viewers
                        if (oc.getType() == currentType) {
//                            for (ModelListener v : modelListeners) {
//                                v.include(oc, chain.getReferenceChain());
//                            }
                        }
                    }
                }
            }
        }
        // Notify all that update is done
//        for (ModelListener v : modelListeners) {
//            v.allIncluded();
//        }

        mOverheadTypeViewer.setTotalMemory(mTotalMemory);
        mOverheadTypeViewer.allIncluded();
    }

    public void reset() {
        // TODO: reset all tables
    }
}

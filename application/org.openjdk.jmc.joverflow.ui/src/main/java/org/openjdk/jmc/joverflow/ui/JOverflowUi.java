package org.openjdk.jmc.joverflow.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.openjdk.jmc.joverflow.support.RefChainElement;
import org.openjdk.jmc.joverflow.ui.model.ClusterType;
import org.openjdk.jmc.joverflow.ui.model.ObjectCluster;
import org.openjdk.jmc.joverflow.ui.model.ReferenceChain;
import org.openjdk.jmc.joverflow.ui.viewers.AncestorViewer;
import org.openjdk.jmc.joverflow.ui.viewers.ClusterGroupViewer;
import org.openjdk.jmc.joverflow.ui.viewers.OverheadTypeViewer;
import org.openjdk.jmc.joverflow.ui.viewers.ReferrerViewer;

import java.util.Collection;

public class JOverflowUi extends Composite {

    private Collection<ReferenceChain> mModel;
    private long mTotalMemory;

    private final OverheadTypeViewer mOverheadTypeViewer; // left-top viewer
    private final ClusterGroupViewer mClusterGroupViewer; // left-bottom viewer
    private final ReferrerViewer mReferrerViewer; // right-top viewer
    private final AncestorViewer mAncestorViewer; // right-bottom viewer

    private boolean mUpdatingModel;

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
                mOverheadTypeViewer.addSelectionChangedListener((event) -> updateModel());
            }

            // Cluster Group Viewer
            {
                Group bottomLeftContainer = new Group(vSashLeft, SWT.NONE);
                bottomLeftContainer.setLayout(new FillLayout(SWT.HORIZONTAL));

                mClusterGroupViewer = new ClusterGroupViewer(bottomLeftContainer, SWT.BORDER | SWT.FULL_SELECTION);
            }
            vSashLeft.setWeights(new int[]{1, 1});
        }

        {
            SashForm vSashRight = new SashForm(hSash, SWT.VERTICAL);
            // Referrer Viewer
            {
                Group topRightContainer = new Group(vSashRight, SWT.NONE);
                topRightContainer.setLayout(new FillLayout(SWT.HORIZONTAL));

                mReferrerViewer = new ReferrerViewer(topRightContainer, SWT.BORDER | SWT.FULL_SELECTION);
                mReferrerViewer.addSelectionChangedListener((event) -> updateModel());
            }

            // AncestorViewer
            {
                Group bottomRightContainer = new Group(vSashRight, SWT.NONE);
                bottomRightContainer.setLayout(new FillLayout(SWT.HORIZONTAL));

                mAncestorViewer = new AncestorViewer(bottomRightContainer, SWT.BORDER | SWT.FULL_SELECTION);
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
        // TODO: Don't do the update on the UI tread.
        if (mUpdatingModel) {
            return;
        }

        mUpdatingModel = true;

        ClusterType currentType = mOverheadTypeViewer.getCurrentType();

        mClusterGroupViewer.setQualifierName(currentType == ClusterType.DUPLICATE_STRING || currentType == ClusterType.DUPLICATE_ARRAY ? "Duplicate" : "Class");
        // Loop all reference chains
        for (ReferenceChain chain : mModel) {
            RefChainElement rce = chain.getReferenceChain();
            // Check filters for reference chains
//            if (mReferrerViewer.getFilter().call(rce) && checkFilter(mAncestorViewer.getFilters(), rce)) {
            if (mReferrerViewer.getFilter().test(rce)) {
                // Loop all object clusters
                for (ObjectCluster oc : chain) {
                    // Check filters for object clusters
//                    if (checkFilter(clusterGroupViewer.getFilters(), oc)) {
                    if (true) {
                        // Add object cluster to type-viewer regardless of type
                        mOverheadTypeViewer.include(oc, rce);
                        // Add type object cluster matches current type and add to all other viewers
                        if (oc.getType() == currentType) {
                            mReferrerViewer.include(oc, chain.getReferenceChain());
                            mClusterGroupViewer.include(oc, chain.getReferenceChain());
                            mAncestorViewer.include(oc, chain.getReferenceChain());
                        }
                    }
                }
            }
        }

        // Notify all that update is done
        mReferrerViewer.allIncluded();
        mClusterGroupViewer.allIncluded();
        mAncestorViewer.allIncluded();

        mOverheadTypeViewer.setTotalMemory(mTotalMemory);
        mOverheadTypeViewer.allIncluded();

        mUpdatingModel = false;
    }

    public void reset() {
        // TODO: reset all viewers
        mOverheadTypeViewer.reset();
        mReferrerViewer.reset();
    }
}

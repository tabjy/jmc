package org.openjdk.jmc.joverflow.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.openjdk.jmc.joverflow.support.RefChainElement;
import org.openjdk.jmc.joverflow.ui.model.ClusterType;
import org.openjdk.jmc.joverflow.ui.model.ModelListener;
import org.openjdk.jmc.joverflow.ui.model.ObjectCluster;
import org.openjdk.jmc.joverflow.ui.model.ReferenceChain;
import org.openjdk.jmc.joverflow.ui.viewers.AncestorViewer;
import org.openjdk.jmc.joverflow.ui.viewers.ClusterGroupViewer;
import org.openjdk.jmc.joverflow.ui.viewers.OverheadTypeViewer;
import org.openjdk.jmc.joverflow.ui.viewers.ReferrerViewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JOverflowUi extends Composite {

    private Collection<ReferenceChain> mModel;
    private long mTotalMemory;

    private final OverheadTypeViewer mOverheadTypeViewer; // left-top viewer
    private final ClusterGroupViewer mClusterGroupViewer; // left-bottom viewer
    private final ReferrerViewer mReferrerViewer; // right-top viewer
    private final AncestorViewer mAncestorViewer; // right-bottom viewer

    private final List<ModelListener> mModelListeners = new ArrayList<>();

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
                mClusterGroupViewer.addSelectionChangedListener((event) -> updateModel());
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
                mAncestorViewer.addSelectionChangedListener((event) -> updateModel());
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
    }

    private void updateModel() {
        // TODO: Don't do the update on the UI tread.
        if (mUpdatingModel) {
            return;
        }

        mUpdatingModel = true;

        ClusterType currentType = mOverheadTypeViewer.getCurrentType();

        mClusterGroupViewer.setQualifierName(currentType == ClusterType.DUPLICATE_STRING || currentType == ClusterType.DUPLICATE_ARRAY ? "Duplicate" : null);
        // Loop all reference chains
        for (ReferenceChain chain : mModel) {
            RefChainElement rce = chain.getReferenceChain();
            // Check filters for reference chains
            if (mReferrerViewer.getFilter().test(rce) && mAncestorViewer.getFilter().test(rce)) {
                // Loop all object clusters
                for (ObjectCluster oc : chain) {
                    // Check filters for object clusters
                    if (mClusterGroupViewer.getFilter().test(oc)) {
                        // Add object cluster to type-viewer regardless of type
                        mOverheadTypeViewer.include(oc, rce);
                        // Add type object cluster matches current type and add to all other viewers
                        if (oc.getType() == currentType) {
                            mReferrerViewer.include(oc, chain.getReferenceChain());
                            mClusterGroupViewer.include(oc, chain.getReferenceChain());
                            mAncestorViewer.include(oc, chain.getReferenceChain());

                            for (ModelListener l : mModelListeners) {
                                l.include(oc, chain.getReferenceChain());
                            }
                        }
                    }
                }
            }
        }

        // Notify all that update is done
        mOverheadTypeViewer.setTotalMemory(mTotalMemory);
        mReferrerViewer.setTotalMemory(mTotalMemory);
        mClusterGroupViewer.setTotalMemory(mTotalMemory);
        mAncestorViewer.setTotalMemory(mTotalMemory);
        
        mReferrerViewer.allIncluded();
        mClusterGroupViewer.allIncluded();
        mAncestorViewer.allIncluded();
        mOverheadTypeViewer.allIncluded();

        for (ModelListener l : mModelListeners) {
            l.allIncluded();
        }

        mUpdatingModel = false;
    }

    void reset() {
        mUpdatingModel = true;

        mOverheadTypeViewer.reset();
        mReferrerViewer.reset();
        mClusterGroupViewer.reset();
        mAncestorViewer.reset();

        mUpdatingModel = false;
        updateModel();
    }

    void addModelListener(final ModelListener listener) {
        mModelListeners.add(listener);
    }

    void removeModelListener(final ModelListener listener) {
        mModelListeners.remove(listener);
    }
}

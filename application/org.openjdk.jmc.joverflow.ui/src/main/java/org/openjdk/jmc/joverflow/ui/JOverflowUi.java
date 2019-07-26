package org.openjdk.jmc.joverflow.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
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

	private final OverheadTypeViewer mOverheadTypeViewer; // left-top viewer
	private final ClusterGroupViewer mClusterGroupViewer; // left-bottom viewer
	private final ReferrerViewer mReferrerViewer; // right-top viewer
	private final AncestorViewer mAncestorViewer; // right-bottom viewer

	private final List<ModelListener> mModelListeners = new ArrayList<>();

	public JOverflowUi(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());

		SashForm hSash = new SashForm(this, SWT.NONE);

		{
			SashForm vSashLeft = new SashForm(hSash, SWT.VERTICAL);
			// Type Viewer (top-left)
			{
				Group topLeftContainer = new Group(vSashLeft, SWT.NONE);
				topLeftContainer.setLayout(new FillLayout(SWT.HORIZONTAL));

				mOverheadTypeViewer = new OverheadTypeViewer(topLeftContainer, SWT.NONE);
				mOverheadTypeViewer.addFilterChangedListener(this::updateModel);
			}

			// Cluster Group Viewer (bottom-left)
			{
				Group bottomLeftContainer = new Group(vSashLeft, SWT.NONE);
				bottomLeftContainer.setLayout(new FillLayout(SWT.HORIZONTAL));

				mClusterGroupViewer = new ClusterGroupViewer(bottomLeftContainer, SWT.NONE);
				mClusterGroupViewer.addFilterChangedListener(this::updateModel);
			}
			vSashLeft.setWeights(new int[] {1, 1});
		}

		{
			SashForm vSashRight = new SashForm(hSash, SWT.VERTICAL);
			// Referrer Viewer (top-right)
			{
				Group topRightContainer = new Group(vSashRight, SWT.NONE);
				topRightContainer.setLayout(new FillLayout(SWT.HORIZONTAL));

				mReferrerViewer = new ReferrerViewer(topRightContainer, SWT.NONE);
				mReferrerViewer.addFilterChangedListener(this::updateModel);
			}

			// Ancestor Viewer (bottom-right)
			{
				Group bottomRightContainer = new Group(vSashRight, SWT.NONE);
				bottomRightContainer.setLayout(new FillLayout(SWT.HORIZONTAL));

				mAncestorViewer = new AncestorViewer(bottomRightContainer, SWT.NONE);
				mAncestorViewer.addFilterChangedListener(this::updateModel);
			}
			vSashRight.setWeights(new int[] {1, 1});
		}

		hSash.setWeights(new int[] {1, 1});

		mModelListeners.add(mClusterGroupViewer);
		mModelListeners.add(mReferrerViewer);
		mModelListeners.add(mAncestorViewer);
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

		mOverheadTypeViewer.setHeapSize(heapSize);
		mReferrerViewer.setHeapSize(heapSize);
		mClusterGroupViewer.setHeapSize(heapSize);
		mAncestorViewer.setHeapSize(heapSize);

		updateModel();
	}

	private void updateModel() {
		ClusterType currentType = mOverheadTypeViewer.getCurrentType();

		mClusterGroupViewer.setQualifierName(
				currentType == ClusterType.DUPLICATE_STRING || currentType == ClusterType.DUPLICATE_ARRAY ?
						"Duplicate" : null);
		// Loop all reference chains
		for (ReferenceChain chain : mModel) {
			RefChainElement rce = chain.getReferenceChain();
			// Check filters for reference chains
			if (mReferrerViewer.filter(rce) && mAncestorViewer.filter(rce)) {
				// Loop all object clusters
				for (ObjectCluster oc : chain) {
					// Check filters for object clusters
					if (mClusterGroupViewer.filter(oc)) {
						// Add object cluster to type-viewer regardless of type
						mOverheadTypeViewer.include(oc, rce);
						// Add type object cluster matches current type and add to all other viewers
						if (mOverheadTypeViewer.filter(oc)) {
							for (ModelListener l : mModelListeners) {
								l.include(oc, chain.getReferenceChain());
							}
						}
					}
				}
			}
		}

		// Notify all that update is done
		mOverheadTypeViewer.allIncluded();
		for (ModelListener l : mModelListeners) {
			l.allIncluded();
		}
	}

	void reset() {
		mOverheadTypeViewer.reset();
		mReferrerViewer.reset();
		mClusterGroupViewer.reset();
		mAncestorViewer.reset();

		updateModel();
	}

	void addModelListener(final ModelListener listener) {
		mModelListeners.add(listener);

		if (mModel != null) {
			updateModel();
		}
	}

	void removeModelListener(final ModelListener listener) {
		mModelListeners.remove(listener);
	}
}

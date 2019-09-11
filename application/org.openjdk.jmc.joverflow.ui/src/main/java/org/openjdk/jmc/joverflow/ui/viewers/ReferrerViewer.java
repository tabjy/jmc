package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.openjdk.jmc.joverflow.support.RefChainElement;
import org.openjdk.jmc.joverflow.ui.model.ObjectCluster;
import org.openjdk.jmc.joverflow.ui.model.ReferrerItem;
import org.openjdk.jmc.joverflow.ui.model.ReferrerItemBuilder;

public class ReferrerViewer extends BaseViewer {

	private final ReferrerTreeViewer mTreeViewer;
	private ReferrerItemBuilder mItemBuilder;

	private ReferrerItem mSelectedItem;

	public ReferrerViewer(Composite parent, int style) {
		mTreeViewer = new ReferrerTreeViewer(parent, style | SWT.FULL_SELECTION);

		mTreeViewer.getControl().addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// no op
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 1) { // left button
					if (mTreeViewer.getSelection().isEmpty()) {
						return;
					}
					IStructuredSelection selection = (IStructuredSelection) mTreeViewer.getSelection();
					mSelectedItem = (ReferrerItem) selection.getFirstElement();

					notifyFilterChangedListeners();
				}
				if (e.button == 3) { // right button
					reset();
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
				// no op
			}
		});
	}

	@Override
	public Control getControl() {
		return mTreeViewer.getControl();
	}

	@Override
	public ISelection getSelection() {
		return mTreeViewer.getSelection();
	}

	@Override
	public void refresh() {
		mTreeViewer.refresh();
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		mTreeViewer.setSelection(selection, reveal);
	}

	@Override
	public void include(ObjectCluster oc, RefChainElement ref) {
		if (mItemBuilder == null) {
			mItemBuilder = new ReferrerItemBuilder(oc, ref);
		} else {
			mItemBuilder.addCluster(oc, ref);
		}
	}

	@Override
	public void allIncluded() {
		if (mItemBuilder == null) {
			((ReferrerTreeViewer.ReferrerTreeContentProvider) mTreeViewer.getContentProvider()).setInput(null);
		} else {
			((ReferrerTreeViewer.ReferrerTreeContentProvider) mTreeViewer.getContentProvider())
					.setInput(mItemBuilder.buildReferrerList());
			mItemBuilder = null;
		}
	}

	@Override
	public void setHeapSize(long size) {
		mTreeViewer.setHeapSize(size);
	}

	@Override
	public void reset() {
		mSelectedItem = null;
		notifyFilterChangedListeners();
	}

	@Override
	public boolean filter(RefChainElement rce) {
		if (mSelectedItem == null) {
			return true;
		}
		return mSelectedItem.check(rce);
	}
}

package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.openjdk.jmc.joverflow.support.RefChainElement;
import org.openjdk.jmc.joverflow.ui.model.MemoryStatisticsItem;
import org.openjdk.jmc.joverflow.ui.model.ObjectCluster;
import org.openjdk.jmc.joverflow.ui.swt.FilterList;
import org.openjdk.jmc.joverflow.ui.util.ColorIndexedArcAttributeProvider;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ClusterGroupViewer extends BaseViewer {

	private final SashForm mContainer;
	private final Label mTitle;
	private final PieChartViewer mPieChart;
	private final FilterList<ObjectCluster> mFilterList;
	private final MemoryStatisticsTableViewer mTableViewer;

	private String mQualifierName;
	private final Map<Object, MemoryStatisticsItem> items = new HashMap<>();

	private boolean mAllIncluded = false;

	public ClusterGroupViewer(Composite parent, int style) {
		mContainer = new SashForm(parent, style);

		{
			Group leftContainer = new Group(mContainer, SWT.NONE);
			leftContainer.setLayout(new FormLayout());

			mTitle = new Label(leftContainer, SWT.NONE);
			{
				FormData data = new FormData();
				data.top = new FormAttachment(0, 10);
				data.left = new FormAttachment(0, 10);
				mTitle.setLayoutData(data);
			}

			{
				SashForm container = new SashForm(leftContainer, SWT.VERTICAL);
				{
					FormData fd_sashForm = new FormData();
					fd_sashForm.top = new FormAttachment(mTitle, 10);
					fd_sashForm.right = new FormAttachment(100, -10);
					fd_sashForm.bottom = new FormAttachment(100, -10);
					fd_sashForm.left = new FormAttachment(0, 10);
					container.setLayoutData(fd_sashForm);
				}

				mPieChart = new PieChartViewer(container, SWT.NONE);
				mPieChart.setContentProvider(ArrayContentProvider.getInstance());
				ColorIndexedArcAttributeProvider provider = new ColorIndexedArcAttributeProvider() {
					@Override
					public int getWeight(Object element) {
						return (int) ((MemoryStatisticsItem) element).getMemory();
					}
				};
				provider.setMinimumArcAngle(5);
				mPieChart.setArcAttributeProvider(provider);

				mPieChart.setMinimumArcAngle(5);
				mPieChart.getPieChart().setZoomRatio(1.2);
				mPieChart.setComparator(new ViewerComparator() {
					@Override
					public int compare(Viewer viewer, Object e1, Object e2) {
						return (int) (((MemoryStatisticsItem) e2).getMemory() - ((MemoryStatisticsItem) e1)
								.getMemory());
					}
				});

				mFilterList = new FilterList<>(container, SWT.NONE);
				mFilterList.addFilterChangedListener(this::notifyFilterChangedListeners);

				container.setWeights(new int[] {3, 2});
			}
		}

		{
			Group tableContainer = new Group(mContainer, SWT.NONE);
			tableContainer.setLayout(new FillLayout(SWT.HORIZONTAL));

			mTableViewer = new MemoryStatisticsTableViewer(tableContainer, SWT.NONE,
					(e) -> mPieChart.getArcAttributeProvider().getColor(e));

			mTableViewer.getTable().addMouseListener(new MouseListener() {
				@Override
				public void mouseDoubleClick(MouseEvent e) {
					// no op
				}

				@Override
				public void mouseDown(MouseEvent e) {
					// no op
				}

				@Override
				public void mouseUp(MouseEvent e) {
					if (e.button != 1 && e.button != 3) {
						return;
					}

					if (mTableViewer.getSelection().isEmpty()) {
						return;
					}
					IStructuredSelection selection = (IStructuredSelection) mTableViewer.getSelection();
					MemoryStatisticsItem item = (MemoryStatisticsItem) selection.getFirstElement();
					if (item.getId() == null) {
						return;
					}

					mFilterList.addFilter(new Predicate<ObjectCluster>() {
						final String qualifierName = mQualifierName;
						final String itemName = item.getId().toString();
						final boolean excluded = e.button == 3;

						@Override
						public boolean test(ObjectCluster oc) {
							if (qualifierName == null) {
								return itemName.equals(oc.getClassName()) ^ excluded;
							}

							if (oc.getQualifier() == null) {
								return true;
							}

							return itemName.equals(oc.getQualifier()) ^ excluded;
						}

						@Override
						public String toString() {
							return (qualifierName == null ? "Class" : mQualifierName) + (excluded ? " ≠ " : " = ")
									//$NON-NLS-1$ //$NON-NLS-2$
									+ item.getId().toString();
						}

						@Override
						public int hashCode() {
							return itemName.hashCode();
						}

						@Override
						public boolean equals(Object obj) {
							if (obj == null) {
								return false;
							}
							if (getClass() != obj.getClass()) {
								return false;
							}

							return hashCode() == obj.hashCode();
						}
					});
				}
			});
		}

		mContainer.setWeights(new int[] {1, 2});
	}

	@Override
	public Control getControl() {
		return mContainer;
	}

	@Override
	public ISelection getSelection() {
		return mTableViewer.getSelection();
	}

	@Override
	public void refresh() {
		mTableViewer.refresh();
		mPieChart.refresh();
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		mTableViewer.setSelection(selection, reveal);
		mPieChart.setSelection(selection, reveal);
	}

	@Override
	public void include(ObjectCluster oc, RefChainElement ref) {
		if (mAllIncluded) {
			for (MemoryStatisticsItem item : items.values()) {
				item.reset();
			}
			mAllIncluded = false;
		}

		String s = mQualifierName != null ? oc.getQualifier() : oc.getClassName();
		MemoryStatisticsItem item = items.get(s);
		if (item == null) {
			item = new MemoryStatisticsItem(s, 0, 0, 0);
			items.put(s, item);
		}
		item.addObjectCluster(oc);
	}

	@Override
	public void allIncluded() {
		Collection<MemoryStatisticsItem> values = items.values();

		((MemoryStatisticsTableViewer.MemoryStatisticsContentProvider) mTableViewer.getContentProvider())
				.setInput(values);
		mPieChart.setInput(values);

		mAllIncluded = true;
	}

	public void setQualifierName(String qualifierName) {
		mQualifierName = qualifierName;
		String text = qualifierName != null ? qualifierName : "Class";
		mTitle.setText(text);
		mTableViewer.setPrimaryColumnText(text);
	}

	public void setHeapSize(long size) {
		mTableViewer.setHeapSize(size);
	}

	@Override
	public boolean filter(ObjectCluster oc) {
		return mFilterList.filter(oc);
	}

	@Override
	public void reset() {
		mFilterList.reset();
	}
}

package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.openjdk.jmc.joverflow.support.RefChainElement;
import org.openjdk.jmc.joverflow.ui.model.MemoryStatisticsItem;
import org.openjdk.jmc.joverflow.ui.model.ObjectCluster;
import org.openjdk.jmc.joverflow.ui.util.ColorIndexedArcAttributeProvider;
import org.openjdk.jmc.joverflow.ui.util.ConcurrentModelInputWrapper;

import java.util.*;
import java.util.List;
import java.util.function.Predicate;

public class ClusterGroupViewer extends BaseViewer {

	private Composite mContainer;
	private Label mTitle;
	private PieChartViewer mPieChart;
	private Group mFilterContainer;
	private final MemoryStatisticsTableViewer mTableViewer;

	private String mQualifierName;
	private final Map<Object, MemoryStatisticsItem> items = new HashMap<>();
	private ConcurrentModelInputWrapper mInputModel = new ConcurrentModelInputWrapper();
	private List<Predicate<ObjectCluster>> mFilters = new ArrayList<>();

	private boolean mAllIncluded = false;

	public ClusterGroupViewer(Composite parent, int style) {
		mContainer = new SashForm(parent, style);

		{
			Group leftContainer = new Group(mContainer, SWT.NONE);
			leftContainer.setLayout(new FillLayout(SWT.VERTICAL));

			mTitle = new Label(leftContainer, SWT.NONE);

			mPieChart = new PieChartViewer(leftContainer, SWT.BORDER);
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
					return (int) (((MemoryStatisticsItem) e2).getMemory() - ((MemoryStatisticsItem) e1).getMemory());
				}
			});

			mFilterContainer = new Group(leftContainer, SWT.NONE);
			mFilterContainer.setLayout(new FillLayout(SWT.VERTICAL));

		}

		{
			Group tableContainer = new Group(mContainer, SWT.NONE);
			tableContainer.setLayout(new FillLayout(SWT.HORIZONTAL));

			mTableViewer = new MemoryStatisticsTableViewer(tableContainer, SWT.BORDER | SWT.FULL_SELECTION,
					(e) -> mPieChart.getArcAttributeProvider().getColor(e));
			mTableViewer.setInput(mInputModel);

			mTableViewer.getTable().addMouseListener(new MouseListener() {
				@Override
				public void mouseDoubleClick(MouseEvent e) {

				}

				@Override
				public void mouseDown(MouseEvent e) {
					if (mTableViewer.getSelection().isEmpty()) {
						return;
					}
					IStructuredSelection selection = (IStructuredSelection) mTableViewer.getSelection();
					MemoryStatisticsItem item = (MemoryStatisticsItem) selection.getFirstElement();
					if (e.button == 1) { // left click
						String qualifierName = mQualifierName;
						String itemName = item.getId().toString();
						boolean excluded = false;
						Predicate<ObjectCluster> filter = (oc) -> {
							if (qualifierName == null) {
								return itemName.equals(oc.getClassName()) ^ excluded;
							}

							if (oc.getQualifier() == null) {
								return true;
							}

							return itemName.equals(oc.getQualifier()) ^ excluded;
						};
						mFilters.add(filter);

						Button button = new Button(mFilterContainer, SWT.NONE);
						button.setText(
								(mQualifierName == null ? "Class" : mQualifierName) + " = " + item.getId().toString());
						button.addMouseListener(new MouseListener() {
							@Override
							public void mouseDoubleClick(MouseEvent e) {
								// no op
							}

							@Override
							public void mouseDown(MouseEvent e) {
								mFilters.remove(filter);
								button.dispose();

								// TODO: investigate why layout is not auto updated
								mFilterContainer.layout(true, true);
								notifyFilterChangedListeners();
							}

							@Override
							public void mouseUp(MouseEvent e) {
								// no op
							}
						});

						// TODO: investigate why layout is not auto updated
						mFilterContainer.layout(true, true);
					} else if (e.button == 3) { // right click
						String qualifierName = mQualifierName;
						String itemName = item.getId().toString();
						boolean excluded = true;
						Predicate<ObjectCluster> filter = (oc) -> {
							if (qualifierName == null) {
								return itemName.equals(oc.getClassName()) ^ excluded;
							}

							if (oc.getQualifier() == null) {
								return true;
							}

							return itemName.equals(oc.getQualifier()) ^ excluded;
						};
						mFilters.add(filter);

						Button button = new Button(mFilterContainer, SWT.NONE);
						button.setText(
								(mQualifierName == null ? "Class" : mQualifierName) + " = " + item.getId().toString());
						button.addMouseListener(new MouseListener() {
							@Override
							public void mouseDoubleClick(MouseEvent e) {
								// no op
							}

							@Override
							public void mouseDown(MouseEvent e) {
								mFilters.remove(filter);
								button.dispose();

								// TODO: investigate why layout is not auto updated
								mFilterContainer.layout(true, true);
								notifyFilterChangedListeners();
							}

							@Override
							public void mouseUp(MouseEvent e) {
								// no op
							}
						});

						// TODO: investigate why layout is not auto updated
						mFilterContainer.layout(true, true);
					} else {
						return;
					}

					notifyFilterChangedListeners();
				}

				@Override
				public void mouseUp(MouseEvent e) {
					// no op
				}
			});
		}
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

		mInputModel.setInput(values);
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
		Predicate<ObjectCluster> res = in -> true;
		for (Predicate<ObjectCluster> filter : mFilters) {
			res = res.and(filter);
		}

		return res.test(oc);
	}

	@Override
	public void reset() {
		mFilters.clear();
		for (Control filter : mFilterContainer.getChildren()) {
			filter.dispose();
		}
	}
}

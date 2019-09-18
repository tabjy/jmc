package org.openjdk.jmc.joverflow.ui.viewers;

import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.deferred.DeferredContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.openjdk.jmc.joverflow.ui.model.ReferrerItem;

// ReferrerTreeViewer is actually a TableViewer with its tree-like content
class ReferrerTreeViewer extends TableViewer {

	private long mHeapSize = 1;

	private TreeViewerColumnComparator mActiveColumnComparator;
	private final DeferredContentProvider mContentProvider;

	ReferrerTreeViewer(Composite parent, int style) {
		super(parent, style | SWT.VIRTUAL | SWT.FULL_SELECTION);

		// FIXME: Bug 165637 - [Viewers] ArrayIndexOutOfBoundsException exception in ConcurrentTableUpdator (used in DeferredContentProvider)
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=165637
		mContentProvider = new DeferredContentProvider((lhs, rhs) -> 0);
		mContentProvider.setFilter(element -> ((ReferrerItem) element).getSize() > 0);
		setContentProvider(mContentProvider);

		createTreeViewerColumn("Referrer", //
				ReferrerItem::getName, //
				null, //
				(lhs, rhs) -> lhs.getName().compareTo(rhs.getName()), //
				false, true);

		createTreeViewerColumn("Memory KiB", //
				model -> String.format("%,.2f (%d%%)", //
						(double) model.getMemory() / 1024f, //
						Math.round((double) model.getMemory() * 100f / (double) mHeapSize)), //
				model -> String.format("%,d Bytes", model.getMemory()), //
				(lhs, rhs) -> (int) (lhs.getMemory() - rhs.getMemory()), //
				true, false);

		createTreeViewerColumn("Overhead KiB", //
				model -> String.format("%,.2f (%d%%)", //
						(double) model.getOvhd() / 1024f, //
						Math.round((double) model.getOvhd() * 100f / (double) mHeapSize)), //
				model -> String.format("%,d Bytes", model.getOvhd()), //
				(lhs, rhs) -> (int) (lhs.getOvhd() - rhs.getOvhd()), false, false);

		createTreeViewerColumn("Objects", //
				model -> String.format("%,d", model.getSize()),//
				null, //
				(lhs, rhs) -> lhs.getSize() - rhs.getSize(), //
				false, false);

		getTable().setLinesVisible(true);
		getTable().setHeaderVisible(true);
		ColumnViewerToolTipSupport.enableFor(this);
	}

	private void createTreeViewerColumn(
			String label, Function<ReferrerItem, String> labelProvider, Function<ReferrerItem, String> toolTipProvider,
			BiFunction<ReferrerItem, ReferrerItem, Integer> comparator, boolean sort, boolean intent) {
		TableViewerColumn column = new TableViewerColumn(this, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText(label);
		column.getColumn().setMoveable(true);

		column.setLabelProvider(new OwnerDrawLabelProvider() {
			@Override
			protected void paint(Event event, Object element) {
				Widget item = event.item;

				if (element == null) {
					// FIXME: Bug 146799 - Blank last table item on virtual table
					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146799
					return;
				}
				Rectangle bounds = ((TableItem) item).getBounds(event.index);
				Point p = event.gc.stringExtent(labelProvider.apply((ReferrerItem) element));

				int margin = (bounds.height - p.y) / 2;
				int dx = bounds.x + margin;

				if (intent) {
					dx += 10 * ((ReferrerItem) element).getLevel();
				}

				event.gc.drawString(labelProvider.apply((ReferrerItem) element), dx, bounds.y + margin, true);
			}

			@Override
			protected void measure(Event event, Object element) {
				// no op
			}

			@Override
			protected void erase(Event event, Object element) {
				// no op
			}

			@Override
			public String getToolTipText(Object element) {
				if (toolTipProvider == null) {
					return super.getToolTipText(element);
				}
				return toolTipProvider.apply((ReferrerItem) element);
			}
		});

		TreeViewerColumnComparator cmp = new TreeViewerColumnComparator() {
			@Override
			int doCompare(Object e1, Object e2) {
				return comparator.apply((ReferrerItem) e1, (ReferrerItem) e2);
			}
		};

		cmp.init(column, sort);
	}

	void setHeapSize(long size) {
		mHeapSize = size;
	}

	abstract class TreeViewerColumnComparator implements Comparator {
		private boolean decreasing = true;

		TableViewerColumn mColumn;

		void init(TableViewerColumn column, boolean sorted) {
			mColumn = column;

			mColumn.getColumn().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					onClicked();
				}
			});

			if (sorted) {
				onClicked();
			}
		}

		@SuppressWarnings("Duplicates")
		private void onClicked() {
			if (mActiveColumnComparator == this) {
				decreasing = !decreasing;
			} else {
				mActiveColumnComparator = this;
			}

			mColumn.getColumn().getParent().setSortColumn(mColumn.getColumn());
			mColumn.getColumn().getParent().setSortDirection(decreasing ? SWT.DOWN : SWT.UP);

			mContentProvider.setSortOrder(this);
		}

		@Override
		public int compare(Object e1, Object e2) {
			if (((ReferrerItem) e1).getLevel() == ((ReferrerItem) e2).getLevel()) {
				return (decreasing ? -1 : 1) * doCompare(e1, e2);
			} else {
				return ((ReferrerItem) e1).getLevel() - ((ReferrerItem) e2).getLevel();
			}
		}

		abstract int doCompare(Object e1, Object e2);
	}
}

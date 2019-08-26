package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.deferred.DeferredContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.openjdk.jmc.joverflow.ui.model.MemoryStatisticsItem;

import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;

class MemoryStatisticsTableViewer extends TableViewer {

	private long mHeapSize = 1;
	private final TableViewerColumn mPrimaryColumn;
	private TableViewerColumnComparator mActiveColumnComparator;
	private final DeferredContentProvider mContentProvider;

	MemoryStatisticsTableViewer(Composite parent, int style, Function<MemoryStatisticsItem, Color> colorProvider) {
		super(parent, style | SWT.VIRTUAL | SWT.FULL_SELECTION);

		// FIXME: Bug 165637 - [Viewers] ArrayIndexOutOfBoundsException exception in ConcurrentTableUpdator (used in DeferredContentProvider)
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=165637
		mContentProvider = new DeferredContentProvider((lhs, rhs) -> 0);
		mContentProvider.setFilter(element -> ((MemoryStatisticsItem) element).getSize() > 0);
		setContentProvider(mContentProvider);

		mPrimaryColumn = createTableColumnViewer("Name", MemoryStatisticsItem::getName, colorProvider,
				(lhs, rhs) -> lhs.getName().compareTo(rhs.getName()), false);

		createTableColumnViewer("Memory KB",
				model -> String.format("%d (%d%%)", model.getMemory() / 1024, model.getMemory() * 100 / mHeapSize),
				null, (lhs, rhs) -> (int) (lhs.getMemory() - rhs.getMemory()), true);

		createTableColumnViewer("Overhead KB",
				model -> String.format("%d (%d%%)", model.getOverhead() / 1024, model.getOverhead() * 100 / mHeapSize),
				null, (lhs, rhs) -> (int) (lhs.getOverhead() - rhs.getOverhead()), false);

		createTableColumnViewer("Objects", model -> String.valueOf(model.getSize()), null,
				(lhs, rhs) -> lhs.getSize() - rhs.getSize(), false);

		getTable().setLinesVisible(true);
		getTable().setHeaderVisible(true);
	}

	private TableViewerColumn createTableColumnViewer(
			String label, Function<MemoryStatisticsItem, String> labelProvider,
			Function<MemoryStatisticsItem, Color> colorProvider,
			BiFunction<MemoryStatisticsItem, MemoryStatisticsItem, Integer> comparator, boolean sort) {
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
				Color bg = event.gc.getBackground();
				Color fg = event.gc.getForeground();

				Point p = event.gc.stringExtent(labelProvider.apply((MemoryStatisticsItem) element));

				int margin = (bounds.height - p.y) / 2;
				int dx = bounds.x + margin;

				if (colorProvider != null) {
					event.gc.setBackground(colorProvider.apply((MemoryStatisticsItem) element));
					event.gc.fillArc(dx, bounds.y + margin + margin, p.y - margin - margin, p.y - margin - margin, 0,
							360);

					dx += p.y + margin;
				}

				event.gc.drawString(labelProvider.apply((MemoryStatisticsItem) element), dx, bounds.y + margin, true);

				event.gc.setBackground(bg);
				event.gc.setForeground(fg);
			}

			@Override
			protected void measure(Event event, Object element) {
				// no op
			}

			@Override
			protected void erase(Event event, Object element) {
				// no op
			}
		});

		TableViewerColumnComparator cmp = new TableViewerColumnComparator() {
			@Override
			int doCompare(Object e1, Object e2) {
				return comparator.apply((MemoryStatisticsItem) e1, (MemoryStatisticsItem) e2);
			}
		};

		cmp.init(column, sort);

		return column;
	}

	void setHeapSize(long size) {
		mHeapSize = size;
	}

	abstract class TableViewerColumnComparator implements Comparator {
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
			return (decreasing ? -1 : 1) * doCompare(e1, e2);
		}

		abstract int doCompare(Object e1, Object e2);
	}

	void setPrimaryColumnText(String text) {
		mPrimaryColumn.getColumn().setText(text);
	}
}

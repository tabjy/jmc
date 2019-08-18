package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.viewers.deferred.DeferredContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.openjdk.jmc.joverflow.ui.model.ReferrerItem;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

// ReferrerTreeViewer is actually a TableViewer with its tree-like content
class ReferrerTreeViewer extends TableViewer {

    private long mHeapSize;

    private TreeViewerColumnComparator mActiveColumnComparator;
    private DeferredContentProvider mContentProvider;

    ReferrerTreeViewer(Composite parent, int style) {
        super(parent, style | SWT.VIRTUAL | SWT.FULL_SELECTION);

        mContentProvider = new DeferredContentProvider((lhs, rhs) -> 0);
        mContentProvider.setFilter(element -> ((ReferrerItem) element).getSize() > 0);
        setContentProvider(mContentProvider);

        // TODO: change to a conversion method (from B to KB) that's not so primitive
        createTreeViewerColumn("Referrer",
                ReferrerItem::getName,
                (lhs, rhs) -> lhs.getName().compareTo(rhs.getName()),
                false, true);

        createTreeViewerColumn("Memory KiB",
                model -> String.format("%d (%d%%)", model.getMemory() / 1024, model.getMemory() * 100 / mHeapSize),
                (lhs, rhs) -> (int) (lhs.getMemory() - rhs.getMemory()),
                true, false);

        createTreeViewerColumn("Overhead KiB",
                model -> String.format("%d (%d%%)", model.getOvhd() / 1024, model.getOvhd() * 100 / mHeapSize),
                (lhs, rhs) -> (int) (lhs.getOvhd() - rhs.getOvhd()),
                false, false);

        createTreeViewerColumn("Objects",
                model -> String.valueOf(model.getSize()),
                (lhs, rhs) -> lhs.getSize() - rhs.getSize(),
                false, false);

        getTable().setLinesVisible(true);
        getTable().setHeaderVisible(true);
    }

    @SuppressWarnings("Duplicates")
    private void createTreeViewerColumn(String label, Function<ReferrerItem, String> labelProvider, BiFunction<ReferrerItem, ReferrerItem, Integer> comparator, boolean sort, boolean intent) {
        TableViewerColumn column = new TableViewerColumn(this, SWT.NONE);
        column.getColumn().setWidth(200);
        column.getColumn().setText(label);
        column.getColumn().setMoveable(true);

        column.setLabelProvider(new OwnerDrawLabelProvider() {
            @Override
            protected void paint(Event event, Object element) {
                Widget item = event.item;

                if (element == null) {
                    // FIXME: Bug 146799 https://bugs.eclipse.org/bugs/show_bug.cgi?id=146799
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
        });

        TreeViewerColumnComparator cmp = new TreeViewerColumnComparator() {
            @Override
            int doCompare(Object e1, Object e2) {
                return comparator.apply((ReferrerItem) e1, (ReferrerItem) e2);
            }
        };

        cmp.init(this, column, sort);
    }

    void setHeapSize(long size) {
        mHeapSize = size;
    }

    abstract class TreeViewerColumnComparator implements Comparator {
        private boolean decreasing = true;

        ColumnViewer mViewer;
        TableViewerColumn mColumn;

        void init(ColumnViewer viewer, TableViewerColumn column, boolean sorted) {
            mViewer = viewer;
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

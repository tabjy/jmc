package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.openjdk.jmc.joverflow.ui.model.ReferrerItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

class ReferrerTreeViewer<T extends ReferrerItem> extends TreeViewer {

    private long mTotalMemory;

    ReferrerTreeViewer(Composite parent, int style) {
        super(parent, style);

        setContentProvider(new ReferrerItemContentProvider());

        createTreeViewerColumn("Referrer",
                T::getName,
                (lhs, rhs) -> lhs.getName().compareTo(rhs.getName()));
        createTreeViewerColumn("Memory KB",
                model -> String.format("%d (%d%%)", model.getMemory() / 1024, model.getMemory() * 100 / mTotalMemory),
                (lhs, rhs) -> (int) (lhs.getMemory() - rhs.getMemory()),
                TreeViewerColumnComparator.Direction.Desc
        );

        createTreeViewerColumn("Overhead KB",
                model -> String.format("%d (%d%%)", model.getOvhd() / 1024, model.getOvhd() * 100 / mTotalMemory),
                (lhs, rhs) -> (int) (lhs.getOvhd() - rhs.getOvhd()));

        createTreeViewerColumn("Objects",
                model -> String.valueOf(model.getSize()),
                (lhs, rhs) -> lhs.getSize() - rhs.getSize());

        getTree().setLinesVisible(true);
        getTree().setHeaderVisible(true);
    }


    private void createTreeViewerColumn(String label, Function<T, String> labelProvider, BiFunction<T, T, Integer> comparator) {
        createTreeViewerColumn(label, labelProvider, comparator, null);
    }

    private void createTreeViewerColumn(String label, Function<T, String> labelProvider, BiFunction<T, T, Integer> comparator, TreeViewerColumnComparator.Direction sortDirection) {
        TreeViewerColumn column = new TreeViewerColumn(this, SWT.NONE);
        column.getColumn().setWidth(200);
        column.getColumn().setText(label);
        column.getColumn().setMoveable(true);

        column.setLabelProvider(new ColumnLabelProvider() {
            @SuppressWarnings("unchecked")
            @Override
            public String getText(Object element) {
                return labelProvider.apply((T) element);
            }
        });

        TreeViewerColumnComparator cmp = new TreeViewerColumnComparator(this, column) {
            @SuppressWarnings("unchecked")
            @Override
            protected int doCompare(Object e1, Object e2) {
                return comparator.apply((T) e1, (T) e2);
            }
        };

        if (sortDirection != null) {
            cmp.setSorter(sortDirection);
        }
    }

    private static class ReferrerItemContentProvider implements ITreeContentProvider {

        private Map<ReferrerItem, List<ReferrerItem>> parentToChildren = new HashMap<>();

        @SuppressWarnings("unchecked")
        @Override
        public Object[] getElements(Object inputElement) {
            List<ReferrerItem> items = (List<ReferrerItem>) inputElement;
            if (items.isEmpty()) {
                return new Object[0];
            }

            if (items.get(0).isBranch()) {
                return items.toArray();
            }

            ReferrerItem parent = items.get(0);

            for (int i = 1; i < items.size(); i++) {
                ReferrerItem item = items.get(i);
                if (!item.isBranch()) {
                    parentToChildren.putIfAbsent(parent, new ArrayList<>());
                    parentToChildren.get(parent).add(item);

                    parent = item;
                }
            }

            return new Object[]{items.get(0)};
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            ReferrerItem item = (ReferrerItem) parentElement;
            if (item.isBranch()) {
                return new Object[0];
            }

            return parentToChildren.get(item).toArray();
        }

        @Override
        public Object getParent(Object element) {
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            if (((ReferrerItem) element).isBranch()) {
                return false;
            }

            return parentToChildren.get(element) != null;
        }
    }

    static abstract class TreeViewerColumnComparator extends ViewerComparator {
        public enum Direction {
            Asc,
            Desc
        }

        private Direction direction = null;
        private TreeViewerColumn column;
        private ColumnViewer viewer;

        TreeViewerColumnComparator(ColumnViewer viewer, TreeViewerColumn column) {
            this.column = column;
            this.viewer = viewer;

            TreeViewerColumnComparator that = this;

            this.column.getColumn().addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (viewer.getComparator() == null) {
                        that.setSorter(Direction.Asc);
                        return;
                    }

                    if (viewer.getComparator() != that) {
                        that.setSorter(Direction.Asc);
                        return;
                    }

                    switch (that.direction) {
                        case Asc:
                            that.setSorter(Direction.Desc);
                            break;
                        case Desc:
                            that.setSorter(null);
                            break;
                    }
                }
            });
        }

        void setSorter(Direction direction) {
            this.direction = direction;

            Tree columnParent = column.getColumn().getParent();
            if (direction == null) {
                columnParent.setSortColumn(null);
                columnParent.setSortDirection(SWT.NONE);
                viewer.setComparator(null);
                return;
            }

            columnParent.setSortColumn(column.getColumn());
            columnParent.setSortDirection(direction == Direction.Asc ? SWT.UP : SWT.DOWN);

            if (viewer.getComparator() == this) {
                viewer.refresh();
            } else {
                viewer.setComparator(this);
            }
        }

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            int multiplier = 0;
            if (direction == Direction.Asc) {
                multiplier = 1;
            } else if (direction == Direction.Desc) {
                multiplier = -1;
            }
            return multiplier * doCompare(e1, e2);
        }

        abstract int doCompare(Object e1, Object e2);
    }

	void setTotalMemory(long memory) {
		mTotalMemory = memory;
	}
}

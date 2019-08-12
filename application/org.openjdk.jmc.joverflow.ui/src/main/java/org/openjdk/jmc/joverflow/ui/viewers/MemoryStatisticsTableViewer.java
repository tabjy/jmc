package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.openjdk.jmc.joverflow.ui.model.MemoryStatisticsItem;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

class MemoryStatisticsTableViewer<T extends MemoryStatisticsItem> extends TableViewer {

    private long mTotalMemory;
    private TableViewerColumn mPrimaryColumn;

    private Function<T, Color> mColorProvider;

    MemoryStatisticsTableViewer(Composite parent, int style, Function<T, Color> colorProvider) {
        super(parent, style);

        mColorProvider = colorProvider;
        setContentProvider(MemoryStatisticsItemContentProvider.getInstance());

        // TODO: change to a conversion method that's not so primitive
        mPrimaryColumn = createTableColumnViewer("Name",
                T::getName,
                mColorProvider,
                (lhs, rhs) -> lhs.getName().compareTo(rhs.getName()),
                null);

        createTableColumnViewer("Memory KB",
                model -> String.format("%d (%d%%)", model.getMemory() / 1024, model.getMemory() * 100 / mTotalMemory),
                null,
                (lhs, rhs) -> (int) (lhs.getMemory() - rhs.getMemory()),
                TableViewerColumnComparator.Direction.Desc);

        createTableColumnViewer("Overhead KB",
                model -> String.format("%d (%d%%)", model.getOverhead() / 1024, model.getOverhead() * 100 / mTotalMemory),
                null,
                (lhs, rhs) -> (int) (lhs.getOverhead() - rhs.getOverhead()),
                null);

        createTableColumnViewer("Objects",
                model -> String.valueOf(model.getSize()),
                null,
                (lhs, rhs) -> lhs.getSize() - rhs.getSize(),
                null);

        getTable().setLinesVisible(true);
        getTable().setHeaderVisible(true);
    }

    private TableViewerColumn createTableColumnViewer(String label, Function<T, String> labelProvider, Function<T, Color> colorProvider, BiFunction<T, T, Integer> comparator, TableViewerColumnComparator.Direction sortDirection) {
        TableViewerColumn column = new TableViewerColumn(this, SWT.NONE);
        column.getColumn().setWidth(200);
        column.getColumn().setText(label);
        column.getColumn().setMoveable(true);

        column.setLabelProvider(new OwnerDrawLabelProvider() {
            @Override
            protected void measure(Event event, Object element) {
                // no op
            }

            @Override
            protected void paint(Event event, Object element) {
                Widget item = event.item;
                Rectangle bounds = ((TableItem) item).getBounds(event.index);
                Color bg = event.gc.getBackground();
                Color fg = event.gc.getForeground();

                Point p = event.gc.stringExtent(labelProvider.apply((T) element));

                int margin = (bounds.height - p.y) / 2;
                int dx = bounds.x + margin;

                if (colorProvider != null) {
                    event.gc.setBackground(colorProvider.apply((T) element));
                    event.gc.fillArc(dx, bounds.y + margin + margin, p.y - margin - margin, p.y - margin - margin, 0, 360);

                    dx += p.y + margin;
                }

                event.gc.setBackground(bg);
                event.gc.setForeground(fg);

                event.gc.drawString(labelProvider.apply((T) element), dx, bounds.y + margin, true);
            }

            @Override
            protected void erase(Event event, Object element) {
                // no op
            }
        });

        TableViewerColumnComparator cmp = new TableViewerColumnComparator(this, column) {
            @SuppressWarnings("unchecked")
            @Override
            protected int doCompare(Object e1, Object e2) {
                return comparator.apply((T) e1, (T) e2);
            }
        };

        if (sortDirection != null) {
            cmp.setSorter(sortDirection);
        }

        return column;
    }

    void setTotalMemory(long memory) {
        mTotalMemory = memory;
    }

    static abstract class TableViewerColumnComparator extends ViewerComparator {
        public enum Direction {
            Asc,
            Desc
        }

        private Direction direction = null;
        private TableViewerColumn column;
        private ColumnViewer viewer;

        TableViewerColumnComparator(ColumnViewer viewer, TableViewerColumn column) {
            this.column = column;
            this.viewer = viewer;

            TableViewerColumnComparator that = this;

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

            Table columnParent = column.getColumn().getParent();
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

    static class MemoryStatisticsItemContentProvider extends ArrayContentProvider {

        private static MemoryStatisticsItemContentProvider instance;

        public static MemoryStatisticsItemContentProvider getInstance() {
            synchronized (MemoryStatisticsItemContentProvider.class) {
                if (instance == null) {
                    instance = new MemoryStatisticsItemContentProvider();
                }
                return instance;
            }
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return Arrays
                    .stream(super.getElements(inputElement))
                    .filter(x -> ((MemoryStatisticsItem) x).getSize() > 0)
                    .toArray();
        }
    }

    void setPrimaryColumnText(String text) {
        mPrimaryColumn.getColumn().setText(text);
    }
}




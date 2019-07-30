package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ReferrerTreeViewer<T extends ReferrerItem> extends TreeViewer {

    private long mTotalMemory = 100000; // TODO

    public ReferrerTreeViewer(Composite parent, int style) {
        super(parent, style);

        setContentProvider(ReferrerItemContentProvider.getInstance());

        createTreeViewerColumn("Referrer",
                T::getName,
                (lhs, rhs) -> lhs.getName().compareTo(rhs.getName()));
        createTreeViewerColumn("Memory KB",
                model -> String.format("%d (%d%%)", model.getMemory() / 1024, model.getMemory() * 100 / mTotalMemory),
                (lhs, rhs) -> (int) (lhs.getMemory() - rhs.getMemory())//, // TODO
//                MemoryStatisticsTableViewer.ColumnViewerComparator.Direction.Desc
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
        TreeViewerColumn column = new TreeViewerColumn(this, SWT.NONE);
        column.getColumn().setWidth(200);
        column.getColumn().setText(label);
        column.getColumn().setMoveable(true);

        column.setLabelProvider(new ColumnLabelProvider() {
            @SuppressWarnings("unchecked")
            @Override
            public String getText(Object element) {
                List<ReferrerItem> items = (List<ReferrerItem>) element;
                return labelProvider.apply((T) items.get(0));
            }
        });
    }

    private static class ReferrerItemContentProvider implements ITreeContentProvider {

        private static ReferrerItemContentProvider instance;

        public static ReferrerItemContentProvider getInstance() {
            synchronized (ReferrerItemContentProvider.class) {
                if (instance == null) {
                    instance = new ReferrerItemContentProvider();
                }
                return instance;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object[] getElements(Object inputElement) {
            List<ReferrerItem> items = (List<ReferrerItem>) inputElement;
            if (items.isEmpty()) {
                return new Object[0];
            }

            if (items.get(0).isBranch()) {
                Object[] res = new Object[items.size()];
                for (int i = 0; i < items.size(); i++) {
                    res[i] = items.subList(i, i + 1);
                }

                return res;
            }

            return new Object[]{items};
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object[] getChildren(Object parentElement) {
            List<ReferrerItem> items = (List<ReferrerItem>) parentElement;
            ReferrerItem item = items.get(0);
            if (item.isBranch()) {
                return new Object[0];
            }

            return new Object[]{items.subList(1, items.size())};
        }

        @Override
        public Object getParent(Object element) {
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean hasChildren(Object element) {
            List<ReferrerItem> items = (List<ReferrerItem>) element;
            return !items.get(0).isBranch();
        }
    }
    // TODO
}

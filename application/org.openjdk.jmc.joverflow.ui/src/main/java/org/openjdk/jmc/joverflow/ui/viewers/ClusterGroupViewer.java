package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.openjdk.jmc.joverflow.support.RefChainElement;
import org.openjdk.jmc.joverflow.ui.model.MemoryStatisticsItem;
import org.openjdk.jmc.joverflow.ui.model.ModelListener;
import org.openjdk.jmc.joverflow.ui.model.ObjectCluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.List;

public class ClusterGroupViewer extends ContentViewer implements ModelListener {

    private Label mTitle;
    private Button mPieChart;
    private Group mFilterContainer;

    private String mQualifierName;
    private final Map<Object, MemoryStatisticsItem> items = new HashMap<>();
    private final MemoryStatisticsTableViewer<MemoryStatisticsItem> mTableViewer;
    private List<Filter> mFilters = new ArrayList<>();

    private boolean mAllIncluded = false;

    public ClusterGroupViewer(Composite parent, int style) {
        SashForm bottomLeftSash = new SashForm(parent, SWT.NONE);
        Group classPieChartContainer = new Group(bottomLeftSash, SWT.NONE);
        classPieChartContainer.setLayout(new FillLayout(SWT.VERTICAL));

        mTitle = new Label(classPieChartContainer, SWT.NONE);

        mPieChart = new Button(classPieChartContainer, SWT.NONE);
        mPieChart.setText("[Pie Chart]");

        mFilterContainer = new Group(classPieChartContainer, SWT.NONE);
        mFilterContainer.setLayout(new FillLayout(SWT.VERTICAL));

        Group classTableContainer = new Group(bottomLeftSash, SWT.NONE);
        classTableContainer.setLayout(new FillLayout(SWT.HORIZONTAL));

        mTableViewer = new MemoryStatisticsTableViewer<>(classTableContainer, SWT.BORDER | SWT.FULL_SELECTION);

        mTableViewer.addSelectionChangedListener((event) -> {
            if (event.getStructuredSelection().isEmpty()) {
                return;
            }
            MemoryStatisticsItem item = (MemoryStatisticsItem) event.getStructuredSelection().getFirstElement();
            Filter filter = new Filter(mQualifierName, item.getId().toString(), false);
            mFilters.add(filter);
            Button button = new Button(mFilterContainer, SWT.NONE);
            button.setText((mQualifierName == null ? "Class" : mQualifierName) + " = " + item.getId().toString());
            button.addMouseListener(new MouseListener() {
                @Override
                public void mouseDoubleClick(MouseEvent e) {
                    // intentionally empty
                }

                @Override
                public void mouseDown(MouseEvent e) {
                    mFilters.remove(filter);
                    button.dispose();

                    // TODO: investigate why layout is not auto updated
                    mFilterContainer.layout(true, true);
                    mTableViewer.getTable().setFocus();
                    mTableViewer.setSelection(StructuredSelection.EMPTY, true);
                }

                @Override
                public void mouseUp(MouseEvent e) {
                    // intentionally empty
                }
            });

            // TODO: investigate why layout is not auto updated
            mFilterContainer.layout(true, true);
        });
    }
    
    @Override
    public Control getControl() {
        return mTableViewer.getControl();
    }

    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        mTableViewer.addSelectionChangedListener(listener);
    }

    @Override
    public ISelection getSelection() {
        return null;
    }

    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        mTableViewer.removeSelectionChangedListener(listener);
    }

    @Override
    public void refresh() {
        mTableViewer.refresh();
    }

    @Override
    public void setSelection(ISelection selection, boolean reveal) {
        mTableViewer.setSelection(selection, reveal);
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
        mTableViewer.setInput(items.values());
        mAllIncluded = true;
    }
    
    public void setQualifierName(String qualifierName) {
        mQualifierName = qualifierName;
        String text = qualifierName != null ? qualifierName : "Class";
        mTitle.setText(text);
        mTableViewer.setPrimaryColumnText(text);
    }
    
    public void setTotalMemory(long memory) {
        mTableViewer.setTotalMemory(memory);
    }

    class Filter implements Predicate<ObjectCluster> {
        private boolean mNegated;
        private String mName;
        private String mItem;

        Filter (String name, String item, boolean exclude) {
            mName = name;
            mNegated = exclude;
            mItem = item;
        }

        @Override
        public boolean test(ObjectCluster oc) {
            if (mName == null) {
                return mItem.equals(oc.getClassName()) ^ mNegated;
            }

            if (oc.getQualifier() == null) {
                return true;
            }

            return mItem.equals(oc.getQualifier()) ^ mNegated;
        }
    }

    public Predicate<ObjectCluster> getFilter() {
        Predicate<ObjectCluster> res = oc -> true;
        for (Filter filter : mFilters) {
            res = res.and(filter);
        }

        return res;
    }
}

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
    private final MemoryStatisticsTableViewer<MemoryStatisticsItem> mTableViewer;

    private String mQualifierName;
    private final Map<Object, MemoryStatisticsItem> items = new HashMap<>();
    private List<Predicate<ObjectCluster>> mFilters = new ArrayList<>();

    private boolean mAllIncluded = false;

    public ClusterGroupViewer(Composite parent, int style) {
        SashForm bottomLeftSash = new SashForm(parent, SWT.NONE);
        Group pieChartContainer = new Group(bottomLeftSash, SWT.NONE);
        pieChartContainer.setLayout(new FillLayout(SWT.VERTICAL));

        mTitle = new Label(pieChartContainer, SWT.NONE);

        mPieChart = new Button(pieChartContainer, SWT.NONE);
        mPieChart.setText("[Pie Chart]");

        mFilterContainer = new Group(pieChartContainer, SWT.NONE);
        mFilterContainer.setLayout(new FillLayout(SWT.VERTICAL));

        Group classTableContainer = new Group(bottomLeftSash, SWT.NONE);
        classTableContainer.setLayout(new FillLayout(SWT.HORIZONTAL));

        mTableViewer = new MemoryStatisticsTableViewer<>(classTableContainer, SWT.BORDER | SWT.FULL_SELECTION);
        mTableViewer.addSelectionChangedListener((event) -> {
            if (event.getStructuredSelection().isEmpty()) {
                return;
            }
            MemoryStatisticsItem item = (MemoryStatisticsItem) event.getStructuredSelection().getFirstElement();

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

    public Predicate<ObjectCluster> getFilter() {
        Predicate<ObjectCluster> res = oc -> true;
        for (Predicate<ObjectCluster> filter : mFilters) {
            res = res.and(filter);
        }

        return res;
    }

    public void reset() {
        mFilters.clear();
        for (Control filter : mFilterContainer.getChildren()) {
            filter.dispose();
        }
    }
}

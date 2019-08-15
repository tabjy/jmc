package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.openjdk.jmc.flightrecorder.ui.FlightRecorderUI;
import org.openjdk.jmc.joverflow.support.RefChainElement;
import org.openjdk.jmc.joverflow.ui.model.MemoryStatisticsItem;
import org.openjdk.jmc.joverflow.ui.model.ObjectCluster;
import org.openjdk.jmc.joverflow.ui.util.ColorIndexedArcAttributeProvider;
import org.openjdk.jmc.joverflow.ui.util.ConcurrentModelInputWrapper;

import java.util.*;
import java.util.List;
import java.util.function.Predicate;

public class AncestorViewer extends BaseViewer {
    private Composite mContainer;
    private PieChartViewer mPieChart;
    private Group mFilterContainer;
    private Text mInput;
    private final MemoryStatisticsTableViewer mTableViewer;

    private String mPrefix = "";
    private List<Predicate<RefChainElement>> mFilters = new ArrayList<>();

    private RefChainElement lastRef;
    private MemoryStatisticsItem lastItem;
    private final Map<Object, MemoryStatisticsItem> items = new HashMap<>();
    private ConcurrentModelInputWrapper mInputModel = new ConcurrentModelInputWrapper();

    private boolean mAllIncluded = false;

    public AncestorViewer(Composite parent, int style) {
        mContainer = new SashForm(parent, style);

        {
            Group leftContainer = new Group(mContainer, SWT.NONE);
            leftContainer.setLayout(new FillLayout(SWT.VERTICAL));

            Label ancestorReferrerLabel = new Label(leftContainer, SWT.NONE);
            ancestorReferrerLabel.setText("Ancestor referrer");

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

            Group prefixContainer = new Group(leftContainer, SWT.NONE);
            prefixContainer.setLayout(new FillLayout(SWT.VERTICAL));

            {
                Label label = new Label(prefixContainer, SWT.NONE);
                label.setText("Ancestor prefix");

                mInput = new Text(prefixContainer, SWT.BORDER);

                Group buttonContainer = new Group(prefixContainer, SWT.NONE);
                buttonContainer.setLayout(new RowLayout(SWT.HORIZONTAL));

                Button clear = new Button(buttonContainer, SWT.NONE);
                Button update = new Button(buttonContainer, SWT.NONE);
                clear.setText("Clear");
                update.setText("Update");
                clear.addMouseListener(new MouseListener() {
                    @Override
                    public void mouseDoubleClick(MouseEvent e) {
                        // intentionally empty
                    }

                    @Override
                    public void mouseDown(MouseEvent e) {
                        mInput.setText("");
                        updatePrefixFilter();
                    }

                    @Override
                    public void mouseUp(MouseEvent e) {
                        // intentionally empty
                    }
                });
                update.addMouseListener(new MouseListener() {
                    @Override
                    public void mouseDoubleClick(MouseEvent e) {
                        // intentionally empty
                    }

                    @Override
                    public void mouseDown(MouseEvent e) {
                        updatePrefixFilter();
                    }

                    @Override
                    public void mouseUp(MouseEvent e) {
                        // intentionally empty
                    }
                });

            }

            mFilterContainer = new Group(leftContainer, SWT.NONE);
            mFilterContainer.setLayout(new FillLayout(SWT.VERTICAL));
        }

        {
            Group tableContainer = new Group(mContainer, SWT.NONE);
            tableContainer.setLayout(new FillLayout(SWT.HORIZONTAL));

            mTableViewer = new MemoryStatisticsTableViewer(tableContainer, SWT.BORDER | SWT.FULL_SELECTION,
                    (e) -> mPieChart.getArcAttributeProvider().getColor(e));
            mTableViewer.setInput(mInputModel);

            mTableViewer.addSelectionChangedListener((event) -> {
                if (event.getStructuredSelection().isEmpty()) {
                    return;
                }
                MemoryStatisticsItem item = (MemoryStatisticsItem) event.getStructuredSelection().getFirstElement();
                if (item.getId() == null) {
                    return;
                }

                String ancestor = item.getId().toString();
                boolean excluded = false;
                Predicate<RefChainElement> filter = (referrer) -> {
                    while (referrer != null) {
                        String refName = referrer.toString();
                        if (ancestor.equals(refName)) {
                            return !excluded;
                        }
                        referrer = referrer.getReferer();
                    }
                    return excluded;
                };
                mFilters.add(filter);

                Button button = new Button(mFilterContainer, SWT.NONE);
                button.setText("Ancestors" + (excluded ? " \u220C " : " \u220B ") + ancestor);
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
                        notifyFilterChangedListeners();
                    }

                    @Override
                    public void mouseUp(MouseEvent e) {
                        // intentionally empty
                    }
                });

                // TODO: investigate why layout is not auto updated
                mFilterContainer.layout(true, true);

                notifyFilterChangedListeners();
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

    private String getAncestorReferrer(RefChainElement referrer) {
        while (referrer != null) {
            if (referrer.getJavaClass() == null) {
                if (referrer.getReferer() != null) {
                    FlightRecorderUI.getDefault().getLogger()
                            .warning("JavaClass for " + referrer + " is null but referrer is " + referrer.getReferer());
                }
                break; // GC root
            } else if (referrer.toString().startsWith(mPrefix)) {
                return referrer.toString();
            }
            referrer = referrer.getReferer();
        }
        return null;
    }

    @Override
    public void include(ObjectCluster oc, RefChainElement ref) {
        if (mAllIncluded) {
            for (MemoryStatisticsItem item : items.values()) {
                item.reset();
            }
            mAllIncluded = false;
        }

        if (ref != lastRef) {
            lastRef = ref;
            String s = getAncestorReferrer(ref);
            lastItem = items.get(s);
            if (lastItem == null) {
                lastItem = new MemoryStatisticsItem(s, 0, 0, 0);
                items.put(s, lastItem);
            }
        }
        lastItem.addObjectCluster(oc);
    }

    @Override
    public void allIncluded() {
        Collection<MemoryStatisticsItem> values = items.values();

        mInputModel.setInput(values);
        mPieChart.setInput(values);

        mAllIncluded = true;
        lastRef = null;
    }

    private void updatePrefixFilter() {
        mPrefix = mInput.getText();

        if (mTableViewer != null) {
            notifyFilterChangedListeners();
        }
    }

    @Override
    public void setHeapSize(long size) {
        mTableViewer.setHeapSize(size);
    }

    @Override
    public boolean filter(ObjectCluster oc) {
        return true;
    }

    @Override
    public boolean filter(RefChainElement rce) {
        Predicate<RefChainElement> res = in -> true;
        for (Predicate<RefChainElement> filter : mFilters) {
            res = res.and(filter);
        }

        return res.test(rce);
    }

    @Override
    public void reset() {
        mFilters.clear();
        for (Control filter : mFilterContainer.getChildren()) {
            filter.dispose();
        }
        mInput.setText("");
        updatePrefixFilter();
    }
}

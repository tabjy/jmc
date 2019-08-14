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
import org.openjdk.jmc.joverflow.ui.model.ModelListener;
import org.openjdk.jmc.joverflow.ui.model.ObjectCluster;
import org.openjdk.jmc.joverflow.ui.util.ColorIndexedArcAttributeProvider;
import org.openjdk.jmc.joverflow.ui.util.ConcurrentModelInputWrapper;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;

public class AncestorViewer extends ContentViewer implements ModelListener {
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
        SashForm bottomRightSash = new SashForm(parent, SWT.NONE);
        Group pieChartContainer = new Group(bottomRightSash, SWT.NONE);
        pieChartContainer.setLayout(new FillLayout(SWT.VERTICAL));

        Label ancestorReferrerLabel = new Label(pieChartContainer, SWT.NONE);
        ancestorReferrerLabel.setText("Ancestor referrer");

        mPieChart = new PieChartViewer(pieChartContainer, SWT.BORDER);
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

        Group prefixContainer = new Group(pieChartContainer, SWT.NONE);
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

        mFilterContainer = new Group(pieChartContainer, SWT.NONE);
        mFilterContainer.setLayout(new FillLayout(SWT.VERTICAL));

        Group ancestorReferrerTableContainer = new Group(bottomRightSash, SWT.NONE);
        ancestorReferrerTableContainer.setLayout(new FillLayout(SWT.HORIZONTAL));

        mTableViewer = new MemoryStatisticsTableViewer(ancestorReferrerTableContainer,
                SWT.BORDER | SWT.FULL_SELECTION, (e) -> mPieChart.getArcAttributeProvider().getColor(e));
        mTableViewer.setInput(mInputModel);

        mTableViewer.addSelectionChangedListener((event) -> {
            if (event.getStructuredSelection().isEmpty()) {
                return;
            }
            MemoryStatisticsItem item = (MemoryStatisticsItem) event.getStructuredSelection().getFirstElement();

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
        return null;
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

    }

    @Override
    public void setSelection(ISelection selection, boolean reveal) {

    }

    private String getAncestorReferrer(RefChainElement referrer) {
        while (referrer != null) {
            if (referrer.getJavaClass() == null) {
                if (referrer.getReferer() != null) {
                    FlightRecorderUI.getDefault().getLogger().warning("JavaClass for " + referrer + " is null but referrer is " + referrer.getReferer());
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

        Instant then = Instant.now();
        mInputModel.setInput(values);
        System.out.println("AncestorViewer table took " + Duration.between(Instant.now(), then));

        then = Instant.now();
        mPieChart.setInput(values);
        System.out.println("AncestorViewer pie chart took " + Duration.between(Instant.now(), then));

        mAllIncluded = true;
        lastRef = null;

        System.out.println("AncestorViewer has " + items.values().size() + " entries");
    }

    private void updatePrefixFilter() {
        mPrefix = mInput.getText();

        if (mTableViewer != null) {
            mTableViewer.getTable().setFocus();
            mTableViewer.setSelection(StructuredSelection.EMPTY, true);
        }
    }

    public Predicate<RefChainElement> getFilter() {
        Predicate<RefChainElement> res = rce -> true;
        for (Predicate<RefChainElement> filter : mFilters) {
            res = res.and(filter);
        }

        return res;
    }

    public void setTotalMemory(long memory) {
        mTableViewer.setTotalMemory(memory);
    }

    public void reset() {
        mFilters.clear();
        for (Control filter : mFilterContainer.getChildren()) {
            filter.dispose();
        }
        mInput.setText("");
        updatePrefixFilter();
    }
}

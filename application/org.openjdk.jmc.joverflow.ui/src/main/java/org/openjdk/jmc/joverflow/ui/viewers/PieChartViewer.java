package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.openjdk.jmc.joverflow.ui.swt.ArcItem;
import org.openjdk.jmc.joverflow.ui.swt.PieChart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class PieChartViewer extends StructuredViewer {

    private PieChart pieChart;
    private IArcAttributeProvider arcAttributeProvider = new ArcAttributeProvider();

    private List<?> inputs = new ArrayList<>();

    public PieChartViewer(Composite parent) {
        this(parent, SWT.BORDER);
    }

    public PieChartViewer(Composite parent, int style) {
        this(new PieChart(parent, style));
    }

    public PieChartViewer(PieChart pieChart) {
        this.pieChart = pieChart;
    }

    public PieChart getPieChart() {
        return pieChart;
    }

    @Override
    protected Widget doFindInputItem(Object element) {
        if (equals(element, getRoot())) {
            return getControl();
        }

        return null;
    }

    @Override
    protected Widget doFindItem(Object element) {
        if (inputs.contains(element)) {
            return pieChart.getItem(inputs.indexOf(element));
        }
        return null;
    }

    @Override
    protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
        updateItems();
    }

    @Override
    protected List getSelectionFromWidget() {
        List<Object> res = new ArrayList<>();
        if (pieChart.getHighlightedItem() == null) {
            return res;
        }

        int i = pieChart.getHighlightedItemIndex();
        if (i == -1) {
            return res;
        }

        res.add(inputs.get(i));
        return res;
    }

    @Override
    protected void internalRefresh(Object element) {
        updateItems();
    }

    @Override
    protected void inputChanged(Object input, Object oldInput) {
        inputs = Arrays.asList(getSortedChildren(getRoot()));
        pieChart.setHighlightedItem(null);
        updateItems();
    }

    @Override
    public void reveal(Object element) {
        // intentionally empty
    }

    @Override
    protected void setSelectionToWidget(List l, boolean reveal) {
        if (l == null) {
            pieChart.setHighlightedItem(null);
            return;
        }

        if (l.size() == 0) {
            return;
        }

        pieChart.setHighlightedItem((ArcItem) doFindItem(l.get(0)));
        pieChart.redraw();
    }

    @Override
    public Control getControl() {
        return pieChart;
    }

    public void setArcAttributeProvider(IArcAttributeProvider provider) {
        if (arcAttributeProvider == null) {
            arcAttributeProvider = new ArcAttributeProvider();
        } else {
            arcAttributeProvider = provider;
        }

    }

    private void updateItems() {
        while (pieChart.getItemCount() < inputs.size()) {
            new ArcItem(pieChart, SWT.BORDER);
        }

        while (inputs.size() < pieChart.getItemCount()) {
            pieChart.removeItem(pieChart.getItemCount() - 1);
        }

        double weightSum = 0;
        for (Object input : inputs) {
            weightSum += arcAttributeProvider.getWeight(input);
        }

        int sum = 0;
        for (int i = 0; i < inputs.size(); i++) {
            Object input = inputs.get(i);
            ArcItem item = pieChart.getItem(i);

            int w = (int) Math.round(360 * (double) arcAttributeProvider.getWeight(input) / weightSum);
            sum += w;
            item.setAngle(w);
            item.setColor(arcAttributeProvider.getColor(input));
        }

        // fix rounding error
        if (sum != 0 && sum != 360) {
            for (int i = inputs.size() - 1; i >= 0; i--) {
                Object input = inputs.get(i);
                ArcItem item = pieChart.getItem(i);

                int w = 360 - sum + (int) Math.round(360 * (double) arcAttributeProvider.getWeight(input) / weightSum);
                if (w < 0) {
                    continue;
                }
                item.setAngle(w);
                break;
            }
        }
    }
}
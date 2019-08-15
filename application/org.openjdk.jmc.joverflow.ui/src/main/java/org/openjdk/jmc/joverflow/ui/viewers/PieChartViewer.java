package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import org.openjdk.jmc.joverflow.ui.swt.ArcItem;
import org.openjdk.jmc.joverflow.ui.swt.PieChart;
import org.openjdk.jmc.joverflow.ui.util.ArcAttributeChangedEvent;
import org.openjdk.jmc.joverflow.ui.util.BaseArcAttributeProvider;
import org.openjdk.jmc.joverflow.ui.util.IArcAttributeProvider;
import org.openjdk.jmc.joverflow.ui.util.IArcAttributeProviderListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class PieChartViewer extends StructuredViewer {

    private PieChart mPieChart;
    private IArcAttributeProvider mArcAttributeProvider = new BaseArcAttributeProvider();
    private ArcItem mOtherArc;
    private int mMinimumArcAngle = 0;

    private List<Object> mInputs = new ArrayList<>();

    public PieChartViewer(Composite parent) {
        this(parent, SWT.NONE);
    }

    public PieChartViewer(Composite parent, int style) {
        this(new PieChart(parent, style));
    }

    public PieChartViewer(PieChart pieChart) {
        mPieChart = pieChart;
    }

    public PieChart getPieChart() {
        return mPieChart;
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
        if (mInputs.contains(element)) {
            return mPieChart.getItem(mInputs.indexOf(element));
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
        if (mPieChart.getHighlightedItem() == null) {
            return res;
        }

        int i = mPieChart.getHighlightedItemIndex();
        if (i == -1) {
            return res;
        }

        res.add(mInputs.get(i));
        return res;
    }

    @Override
    protected void internalRefresh(Object element) {
        updateItems();
    }

    @Override
    protected void inputChanged(Object input, Object oldInput) {
        mInputs = Arrays.asList(getSortedChildren(getRoot()));
        mPieChart.setHighlightedItem(null);
        updateItems();
    }

    @Override
    public void reveal(Object element) {
        // intentionally empty
    }

    @Override
    protected void setSelectionToWidget(List l, boolean reveal) {
        if (l == null) {
            mPieChart.setHighlightedItem(null);
            return;
        }

        if (l.size() == 0) {
            return;
        }

        mPieChart.setHighlightedItem((ArcItem) doFindItem(l.get(0)));
        mPieChart.redraw();
    }

    @Override
    public Control getControl() {
        return mPieChart;
    }

    public void setArcAttributeProvider(IArcAttributeProvider provider) {
        if (mArcAttributeProvider == null) {
            mArcAttributeProvider = new BaseArcAttributeProvider();
        } else {
            mArcAttributeProvider = provider;
        }
    }

    public IArcAttributeProvider getArcAttributeProvider() {
        return mArcAttributeProvider;
    }

    private void updateItems() {
        ArcAttributeChangedEvent event = new ArcAttributeChangedEvent(mArcAttributeProvider, mInputs.toArray());
        for (IArcAttributeProviderListener l : mArcAttributeProvider.getListenerList()) {
            l.arcAttributeProviderChanged(event);
        }

        int weightSum = 0;
        for (Object input : mInputs) {
            weightSum += mArcAttributeProvider.getWeight(input);
        }

        double otherAngle = 0;
        List<Object> inputs = new ArrayList<>();
        List<Double> angles = new ArrayList<>();
        for (Object input: mInputs) {
            double angle = 360f * (double) mArcAttributeProvider.getWeight(input) / (double) weightSum;
            if (angle >= mMinimumArcAngle) {
                inputs.add(input);
                angles.add(angle);
            } else {
                otherAngle += angle;
            }
        }

        while (mPieChart.getItemCount() < inputs.size()) {
            new ArcItem(mPieChart, SWT.NONE);
        }

        while (inputs.size() < mPieChart.getItemCount()) {
            mPieChart.removeItem(mPieChart.getItemCount() - 1);
        }

        if (otherAngle != 0) {
            mOtherArc = new ArcItem(mPieChart, SWT.NONE);
        }

        int angleSum = 0;
        for (int i = 0; i < inputs.size(); i++) {
            Object input = inputs.get(i);
            ArcItem item = mPieChart.getItem(i);

            int w = (int) Math.round(angles.get(i));
            angleSum += w;
            item.setAngle(w);
            item.setColor(mArcAttributeProvider.getColor(input));
        }

        if (otherAngle != 0) {
            int w = 360 - angleSum;
            if (w > 0) {
                mOtherArc.setAngle(w);
                mOtherArc.setColor(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
                return;
            }
        } else {
            mOtherArc = null;
        }

        // fix rounding error
        if (angleSum != 0 && angleSum != 360 && inputs.size() != 0) {
            for (int i = inputs.size() - 1; i >= 0; i--) {
                Object input = inputs.get(i);
                ArcItem item = mPieChart.getItem(i);

                int w = 360 - angleSum + (int) Math.round(360 * (double) mArcAttributeProvider.getWeight(input) / weightSum);
                if (w < 0) {
                    continue;
                }
                item.setAngle(w);
                break;
            }
        }
    }

    public void setMinimumArcAngle(int angle) {
        mMinimumArcAngle = angle;
    }

    @Override
    protected void handleDispose(DisposeEvent event) {
        mArcAttributeProvider.dispose();
        super.handleDispose(event);
    }
}
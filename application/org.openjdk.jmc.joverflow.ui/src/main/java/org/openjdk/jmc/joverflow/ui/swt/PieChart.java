package org.openjdk.jmc.joverflow.ui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

import java.util.ArrayList;
import java.util.List;

public class PieChart extends Canvas {
    private static final int MARGIN = 5;

    private double mZoomRatio = 1;

    private final List<ArcItem> mArcs = new ArrayList<>();
    private ArcItem mHighlightedItem;

    public PieChart(Composite parent, int style) {
        super(parent, style);

        setLayout(new FillLayout());

        addPaintListener((e) -> {
            int x = this.getClientArea().width / 2;
            int y = this.getClientArea().height / 2;

            Point center = new Point(x, y);

            int radius = Math.min(x, y) - MARGIN;
            radius = (int) (radius / mZoomRatio);
            if (radius < 0) {
                radius = 0;
            }

            int startAngle = 0;
            Color background = e.gc.getBackground();
            for (ArcItem item : mArcs) {
                e.gc.setBackground(background);
                if (mHighlightedItem == item) {
                    item.paintArc(e.gc, center, radius, startAngle, mZoomRatio, (style & SWT.BORDER) == SWT.BORDER);
                } else {
                    item.paintArc(e.gc, center, radius, startAngle, 1, (style & SWT.BORDER) == SWT.BORDER);
                }
                startAngle += item.getAngle();
            }

            if (startAngle < 360 && (style & SWT.BORDER) == SWT.BORDER) {
                e.gc.drawArc(center.x - radius, center.y - radius, radius * 2, radius * 2, startAngle, 360 - startAngle);
            }
        });

        addListener(SWT.Resize, (Event e) -> redraw());

        addMouseMoveListener(e -> {
            mHighlightedItem = getItem(new Point(e.x, e.y));
            redraw();
        });
    }

    void createItem(ArcItem arc, int i) {
        if (i < 0 || i > mArcs.size()) {
            SWT.error(SWT.ERROR_INVALID_RANGE);
            return;
        }

        if (i == mArcs.size()) {
            mArcs.add(arc);
            return;
        }

        mArcs.get(i).dispose();
        mArcs.set(i, arc);
    }

    public int getItemCount() {
        return mArcs.size();
    }

    public ArcItem[] getItems() {
        return mArcs.toArray(new ArcItem[0]);
    }

    public ArcItem getItemByAngle(int angle) {
        angle %= 360;

        if (angle < 0) {
            angle += 360;
        }

        int startAngle = 0;
        for (ArcItem item : mArcs) {
            if (angle > startAngle && angle < startAngle + item.getAngle()) {
                return item;
            }

            startAngle += item.getAngle();
        }

        return null;
    }

    // Get item via a (x, y) coordinate on the canvas. Useful for handling mouse events.
    public ArcItem getItem(Point point) {
        int x = this.getClientArea().width / 2;
        int y = this.getClientArea().height / 2;

        int radius = Math.min(x, y) - MARGIN;
        radius = (int) (radius / mZoomRatio);

        if (radius < 0) {
            radius = 0;
        }

        x = point.x - x;
        y = y - point.y;

        ArcItem item = getItemByAngle((int) Math.toDegrees(Math.atan2(y, x)));
        if (item == null) {
            return null;
        }

        if (item == mHighlightedItem && Math.sqrt(x * x + y * y) < (radius * mZoomRatio)) {
            return item;
        }

        if (Math.sqrt(x * x + y * y) < radius) {
            return item;
        }

        return null;
    }

    public ArcItem getItem(int index) {
        return mArcs.get(index);
    }

    public int indexOf(ArcItem item) {
        return mArcs.indexOf(item);
    }

    public void removeItem(int index) {
        mArcs.get(index).dispose();
        mArcs.remove(index);
    }

    public void setZoomRatio(double ratio) {
        mZoomRatio = ratio;
    }

    public ArcItem getHighlightedItem() {
        return mHighlightedItem;
    }

    public int getHighlightedItemIndex() {
        return indexOf(mHighlightedItem);
    }

    public void setHighlightedItem(ArcItem item) {
        if (item == null) {
            mHighlightedItem = null;
            return;
        }

        if (!mArcs.contains(item)) {
            throw new IllegalArgumentException("invalid item");
        }

        mHighlightedItem = item;
    }
}

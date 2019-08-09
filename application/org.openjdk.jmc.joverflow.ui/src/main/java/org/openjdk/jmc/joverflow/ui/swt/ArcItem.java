package org.openjdk.jmc.joverflow.ui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Item;

public class ArcItem extends Item {
    private PieChart mParent;
    private int mStyle;

    private int mAngle = 0;
    private Color mColor;

    public ArcItem(PieChart parent, int style) {
        this(parent, style, parent.getItemCount());
    }

    public ArcItem(PieChart parent, int style, int index) {
        super(parent, style, index);
        this.mParent = parent;
        this.mStyle = style;

        parent.createItem(this, index);
    }

    public void setAngle(int mAngle) {
        if (mAngle < 0) {
            SWT.error(SWT.ERROR_INVALID_RANGE);
            return;
        }

        this.mAngle = mAngle % 360;
        mParent.redraw();
    }

    public int getAngle() {
        return mAngle;
    }

    public void setColor(Color mColor) {
        this.mColor = mColor;
        mParent.redraw();
    }

    public Color getColor() {
        return mColor;
    }

    void paintArc(GC gc, Point center, int radius, int startAngle, double zoomRatio, boolean paintArcBorder) {
        if (mAngle < 0) {
            SWT.error(SWT.ERROR_INVALID_RANGE);
        }

        if (mColor != null) {
            gc.setBackground(this.mColor);
        }

        int outerRadius = (int) (radius * zoomRatio);

        gc.fillArc(center.x - outerRadius, center.y - outerRadius, outerRadius * 2, outerRadius * 2, startAngle, mAngle);

        if (paintArcBorder) {
            gc.drawArc(center.x - outerRadius, center.y - outerRadius, outerRadius * 2, outerRadius * 2, startAngle, mAngle);
            if (zoomRatio != 1) {
                gc.drawLine(
                        (int) (center.x + Math.cos(Math.toRadians(startAngle)) * radius),
                        (int) (center.y - Math.sin(Math.toRadians(startAngle)) * radius),
                        (int) (center.x + Math.cos(Math.toRadians(startAngle)) * outerRadius),
                        (int) (center.y - Math.sin(Math.toRadians(startAngle)) * outerRadius));
                gc.drawLine(
                        (int) (center.x + Math.cos(Math.toRadians(startAngle + mAngle)) * radius),
                        (int) (center.y - Math.sin(Math.toRadians(startAngle + mAngle)) * radius),
                        (int) (center.x + Math.cos(Math.toRadians(startAngle + mAngle)) * outerRadius),
                        (int) (center.y - Math.sin(Math.toRadians(startAngle + mAngle)) * outerRadius));
            }
        }

        if ((mStyle & SWT.BORDER) == SWT.BORDER) {
            gc.drawLine(
                    center.x,
                    center.y,
                    (int) (center.x + Math.cos(Math.toRadians(startAngle)) * radius),
                    (int) (center.y - Math.sin(Math.toRadians(startAngle)) * radius));
            gc.drawLine(
                    center.x,
                    center.y,
                    (int) (center.x + Math.cos(Math.toRadians(startAngle + mAngle)) * radius),
                    (int) (center.y - Math.sin(Math.toRadians(startAngle + mAngle)) * radius));
        }
    }
}
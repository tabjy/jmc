package org.openjdk.jmc.joverflow.ui.viewers;

import java.util.EventObject;

public class ArcAttributeChangedEvent extends EventObject {
    private Object[] mElements;
    public ArcAttributeChangedEvent(IArcAttributeProvider source, Object[] elements) {
        super(source);
        mElements = elements;
    }

    Object[] getElements() {
        return mElements;
    }
}

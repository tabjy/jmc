package org.openjdk.jmc.joverflow.ui.util;

import java.util.EventObject;

public class ArcAttributeChangedEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	private final Object[] mElements;

	public ArcAttributeChangedEvent(IArcAttributeProvider source, Object[] elements) {
		super(source);
		mElements = elements;
	}

	Object[] getElements() {
		return mElements;
	}
}

package org.openjdk.jmc.joverflow.ui.swt;

import java.util.HashSet;
import java.util.function.Predicate;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.openjdk.jmc.joverflow.ui.util.FilterChangedListener;

public class FilterList<T> extends Composite {

	private final ScrolledComposite mScrolledComposite;
	private final Composite mFilterContainer;

	private final HashSet<Predicate<T>> mFilters = new HashSet<>();
	private final ListenerList<FilterChangedListener> mListeners = new ListenerList<>();

	public FilterList(Composite parent, int style) {
		super(parent, style);

		setLayout(new FillLayout());

		mScrolledComposite = new ScrolledComposite(this, SWT.V_SCROLL | SWT.BORDER);
		mFilterContainer = new Composite(mScrolledComposite, SWT.NONE);
		mFilterContainer.setLayout(new ColumnLayout());

		mScrolledComposite.setContent(mFilterContainer);
		mScrolledComposite.setExpandVertical(true);
		mScrolledComposite.setExpandHorizontal(true);
	}

	public boolean addFilter(Predicate<T> filter) {
		if (!mFilters.add(filter)) {
			return false;
		}

		Button button = new Button(mFilterContainer, SWT.NONE);
		button.setText(filter.toString());
		button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mFilters.remove(filter);

				button.dispose();
				FilterList.this.layout(true, true);

				Rectangle r = mScrolledComposite.getClientArea();
				mScrolledComposite.setMinSize(mFilterContainer.computeSize(r.width, SWT.DEFAULT));

				notifyFilterChangedListeners();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// no op
			}
		});

		layout(true, true);

		Rectangle r = mScrolledComposite.getClientArea();
		mScrolledComposite.setMinSize(mFilterContainer.computeSize(r.width, SWT.DEFAULT));

		notifyFilterChangedListeners();

		return true;
	}

	public boolean filter(T target) {
		Predicate<T> res = in -> true;
		for (Predicate<T> filter : mFilters) {
			res = res.and(filter);
		}

		return res.test(target);
	}

	public void reset() {
		mFilters.clear();
		for (Control filter : mFilterContainer.getChildren()) {
			filter.dispose();
		}

		notifyFilterChangedListeners();
	}

	public void addFilterChangedListener(FilterChangedListener listener) {
		mListeners.add(listener);
	}

	public void removeFilterChangedListener(FilterChangedListener listener) {
		mListeners.remove(listener);
	}

	private void notifyFilterChangedListeners() {
		for (FilterChangedListener l : mListeners) {
			l.onFilterChanged();
		}
	}
}

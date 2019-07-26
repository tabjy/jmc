package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.openjdk.jmc.joverflow.heap.model.JavaField;
import org.openjdk.jmc.joverflow.heap.model.JavaObject;
import org.openjdk.jmc.joverflow.heap.model.JavaObjectArray;
import org.openjdk.jmc.joverflow.heap.model.JavaThing;
import org.openjdk.jmc.joverflow.heap.model.JavaValueArray;
import org.openjdk.jmc.joverflow.ui.model.JavaThingItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.StreamSupport;

public class JavaThingTreeViewer<T extends JavaThingItem> extends TreeViewer {
	public JavaThingTreeViewer(Composite parent, int style) {
		super(parent, style);

		setContentProvider(new JavaThingItemContentProvider());

		createTreeViewerColumn("Name", T::getName);
		createTreeViewerColumn("Value", T::getValue);
		createTreeViewerColumn("Size", T::getSize);

		getTree().setLinesVisible(true);
		getTree().setHeaderVisible(true);
	}

	private void createTreeViewerColumn(String label, Function<T, String> labelProvider) {
		TreeViewerColumn column = new TreeViewerColumn(this, SWT.NONE);
		column.getColumn().setWidth(300);
		column.getColumn().setText(label);
		column.getColumn().setMoveable(true);

		column.setLabelProvider(new ColumnLabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element) {
				return labelProvider.apply((T) element);
			}
		});
	}

	private class JavaThingItemContentProvider implements ITreeContentProvider {

		@SuppressWarnings("unchecked")
		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement == null) {
				return new Object[0];
			}

			List<JavaThingItem> items = (List<JavaThingItem>) inputElement;
			return items.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			JavaThingItem item = (JavaThingItem) parentElement;
			Iterable<JavaThingItem> childItems = item.getChildItems();
			if (childItems == null) {
				ArrayList<JavaThingItem> items = new ArrayList<>();
				JavaThing thing = item.getContent();
				if (thing instanceof JavaObject) {
					JavaObject o = (JavaObject) thing;
					JavaField[] fields = o.getClazz().getFieldsForInstance();
					JavaThing[] values = o.getFields();
					for (int i = 0; i < fields.length; i++) {
						items.add(new JavaThingItem(item.getLevel() + 1, fields[i].getName(), values[i]));
					}
				} else if (thing instanceof JavaObjectArray) {
					JavaObjectArray o = (JavaObjectArray) thing;
					int i = 0;
					for (JavaThing th : o.getElements()) {
						items.add(new JavaThingItem(item.getLevel() + 1, "[" + (i++) + "]",
								th)); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} else if (thing instanceof JavaValueArray) {
					JavaValueArray o = (JavaValueArray) thing;
					int i = 0;
					for (String value : o.getValuesAsStrings()) {
						items.add(new JavaThingItem(item.getLevel() + 1, "[" + (i++) + "]", value, o.getElementSize(),
								null)); //$NON-NLS-1$ //$NON-NLS-2$
					}

				}
				item.setChildItems(items);
				return items.toArray();
			}

			return StreamSupport.stream(childItems.spliterator(), false).toArray();
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			JavaThingItem item = (JavaThingItem) element;
			JavaThing thing = item.getContent();
			return thing instanceof JavaObject && ((JavaObject) thing).getClazz().getFieldsForInstance().length > 0
					|| thing instanceof JavaObjectArray && ((JavaObjectArray) thing).getLength() > 0
					|| thing instanceof JavaValueArray && ((JavaValueArray) thing).getLength() > 0;
		}
	}
}

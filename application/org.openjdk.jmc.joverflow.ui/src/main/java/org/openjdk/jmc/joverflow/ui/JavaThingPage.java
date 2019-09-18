package org.openjdk.jmc.joverflow.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.Page;
import org.openjdk.jmc.joverflow.heap.model.JavaHeapObject;
import org.openjdk.jmc.joverflow.support.RefChainElement;
import org.openjdk.jmc.joverflow.ui.model.JavaThingItem;
import org.openjdk.jmc.joverflow.ui.model.ModelListener;
import org.openjdk.jmc.joverflow.ui.model.ObjectCluster;
import org.openjdk.jmc.joverflow.ui.viewers.JavaThingTreeViewer;
import org.openjdk.jmc.ui.misc.DisplayToolkit;

public class JavaThingPage extends Page implements ModelListener {
	private final JOverflowEditor mEditor;
	private JavaThingTreeViewer mTreeViewer;

	private static final int MAX = 500;
	private final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(1);

	private FutureTask<Void> mCurrentTask;
	private Future<?> mBackground;
	private final int[] mObjects = new int[MAX];
	private int mObjectsInArray;
	private int mTotalInstancesCount;
	private boolean mTaskCancelled = false;

	private Object mInput;

	JavaThingPage(JOverflowEditor editor) {
		mEditor = editor;
	}

	@Override
	public void createControl(Composite parent) {
		mTreeViewer = new JavaThingTreeViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		updateInput();
	}

	@Override
	public Control getControl() {
		return mTreeViewer.getControl();
	}

	@Override
	public void setFocus() {
		mTreeViewer.getTree().setFocus();
	}

	@Override
	public void dispose() {
		EXECUTOR_SERVICE.shutdown();
		super.dispose();
	}

	@Override
	public void include(ObjectCluster oc, RefChainElement ref) {
		int insertCount = Math.min(oc.getObjectCount(), MAX - mObjectsInArray);
		for (int i = 0; i < insertCount; i++) {
			mObjects[mObjectsInArray++] = oc.getGlobalObjectIndex(i);
		}
		mTotalInstancesCount += oc.getObjectCount();
	}

	@Override
	public void allIncluded() {
		if (mCurrentTask != null) {
			mTaskCancelled = true;
			mCurrentTask
					.cancel(false);// Don't stop the thread directly. Interruption breaks the atomicity inside getObjectAtGlobalIndex
		}

		if (mBackground != null && !mBackground.isDone()) {
			mBackground.cancel(false);
		}

		int[] objects = Arrays.copyOf(mObjects, mObjectsInArray);
		int instanceCount = mTotalInstancesCount;

		updateInput(null);

		mTaskCancelled = false;
		mCurrentTask = new FutureTask<>(() -> {
			List<JavaThingItem> items = new ArrayList<>();
			for (int i : objects) {
				if (mTaskCancelled) {
					return null;
				}
				JavaHeapObject o = getObjectAtPosition(i);
				items.add(new JavaThingItem(0, o.idAsString(), o));
			}
			if (instanceCount > mObjects.length) {
				items.add(new JavaThingItem(0, "...", (instanceCount - mObjects.length) + " more instances", 0, null) {
					@Override
					public String getSize() {
						return "";
					}
				});
			}

			DisplayToolkit.inDisplayThread().execute(() -> updateInput(items));

			return null;
		});
		mBackground = EXECUTOR_SERVICE.submit(mCurrentTask);

		mObjectsInArray = 0;
		mTotalInstancesCount = 0;
	}

	private void updateInput() {
		updateInput(mInput);
	}

	private void updateInput(Object input) {
		mInput = input;
		if (mTreeViewer != null) {
			mTreeViewer.setInput(mInput);
		}
	}

	private JavaHeapObject getObjectAtPosition(int globalObjectPos) {
		return mEditor.getSnapshot().getObjectAtGlobalIndex(globalObjectPos);
	}
}

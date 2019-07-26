/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The contents of this file are subject to the terms of either the Universal Permissive License
 * v 1.0 as shown at http://oss.oracle.com/licenses/upl
 *
 * or the following license:
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openjdk.jmc.joverflow.ui;

import java.util.Collection;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPathEditorInput;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.openjdk.jmc.ui.misc.DialogToolkit;
import org.openjdk.jmc.joverflow.heap.model.Snapshot;
import org.openjdk.jmc.joverflow.ui.model.ModelLoader;
import org.openjdk.jmc.joverflow.ui.model.ModelLoaderListener;
import org.openjdk.jmc.joverflow.ui.model.ReferenceChain;

public class JOverflowEditor extends EditorPart {

	private ModelLoader loader;
	public static final String EDITOR_ID = "org.openjdk.jmc.joverflow.ui.JOverflowEditor";
	private Snapshot snapshot;

	/*
	protected synchronized Scene createScene() {
		IEditorInput input = getEditorInput();
		IPathEditorInput ipei;
		if (input instanceof IPathEditorInput) {
			ipei = (IPathEditorInput) input;
		} else {
			ipei = input.getAdapter(IPathEditorInput.class);
		}
		if (ipei == null) {
			// Not likely to be null, but guard just in case
			throw new IllegalArgumentException("The JOverflow editor cannot handle the provided editor input");
		}
		final String fileName = ipei.getPath().toOSString();
		loader = new ModelLoader(fileName, new ModelLoaderListener() {

			@Override
			public void onProgressUpdate(final double progress) {
				Platform.runLater(() -> {
//						loderUi.setProgress(progress);
				});
			}

			@Override
			public void onModelLoaded(Snapshot snapshot, final Collection<ReferenceChain> model) {
				setModelLoaded(snapshot);
				Platform.runLater(() -> {
//					ui.setModel(model);
//					loderUi.clear();
				});
			}

			@Override
			public void onModelLoadFailed(final Throwable failure) {
				getSite().getShell().getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {
						String message = failure.getLocalizedMessage();
						DialogToolkit.showException(getSite().getShell(), "Could not open " + fileName, message, failure);
						getSite().getPage().closeEditor(JOverflowEditor.this, false);
					}
				});
				cancelAndClearLoader();
			}
		});

		Executors.newSingleThreadExecutor().submit(loader);
		/*
		addToolbarAction(new Action("Reset") {
			{
				setImageDescriptor(JOverflowPlugin.getDefault().getMCImageDescriptor(JOverflowPlugin.ICON_UNDO_EDIT));
			}

			@Override
			public void run() {
				ui.reset();
			}
		});
		
		return null;
	}
*/

	@Override
	public void createPartControl(Composite parent) {

	}

	@Override
	public void dispose() {
		super.dispose();
		cancelAndClearLoader();
		if (snapshot != null) {
			snapshot.discard();
		}
	}

	@Override
	public void setFocus() {

	}

	@Override
	public void doSave(IProgressMonitor monitor) {

	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {

	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void setInput(IEditorInput ei) {
		super.setInput(ei);
		setPartName(ei.getName());
	}

	private synchronized void cancelAndClearLoader() {
		if (loader != null) {
			loader.cancel();
			loader = null;
		}
	}

	private synchronized void setModelLoaded(Snapshot snapshot) {
		if (loader == null) {
			// Already canceled
			snapshot.discard();
		} else {
			this.snapshot = snapshot;
			loader = null;
		}
	}

	synchronized Snapshot getSnapshot() {
		return snapshot;
	}

}

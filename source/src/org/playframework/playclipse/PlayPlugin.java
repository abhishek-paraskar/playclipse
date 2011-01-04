/*
 * Playclipse - Eclipse plugin for the Play! Framework
 * Copyright 2009 Zenexity
 *
 * This file is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.playframework.playclipse;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PlayPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.playframework.playclipse";

	// The shared instance
	private static PlayPlugin plugin;

	// Preferences
	public static final String PREF_TMPL_IDENT = "_play_tmpl_ident";
	public static final String PREF_BROWSER = "pref_browser";
	public static final String PREF_BROWSER_INTERNAL = "internal";
	public static final String PREF_BROWSER_EXTERNAL = "external";

	/**
	 * The constructor
	 */
	public PlayPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		/*
		 * ICommandService commandService =
		 * (ICommandService)plugin.getWorkbench(
		 * ).getService(ICommandService.class);
		 * commandService.addExecutionListener(new IExecutionListener() { public
		 * void notHandled(final String commandId, final NotHandledException
		 * exception) {} public void postExecuteFailure(final String commandId,
		 * final ExecutionException exception) {} public void
		 * postExecuteSuccess(final String commandId, final Object returnValue)
		 * {} public void preExecute( final String commandId, final
		 * ExecutionEvent event ) { if
		 * (commandId.equals("org.eclipse.ui.file.save")) { IEditorPart editor =
		 * HandlerUtil.getActiveEditor(event); if (editor instanceof Editor) {
		 * ((Editor)editor).updateMarkers(); } } } });
		 */
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static PlayPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static void showError(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		showError(sw.toString());
	}

	public static void showError(String e) {
		MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "JapidPlayclipse", e);
	}

	public static void showInfo(String e) {
		MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "JapidPlayclipse", e);
	}

	public static boolean showConfirm(String string) {
		return MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "JapidPlayclipse", string);
	}

//	public static void status(final String message) {
//		final Display display = Display.getDefault();
//
//		new Thread() {
//			@Override
//			public void run() {
//				display.syncExec(new Runnable() {
//					@Override
//					public void run() {
//						IWorkbench wb = PlatformUI.getWorkbench();
//						IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
//						IWorkbenchPage page = win.getActivePage();
//						IWorkbenchPart part = page.getActivePart();
//						IWorkbenchPartSite site = part.getSite();
	// this casting is not working 
//						IViewSite vSite = (IViewSite) site;
//						IActionBars actionBars = vSite.getActionBars();
//
//						if (actionBars == null)
//							return;
//
//						IStatusLineManager statusLineManager = actionBars.getStatusLineManager();
//
//						if (statusLineManager == null)
//							return;
//
//						statusLineManager.setMessage(message);
//					}
//				});
//			}
//		}.start();
//	}
}

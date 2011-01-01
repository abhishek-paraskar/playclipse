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

package org.playframework.playclipse.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.playframework.playclipse.Navigation;

/**
 *  go to the action method from the @{...} notation
 */
public class BrowseToActionHandler extends AbstractHandler {
	private static final String APP_VIEWS = "app/views";
	private static final String APP_JAPIDVIEWS = "app/japidviews";

	/**
	 * The constructor.
	 */
	public BrowseToActionHandler() {
	}

	
	/**
	 * the command has been executed, so let's extract the needed information
	 * from the application context.
	 * 
	 * TODO: bran: parse @{namespace.controller.action()}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String action = null;
		// probably a selection from the explorer popup.
		IWorkbenchWindow win = HandlerUtil.getActiveWorkbenchWindow(event);
		ISelection activeMenuSelection = HandlerUtil.getActiveMenuSelection(event);
		
		IProject p  = null;
		if (activeMenuSelection instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection)activeMenuSelection;
			p = Navigation.getProject(selection);
			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof IFile) {
				IFile f = (IFile) firstElement;
				IPath pa = f.getProjectRelativePath();
				action = getDefaultActionFromPath(pa.toString());
			}

		}

		
		System.out.println("action = " + action);
		
		if (action == null || p == null) {
			System.out.println("no action to go to.");
		}
		else {
			new Navigation(win, p).goToAction(action);
		}
		return null;
	}

	/**
	 * @param editor
	 * @param action
	 * @param relativePath
	 * @return
	 */
	private String getDefaultActionFromPath(String relativePath) {
		String action = null;
		if (relativePath.startsWith(APP_JAPIDVIEWS)) {
			action = relativePath.substring(APP_JAPIDVIEWS.length() + 1).replace("/", ".");
			action = action.substring(0, action.length() - ".html".length());
		}
		else if (relativePath.startsWith(APP_VIEWS)) {
			action = relativePath.substring(APP_VIEWS.length() + 1).replace("/", ".");
			action = action.substring(0, action.length() - ".html".length());
		} 
		return action;
	}

}

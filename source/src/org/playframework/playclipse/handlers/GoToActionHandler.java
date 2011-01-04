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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IFileEditorInput;
import org.playframework.playclipse.Navigation;

import fr.zenexity.pdt.editors.EditorHelper;

/**
 *  go to the action method from the @{...} notation
 */
public class GoToActionHandler extends AbstractHandler {
	private static final String APP_VIEWS = "app/views";
	private static final String APP_JAPIDVIEWS = "app/japidviews";

	/**
	 * The constructor.
	 */
	public GoToActionHandler() {
	}

	private String fromView(EditorHelper editor) {
		String action  = getDefaultAction(editor);
		String controller = (action != null && action.contains(".")) ? action.substring(0, action.lastIndexOf('.')) : null;
		String line = editor.getLine(editor.getCurrentLineNo());
		Pattern pt = Pattern.compile("@\\{([^}]+)\\}");
		Matcher m = pt.matcher(line);
		if (m.find()) {
			action = m.group().replace("@{", "").replace("}", "").replaceAll("\\(.*\\)", "");
			if (action.contains("(")) {
				action = action.substring(0, action.indexOf("(")).trim();
			}
			if (!action.contains(".")) {
//				action = editor.enclosingDirectory() + "." + action;
				action = controller + "." + action;
			}
		} 
		return action;
	}

	private String fromRoutes(EditorHelper editor) {
		String line = editor.getLine(editor.getCurrentLineNo());
		String[] lineArr = line.trim().split("\\s+");
		return lineArr[lineArr.length - 1];
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
		EditorHelper editor = EditorHelper.getCurrent(event);
		if(editor == null) {
			return null;
		}
		else {
			action = fromView(editor);
		}
		
		
		System.out.println("action = " + action);
		
		if (action == null) {
			System.out.println("no action to go to.");
			return null;
		}
		else {
			(new Navigation(editor)).goToAction(action);
			return null;
		}
	}

	private String getDefaultAction(EditorHelper editor) {
		String action = null;
		String relativePath = getCurrentEditorRelativePathFromProject(editor);
		action = getDefaultActionFromPath(relativePath);
		if (action == null && relativePath.equals("conf/routes")) {
				action = fromRoutes(editor);
		}
		return action;
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

	/**
	 * @param editor
	 * @return a path from the project root:  e.g.: app/views/my/Controller/action.html 
	 */
	private String getCurrentEditorRelativePathFromProject(EditorHelper editor) {
		IPath path = ((IFileEditorInput) editor.textEditor.getEditorInput()).getFile().getFullPath();
		String pname = editor.getProject().getName();
		String relativePath = path.toString().substring(pname.length() + 2);
		return relativePath;
	}
}

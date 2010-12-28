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
import org.playframework.playclipse.Navigation;
import org.playframework.playclipse.editors.html.HTMLEditor;
import org.playframework.playclipse.editors.route.RouteEditor;

import fr.zenexity.pdt.editors.EditorHelper;

/**
 *  go to the action method from the @{...} notation
 */
public class GoToActionHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public GoToActionHandler() {
	}

	private String fromView(EditorHelper editor) {
		String line = editor.getLine(editor.getCurrentLineNo());
		String action;
		Pattern pt = Pattern.compile("@\\{([^}]+)\\}");
		Matcher m = pt.matcher(line);
		if (m.find()) {
			action = m.group().replace("@{", "").replace("}", "").replace("(.*)", "");
			if (!action.contains(".")) {
				action = editor.enclosingDirectory() + "." + action;
			}
		} else {
			action = editor.enclosingDirectory() + "." + editor.getTitle().replace(".html", "");
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
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String action = null;
		EditorHelper editor = EditorHelper.getCurrent(event);
		if (editor.textEditor instanceof HTMLEditor) {
			action = fromView(editor);
			System.out.println("View!!");
		} else if (editor.textEditor instanceof RouteEditor) {
			action = fromRoutes(editor);
			System.out.println("Routes!!");
		}
		else {
			// bran - assuming from the views
			action = fromView(editor);
			System.out.println("assuming from a View...");
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
}

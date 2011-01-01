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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.playframework.playclipse.FilesAccess;
import org.playframework.playclipse.Navigation;

import fr.zenexity.pdt.editors.EditorHelper;

/**
 * Go to the view (template) corresponding to the current action
 */
public class BrowseToViewHandler extends AbstractHandler {
	static Pattern stringPattern = Pattern.compile("\"(.*)\"");
	static Pattern methodNamePattern = Pattern.compile("\\w+\\s*\\(");
	static Pattern renderJapidPattern = Pattern.compile("renderJapid\\s*\\(");
	static Pattern renderJapidWithPattern = Pattern.compile("renderJapidWith\\s*\\(\\s*\"(.*)\"");
	private IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public BrowseToViewHandler() {
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// probably a selection from the explorer popup.
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		ISelection activeMenuSelection = HandlerUtil.getActiveMenuSelection(event);

		boolean useJapid = false;
		String viewName = null;
		String controllerName = null;

		String packageName = "";

		IProject p = null;

		if (activeMenuSelection instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) activeMenuSelection;
			p = Navigation.getProject(selection);
			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof IMethod) {
				IMethod m = (IMethod) firstElement;
				ICompilationUnit unit = m.getCompilationUnit();
				try {
					IPackageDeclaration[] packs = unit.getPackageDeclarations();
					if (packs.length < 1) {
						info("This action can only apply to controllers.");
						return null;
					} else {
						packageName = packs[0].getElementName();
						if (!packageName.startsWith("controllers")) {
							info("This action can only apply to controllers.");
							return null;
						}
					}

					IType type = unit.getTypes()[0];
					controllerName = type.getElementName();
					String superclassName = type.getSuperclassName();
					if (superclassName.toLowerCase().contains("japid")) {
						useJapid = true;
					}

					List<IJavaElement> path = getJavaElementsPath(m);
					if (path.size() == 7) {
						int flags = m.getFlags();
						
						if (Flags.isPublic(flags) && Flags.isStatic(flags)) {
							viewName = m.getElementName();
						} else {
							info("The selected method " + m.getElementName()
									+ " is not public static of the top controller, thus not a valid action method.");
							return null;
						}
					}
				} catch (JavaModelException e) {
					e.printStackTrace();
					return null;
				}
			}

			if (viewName == null) {
				String string = "Use this command in a controller action body, or on a render...() line";
				info(string);
			} else {
				if (!viewName.startsWith("app")) {
					viewName = "app/" + (useJapid ? "japidviews" : "views") + "/"
							+ (packageName.equals("controllers") ? "" : packageName.substring(12).replace('.', '/') + "/") + controllerName
							+ "/" + viewName + ".html";
				}

				(new Navigation(window, p)).goToViewAbs(viewName);
			}
		}

		return null;
	}

	/**
	 * @param selection
	 * @param unit
	 */
	private String getEnclosingActionName(ITextSelection selection, ICompilationUnit unit) {
		IJavaElement selected;
		try {
			selected = unit.getElementAt(selection.getOffset());
			List<IJavaElement> path = getJavaElementsPath(selected);
			if (path.size() >= 7) {
				IJavaElement el = path.get(6);
				if (el.getElementType() == IJavaElement.METHOD) {
					IMethod sm = (IMethod) el;
					int flags = sm.getFlags();
					String actionMethodName = el.getElementName();
					if (Flags.isPublic(flags) && Flags.isStatic(flags)) {
						return actionMethodName;
					} else {
						info("The enclosig method " + actionMethodName + " is not public static, thus not a valid action method.");
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return null;
	}

	static void printAstPath(IJavaElement elem) {
		System.out.println(elem.getClass() + ":" + elem.getElementType() + ":" + elem.getElementName());
		IJavaElement parent = elem.getParent();
		if (parent != null) {
			printAstPath(parent);
		}
	}

	/**
	 * the result pattern: {JavaModel, JavaProject, packageFragmentRoot,
	 * PackageFragment (package name), CompilationUnit (source file title),
	 * SouceType (class name}...
	 * 
	 * @param elem
	 * @return
	 */
	static List<IJavaElement> getJavaElementsPath(IJavaElement elem) {
		List<IJavaElement> path = new ArrayList<IJavaElement>();
		path.add(elem);
		elem = elem.getParent();
		while (elem != null) {
			path.add(elem);
			elem = elem.getParent();
		}
		Collections.reverse(path);
		return path;
	}

	/**
	 * @param window
	 * @param string
	 */
	private void info(String string) {
		MessageDialog.openInformation(window.getShell(), "Playclipse", string);
	}
}

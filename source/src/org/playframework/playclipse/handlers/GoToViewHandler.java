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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.playframework.playclipse.FilesAccess;
import org.playframework.playclipse.Navigation;
import org.playframework.playclipse.PlayPlugin;

import fr.zenexity.pdt.editors.EditorHelper;

/**
 * Go to the view (template) corresponding to the current action
 */
public class GoToViewHandler extends AbstractHandler {
	static Pattern stringPattern = Pattern.compile("\"(.*)\"");
	static Pattern methodNamePattern = Pattern.compile("\\w+\\s*\\(");
	static Pattern renderJapidPattern = Pattern.compile("renderJapid\\s*\\(");
	static Pattern renderJapidWithPattern = Pattern.compile("renderJapidWith\\s*\\(\\s*\"(.*)\"");
	private IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public GoToViewHandler() {
	}

//	@SuppressWarnings("restriction")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		EditorHelper editor = EditorHelper.getCurrent(event);

		if(editor != null) {
			String relativePath = ((IFileEditorInput) editor.textEditor.getEditorInput()).getFile().getProjectRelativePath().toString();
			if (relativePath.startsWith("app/japidviews/") && relativePath.endsWith(".java")) {
				// bran: if we are in the japid derived Java template code, let's switch back to the html view
				String jFile = relativePath.substring(0, relativePath.lastIndexOf("java")) + "html";
				IFile f = editor.getProject().getFile(jFile);
				try {
					FilesAccess.openFile(f);
				} catch (CoreException e) {
					PlayPlugin.showError(e);
				}
				return null;		
			}
		}
		
		boolean useJapid = true;

		String line;
		String viewName = null;
		String title = editor.getTitle();
		String controllerName = title.replace(".java", "");

		String packageName = "";
		IEditorInput editorInput = editor.textEditor.getEditorInput();
		
		ITextSelection selection = (ITextSelection) editor.textEditor.getSelectionProvider().getSelection();
		IJavaElement elem = JavaUI.getEditorInputJavaElement(editorInput);
		ICompilationUnit unit = null;
		if (elem instanceof ICompilationUnit) {
			unit = (ICompilationUnit) elem;
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
				
				// get the class declaration line
				IType type = unit.getType(controllerName);
				String superclassName = type.getSuperclassName();
				if (superclassName.toLowerCase().contains("japid")) {
					useJapid = true;
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println(elem.getElementType() + ":" + elem.getElementName());
		}

		viewName = getEnclosingActionName(selection, unit);

		int lineNo = editor.getCurrentLineNo();
		line = editor.getLine(lineNo);
		if (line.contains("render")) {
			if (!line.contains("renderJapid")) {
				Pattern pt = Pattern.compile("\"(.*)\"");
				Matcher m = pt.matcher(line);
				if (m.find()) {
					// render classic groovy
					useJapid = false;
					// There is a custom view, by renderTemplate()
					viewName = "app/views/" + m.group().replace("\"", "");
				}
			} else {
				// render japid
				useJapid = true;
				if(line.contains("renderJapidWith")) {
					// explicit template name
					Matcher m = stringPattern.matcher(line);
					if (m.find()) {
						// There is a custom view
						// the template strin is the view name in relative to the japidviews directory
						viewName = "app/japidviews/" + m.group().replace("\"", "");
					}
					else {
						System.out.println("the first param of renderJapidWith is not a String. Strange....");
					}
				}
			}
		}
		
		if (viewName == null) {
			String string = "Use this command in a controller action body, or on a render...() line";
			info(string);
		} else {
			if (!viewName.startsWith("app")) {
				
				viewName = "app/" + (useJapid? "japidviews" : "views") + "/" 
					+ (packageName.equals("controllers") ? "" : packageName.substring(12).replace('.', '/') + "/")
					+ controllerName + "/" + viewName + ".html";
			}
			
			(new Navigation(editor)).goToViewAbs(viewName);
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
					IMethod sm = (IMethod)el;
					int flags = sm.getFlags();
					String actionMethodName = el.getElementName();
					if (Flags.isPublic(flags) && Flags.isStatic(flags)) {
						return actionMethodName;
					}
					else {
						info("The enclosig method " + actionMethodName + " is not public static, thus not a valid action method.");
					}
				}
			}
		}
		catch (JavaModelException e) {
			PlayPlugin.showError(e);
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
	 * the result pattern: 
	 * 	{JavaModel, JavaProject, packageFragmentRoot, PackageFragment (package name), 
	 * 	CompilationUnit (source file title), SouceType (class name}...
	 * @param elem
	 * @return
	 */
	public static List<IJavaElement> getJavaElementsPath(IJavaElement elem) {
		List<IJavaElement> path = new ArrayList<IJavaElement>();
		if (elem == null)
			return path;
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

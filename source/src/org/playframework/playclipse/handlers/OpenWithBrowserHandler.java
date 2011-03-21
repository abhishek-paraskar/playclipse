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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
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
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.playframework.playclipse.PlayPlugin;

import fr.zenexity.pdt.editors.EditorHelper;

/**
 * open the action based on the {controller}/{action} catch all routing. 
 * 
 * TODO: need to do reverse routing and get the right URL 
 */
public class OpenWithBrowserHandler extends AbstractHandler {
	private IWorkbenchWindow window;
	static Pattern portlinePattern = Pattern.compile("http\\.port\\s*=\\s*(\\d*)\\s*");

	public OpenWithBrowserHandler() {
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		EditorHelper editor = EditorHelper.getCurrent(event);
		IProject project = editor.getProject();
		
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
				
//				// get the class declaration line
//				IType type = unit.getType(controllerName);
//				ITypeHierarchy superTypes = type.newSupertypeHierarchy(null);
////				String name = JapidController.class.getName(); // this will require play.jar
//				String name = "cn.bran.play.JapidController";
//				IType japidController = jProject.findType(name);
//				
				// current selected elem
			      IJavaElement[] elements= unit.codeSelect(selection.getOffset(), selection.getLength());
			      if (elements.length > 0) {
			    	  // TODO extract the current selection to tell if the cursor in on renderJapidXXX line
//			    	  System.out.println(elements);
			      }

			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println(elem.getElementType() + ":" + elem.getElementName());
		}

		viewName = getEnclosingActionName(selection, unit);
		
		if (viewName == null) {
			String string = "Use this command in a controller action body, or on a render...() line";
			info(string);
		} else {
			if (!viewName.startsWith("app")) {
				
				viewName = (packageName.equals("controllers") ? "" : packageName.substring(12) + "." )  + controllerName + "/" + viewName;
				// let's parsing the application.conf
				IFile confFile = project.getFile("conf/application.conf");
				InputStream contents = null;
				try {
					String portpart = "9000";
					contents =  confFile.getContents();
					BufferedReader br = new BufferedReader(new InputStreamReader(contents, "UTF-8"));
					String line = "";
					
					while ((line = br.readLine()) != null) {
						line = line.trim();
						if (!line.startsWith("#")) {
							Matcher matcher = portlinePattern.matcher(line);
							if (matcher.matches()) {
								portpart = matcher.group(1);
								break;
								// open the browser
							}
						}
					}
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL("http://localhost:" + portpart + "/" + viewName));
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
				finally {
					try {
						contents.close();
					}
					catch (Exception ee) {}
				}
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

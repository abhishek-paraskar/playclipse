package org.playframework.playclipse;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;

import fr.zenexity.pdt.editors.EditorHelper;

public final class Navigation {
//	private EditorHelper editorHelper;
//	private IWorkbenchWindow window;
	private IProject project;
	private IJavaProject javaProject;

	public Navigation(EditorHelper editorHelper) {
//		this.editorHelper = editorHelper;
//		this.window = editorHelper.getWindow();
		this.project = editorHelper.getProject();
		this.javaProject = JavaCore.create(project);
	}

	public Navigation(IWorkbenchWindow window,  IProject project) {
//		this.window = window;
		this.project = project;
		this.javaProject = JavaCore.create(project);
	}

	public Navigation(IProject project) {
//		this.window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		this.project = project;
		this.javaProject = JavaCore.create(project);
	}
	
	
	private IType findType(String name) {
		try {
			IType type = javaProject.findType(name);
//			System.out.println("Type for " + name + ": " + type);
			return type;
		} catch (CoreException e) {
			PlayPlugin.showError(e.getMessage());
		}
		return null;
	}

	/**
	 * Open the requested action in an editor
	 * @param action the fully qualified action, such as namespace.className.method
	 */
	public void goToAction(String action) {
		action = (action.startsWith("controllers.") ? "": "controllers.") + action; 
		String fullClassName = action.replaceFirst(".[^.]+$", "");
		System.out.println("goToAction for class: " + fullClassName);
		String method = action.substring(action.lastIndexOf('.') + 1);
		IType type = findType(fullClassName);
		if (type == null) {
			PlayPlugin.showError("The controller " + fullClassName + " can't be found.");
			return;
		}
		
		IFile file;
		try {
			file = (IFile)type.getCompilationUnit().getCorrespondingResource();
		} catch (JavaModelException e1) {
			// Should not happen
			e1.printStackTrace();
			return;
		}
		IEditorPart newEditorPart;
		try {
			newEditorPart = FilesAccess.openFile(file);
			focusOrCreateMethod(newEditorPart, type, method);
		} catch (CoreException e) {
			// Should never happen
			e.printStackTrace();
		}
	}
	
	/**
	 * open a java code editor for the class source
	 * @param className
	 * @author bran
	 */
	public void openClass(String className) {
		IType type = findType(className);
		if (type == null) {
			PlayPlugin.showError("The controller " + className + " can't be found.");
			return;
		}
		
		IFile file = null;
		try {
			if (!type.isBinary()) {
				ICompilationUnit cu = type.getCompilationUnit();
				file = (IFile)cu.getCorrespondingResource();
			} else {
				// the type is in binary form, meaning a type in the classpath
				if(type.getParent() instanceof IClassFile) {
					return; // TODO what to do with class in classpath?
				}
			}
		} catch (JavaModelException e1) {
			// Should not happen
			e1.printStackTrace();
			return;
		}
		try {
			if (file != null)
				FilesAccess.openFile(file);
		} catch (CoreException e) {
			PlayPlugin.showError(e.getMessage());
		}
		
	}

	/**
	 * In an open editor, move the cursor to the line corresponding to the method if
	 * it exists, offer the user to create it otherwise.
	 * @param editorPart
	 * @param type
	 * @param methodName
	 * @throws JavaModelException
	 */
	private void focusOrCreateMethod(IEditorPart editorPart, IType type, String methodName) throws JavaModelException {
		// We can't just use IType.getMethod because we don't know the arguments
		ISourceRange sourceRange = null;
		IMethod[] methods;
		System.out.println("Looking for method: " + methodName);
			methods = type.getMethods();
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].getElementName().equals(methodName)) {
					sourceRange = methods[i].getSourceRange();
				}
			}
		if (sourceRange != null) {
			FilesAccess.goToCharacter(editorPart, sourceRange.getOffset());
		} else if (PlayPlugin.showConfirm("The method " + methodName + " doesn't exist, do you want to create it?")) {
				IMethod newMethod = type.createMethod("public static void "+methodName+"() {\n\n}\n", null, false, null);
				FilesAccess.goToCharacter(editorPart, newMethod.getSourceRange().getOffset());
		}
	}

	public void goToView(String viewName) {
		IFile file = project.getFile("app/views/" + viewName);
		openOrCreate(file);
	}

	/**
	 * 
	 * @param viewName view name starting with "app/..."
	 */
	public void goToViewAbs(String viewName) {
		IFile file = project.getFile(viewName);
		openOrCreate(file);
	}

	public void openOrCreate(String path) {
		IFile file = project.getFile(path);
		openOrCreate(file);
	}

	private void openOrCreate(IFile file) {
		if (file.exists()) {
			try {
				FilesAccess.openFile(file);
			} catch (CoreException e) {
				// Should never happen (we checked for file.exist())
				e.printStackTrace();
			}
			return;
		}
		String path = file.getFullPath().toString();
		if (PlayPlugin.showConfirm("The file " + path + " can't be found, do you want to create it?")) {
			String[] titleArr = path.split("/");
			String title = titleArr[titleArr.length - 1].replace(".html", "");
			String content = CodeTemplates.view(title);
			if (path.contains("japidviews"))
					content = CodeTemplates.japidView(title);
			FilesAccess.createAndOpen(file, content, FilesAccess.FileType.HTML);
		}
	}

	public static IProject getProject(IStructuredSelection selection) {
		Object obj = selection.getFirstElement();
		if (obj instanceof IJavaElement) {
			obj = ((IJavaElement)obj).getResource();
		}
		if (obj instanceof IResource) {
			IContainer container;
			if (obj instanceof IContainer) {
				container = (IContainer) obj;
			} else {
				container = ((IResource) obj).getParent();
			}
			while (container != null) {
				if (container instanceof IProject) {
					return (IProject)container;
				}
				container = container.getParent();
			}
		}
		return null;
	}

	public void goToJapidView(String viewName) {
		IFile file = project.getFile("app/japidviews/" + viewName);
		openOrCreate(file);	}

}

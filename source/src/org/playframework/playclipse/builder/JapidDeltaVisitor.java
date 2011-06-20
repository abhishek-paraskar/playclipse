package org.playframework.playclipse.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import bran.japidplugin.TemplateTransformer;

import cn.bran.japid.util.DirUtil;

public class JapidDeltaVisitor implements IResourceDeltaVisitor {
	static {
		TemplateTransformer.initTemplateCLassMeta();
	}

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource res = delta.getResource();
		IPath ppath = res.getProjectRelativePath();
		int segmentCount = ppath.segmentCount();
		if (segmentCount == 1 && !"app".equals(ppath.segment(0))) {
			return false;
		} else if (segmentCount >= 2) {
			if ("japidviews".equals(ppath.segment(1))) {
 				handleJapidviewsChange(delta, res);
			}
			else if ("controllers".equals(ppath.segment(1))) {
				handleControllersChange(delta, res);
			}
			else {
				return false;
			}
		}

		return true;
	}

	private void handleControllersChange(IResourceDelta delta, IResource res) {
		switch (delta.getKind()) {
		case IResourceDelta.REMOVED:
			log(" deleted: " + res);
			break;
		case IResourceDelta.ADDED:
			log(" added: " + res);
			break;
		case IResourceDelta.CHANGED:
			log(" changed: " + res);
			break;
		default:
			break;
		}
	}

	/**
	 * @param delta
	 * @param res
	 * @throws CoreException
	 */
	private void handleJapidviewsChange(IResourceDelta delta, IResource res) throws CoreException {
		TemplateTransformer.resetImports(res.getProject());
		switch (delta.getKind()) {
		case IResourceDelta.REMOVED:
			if (res instanceof IFile) {
				IFile f = ((IFile) res);
				System.out.println("JapidDeltaVisitor: file deleted: " + f);
				if (isTemplateSource(f)) {
					removeDerivedFile(f);
				} else {
					IFile template = getTemplateSource(f);
					if (template != null) {
						JapidFullBuildVisitor.convertTemplate(template);
					}
				}
			}
			break;
		case IResourceDelta.ADDED:
			log(" added: " + res);
			if (res instanceof IFile) {
				IFile f = ((IFile) res);
				if (isTemplateSource(f)) {
//					removeDerivedFile(f);
					JapidFullBuildVisitor.convertTemplate(res);
				}
			}
			break;
		case IResourceDelta.CHANGED:
			log(" changed: " + res);
			if (res instanceof IFile) {
				// remove derived first
				IFile f = ((IFile) res);
				if (isTemplateSource(f)) {
//					removeDerivedFile(f);
					JapidFullBuildVisitor.convertTemplate(res);
				} else {
					IFile template = getTemplateSource(f);
					// result of refactoring, which should be overridden
					if (template != null) {
						JapidFullBuildVisitor.convertTemplate(template);
					}
				}
				// create new
				break;
			}
			break;
		default:
			break;
		}
	}

	/**
	 * determine if the resource is the artifact of a Japid template and return
	 * the source template if it is.
	 * 
	 * @param f
	 * @throws CoreException
	 */
	private IFile getTemplateSource(IFile f) throws CoreException {
		String filePath = f.getProjectRelativePath().toString();
		if (filePath.endsWith(".java") && !filePath.contains("_javatags")) {
			String srcTemplate = DirUtil.mapJavaToSrc(filePath);
			IFile srcTemplateFile = f.getProject().getFile(srcTemplate);
			if (srcTemplateFile.exists() && isTemplateSource(srcTemplateFile)) {
				// let's convert it
				return srcTemplateFile;
			}
		}
		return null;
	}
//
//	/**
//	 * @param res
//	 * @throws CoreException
//	 */
//	private void inspectAndGen(IResource res) throws CoreException {
//		if (res instanceof IFile) {
//			// remove derived first
//			IFile f = ((IFile) res);
//			if (isTemplateSource(f)) {
//				removeDerivedFile(f);
//				JapidFullBuildVisitor.convertTemplate(res);
//			} else {
//				IFile template = getTemplateSource(f);
//				if (template != null) {
//					JapidFullBuildVisitor.convertTemplate(template);
//				}
//			}
//			// create new
//		}
//	}

	/**
	 * @param f
	 * @throws CoreException
	 */
	private static void removeDerivedFile(IFile f) throws CoreException {
		// remove the generated java code
		String filePath = f.getProjectRelativePath().toString();
		String templateJavaFile = DirUtil.mapSrcToJava(filePath);
		IFile jFile = f.getProject().getFile(templateJavaFile);
		if (jFile.exists()) {
			log("removeDerivedFile(): " + jFile);
			jFile.delete(true, null);
		}
	}

	/**
	 * @param msg
	 */
	private static void log(String msg) {
		System.out.println("JapidDeltaVisitor." + msg);
	}

	/**
	 * @param f
	 * @param filePath
	 * @return
	 */
	public static boolean isTemplateSource(IFile f) {
		String filePath = f.getProjectRelativePath().toString();
		boolean isTemplate = filePath.startsWith("app/japidviews") &&
				("html".equals(f.getFileExtension())
						|| "xml".equals(f.getFileExtension())
						|| "json".equals(f.getFileExtension())
						|| "js".equals(f.getFileExtension())
						|| "css".equals(f.getFileExtension())
				|| "txt".equals(f.getFileExtension())
				);
		return isTemplate;
	}

}

package org.playframework.playclipse.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

import cn.bran.japid.util.DirUtil;

public class JapidDeltaVisitor implements IResourceDeltaVisitor {
	static {
		JapidFullBuildVisitor.initTemplateCLassMeta();
	}
	
	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource res = delta.getResource();
		switch (delta.getKind()) {
		case IResourceDelta.REMOVED:
			if (res instanceof IFile) {
				IFile f = ((IFile)res);
				System.out.println("res deleted: " + f);
				if (isTemplateSource(f)) {
					// remove the generated java code
					String filePath = f.getProjectRelativePath().toString();
					String templateJavaFile  = DirUtil.mapSrcToJava(filePath);
					IFile jFile = f.getProject().getFile(templateJavaFile);
					if (jFile.exists()) {
						jFile.delete(true, null);
					}
				}
			}
			break;
		case IResourceDelta.ADDED:
		case IResourceDelta.CHANGED:
			if (res instanceof IFile) {
				JapidFullBuildVisitor.convertTemplate(res);
			}
			break;
		}

		return true;
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

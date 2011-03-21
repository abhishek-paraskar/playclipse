package org.playframework.playclipse.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

public class JapidCleanVisitor implements IResourceVisitor {

	@Override
	public boolean visit(IResource res) throws CoreException {
		if (res instanceof IFile) {
			IFile f = ((IFile)res);
			String filePath = f.getProjectRelativePath().toString();
			boolean isTemplateJava = 
				filePath.startsWith("app/japidviews") 
				&& "java".equals(f.getFileExtension())
				&& !filePath.contains("_javatags");
			if (isTemplateJava) {
				System.out.println("cleanvisitor: delete: " + f.getFullPath());
				f.delete(true, null);
			}
		}
		return true;
	}

}

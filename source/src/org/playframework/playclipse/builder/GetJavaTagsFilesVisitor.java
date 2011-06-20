package org.playframework.playclipse.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

import cn.bran.japid.classmeta.AbstractTemplateClassMetaData;

public class GetJavaTagsFilesVisitor implements IResourceVisitor {
	private static final String APP_JAPIDVIEWS_JAVATAGS = "app/japidviews/_javatags/";
	public List<String> tagClassNames = new ArrayList<String>();
	
	@Override
	public boolean visit(IResource res) throws CoreException {
		if (res instanceof IFile) {
			IFile f = ((IFile)res);
			String filePath = f.getProjectRelativePath().toString();
			boolean isTemplateJava = 
				filePath.startsWith(APP_JAPIDVIEWS_JAVATAGS) && "java".equals(f.getFileExtension());
			if (isTemplateJava) {
				String className = filePath.substring("app/".length()).replace('/', '.');
				className = className.substring(0, className.lastIndexOf(".java"));
				tagClassNames.add(className);
			}
		}
		return true;
	}

	public void addJavaTagsImports() {
		for (String cname: tagClassNames) {
			AbstractTemplateClassMetaData.addImportStaticGlobal(cname);
		}
	}
}

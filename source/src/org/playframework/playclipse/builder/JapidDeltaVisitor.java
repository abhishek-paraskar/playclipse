package org.playframework.playclipse.builder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

import bran.japidplugin.TemplateTransformer;
import cn.bran.japid.classmeta.AbstractTemplateClassMetaData;
import cn.bran.play.JapidPlayAdapter;
import cn.bran.play.JapidPlugin;
import cn.bran.play.NoEnhance;
import cn.bran.play.WebUtils;

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
				String filePath = f.getProjectRelativePath().toString();
				boolean isTemplate = filePath.startsWith("app/japidviews") && "html".equals(f.getFileExtension());
				if (isTemplate) {
					// remove the generated java code
					String templateJavaFile  = filePath.substring(0, filePath.lastIndexOf(".html")) + ".java";
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


}

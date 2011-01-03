package org.playframework.playclipse.builder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

import bran.japidplugin.TemplateTransformer;

import cn.bran.japid.classmeta.AbstractTemplateClassMetaData;
import cn.bran.play.JapidPlayAdapter;
import cn.bran.play.JapidPlugin;
import cn.bran.play.NoEnhance;
import cn.bran.play.WebUtils;

public class JapidFullBuildVisitor implements IResourceVisitor {
	static {
		initTemplateCLassMeta();
	}

	public static void initTemplateCLassMeta() {
		AbstractTemplateClassMetaData.addImportStatic(JapidPlayAdapter.class);
		AbstractTemplateClassMetaData.addImportStaticGlobal("play.data.validation.Validation");
		AbstractTemplateClassMetaData.addImportStaticGlobal("play.templates.JavaExtensions");
		AbstractTemplateClassMetaData.addImportStatic(WebUtils.class);
		AbstractTemplateClassMetaData.addAnnotation(NoEnhance.class);
		AbstractTemplateClassMetaData.addImportLineGlobal(JapidPlugin.JAPIDVIEWS_ROOT + "._layouts.*");
		AbstractTemplateClassMetaData.addImportLineGlobal(JapidPlugin.JAPIDVIEWS_ROOT + "._javatags.*");
		AbstractTemplateClassMetaData.addImportLineGlobal(JapidPlugin.JAPIDVIEWS_ROOT + "._tags.*");
		AbstractTemplateClassMetaData.addImportLineGlobal("play.mvc.Scope.*");
		AbstractTemplateClassMetaData.addImportLineGlobal("play.mvc.Http.*");
		AbstractTemplateClassMetaData.addImportLineGlobal("play.data.validation.Validation");
		AbstractTemplateClassMetaData.addImportLineGlobal("play.data.validation.Error");
		AbstractTemplateClassMetaData.addImportLineGlobal("models.*");
		AbstractTemplateClassMetaData.addImportLineGlobal("controllers.*");
		AbstractTemplateClassMetaData.addImportLineGlobal("static  japidviews._javatags.JapidWebUtil.*");
	}

	@Override
	public boolean visit(IResource res) throws CoreException {
		if (res instanceof IFile) {
			convertTemplate(res);
		}
		return true;
	}
	/**
	 * @param res
	 * @throws CoreException
	 */
	public static void convertTemplate(IResource res) throws CoreException {
		IFile f = ((IFile)res);
		String filePath = f.getProjectRelativePath().toString();
		boolean isTemplate = filePath.startsWith("app/japidviews") && "html".equals(f.getFileExtension());
		if (isTemplate) {
			String templateJavaFile  = f.getProjectRelativePath().toString().substring(0, filePath.lastIndexOf(".html")) + ".java";
			IFile jFile = f.getProject().getFile(templateJavaFile);
			
			InputStream is = transform(f);
			
			if (!jFile.exists()) {
				jFile.create(is, true, null);
				jFile.setDerived(true, null);
			}
			else {
				jFile.setContents(is, IFile.FORCE, null);
				jFile.setDerived(true, null);
			}
		}
	}

	private static InputStream transform(IFile f) {
		try {
			String code = TemplateTransformer.generate(f);
			return new ByteArrayInputStream(code.getBytes("UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}

package org.playframework.playclipse.builder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;

import bran.japidplugin.TemplateTransformer;
import cn.bran.japid.classmeta.AbstractTemplateClassMetaData;
import cn.bran.japid.util.DirUtil;
import cn.bran.play.JapidPlayAdapter;
import cn.bran.play.JapidPlugin;

public class JapidFullBuildVisitor implements IResourceVisitor {
	static {
		initTemplateCLassMeta();
	}

	public static void initTemplateCLassMeta() {
		AbstractTemplateClassMetaData.addImportStatic(JapidPlayAdapter.class);
		AbstractTemplateClassMetaData.addImportStaticGlobal("play.data.validation.Validation");
		AbstractTemplateClassMetaData.addImportStaticGlobal("play.templates.JavaExtensions");
		// AbstractTemplateClassMetaData.addImportStatic(WebUtils.class);
		// AbstractTemplateClassMetaData.addAnnotation(NoEnhance.class);
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
		IFile f = ((IFile) res);
		if (JapidDeltaVisitor.isTemplateSource(f)) {
			InputStream is = transform(f);

			String templateJavaFile = DirUtil.mapSrcToJava(f.getProjectRelativePath().toString());
			IFile jFile = f.getProject().getFile(templateJavaFile);

			if (!jFile.exists()) {
				jFile.create(is, true, null);
			} else {
				setReadOnly(jFile, false);
				 jFile.setContents(is, IFile.FORCE, null);
//				jFile.setContents(is, 0, null);
				// don't use force seems safer
				// in case of out of sync
			}
			jFile.setDerived(true, null);
//			setReadOnly(jFile, true);
//			jFile.refreshLocal(0, null);
//			res.getParent().refreshLocal(IResource.DEPTH_ONE, null);
			System.out.println("JapidFulBuildVisitor.convertTemplate(), from '" + f.getFullPath() + "' to '" + jFile.getFullPath() + "'");
		}
	}

	/**
	 * @param jFile
	 * @param b 
	 * @throws CoreException
	 */
	private static void setReadOnly(IFile jFile, boolean b) throws CoreException {
		ResourceAttributes ra = new ResourceAttributes();
		ra.setReadOnly(b);
		jFile.setResourceAttributes(ra);
	}

	private static InputStream transform(IFile f) {
		try {
			String code = TemplateTransformer.generate(f);
			return new ByteArrayInputStream(code.getBytes("UTF-8"));
		} catch (Exception e) {
			// ByteArrayOutputStream out = new ByteArrayOutputStream();
			// PrintStream ps = new PrintStream(out, true);
			// e.printStackTrace(ps);
			// put the compiling error in the generated file to get the
			// attention.
			String err = "XXX//~~Japid compiler generated message:\n\n//Error in compiling file: " + f.getName()
					+ ". The error message is:\n\n//" + e.getMessage();
			err += "\n\n//Please fix the error in the template file. This file will be re-generated. ";
			try {
				return new ByteArrayInputStream(err.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e1) {
				throw new RuntimeException(err);
			}
		}
	}

}

package org.playframework.playclipse.builder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.playframework.playclipse.PlayPlugin;

import bran.japidplugin.TemplateTransformer;

public class JapidFullBuildCollector implements IResourceVisitor {
	static {
		JapidFullBuildVisitor.initTemplateCLassMeta();
	}
	
	List<IFile> allFiles = new ArrayList<IFile>();

	@Override
	public boolean visit(IResource res) throws CoreException {
		if (res instanceof IFile) {
			IFile f = ((IFile) res);
			String filePath = f.getProjectRelativePath().toString();
			boolean isTemplate = filePath.startsWith("app/japidviews") && "html".equals(f.getFileExtension());
			if (isTemplate) {
				allFiles.add(f);
			}
		}
		return true;
	}

	public void startWork(IProgressMonitor mon) {
		if (allFiles.size() > 0) {
			mon.beginTask("Compiling Japid templates", allFiles.size());
			int counter = 1;
			for (IFile f : allFiles) {
				String filePath = f.getProjectRelativePath().toString();
				String templateJavaFile = filePath.substring(0, filePath.lastIndexOf(".html")) + ".java";
				IFile jFile = f.getProject().getFile(templateJavaFile);
				mon.subTask("Compiling: " + filePath);
				InputStream is = transform(f);

				try {
					if (!jFile.exists()) {
						jFile.create(is, true, null);
						jFile.setDerived(true, null);
					} else {
						jFile.setContents(is, IFile.FORCE, null);
						jFile.setDerived(true, null);
					}
				} catch (CoreException e) {
					PlayPlugin.showError(e);
				}
				mon.worked(counter++);
			}
			mon.done();
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

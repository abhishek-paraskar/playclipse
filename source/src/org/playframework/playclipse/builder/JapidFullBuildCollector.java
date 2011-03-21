package org.playframework.playclipse.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.playframework.playclipse.PlayPlugin;

/**
 * Collect all html templates and generate java source from them in a batch
 * 
 * @author Bing Ran<bing_ran@hotmail.com>
 * 
 */
public class JapidFullBuildCollector implements IResourceVisitor {
	static {
		JapidFullBuildVisitor.initTemplateCLassMeta();
	}

	List<IFile> allFiles = new ArrayList<IFile>();

	@Override
	public boolean visit(IResource res) throws CoreException {
		if (res instanceof IFile) {
			IFile f = ((IFile) res);
			if (JapidDeltaVisitor.isTemplateSource(f)) {
				allFiles.add(f);
			}
		}
		return true;
	}

	public void build(IProgressMonitor mon) {
		if (allFiles.size() > 0) {
			mon.beginTask("Compiling Japid templates", allFiles.size());
			int counter = 1;
			for (IFile f : allFiles) {
				try {
					JapidFullBuildVisitor.convertTemplate(f);
				} catch (CoreException e) {
					e.printStackTrace();
					PlayPlugin.showError(e);
				}
				mon.worked(counter++);
			}
			mon.done();
		}
	}
}

package org.playframework.playclipse.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.playframework.playclipse.FilesAccess;
import org.playframework.playclipse.PlayPlugin;
import org.playframework.playclipse.builder.JapidFullBuildCollector;
import org.playframework.playclipse.builder.PlayNature;

import cn.bran.play.JapidPlugin;

/**
 * the handler that invoke Japid gen() command
 * 
 * @author Bing Ran<bing_ran@hotmail.com>
 * 
 */
public class JapidRegenHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IProject project = null;
		project = FilesAccess.getProjectFromMenuSelection(event);

		IProgressMonitor dummyPM = new IProgressMonitor() {

			@Override
			public void worked(int work) {
			}

			@Override
			public void subTask(String name) {
			}

			@Override
			public void setTaskName(String name) {
			}

			@Override
			public void setCanceled(boolean value) {
			}

			@Override
			public boolean isCanceled() {
				return false;
			}

			@Override
			public void internalWorked(double work) {
			}

			@Override
			public void done() {
			}

			@Override
			public void beginTask(String name, int totalWork) {
			}
		};

		if (project != null) {
			try {
				JapidFullBuildCollector batchCompiler = new JapidFullBuildCollector();
				project.accept(batchCompiler);
				batchCompiler.build(dummyPM);
			} catch (CoreException e) {
				PlayPlugin.showError(e);
			}
		}
		return null;
	}

}

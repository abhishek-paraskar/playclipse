package org.playframework.playclipse.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.playframework.playclipse.FilesAccess;
import org.playframework.playclipse.builder.PlayNature;

/**
 * the handler that get's hooked up to the popup menu in in the explorer views
 * to add play nature.
 * 
 * @author Bing Ran<bing_ran@hotmail.com>
 * 
 */
public class AddNatureHandler extends AbstractHandler {

	/**
	 * Toggles sample nature on a project
	 * 
	 * @param project
	 *            to have sample nature added or removed
	 */
	private void addNature(IProject project) {
		try {
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();

			for (int i = 0; i < natures.length; ++i) {
				if (PlayNature.NATURE_ID.equals(natures[i])) {
					return;
				}
			}

			// Add the nature
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = PlayNature.NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IProject project = null;
		project = FilesAccess.getProjectFromMenuSelection(event);
		if (project != null)
			addNature(project);
		else {
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
			MessageDialog.openError(window.getShell(), "PlayClipse", "Cannot find the project object.");
		}
		return null;
	}

}

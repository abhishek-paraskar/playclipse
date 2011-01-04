
package org.playframework.playclipse.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IFileEditorInput;
import org.playframework.playclipse.FilesAccess;
import org.playframework.playclipse.PlayPlugin;

import fr.zenexity.pdt.editors.EditorHelper;

/**
 *  from the Japid template html go to the derived Java file, in the editor pane
 *  
 *  @author bran
 */
public class GoToDerivedJavaHandler extends AbstractHandler {


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String jFile = null;
		EditorHelper editor = EditorHelper.getCurrent(event);
		if(editor != null) {
			String relativePath = ((IFileEditorInput) editor.textEditor.getEditorInput()).getFile().getProjectRelativePath().toString();
			jFile = relativePath.substring(0, relativePath.lastIndexOf("html")) + "java";
			IFile f = editor.getProject().getFile(jFile);
			try {
				FilesAccess.openFile(f);
			} catch (CoreException e) {
				PlayPlugin.showError(e);
			}
		}
		return null;
	}


}

package org.playframework.playclipse.wizards;

import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

public class ControllerWizardPage extends ClassWizardPage {

	public ControllerWizardPage(ISelection selection) {
		super(selection);
		setTitle("Play Controller");
	}

	@Override
	protected String description() { return "Create a new controller for your Play project."; }

	@Override
	protected String defaultName() { return "MyController"; }

	@Override
	protected String defaultPackage() {
		return "controllers";
	}

	@Override
	protected String nameLabel() {
		return "&Controller name:";
	}

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> pa = super.getParameters();
		pa.put("useJapid", String.valueOf(super.useJapid));
		return pa;
	}

	@Override
	protected void initialize() {
		super.initialize();
		if (containerText.getText().trim().length() ==0) {
			IProject proj = getEditorProject();
			if (proj != null) {
				String pname = proj.getName();
				containerText.setText("/" + pname + "/app/controllers");
			}
		}
	}

	private static IProject getEditorProject() {
		ITextEditor editor = (ITextEditor) PlatformUI.getWorkbench()
		 	.getActiveWorkbenchWindow().getActivePage().getActiveEditor();

		try {
			IFile curfile = ((IFileEditorInput) editor.getEditorInput()).getFile();
			return curfile.getProject();
		} catch (Exception e) {
			return null;
		}
	}
	
}

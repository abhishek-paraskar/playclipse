package bran.japidplugin;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class RenameTemplateChange extends RenameTemplateChangeBase{


	public RenameTemplateChange(IPath resourcePath, String newName) {
		super(resourcePath, newName);
	}

	public RenameTemplateChange(IPath newPath, String oldName, long currentStamp) {
		super(newPath, oldName, currentStamp);
	}

	@Override
	public String getName() {
		return "Change template name: " + fResourcePath + " to " + fNewName;
	}

	@Override
	protected void prePerform(IResource srcRes, IPath newPath) throws CoreException {
	}

	@Override
	protected void postPerform(IResource srcRes, IPath newPath) throws CoreException {
//		cleanJapidviews();
	}

}

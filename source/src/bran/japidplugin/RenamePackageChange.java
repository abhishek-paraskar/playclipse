package bran.japidplugin;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

public class RenamePackageChange extends RenameTemplateChangeBase {

	private static final String CONTROLLERS = "controllers.";

	public RenamePackageChange(IPath resourcePath, String newName) {
		super(resourcePath, newName);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		return super.perform(pm);
	}


	@Override
	protected void prePerform(IResource srcRes, IPath newPath) throws CoreException {
		// XXX to scale we can do a Java search for all the references in the 
		// derived Java classes and removed those for better performance
//		cleanJapidviews();
	}


	@Override
	protected void postPerform(IResource srcRes, IPath newPath) throws CoreException {
//		cleanJapidviews();
	}


	@Override
	public String getName() {
		return "Renaming controller package from: '" + fResourcePath + "' to 'app/" + fNewName + "'"; 
	}

	@Override
	protected IPath getDestinationResourcePath(IPath path, String newName) {
		IPath japidPath = getJapidviewsPath();

		if (newName.startsWith(CONTROLLERS)) {
			newName = newName.substring(CONTROLLERS.length());
		}
		return japidPath.append(newName.replace('.', '/'));
	}
}

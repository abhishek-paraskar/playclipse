package bran.japidplugin;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * rename a package of templates due the change to a controller name
 * 
 * @author Bing Ran<bing_ran@hotmail.com>
 *
 */
public class RenameTemplatePackageChange extends RenameTemplateChangeBase {


	public RenameTemplatePackageChange(IPath resourcePath, String newName) {
		super(resourcePath, newName);
	}

	public RenameTemplatePackageChange(IPath newPath, String oldName, long currentStamp) {
		super(newPath, oldName, currentStamp);
	}

	@Override
	public String getName() {
		IPath parent = fResourcePath.removeLastSegments(1);
		return "Renaming template folder from: '" + fResourcePath + "' to: '" + parent + "/" + fNewName + "'";
	}

	@Override
	protected void prePerform(IResource srcRes, IPath newPath) throws CoreException {
		// remove all the Java artifact of japidviews to prevent refactoring the code from "invoke" tag 
//		cleanJapidviews(); // way too expensive
	}

	@Override
	protected void postPerform(IResource srcRes, IPath newPath) throws CoreException  {
//		cleanJapidviews();
	}
}

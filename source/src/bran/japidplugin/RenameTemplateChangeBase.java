package bran.japidplugin;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.resource.ResourceChange;
import org.playframework.playclipse.PlayPlugin;
import org.playframework.playclipse.builder.JapidCleanVisitor;

import cn.bran.play.JapidPlugin;

public abstract class RenameTemplateChangeBase extends ResourceChange{

	protected final String fNewName;
	// source in the japidviews to be renamed
	protected final IPath fResourcePath;
	protected final long fStampToRestore;

	protected ChangeDescriptor fDescriptor;

	/**
	 * Creates the change.
	 *
	 * @param resourcePath the path of the resource to rename
	 * @param newName the new name. Must not be empty.
	 */
	public RenameTemplateChangeBase(IPath resourcePath, String newName) {
		this(resourcePath, newName, IResource.NULL_STAMP);
	}

	/**
	 * Creates the change with a time stamp to restore.
	 *
	 * @param resourcePath  the path of the resource to rename
	 * @param newName the new name. Must not be empty.
	 * @param stampToRestore the time stamp to restore or {@link IResource#NULL_STAMP} to not restore the
	 * time stamp.
	 */
	protected RenameTemplateChangeBase(IPath resourcePath, String newName, long stampToRestore) {
		if (resourcePath == null || newName == null || newName.length() == 0) {
			throw new IllegalArgumentException();
		}

		String resPathName = resourcePath.toString();
		if (!resPathName.contains("japidviews"))
			throw new IllegalArgumentException("resource to rename must be in the 'japidviews' tree: " + resPathName);
		
		fResourcePath= resourcePath;
		fNewName= newName;
		fStampToRestore= stampToRestore;
		fDescriptor= null;
		setValidationMethod(VALIDATE_NOT_DIRTY);
	}

	@Override
	public ChangeDescriptor getDescriptor() {
		return fDescriptor;
	}

	public void setDescriptor(ChangeDescriptor descriptor) {
		fDescriptor= descriptor;
	}


	@Override
	protected IResource getModifiedResource() {
		return ResourcesPlugin.getWorkspace().getRoot().findMember(fResourcePath);
	}

	protected IResource getJapidViewsResource() {
		return ResourcesPlugin.getWorkspace().getRoot().findMember(getJapidviewsPath());
	}

	protected IResource getDestinationResource() {
		IPath desPath = getDestinationResourcePath(fResourcePath, fNewName);
		return ResourcesPlugin.getWorkspace().getRoot().findMember(desPath);
	}


	/**
	 * Returns the new name.
	 *
	 * @return return the new name
	 */
	public String getNewName() {
		return fNewName;
	}

	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		try {
			pm.beginTask(getName(), 1);
	
			IResource srcRes= getModifiedResource();
			
			long currentStamp= srcRes.getModificationStamp();
			IResource desRes = getDestinationResource();
			if (desRes != null)
				return null;
			
			IPath destPath= getDestinationResourcePath();
			
			prePerform(srcRes, destPath);
			srcRes.move(destPath, IResource.SHALLOW, pm);
			postPerform(srcRes, destPath);
			
			if (fStampToRestore != IResource.NULL_STAMP) {
				IResource newResource= ResourcesPlugin.getWorkspace().getRoot().findMember(destPath);
				newResource.revertModificationStamp(fStampToRestore);
			}
			String oldName= fResourcePath.lastSegment();
			return new RenameTemplateChange(destPath, oldName, currentStamp);
		}
		catch(Exception e) {
			PlayPlugin.showError(e.getMessage());
			if (e instanceof NullPointerException)
				throw new CoreException(null);
		}
		finally {
			pm.done();
		}
		return null;
	}

	abstract protected void prePerform(IResource srcRes, IPath newPath) throws CoreException;
	abstract protected void postPerform(IResource srcRes, IPath newPath) throws CoreException;

	private IPath getDestinationResourcePath() {
		return getDestinationResourcePath(fResourcePath, fNewName);
	}

	protected IPath getDestinationResourcePath(IPath srcPath, String newName) {
		return srcPath.removeLastSegments(1).append(newName);
	}

	/**
	 * @param path
	 * @return
	 */
	protected IPath getJapidviewsPath() {
		String[] segments = fResourcePath.segments();
		int i = 0;
		for (; i < segments.length; i++) {
			if ("japidviews".equals(segments[i])) {
				break;
			}
		}
		IPath japidPath = fResourcePath.uptoSegment(++i);
		return japidPath;
	}

	/**
	 * an expensive operation since it would trigger the regeneration of 
	 * @throws CoreException
	 */
	protected void cleanJapidviews() throws CoreException {
		IResource japids = getJapidViewsResource();
		japids.accept(new JapidCleanVisitor());
	}
	
}

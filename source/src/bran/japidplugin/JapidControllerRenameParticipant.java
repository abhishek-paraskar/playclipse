package bran.japidplugin;


import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEditGroup;
import org.playframework.playclipse.builder.PlayBuilder;
import org.playframework.playclipse.builder.Renaming;


/*
   A rename participant that updates type references in '*.special' files.
 

 	<extension point="org.eclipse.ltk.core.refactoring.renameParticipants">
	  <renameParticipant
	  	id="org.eclipse.jdt.ui.examples.MyRenameTypeParticipant"
	  	name="Rename participant for *.special files"
	  	class="org.eclipse.jdt.ui.examples.MyRenameTypeParticipant">
	  	<enablement>
	  	  <with variable="affectedNatures">
	  	    <iterate operator="or">
	  	      <equals value="org.eclipse.jdt.core.javanature"/>
	  	    </iterate>
	  	  </with>
	  	  <with variable="element">
		  	 <instanceof value="org.eclipse.jdt.core.IType"/>
	  	  </with>
	  	</enablement>
	  </renameParticipant>
	</extension>
	
 */


public class JapidControllerRenameParticipant extends RenameParticipant {
	static enum ChangeType {
		method, type, packageFragment
	}
	private static final String CONTROLLERS = "controllers.";
	private IType fType;
	private IMethod method;
	private IJavaProject jProject;
	private IProject project;
	private ChangeType changeType;
	// the destination package
	private String packageName;
	
	private static final String[] templateExtensions = {".html", ".xml", ".json", ".js", ".css", ".txt"};
	@Override
	protected boolean initialize(Object element) {
		if(element instanceof IType) {
			fType= (IType) element;
			changeType = ChangeType.type;
			jProject = fType.getJavaProject();
			project = jProject.getProject();
			packageName = fType.getFullyQualifiedName();
		}
		else if (element instanceof IMethod) {
			changeType = ChangeType.method;
			method = (IMethod) element;
			fType=method.getDeclaringType();
			packageName = fType.getFullyQualifiedName();
			jProject = fType.getJavaProject();
			project = jProject.getProject();
		}
		else if (element instanceof IPackageFragment) {
			changeType = ChangeType.packageFragment;
			IPackageFragment p = (IPackageFragment) element;
			packageName = p.getElementName();
			jProject = p.getJavaProject();
			project = jProject.getProject();
		}
		return true;
	}

	@Override
	public String getName() {
		return "Japid Template Naming Participation"; 
	}
	
	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) {
		return new RefactoringStatus();
	}

	@Override
	public Change createPreChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
//		return doChange(pm);
		return null;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException {
		return doChange(pm);
//		return null;
	}

	/**
	 * @param pm
	 * @return
	 */
	private Change doChange(IProgressMonitor pm) {
		if (changeType == ChangeType.type) {
			return renameTemplatePackage(pm);
		}
		else if (changeType == ChangeType.method)  {
			// method change
			return renameTemplateFile(pm);
		}
		else if (changeType == ChangeType.packageFragment)  {
			return renamePackage(pm);
		}
		return null;
	}

	private Change renameTemplateFile(IProgressMonitor pm) {
		CompositeChange result = new CompositeChange("Japid Template File name change."); 
		final String newName= getArguments().getNewName();
		if (packageName.startsWith(CONTROLLERS)) {
			try {
				fType.getParent().getResource().refreshLocal(IResource.DEPTH_INFINITE, pm);
			} catch (CoreException e) {
				e.printStackTrace();
				return null;
			}
			
			String filenameRoot = packageName.substring(CONTROLLERS.length()) + "." + method.getElementName();
			for (String extension: templateExtensions) {
				String tempName = "app/japidviews/" + filenameRoot.replace('.', '/') + extension; 
				IFile file = project.getFile(tempName);
				if (file.exists()) {
					RenameTemplateChange ren = new RenameTemplateChange(file.getFullPath(), newName + extension);
					result.add(ren);;
				}
			}
		}
		return result.getChildren().length > 0 ? result : null;
	}

	/**
	 * renaming packages are tricky when dealing with sub-packages. The renaming dialog gives users the to choice to 
	 * rename the sub-package. The default is not to do it.
	 * 
	 * How do I know if the user also would like rename the subs?
	 * 
	 * Let's make it a rule to always rename sub directory!
	 * 
	 * @param pm
	 * @return
	 */
	private Change renamePackage(IProgressMonitor pm) {
		RenameArguments args = getArguments();
		final String newName = args.getNewName();
		if (newName.startsWith(CONTROLLERS) && packageName.startsWith(CONTROLLERS)) {
			PlayBuilder.packageRenamingRefactor(new Renaming(packageName, newName));
			// find out the 
			String pathName = packageName.substring(CONTROLLERS.length());
			String tfolder = "app/japidviews/" + pathName.replace('.', '/');
			IFolder folder = project.getFolder(tfolder);
			if (folder.exists()) {
				// let rename it
				try {
					folder.refreshLocal(IResource.DEPTH_INFINITE, pm);
				} catch (CoreException e1) {
					e1.printStackTrace();
				}
				RenameTemplateChangeBase ren = new RenamePackageChange(folder.getFullPath(), newName);
				return ren;
			}
		}
		return null;
	}

	private Change renameTemplatePackage(IProgressMonitor pm) {
		final String newName= getArguments().getNewName();
		if (packageName.startsWith(CONTROLLERS)) {
			
			String pathName = packageName.substring(CONTROLLERS.length());
			String tfolder = "app/japidviews/" + pathName.replace('.', '/');
			IFolder folder = project.getFolder(tfolder);
			if (folder.exists()) {
				// let rename it
				try {
					folder.refreshLocal(IResource.DEPTH_INFINITE, pm);
				} catch (CoreException e1) {
					e1.printStackTrace();
				}
				RenameTemplateChangeBase ren = new RenameTemplatePackageChange(folder.getFullPath(), newName);
				return ren;
			}
			//
		}
		return null;
	}

	/**
	 * @param pm
	 * @return
	 */
	private Change renameRefs(IProgressMonitor pm) {
		final HashMap<IFile, Change> changes= new HashMap<IFile, Change>();
		final String newName= getArguments().getNewName();
		
		// use the text search engine to find matches in my special files
		// in a real world implementation, clients would use their own, more precise search engine
		
		IResource[] roots= { fType.getJavaProject().getProject() };  // limit to the current project
		String[] fileNamePatterns= { "*.japid" }; //$NON-NLS-1$ // all files with file suffix 'special'
		FileTextSearchScope scope= FileTextSearchScope.newSearchScope(roots , fileNamePatterns, false);
		Pattern pattern= Pattern.compile(fType.getElementName()); // only find the simple name of the type
		
		TextSearchRequestor collector= new TextSearchRequestor() {
			@Override
			public boolean acceptPatternMatch(TextSearchMatchAccess matchAccess) throws CoreException {
				IFile file= matchAccess.getFile();
				TextFileChange change= (TextFileChange) changes.get(file);
				if (change == null) {
					TextChange textChange= getTextChange(file); // an other participant already modified that file?
					if (textChange != null) {
						return false; // don't try to merge changes
					}
					change= new TextFileChange(file.getName(), file);
					change.setEdit(new MultiTextEdit());
					changes.put(file, change);
				}
				ReplaceEdit edit= new ReplaceEdit(matchAccess.getMatchOffset(), matchAccess.getMatchLength(), newName);
				change.addEdit(edit);
				change.addTextEditGroup(new TextEditGroup("Update type reference", edit)); //$NON-NLS-1$
				return true;
			}
		};
		TextSearchEngine.create().search(scope, collector, pattern, pm);
		
		if (changes.isEmpty())
			return null;
		
		CompositeChange result= new CompositeChange("My special file updates"); //$NON-NLS-1$
		for (Iterator<Change> iter= changes.values().iterator(); iter.hasNext();) {
			result.add(iter.next());
		}
		return result;
	}

}
 

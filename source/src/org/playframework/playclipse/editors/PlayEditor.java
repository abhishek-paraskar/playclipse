package org.playframework.playclipse.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IFileEditorInput;
import org.playframework.playclipse.Navigation;

import fr.zenexity.pdt.editors.Editor;

public abstract class PlayEditor extends Editor {

	private static final String ACTION_IN_TAG = "action_in_tag";
	private static final String EXTENDS = "extends";
	private static final String TAG = "tag";
	private static final String ACTION = "action";
	private Navigation navigation;

	protected Navigation getNav() {
		if (navigation == null) {
			navigation = new Navigation(getHelper());
		}
		return navigation;
	}

	@Override
	public void openLink(IHyperlink link) {
		boolean isJapidView  =false;
		String filepath = getRelativePath().toString();
		if(filepath.startsWith("app/japidviews")) {
			isJapidView = true;
		}
		else {
			isJapidView = false;
		}
		
		String hyperlinkText = link.getHyperlinkText();
		String linkText = hyperlinkText;
		String typeLabel = link.getTypeLabel();
		if (typeLabel.equals(ACTION)) {
			if (linkText.startsWith("'") && linkText.endsWith("'")) {
				// Static file, e.g. @{'/public/images/favicon.png'}
				String path = linkText.substring(1, linkText.length() - 1);
				getNav().openOrCreate(path);
				return;
			}
			String nakedAction = linkText.replaceFirst("\\(.*\\)", "");
			if (nakedAction.indexOf('.') == -1) {
				// Relative reference, e.g. just "index"
				IFile curfile = ((IFileEditorInput)getEditorInput()).getFile();
				String controller = curfile.getParent().getName();
				nakedAction = controller + "." + nakedAction;
			}
			getNav().goToAction(nakedAction);
			return;
		}
		String hyper = hyperlinkText.replace('.', '/');
		if (typeLabel.equals(TAG)) {
			String tagName = hyper + ".html";
			if (isJapidView) {
				if (tagName.contains("/")){
					// should use absolute
					if (tagName.startsWith("japidviews")) {
						getNav().goToViewAbs("app/" + tagName);
					}
					else {
						getNav().goToViewAbs("app/japidviews/" + tagName);
					}
				}
				else {
					// simple tag name. test same package and the in the _tags package
					IFolder tagFolder = (IFolder)((IFileEditorInput)getEditorInput()).getFile().getParent();
					IFile tagFile = tagFolder.getFile(tagName);
					if (tagFile.exists()) {
						getNav().goToViewAbs(tagFile.getProjectRelativePath().toString());
					}
					else {
						getNav().goToViewAbs("app/japidviews/_tags/" + tagName);
					}
				}
			}
			else {
				getNav().goToView("tags/" + tagName);
			}
			return;
		}
		if (typeLabel.equals(EXTENDS) || typeLabel.equals("include")) {
			String layoutName = hyper;
			if (!hyper.endsWith("/html"))
				layoutName = hyper + ".html";
			else 
				layoutName = hyper.substring(0, hyper.lastIndexOf("/html")) + ".html";
				
			if (isJapidView) {
				if (layoutName.contains("/")){
					// should use absolute
					if (layoutName.startsWith("japidviews")) {
						getNav().goToViewAbs("app/" + layoutName);
					}
					else {
						getNav().goToViewAbs("app/japidviews/" + layoutName);
					}
				}
				else {
					// simple layout name. test same package and the in the _layouts package
					IFolder srcFolder = (IFolder)((IFileEditorInput)getEditorInput()).getFile().getParent();
					IFile layoutFile = srcFolder.getFile(layoutName);
					if (layoutFile.exists()) {
						getNav().goToViewAbs(layoutFile.getProjectRelativePath().toString());
					}
					else {
						getNav().goToViewAbs("app/japidviews/_layouts/" + layoutName);
					}
				}
			}
			else {
				getNav().goToView("layouts/" + layoutName);
			}
		}
		if (typeLabel.equals(ACTION_IN_TAG)) {
			System.out.println(linkText);
			String nakedAction = linkText.replace("@", "").replaceFirst("\\(.*\\)", "");
			getNav().goToAction(nakedAction);
		}
	}
	
}

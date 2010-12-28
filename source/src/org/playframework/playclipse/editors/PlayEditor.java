package org.playframework.playclipse.editors;

import org.eclipse.core.resources.IFile;
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
		String linkText = link.getHyperlinkText();
		if (link.getTypeLabel().equals(ACTION)) {
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
		if (link.getTypeLabel().equals(TAG)) {
			getNav().goToView("tags/" + link.getHyperlinkText().replace('.', '/') + ".html");
			return;
		}
		if (link.getTypeLabel().equals(EXTENDS) || link.getTypeLabel().equals("include")) {
			String path = link.getHyperlinkText();
			getNav().goToView(path);
		}
		if (link.getTypeLabel().equals(ACTION_IN_TAG)) {
			System.out.println(linkText);
			String nakedAction = linkText.replace("@", "").replaceFirst("\\(.*\\)", "");
			getNav().goToAction(nakedAction);
		}
	}
	
}

package org.playframework.playclipse.editors;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IFileEditorInput;
import org.playframework.playclipse.FilesAccess;
import org.playframework.playclipse.Navigation;
import org.playframework.playclipse.PlayPlugin;
import org.playframework.playclipse.editors.html.HTMLEditor;

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
		boolean isJapidView = false;
		String filepath = getRelativePath().toString();
		if (filepath.startsWith("app/japidviews")) {
			isJapidView = true;
		} else {
			isJapidView = false;
		}

		String hyperlinkText = link.getHyperlinkText();
		String linkText = hyperlinkText;
		String typeLabel = link.getTypeLabel();
		if (typeLabel.equals(HTMLEditor.IMPORT)) {
			if (linkText.endsWith(".") || linkText.endsWith("*")) {
				return;
			}
			String imported = linkText.replace('.', '/');
			
			IFile file = getProject().getFile("app/" + imported + ".java");
			try {
				FilesAccess.openFile(file);
			} catch (Exception e) {
				PlayPlugin.showError(e.getMessage());
			}
			return;
		}
		
		if (typeLabel.equals(HTMLEditor.IMPORT_STATIC)) {
			String imported = linkText.replace('.', '/');
			if (imported.endsWith("/"))
				imported = imported.substring(0, imported.length() - 1);
			IFile file = getProject().getFile("app/" + imported + ".java");
			try {
				FilesAccess.openFile(file);
			} catch (CoreException e) {
				PlayPlugin.showError(e.getMessage());
			}
			return;
		}
		
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
				IFile curfile = ((IFileEditorInput) getEditorInput()).getFile();
				String controller = curfile.getParent().getName();
				nakedAction = controller + "." + nakedAction;
			}

			if (nakedAction.indexOf('.') == nakedAction.lastIndexOf('.')) {
				// controller name without package. I need to figure out the
				// full qualified name
				String className = nakedAction.substring(0, nakedAction.indexOf('.'));
				// how about controllers.xxx
				if (getProject().getFile("app/controllers/" + className + ".java").exists()) {
					getNav().goToAction(nakedAction);
				} else {
					// I need to find if there is controller imports
					IFile curfile = ((IFileEditorInput) getEditorInput()).getFile();
					try {
						InputStream contents = curfile.getContents();
						BufferedReader reader = new BufferedReader(new InputStreamReader(contents));
						String line = "";// reader.readLine();
						Pattern p = Pattern.compile("\\s*`\\s*import\\s*(controllers\\.[^;]*)");
						while ((line = reader.readLine()) != null) {
							Matcher matcher = p.matcher(line);
							if (matcher.find()) {
								String pack = matcher.group(1).trim();
								String fqname = null;
								if (pack.endsWith(".*")) {
									fqname = pack.substring(0, pack.lastIndexOf("*")) + className;
									String src = "app/" + fqname.replace('.', '/') + ".java";
									if (getProject().getFile(src).exists()) {
										fqname += nakedAction.substring(nakedAction.indexOf('.'));
									} else {
										fqname = null;
									}
								} else if (pack.endsWith("." + className)) {
									fqname = pack + nakedAction.substring(nakedAction.indexOf('.'));
								}

								if (fqname != null) {
									getNav().goToAction(fqname);
									return;
								}
							}
						}
					} catch (Exception e) {
						PlayPlugin.showError(e);
					}
				}
			} else {
				getNav().goToAction(nakedAction);
			}
			return;
		}
		String hyper = hyperlinkText.replace('.', '/').trim();
		if (typeLabel.equals(TAG)) {
			String tagName = hyper + ".html";
			if (isJapidView) {
				if (tagName.contains("/")) {
					// should use absolute
					if (tagName.startsWith("japidviews")) {
						getNav().goToViewAbs("app/" + tagName);
					} else {
						getNav().goToViewAbs("app/japidviews/" + tagName);
					}
				} else {
					// simple tag name. test same package and the in the _tags
					// package
					IFolder tagFolder = (IFolder) ((IFileEditorInput) getEditorInput()).getFile().getParent();
					IFile tagFile = tagFolder.getFile(tagName);
					if (tagFile.exists()) {
						getNav().goToViewAbs(tagFile.getProjectRelativePath().toString());
					} else {
						getNav().goToViewAbs("app/japidviews/_tags/" + tagName);
					}
				}
			} else {
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
				if (layoutName.contains("/")) {
					// should use absolute
					if (layoutName.startsWith("japidviews")) {
						getNav().goToViewAbs("app/" + layoutName);
					} else {
						getNav().goToViewAbs("app/japidviews/" + layoutName);
					}
				} else {
					// simple layout name. test same package and the in the
					// _layouts package
					IFolder srcFolder = (IFolder) ((IFileEditorInput) getEditorInput()).getFile().getParent();
					IFile layoutFile = srcFolder.getFile(layoutName);
					if (layoutFile.exists()) {
						getNav().goToViewAbs(layoutFile.getProjectRelativePath().toString());
					} else {
						getNav().goToViewAbs("app/japidviews/_layouts/" + layoutName);
					}
				}
			} else {
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

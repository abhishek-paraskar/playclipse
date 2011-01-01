package org.playframework.playclipse.handlers;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.playframework.playclipse.PlayPlugin;

public class TestsHandler extends AbstractHandler {

	// TODO: Get the real port from application.conf
	private String TESTS_URL = "http://localhost:9000/@tests?select=all&auto=yes";

	public TestsHandler() {
	}

	/**
	 * the command has been executed, so let's extract the needed information
	 * from the application context.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			String browserPref = PlayPlugin.getDefault().getPreferenceStore().getString(PlayPlugin.PREF_BROWSER);
			if (browserPref.equals(PlayPlugin.PREF_BROWSER_INTERNAL)) {
				openInInternal(TESTS_URL);
			} else {
				openInExternal(TESTS_URL);
			}
		} catch (PartInitException e) {
		} catch (MalformedURLException e) {
		}
		return null;
	}

	private void openInExternal(String url) throws PartInitException, MalformedURLException {
		PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(url));
	}

	private void openInInternal(String url) throws PartInitException, MalformedURLException {
		IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser("testbrowser");
		browser.openURL(new URL(url));
	}

}

package org.playframework.playclipse.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.playframework.playclipse.PlayPlugin;
import org.playframework.playclipse.editors.ConfEditor;
import org.playframework.playclipse.editors.html.HTMLEditor;
import org.playframework.playclipse.editors.route.RouteEditor;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = PlayPlugin.getDefault().getPreferenceStore();

		store.setDefault(PlayPlugin.PREF_BROWSER, PlayPlugin.PREF_BROWSER_INTERNAL);

		RouteEditor.initRoutePrefStore(store);

		ConfEditor.initConfPrefStore(store);

		HTMLEditor.initHtmlEditorPrefStore(store);
	}

}

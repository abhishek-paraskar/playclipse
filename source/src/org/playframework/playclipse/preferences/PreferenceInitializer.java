package org.playframework.playclipse.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.playframework.playclipse.PlayPlugin;
import org.playframework.playclipse.editors.ConfEditor;
import org.playframework.playclipse.editors.html.HTMLEditor;
import org.playframework.playclipse.editors.route.RouteEditor;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = PlayPlugin.getDefault().getPreferenceStore();

		store.setDefault(PlayPlugin.PREF_BROWSER, PlayPlugin.PREF_BROWSER_INTERNAL);

		PreferenceConverter.setDefault(store, RouteEditor.ACTION_COLOR, new RGB(200, 0, 0));
		PreferenceConverter.setDefault(store, RouteEditor.KEYWORD_COLOR, new RGB(0, 200, 0));
		PreferenceConverter.setDefault(store, RouteEditor.URL_COLOR, new RGB(0, 0, 200));
		PreferenceConverter.setDefault(store, RouteEditor.COMMENT_COLOR, new RGB(90, 90, 90));
		PreferenceConverter.setDefault(store, RouteEditor.DEFAULT_COLOR, new RGB(0, 0, 0));
		store.setDefault(RouteEditor.MISSING_ROUTE, "error");
		store.setDefault(RouteEditor.SOFT_TABS, false);
		store.setDefault(RouteEditor.SOFT_TABS_WIDTH, 4);

		PreferenceConverter.setDefault(store, ConfEditor.COMMENT_COLOR, new RGB(90, 90, 90));
		PreferenceConverter.setDefault(store, ConfEditor.KEY_COLOR, new RGB(150, 0, 0));
		PreferenceConverter.setDefault(store, ConfEditor.DEFAULT_COLOR, new RGB(0, 0, 0));
		store.setDefault(ConfEditor.SOFT_TABS, false);
		store.setDefault(ConfEditor.SOFT_TABS_WIDTH, 4);

		PreferenceConverter.setDefault(store, HTMLEditor.ACTION_COLOR, new RGB(255, 0, 192));
		PreferenceConverter.setDefault(store, HTMLEditor.DEFAULT_COLOR, new RGB(0, 0, 0));
		PreferenceConverter.setDefault(store, HTMLEditor.DOCTYPE_COLOR, new RGB(127, 127, 127));
		PreferenceConverter.setDefault(store, HTMLEditor.EXPR_COLOR, new RGB(255, 144, 0));
		PreferenceConverter.setDefault(store, HTMLEditor.HTML_COLOR, new RGB(0, 0, 0));
		PreferenceConverter.setDefault(store, HTMLEditor.KEYWORD_COLOR, new RGB(0, 0, 0));
		PreferenceConverter.setDefault(store, HTMLEditor.SKIPPED_COLOR, new RGB(90, 90, 90));
		PreferenceConverter.setDefault(store, HTMLEditor.TAG_COLOR, new RGB(129, 0, 153));
		PreferenceConverter.setDefault(store, HTMLEditor.STRING_COLOR, new RGB(5, 152, 220));
		PreferenceConverter.setDefault(store, HTMLEditor.JAVA_LINE_COLOR, new RGB(33, 33, 220));
		store.setDefault(HTMLEditor.MISSING_ACTION, "error");
		store.setDefault(HTMLEditor.SOFT_TABS, false);
		store.setDefault(HTMLEditor.SOFT_TABS_WIDTH, 4);
	}

}

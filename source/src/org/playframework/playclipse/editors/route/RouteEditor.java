package org.playframework.playclipse.editors.route;

import java.util.regex.Pattern;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.playframework.playclipse.ModelInspector;
import org.playframework.playclipse.PlayPlugin;
import org.playframework.playclipse.editors.PlayEditor;

public class RouteEditor extends PlayEditor {

	private static final String COMMENT = "comment";

	private static final String ACTION2 = "action";

	private static final String URL = "url";

	private static final String KEYWORD = "keyword";

	private static final String DEFAULT = "default";
	
	public static final String KEYWORD_COLOR = "route_keyword_color";
	public static final String URL_COLOR = "route_url_color";
	public static final String COMMENT_COLOR = "route_comment_color";
	public static final String ACTION_COLOR = "route_action_color";
	public static final String DEFAULT_COLOR = "route_default_color";

	/**
	 * Can be: "error", "warning" or "ignore"
	 */
	public static final String MISSING_ROUTE = "route_missing_route";
	public static final String SOFT_TABS = "route_soft_tabs";
	public static final String SOFT_TABS_WIDTH = "route_soft_tabs_width";

	String oldState = DEFAULT;
	IJavaProject javaProject;
	ModelInspector inspector;

	public RouteEditor() {
		super();
		setSourceViewerConfiguration(new RouteConfiguration(this));
		IPreferenceStore store = PlayPlugin.getDefault().getPreferenceStore();
		useSoftTabs = store.getBoolean(SOFT_TABS);
		softTabsWidth = store.getInt(SOFT_TABS_WIDTH);
	}

	@Override
	public String[] getTypes() {
		return new String[] {
				DEFAULT,
				KEYWORD,
				URL,
				ACTION2,
				COMMENT
		};
	}

	@Override
	public String autoClose(char pc, char c, char nc) {
		return null;
	}

	Pattern action = Pattern.compile("\\s(\\w[\\.\\w]+)");

	@Override
	public IHyperlink detectHyperlink(ITextViewer textViewer, IRegion region) {
		BestMatch match = findBestMatch(region.getOffset(), action);
		if(match != null) {
			if(match.is(action)) {
				return match.hyperlink(ACTION2, 1, 0);
			}
		}
		return null;
	}

	@Override
	public String getStylePref(String type) {
		if(type.equals(KEYWORD)) {
			return KEYWORD_COLOR;
		}
		if(type.equals(URL)) {
			return URL_COLOR;
		}
		if(type.equals(COMMENT)) {
			return COMMENT_COLOR;
		}
		if(type.equals(ACTION2)) {
			return ACTION_COLOR;
		}
		return DEFAULT_COLOR;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String key = event.getProperty();
		if (key.equals(SOFT_TABS)) {
			useSoftTabs = ((Boolean)event.getNewValue()).booleanValue();
		}
		super.propertyChange(event);
	}

	@Override
	public String scan() {
		if (isNext("\n")) {
			return found(DEFAULT, 1);
		}
		if (state != COMMENT && isNext("#")) {
			return found(COMMENT, 0);
		}
		if (state == DEFAULT && isNext("GET")) {
			return found(KEYWORD, 0);
		}
		if (state == DEFAULT && isNext("POST")) {
			return found(KEYWORD, 0);
		}
		if (state == DEFAULT && isNext("PUT")) {
			return found(KEYWORD, 0);
		}
		if (state == DEFAULT && isNext("DELETE")) {
			return found(KEYWORD, 0);
		}
		if (state == DEFAULT && isNext("OPTIONS")) {
			return found(KEYWORD, 0);
		}
		if (state == DEFAULT && isNext("HEAD")) {
			return found(KEYWORD, 0);
		}
		if (state == DEFAULT && isNext("*")) {
			return found(KEYWORD, 0);
		}
		if ((state == KEYWORD || state == URL) && nextIsSpace()) {
			oldState = state;
			return found(DEFAULT, 0);
		}
		if (state == DEFAULT && isNext("/")) {
			return found(URL, 0);
		}
		if (state == DEFAULT && oldState == URL && !nextIsSpace()) {
			return found(ACTION2, 0);
		}
		return null;
	}

	@Override
	public void templates(String contentType, String ctx) {
	}

}

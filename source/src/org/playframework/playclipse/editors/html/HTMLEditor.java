package org.playframework.playclipse.editors.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.playframework.playclipse.PlayPlugin;
import org.playframework.playclipse.editors.PlayEditor;

public class HTMLEditor extends PlayEditor {

	private static final String ACTION_IN_TAG2 = "action_in_tag";
	private static final String INCLUDE2 = "include";
	private static final String EXTENDS = "extends";
	private static final String KEYWORD = "keyword";
	private static final String COMMENT = "skipped"; // comment area
	private static final String ACTION2 = "action";
	public static final String IMPORT = "import";
	public static final String IMPORT_STATIC = "import_static";
	private static final String EXPRESSION = "expression";
	private static final String PLAY_TAG = "tag";
	private static final String STRING = "string";
	private static final String HTML = "html";
	private static final String DOCTYPE = "doctype";
	private static final String DEFAULT = "default";
	
	public static final String DEFAULT_COLOR = "html_default_color";
	public static final String DOCTYPE_COLOR = "html_doctype_color";
	public static final String HTML_COLOR = "html_html_color";
	public static final String HTML_TAG_COLOR = "html_tag_color";
	public static final String EXPR_COLOR = "html_expr_color";
	public static final String ACTION_COLOR = "html_action_color";
	public static final String SKIPPED_COLOR = "html_skipped_color";
	public static final String KEYWORD_COLOR = "html_keyword_color";
	public static final String STRING_COLOR = "html_string_color";
	public static final String JAVA_LINE_COLOR = "java_line_color";

	public static final String SOFT_TABS = "html_soft_tabs";
	public static final String SOFT_TABS_WIDTH = "html_soft_tabs_width";

	public static final String MISSING_ACTION = "html_missing_action";

	// japid
	public static final String JAVA_LINE = "japid_java_line";
	public static final String CODE_BLOCK = "japid_code_block";

	private ProjectionSupport projectionSupport;

	public HTMLEditor() {
		super();
		setSourceViewerConfiguration(new HTMLConfiguration(this));
		IPreferenceStore store = PlayPlugin.getDefault().getPreferenceStore();
		useSoftTabs = store.getBoolean(SOFT_TABS);
		softTabsWidth = store.getInt(SOFT_TABS_WIDTH);
	}

	@Override
	public String[] getTypes() {
		return new String[] { DEFAULT, DOCTYPE, HTML, STRING, PLAY_TAG, EXPRESSION, ACTION2, COMMENT, KEYWORD, JAVA_LINE , CODE_BLOCK};
	}

	@Override
	public String getStylePref(String type) {
		if (type.equals(DOCTYPE)) {
			return DOCTYPE_COLOR;
		}
		if (type.equals(HTML)) {
			return HTML_COLOR;
		}
		if (type.equals(STRING)) {
			return STRING_COLOR;
		}
		if (type.equals(PLAY_TAG)) {
			return HTML_TAG_COLOR;
		}
		if (type.equals(EXPRESSION)) {
			return EXPR_COLOR;
		}
		if (type.equals(ACTION2)) {
			return ACTION_COLOR;
		}
		if (type.equals(COMMENT)) {
			return SKIPPED_COLOR;
		}
		if (type.equals(KEYWORD)) {
			return KEYWORD_COLOR;
		}
		if (type.equals(JAVA_LINE)) {
			return JAVA_LINE_COLOR;
		}
		if (type.equals(CODE_BLOCK)) {
			return JAVA_LINE_COLOR;
		}
		return DEFAULT_COLOR;
	}

	// Auto-close

	@Override
	public String autoClose(char pc, char c, char nc) {
//		if (c == '`') {
//			return "`";
//		}
		if (c == '<') {
			return ">";
		}
		if (c == '>' && nc == '>') {
			return SKIP;
		}
		if (c == '{') {
			return "}";
		}
		if (c == '}' && nc == '}') {
			return SKIP;
		}
		if (c == '(') {
			return ")";
		}
		if (c == ')' && nc == ')') {
			return SKIP;
		}
		if (c == '[') {
			return "]";
		}
		if (c == ']' && nc == ']') {
			return SKIP;
		}
		if (c == '\'') {
			if (nc == '\'') {
				return SKIP;
			}
			return "\'";
		}
		if (c == '\"') {
			if (nc == '\"') {
				return SKIP;
			}
			return "\"";
		}
		return null;
	};

	// Template

	@Override
	public void templates(String contentType, String ctx) {
		if (contentType == DEFAULT || contentType == HTML || contentType == STRING) {
			template("$", "Insert dynamic expression", "$${${}}${cursor}");
			template(PLAY_TAG, "Insert tag without body", "#{${name} ${}/}${cursor}");
			template(ACTION2, "Insert action", "@{${}}${cursor}");
			template(PLAY_TAG, "Insert tag with body", "##{${name} ${}}${cursor}#{/${name}}");
		}
		if (contentType == DEFAULT) {
			template("if", "Insert a #if tag", "#{if ${}}\n    ${cursor}\n#{/if}");
			template(EXTENDS, "Insert a #extends tag", "#{extends '${}' /}${cursor}");
			template("list", "Insert a #list tag", "#{list ${}, as:'${i}'}\n    ${cursor}\n#{/list>");
			template(DOCTYPE, "Insert an HTML5 doctype element", "<!DOCTYPE html>");
		}
		// Magic
		Matcher isHtmlTag = Pattern.compile("<([a-zA-Z]+)>").matcher(ctx);
		if (isHtmlTag.matches()) {
			String closeTag = "</" + isHtmlTag.group(1) + ">";
			template(ctx, "Close the " + ctx + " HTML tag", "${cursor}" + closeTag);
		}
	}

	// Hyperlinks: note: the intersting part must the match group 1. see the
	// hyperlink method in Editor

	static Pattern extend_s = Pattern.compile("#\\{extends\\s+'([^']+)'");
	static Pattern extends_japid = Pattern.compile("[^`]?`\\s*extends\\s+'([^']+)'");
	static Pattern extends_japid2 = Pattern.compile("[^`]?`\\s*extends\\s+\"([^\"]+)\"");
	static Pattern extends_japid3 = Pattern.compile("[^`]?`\\s*extends\\s+([^\"\']+)");
	static Pattern import_line = Pattern.compile("[^`]?`\\s*import\\s+([a-zA-Z0-9\\._]+)");
	static Pattern import_static = Pattern.compile("[^`]?`\\s*import\\s+static\\s+([a-zA-Z0-9\\._]+)");
	static Pattern include = Pattern.compile("#\\{include\\s+'([^']+)'");
	static Pattern action_invoke = Pattern.compile("#\\{\\s*invoke\\s+([-a-zA-Z0-9\\._]+)");
	static Pattern action_invoke2 = Pattern.compile("[^`]?`\\s*invoke\\s+([-a-zA-Z0-9\\\\._]+)");
	static Pattern action_invoke3 = Pattern.compile("[^`]?`a\\s+([\\w\\d\\./]+)");
	static Pattern action_invoke4 = Pattern.compile("[^`]?`\\s*action\\s+([\\w\\d\\./]+)");
	static Pattern action = Pattern.compile("@\\{([^}]+)\\}");
	static Pattern action_in_tag = Pattern.compile("#\\{.+(@.+[)])");
	static Pattern playTag = Pattern.compile("#\\{([-a-zA-Z0-9\\./_]+)");
	static Pattern japidTag = Pattern.compile("[^`]*`tag\\s+([\\w\\d\\./]+)");
	static Pattern japidTagShort = Pattern.compile("[^`]*`t\\s+([\\w\\d\\./]+)");

	@Override
	public IHyperlink detectHyperlink(ITextViewer textViewer, IRegion region) {
		BestMatch match = findBestMatch(region.getOffset(),
				include, action_invoke, extend_s, extends_japid, extends_japid2, extends_japid3,
				action, action_in_tag, playTag, action_invoke2, action_invoke3, action_invoke4, import_line, import_static,
				japidTag, japidTagShort
				);
		if (match != null) {
			// System.out.println(match.text());
			if (match.is(action)) {
				return match.hyperlink(ACTION2, 0, 0);
			}
			if (match.is(action_invoke)) {
				return match.hyperlink(ACTION2, match.matcher.start(1) - match.matcher.start(), 0);
			}
			if (match.is(action_invoke2)) {
				return match.hyperlink(ACTION2, match.matcher.start(1) - match.matcher.start(), 0);
			}
			if (match.is(action_invoke3)) {
				return match.hyperlink(ACTION2, match.matcher.start(1) - match.matcher.start(), 0);
			}
			if (match.is(action_invoke4)) {
				return match.hyperlink(ACTION2, match.matcher.start(1) - match.matcher.start(), 0);
			}
			if (match.is(playTag)) {
				if (!match.text().equals("invoke") && !match.text().equals("Each"))
					return match.hyperlink(PLAY_TAG, 2, 0);
			}
			if (match.is(extend_s)) {
				return match.hyperlink(EXTENDS, match.matcher.start(1) - match.matcher.start(), -1);
			}
			if (match.is(extends_japid)) {
				return match.hyperlink(EXTENDS, match.matcher.start(1) - match.matcher.start(), -1);
			}
			if (match.is(extends_japid2)) {
				return match.hyperlink(EXTENDS, match.matcher.start(1) - match.matcher.start(), -1);
			}
			if (match.is(extends_japid3)) {
				return match.hyperlink(EXTENDS, match.matcher.start(1) - match.matcher.start(), 0);
			}
			if (match.is(include)) {
				return match.hyperlink(INCLUDE2, match.matcher.start(1) - match.matcher.start(), -1);
			}
			if (match.is(action_in_tag)) {
				return match.hyperlink(ACTION_IN_TAG2, match.matcher.start(1) - match.matcher.start(), 0);
			}
			if (match.is(import_line)) {
				return match.hyperlink(IMPORT, match.matcher.start(1) - match.matcher.start(), 0);
			}
			if (match.is(import_static)) {
				return match.hyperlink(IMPORT_STATIC, match.matcher.start(1) - match.matcher.start(), 0);
			}
			if (match.is(japidTag)) {
				return match.hyperlink(PLAY_TAG, match.matcher.start(1) - match.matcher.start(), 0);
			}
			if (match.is(japidTagShort)) {
				return match.hyperlink(PLAY_TAG, match.matcher.start(1) - match.matcher.start(), 0);
			}
		}
		return null;
	}

	// Scanner

	boolean consumeString = false;
	char openedString = ' ';
	String oldState = DEFAULT;
	String oldStringState = DEFAULT;
	private int offsetInJavaLine;

	@Override
	protected void reset() {
		super.reset();
		consumeString = false;
		oldState = DEFAULT;
	}

	@Override
	public String scan() {
		if (isNext("*{") && state != COMMENT) {
			saveState();
			return found(COMMENT, 0);
		}
		if (state == COMMENT) {
			if (isNext("}*")) {
				return found(oldState, 2);
			}
		}
		if (state == DEFAULT || state == HTML || state == STRING) {
			if (isNext("#{")) {
				saveState();
				return found(PLAY_TAG, 0);
			}
			if (isNext("%{")) {
				saveState();
				return found(CODE_BLOCK, 0);
			}
			if (isNext("~[")) {
				saveState();
				return found(CODE_BLOCK, 0);
			}
			if (isNext("${")) {
				saveState();
				return found(EXPRESSION, 0);
			}
			if (isNext("@{") || isNext("@@{")) {
				saveState();
				return found(ACTION2, 0);
			}
			if (isNext("``")) {
//				return found(oldState, 2);
				return found(state, 2);
			}
			if (isNext("`") && !isNext("``")) {
				saveState();
				return found(JAVA_LINE, 0);
			}
			// was trying to implement an escape-less }, but it may be too
			// confusing with json, javascript syntax etc.
			// so it's disabled for now.
			// if(isNext("}")){
			// String prevTokenString = content.substring(end2, end - 1);
			// if (allLeadingSpaceInline(prevTokenString)) {
			// saveState();
			// return found(JAVA_LINE, 0);
			// }
			// }
		}

		if (state == JAVA_LINE) {
			offsetInJavaLine++;
			if (isNext("\n")) {
				offsetInJavaLine = 0;
				return found(oldState, 1);
			}
			else if (isNext("`")) {
				if (offsetInJavaLine > 1) {
					offsetInJavaLine = 0;
					return found(oldState, 1);
				}
			}
		}
		if (state == CODE_BLOCK) {
			if (isNext("}%") || isNext("]~")) {
				return found(oldState, 2);
			}
		}
		if (state == PLAY_TAG || state == EXPRESSION || state == ACTION2) {
			if (isNext("}")) {
				return found(oldState, 1);
			}
		}
		if (state == DEFAULT) {
			if (isNext("<!DOCTYPE")) {
				return found(DOCTYPE, 0);
			}
			if (isNext("<")) {
				return found(HTML, 0);
			}
			if (isNext("var ")) {
				return found(KEYWORD, 0);
			}
			if (isNext("def ")) {
				return found(KEYWORD, 0);
			}
			if (isNext("return ")) {
				return found(KEYWORD, 0);
			}
			if (isNext("function(")) {
				return found(KEYWORD, 0);
			}
			if (isNext("function ")) {
				return found(KEYWORD, 0);
			}
			if (isNext("if(")) {
				return found(KEYWORD, 0);
			}
			if (isNext("if ")) {
				return found(KEYWORD, 0);
			}
			if (isNext("else ")) {
				return found(KEYWORD, 0);
			}
			if (isNext("switch(")) {
				return found(KEYWORD, 0);
			}
			if (isNext("switch ")) {
				return found(KEYWORD, 0);
			}
		}
		if (state == KEYWORD) {
			if (isNext(" ") || isNext("(")) {
				return found(DEFAULT, 0);
			}
		}
		if (state == DOCTYPE || state == HTML) {
			if (isNext(">")) {
				return found(DEFAULT, 1);
			}
		}
		if (state == HTML) {
			if (isNext("\"")) {
				openedString = '\"';
				consumeString = false;
				oldStringState = state;
				return found(STRING, 0);
			}
			if (isNext("'")) {
				openedString = '\'';
				consumeString = false;
				oldStringState = state;
				return found(STRING, 0);
			}
		}
		if (state == STRING) {
			if (isNext("" + openedString) && consumeString) {
				return found(oldStringState, 1);
			}
			consumeString = true;
		}
		return null;
	}

	/**
	 * bran:copied from JapidParser
	 * 
	 * @param curToken
	 * @return
	 */
	static boolean allLeadingSpaceInline(String curToken) {
		boolean allLeadingSpace = true;
		int len = curToken.length();
		for (int i = len - 1; i > -1; i--) {
			char ch = curToken.charAt(i);
			if (ch == '\n')
				break;
			else if (ch == ' ' || ch == '\t')
				continue;
			else {
				allLeadingSpace = false;
				break;
			}
		}
		return allLeadingSpace;
	}

	/**
	 * 
	 */
	private void saveState() {
		oldState = state;
	}

	// Folding

	private Annotation[] oldAnnotations;
	private ProjectionAnnotationModel annotationModel;

	public void updateFoldingStructure(ArrayList<Position> positions) {
		Annotation[] annotations = new Annotation[positions.size()];

		// this will hold the new annotations along
		// with their corresponding positions
		HashMap<ProjectionAnnotation, Position> newAnnotations = new HashMap<ProjectionAnnotation, Position>();

		for (int i = 0; i < positions.size(); i++) {
			ProjectionAnnotation annotation = new ProjectionAnnotation();
			newAnnotations.put(annotation, positions.get(i));
			annotations[i] = annotation;
		}

		annotationModel.modifyAnnotations(oldAnnotations, newAnnotations, null);

		oldAnnotations = annotations;
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
		projectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
		projectionSupport.install();
		viewer.doOperation(ProjectionViewer.TOGGLE);
		annotationModel = viewer.getProjectionAnnotationModel();
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		ISourceViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		getSourceViewerDecorationSupport(viewer);
		viewer.getTextWidget().addVerifyListener(this);
		return viewer;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String key = event.getProperty();
		if (key.equals(SOFT_TABS)) {
			useSoftTabs = ((Boolean) event.getNewValue()).booleanValue();
		}
		super.propertyChange(event);
	}

	/**
	 * @return
	 */
	public static Map<String, String> setupHtmlEditorPrefFields() {
		Map<String, String> fields = new LinkedHashMap<String, String>();
		fields.put(DEFAULT_COLOR, "Default Color");
		fields.put(STRING_COLOR, "String Color");
		fields.put(ACTION_COLOR, "Action Color");
		fields.put(DOCTYPE_COLOR, "Doctype Color");
		fields.put(EXPR_COLOR, "Expr Color");
		fields.put(HTML_COLOR, "HTML Color");
		fields.put(KEYWORD_COLOR, "Keyword Color");
		fields.put(SKIPPED_COLOR, "Skipped Color");
		fields.put(HTML_TAG_COLOR, "Tag Color");
		fields.put(JAVA_LINE_COLOR, "Java Code Color");
		return fields;
	}

	/**
	 * @param store
	 */
	public static void initHtmlEditorPrefStore(IPreferenceStore store) {
		PreferenceConverter.setDefault(store, ACTION_COLOR, new RGB(255, 0, 192));
		PreferenceConverter.setDefault(store, DEFAULT_COLOR, new RGB(0, 0, 0));
		PreferenceConverter.setDefault(store, DOCTYPE_COLOR, new RGB(127, 127, 127));
		PreferenceConverter.setDefault(store, EXPR_COLOR, new RGB(255, 144, 0));
		PreferenceConverter.setDefault(store, HTML_COLOR, new RGB(0, 0, 0));
		PreferenceConverter.setDefault(store, KEYWORD_COLOR, new RGB(0, 0, 0));
		PreferenceConverter.setDefault(store, SKIPPED_COLOR, new RGB(90, 90, 90));
		PreferenceConverter.setDefault(store, HTML_TAG_COLOR, new RGB(129, 0, 153));
		PreferenceConverter.setDefault(store, STRING_COLOR, new RGB(5, 152, 220));
		PreferenceConverter.setDefault(store, JAVA_LINE_COLOR, new RGB(33, 33, 180));
		store.setDefault(MISSING_ACTION, "error");
		store.setDefault(SOFT_TABS, false);
		store.setDefault(SOFT_TABS_WIDTH, 4);
	}

}

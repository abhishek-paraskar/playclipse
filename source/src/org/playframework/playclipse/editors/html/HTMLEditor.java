package org.playframework.playclipse.editors.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.preference.IPreferenceStore;
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
import org.eclipse.swt.widgets.Composite;
import org.playframework.playclipse.PlayPlugin;
import org.playframework.playclipse.editors.PlayEditor;

public class HTMLEditor extends PlayEditor {

	private static final String ACTION_IN_TAG2 = "action_in_tag";
	private static final String INCLUDE2 = "include";
	private static final String EXTENDS = "extends";
	private static final String KEYWORD = "keyword";
	private static final String SKIPPED = "skipped";
	private static final String ACTION2 = "action";
	private static final String EXPRESSION = "expression";
	private static final String TAG2 = "tag";
	private static final String STRING = "string";
	private static final String HTML = "html";
	private static final String DOCTYPE = "doctype";
	private static final String DEFAULT = "default";
	public static final String DEFAULT_COLOR = "html_default_color";
	public static final String DOCTYPE_COLOR = "html_doctype_color";
	public static final String HTML_COLOR = "html_html_color";
	public static final String TAG_COLOR = "html_tag_color";
	public static final String EXPR_COLOR = "html_expr_color";
	public static final String ACTION_COLOR = "html_action_color";
	public static final String SKIPPED_COLOR = "html_skipped_color";
	public static final String KEYWORD_COLOR = "html_keyword_color";
	public static final String STRING_COLOR = "html_string_color";

	public static final String SOFT_TABS = "html_soft_tabs";
	public static final String SOFT_TABS_WIDTH = "html_soft_tabs_width";

	public static final String MISSING_ACTION = "html_missing_action";

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
		return new String[] {DEFAULT, DOCTYPE, HTML, STRING, TAG2, EXPRESSION, ACTION2, SKIPPED, KEYWORD};
	}

	@Override
	public String getStylePref(String type) {
		if(type.equals(DOCTYPE)) {
			return DOCTYPE_COLOR;
		}
		if(type.equals(HTML)) {
			return HTML_COLOR;
		}
		if(type.equals(STRING)) {
			return STRING_COLOR;
		}
		if(type.equals(TAG2)) {
			return TAG_COLOR;
		}
		if(type.equals(EXPRESSION)) {
			return EXPR_COLOR;
		}
		if(type.equals(ACTION2)) {
			return ACTION_COLOR;
		}
		if(type.equals(SKIPPED)) {
			return SKIPPED_COLOR;
		}
		if(type.equals(KEYWORD)) {
			return KEYWORD_COLOR;
		}
		return DEFAULT_COLOR;
	}

	// Auto-close

	@Override
	public String autoClose(char pc, char c, char nc) {
		if(c == '<') {
			return ">";
		}
		if(c == '>' && nc == '>') {
			return SKIP;
		}
		if(c == '{') {
			return "}";
		}
		if(c == '}' && nc == '}') {
			return SKIP;
		}
		if(c == '(') {
			return ")";
		}
		if(c == ')' && nc == ')') {
			return SKIP;
		}
		if(c == '[') {
			return "]";
		}
		if(c == ']' && nc == ']') {
			return SKIP;
		}
		if(c == '\'') {
			if(nc == '\'') {
				return SKIP;
			}
			return "\'";
		}
		if(c == '\"') {
			if(nc == '\"') {
				return SKIP;
			}
			return "\"";
		}
		return null;
	};

	// Template

	@Override
	public void templates(String contentType, String ctx) {
		if(contentType == DEFAULT || contentType == HTML || contentType == STRING) {
			template("$", "Insert dynamic expression", "$${${}}${cursor}");
			template(TAG2, "Insert tag without body", "#{${name} ${}/}${cursor}");
			template(ACTION2, "Insert action", "@{${}}${cursor}");
			template(TAG2, "Insert tag with body", "##{${name} ${}}${cursor}#{/${name}}");
		}
		if(contentType == DEFAULT) {
			template("if", "Insert a #if tag", "#{if ${}}\n    ${cursor}\n#{/if}");
			template(EXTENDS, "Insert a #extends tag", "#{extends '${}' /}${cursor}");
			template("list", "Insert a #list tag", "#{list ${}, as:'${i}'}\n    ${cursor}\n#{/list>");
			template(DOCTYPE, "Insert an HTML5 doctype element", "<!DOCTYPE html>");
		}
		// Magic
		Matcher isTag = Pattern.compile("<([a-zA-Z]+)>").matcher(ctx);
		if(isTag.matches()) {
			String closeTag = "</" + isTag.group(1) + ">";
			template(ctx, "Close the " + ctx + " HTML tag", "${cursor}"+closeTag);
		}
	}

	// Hyperlink

	Pattern extend_s = Pattern.compile("#\\{extends\\s+'([^']+)'");
	Pattern extends_japid = Pattern.compile("`\\s*extends\\s+'([^']+)'");
	Pattern extends_japid2 = Pattern.compile("`\\s*extends\\s+\"([^\"]+)\"");
	Pattern include = Pattern.compile("#\\{include\\s+'([^']+)'");
	Pattern action_invoke = Pattern.compile("#\\{\\s*invoke\\s+([-a-zA-Z0-9\\._]+)");
	Pattern action = Pattern.compile("@\\{([^}]+)\\}");
	Pattern action_in_tag = Pattern.compile("#\\{.+(@.+[)])");
	Pattern tag = Pattern.compile("#\\{([-a-zA-Z0-9\\./_]+)");

	@Override
	public IHyperlink detectHyperlink(ITextViewer textViewer, IRegion region) {
		BestMatch match = findBestMatch(region.getOffset(), include, action_invoke, extend_s, extends_japid, extends_japid2, action, action_in_tag, tag);
		if(match != null) {
//			System.out.println(match.text());
			if(match.is(action) ) {
				return match.hyperlink(ACTION2, 0, 0);
			}
			if(match.is(action_invoke) ) {
				return match.hyperlink(ACTION2, match.matcher.start(1) - match.matcher.start(), 0);
			}
			if(match.is(tag)) {
				if (!match.text().equals("invoke") && !match.text().equals("Each"))
					return match.hyperlink(TAG2, 2, 0);
			}
			if(match.is(extend_s)) {
				return match.hyperlink(EXTENDS, match.matcher.start(1) - match.matcher.start(), -1);
			}
			if(match.is(extends_japid)) {
				return match.hyperlink(EXTENDS, match.matcher.start(1) - match.matcher.start(), -1);
			}
			if(match.is(extends_japid2)) {
				return match.hyperlink(EXTENDS, match.matcher.start(1) - match.matcher.start(), -1);
			}
			if(match.is(include)) {
				return match.hyperlink(INCLUDE2, match.matcher.start(1) - match.matcher.start(), -1);
			}
			if(match.is(action_in_tag)) {
				return match.hyperlink(ACTION_IN_TAG2, match.matcher.start(1) - match.matcher.start(), 0);
			}	
		}
		return null;
	}

	// Scanner

	boolean consumeString = false;
	char openedString = ' ';
	String oldState = DEFAULT;
	String oldStringState = DEFAULT;

	@Override
	protected void reset() {
		super.reset();
		consumeString = false;
		oldState = DEFAULT;
	}

	@Override
	public String scan() {
		if(isNext("*{") && state != SKIPPED) {
			oldState = state;
			return found(SKIPPED, 0);
		}
		if(state == SKIPPED) {
			if(isNext("}*")) {
				return found(oldState, 2);
			}
		}
		if(state == DEFAULT || state == HTML || state == STRING) {
			if(isNext("#{")) {
				oldState = state;
				return found(TAG2, 0);
			}
			if(isNext("${")) {
				oldState = state;
				return found(EXPRESSION, 0);
			}
			if(isNext("@{") || isNext("@@{")) {
				oldState = state;
				return found(ACTION2, 0);
			}
		}
		if(state == TAG2 || state == EXPRESSION || state == ACTION2) {
			if(isNext("}")) {
				return found(oldState, 1);
			}
		}
		if(state == DEFAULT) {
			if(isNext("<!DOCTYPE")) {
				return found(DOCTYPE, 0);
			}
			if(isNext("<")) {
				return found(HTML, 0);
			}
			if(isNext("var ")) {
				return found(KEYWORD, 0);
			}
			if(isNext("def ")) {
				return found(KEYWORD, 0);
			}
			if(isNext("return ")) {
				return found(KEYWORD, 0);
			}
			if(isNext("function(")) {
				return found(KEYWORD, 0);
			}
			if(isNext("function ")) {
				return found(KEYWORD, 0);
			}
			if(isNext("if(")) {
				return found(KEYWORD, 0);
			}
			if(isNext("if ")) {
				return found(KEYWORD, 0);
			}
			if(isNext("else ")) {
				return found(KEYWORD, 0);
			}
			if(isNext("switch(")) {
				return found(KEYWORD, 0);
			}
			if(isNext("switch ")) {
				return found(KEYWORD, 0);
			}
		}
		if(state == KEYWORD) {
			if(isNext(" ") || isNext("(")) {
				return found(DEFAULT, 0);
			}
		}
		if(state == DOCTYPE || state == HTML) {
			if(isNext(">")) {
				return found(DEFAULT, 1);
			}
		}
		if(state == HTML) {
			if(isNext("\"")) {
				openedString = '\"';
				consumeString = false;
				oldStringState = state;
				return found(STRING, 0);
			}
			if(isNext("'")) {
				openedString = '\'';
				consumeString = false;
				oldStringState = state;
				return found(STRING, 0);
			}
		}
		if(state == STRING) {
			if(isNext(""+openedString) && consumeString) {
				return found(oldStringState, 1);
			}
			consumeString = true;
		}
		return null;
	}

	// Folding

	private Annotation[] oldAnnotations;
	private ProjectionAnnotationModel annotationModel;

	public void updateFoldingStructure(ArrayList<Position> positions) {
		Annotation[] annotations = new Annotation[positions.size()];

		//this will hold the new annotations along
		//with their corresponding positions
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
		ProjectionViewer viewer = (ProjectionViewer)getSourceViewer();
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
			useSoftTabs = ((Boolean)event.getNewValue()).booleanValue();
		}
		super.propertyChange(event);
	}

}

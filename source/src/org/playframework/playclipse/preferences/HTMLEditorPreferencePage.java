package org.playframework.playclipse.preferences;

import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.playframework.playclipse.editors.html.HTMLEditor;

public class HTMLEditorPreferencePage extends PlayEditorPreferencePage {

	public HTMLEditorPreferencePage() {
		super();
		setDescription("Play HTML Template Editor");
	}

	@Override
	public Map<String, String> getColorFields() {
		return HTMLEditor.setupHtmlEditorPrefFields();
	}

	@Override
	public void createFieldEditors() {
		super.createFieldEditors();
		String[][] missingRouteKeyValues = {
				{"Ignore", "ignore"},
				{"Warning", "warning"},
				{"Error", "error"}
		};
		addField(new ComboFieldEditor(HTMLEditor.MISSING_ACTION, "When an action is missing", missingRouteKeyValues, getFieldEditorParent()));
		addField(new BooleanFieldEditor(HTMLEditor.SOFT_TABS, "Indent with spaces (soft tabs)", getFieldEditorParent()));
		addField(new IntegerFieldEditor(HTMLEditor.SOFT_TABS_WIDTH, "Soft tabs length", getFieldEditorParent()));
	}

}

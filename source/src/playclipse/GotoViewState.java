package playclipse;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;

public class GotoViewState extends AbstractSourceProvider {
	public static final String STATE = "Playclipse.isEditingView";
	public final static String ENABLED = "ENABLED";
	public final static String DISENABLED = "DISENABLED";
	boolean enabled = true;
	@Override
	public void dispose() {
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map getCurrentState() {
		System.out.println(";;; get state");
		Map map = new HashMap(1);
		String value = enabled ? ENABLED : DISENABLED;
		map.put(STATE, value);
		return map;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] {STATE};
	}

}

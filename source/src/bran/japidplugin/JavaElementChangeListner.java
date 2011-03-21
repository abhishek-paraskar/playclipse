package bran.japidplugin;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;

public class JavaElementChangeListner implements IElementChangedListener {

	@Override
	public void elementChanged(ElementChangedEvent event) {
		boolean res = hasTypeAddedOrRemoved(event.getDelta());
		
	}

	private boolean hasTypeAddedOrRemoved(IJavaElementDelta delta) {
		IJavaElement elem = delta.getElement();
		int kind = delta.getKind();
		boolean changed;
		boolean added;
		boolean removed;
		if (kind == IJavaElementDelta.CHANGED)
			changed = true;
		else if (kind == IJavaElementDelta.ADDED)
			added = true;
		else if (kind == IJavaElementDelta.REMOVED)
			removed = true;
		
		boolean isAddedOrRemoved = (kind != IJavaElementDelta.CHANGED);
		
		switch(elem.getElementType()) {
		case IJavaElement.JAVA_MODEL:
		case IJavaElement.JAVA_PROJECT:
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
		case IJavaElement.PACKAGE_FRAGMENT:
				if (isAddedOrRemoved)
					return true;
				return processChildrenDelta(delta.getAffectedChildren());
		case IJavaElement.COMPILATION_UNIT:
			ICompilationUnit cu = (ICompilationUnit) elem;
			if (cu.getPrimary().equals(cu)) {
				System.out.println("code change:" + cu.getElementName());
				if (isAddedOrRemoved || isPossibleStructruralChange(delta.getFlags())) {
					return true;
				}
			}
			return processChildrenDelta(delta.getAffectedChildren());
		case IJavaElement.TYPE:
			if (isAddedOrRemoved)
				return true;
			return processChildrenDelta(delta.getAffectedChildren());
			default:
				return false;
		}
	}

	private boolean isPossibleStructruralChange(int flags) {
		return (flags & IJavaElementDelta.F_CONTENT)  != 0 && (flags & IJavaElementDelta.F_FINE_GRAINED) == 0;
	}

	private boolean hasSet(int fContent) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean processChildrenDelta(IJavaElementDelta[] children) {
		for(int i = 0; i < children.length; i++) {
			if (hasTypeAddedOrRemoved(children[i]))
				return true;
		}
		return false;
	} 
}
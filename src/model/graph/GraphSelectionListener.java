package model.graph;

import java.util.EventListener;

public interface GraphSelectionListener extends EventListener {
	
	/**
	 * Fired when a <code>GraphSelectionModel</code> changes
	 * @param gsEvent		- the <code>GraphSelectionEvent</code> being passed
	 */
	void valueChanged (GraphSelectionEvent gsEvent);
}




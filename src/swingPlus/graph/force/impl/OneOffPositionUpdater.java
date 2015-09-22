package swingPlus.graph.force.impl;


import org.apache.log4j.Logger;

import swingPlus.graph.JGraph;
import swingPlus.graph.force.PositionUpdater;

public class OneOffPositionUpdater implements PositionUpdater {

	private final static Logger LOGGER = Logger.getLogger (OneOffPositionUpdater.class);
	double lastTension = Double.MAX_VALUE;
	double totalTension = 0.0;
	
	@Override
	public boolean haltLayout (final JGraph graph) {
		return true;
	}

	
	/*
	 * Once forces are calculated then use them to move objects
	 * Returns total force squared (strain squared) on object placement 
	 */
	@Override
	public void updatePositions (final JGraph graph) {
    	// EMPTY
	}
}

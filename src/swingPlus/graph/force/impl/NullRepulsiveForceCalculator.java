package swingPlus.graph.force.impl;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;

import swingPlus.graph.JGraph;
import swingPlus.graph.force.RepulsiveForceCalculationInterface;
import ui.GraphUI;

public class NullRepulsiveForceCalculator implements
		RepulsiveForceCalculationInterface {

	@Override
	public void calculateRepulsiveForces (final JGraph graph) {
		// EMPTY
	}
	
	@Override
	public void cleanup () {
		// EMPTY
	}

	@Override
	public Object getNearestTo (final JGraph graph, final Point point) {
		return null;
	}

	@Override
	public void reveal (final Graphics graphics, final GraphUI gui, final Point2D.Double location) {
		// EMPTY
	}

}

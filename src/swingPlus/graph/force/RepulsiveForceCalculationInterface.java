package swingPlus.graph.force;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;

import swingPlus.graph.JGraph;
import ui.GraphUI;

public interface RepulsiveForceCalculationInterface {
	
	public void calculateRepulsiveForces (final JGraph graph);
	
	public void cleanup ();
	
	public Object getNearestTo (final JGraph graph, final Point point);
	
	/**
	 * 
	 * @param graphics
	 * @param gui
	 * @param location - position used to show which quadtrees are within d / s < theta of that position
	 */
	public void reveal (final Graphics graphics, final GraphUI gui, final Point2D.Double location);
}

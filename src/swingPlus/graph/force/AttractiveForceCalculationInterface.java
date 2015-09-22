package swingPlus.graph.force;

import java.awt.Point;

import swingPlus.graph.JGraph;

public interface AttractiveForceCalculationInterface {
	
	public void calculateAttractiveForces (JGraph graph);
	
	public void cleanup ();
	
	public Object getNearestTo (JGraph graph, Point p);
}

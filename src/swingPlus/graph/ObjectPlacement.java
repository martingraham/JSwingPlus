package swingPlus.graph;

import java.awt.geom.Point2D;

public interface ObjectPlacement {

	public Point2D.Double getLocation ();
	
	public void setLocation (Point2D.Double location);
	
	public void incForce (double deltafx, double deltafy);
	
	public void incForceX (double deltafx);
	
	public void incForceY (double deltafy);
	
	public double getForceX ();
	
	public double getForceY ();
	
	public double getVelocityX ();
	
	public double getVelocityY ();
	
	public void putForce ();
	
	public void setForceInactive (boolean fInactive);
	
	public boolean isForceInactive ();
}

package swingPlus.graph;


import java.awt.geom.Point2D;



public class BasicObjectPlacement implements ObjectPlacement {
	
	private final static double ANG_THRESHOLD_F = Math.cos (45.0 * Math.PI / 180);// * Math.cos (45.0 * Math.PI / 180);
	private final static double ANG_THRESHOLD_B = Math.cos (30.0 * Math.PI / 180);// * Math.cos (45.0 * Math.PI / 180);

	
	Point2D.Double point;
	double fx, fy, oldfux, oldfuy, useful;
	double velocityX, velocityY;
	boolean fInactive;

	public BasicObjectPlacement () {
		this (0, 0);
	}
	
	public BasicObjectPlacement (final double x, final double y) {
		point = new Point2D.Double (x, y);
	}

	public final double getX() {
		return point.getX();
	}

	public final double getY() {
		return point.getY();
	}

	public final void setX (final double x) {
		point.x = x;
	}

	public final void setY (final double y) {
		point.y = y;
	}
	
	public Point2D.Double getLocation () { return point; }
	
	public void setLocation (final Point2D.Double location) {
		point.x = location.x;
		point.y = location.y;
	}
	
	@Override
	public void incForce (final double deltafx, final double deltafy) {
		fx += deltafx;
		fy += deltafy;
	}
	
	
	@Override
	public void incForceX (final double deltafx) {
		fx += deltafx;
	}
	
	@Override
	public void incForceY (final double deltafy) {
		fy += deltafy;
	}
	
	@Override
	public double getVelocityX () { return velocityX; }
	
	@Override
	public double getVelocityY () { return velocityY; }


	
	void calcUsefulMove () {

		final double force = Math.sqrt ((fx * fx) + (fy * fy));
		final double fux = fx / force;
		final double fuy = fy / force;

		final double dotProduct = (fux * oldfux) + (fuy * oldfuy);
		if (dotProduct > ANG_THRESHOLD_F && useful <= 2.5) {
			useful += 0.1;
		}
		else if (-dotProduct > ANG_THRESHOLD_B && useful >= 0.0) {
			//useful -= 0.1;
			useful /= 2.0;
		}

		oldfux = fux;
		oldfuy = fuy;
	}
	

	@Override
	public double getForceX() {
		return fx;
	}

	@Override
	public double getForceY() {
		return fy;
	}
	
	@Override
	public void putForce () {

		if (isForceInactive ()) {
			velocityX = 0.0;
			velocityY = 0.0;
		} else {
			calcUsefulMove();
			final double mult = 10.0 * useful;
			velocityX = (Math.min (Math.max (fx, -1.0), 1.0) * mult);
			velocityY = (Math.min (Math.max (fy, -1.0), 1.0) * mult);
			setX (getX() + velocityX);
			setY (getY() + velocityY);
		}
		fx = 0.0;
		fy = 0.0;
	}
	
	
	public void setForceInactive (final boolean fInactive) {
		this.fInactive = fInactive;
	}
	
	public boolean isForceInactive () { return fInactive; }
}

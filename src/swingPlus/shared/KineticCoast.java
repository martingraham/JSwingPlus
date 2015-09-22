package swingPlus.shared;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.Timer;

import org.apache.log4j.Logger;


public class KineticCoast extends Timer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3950841872992144530L;
	private static final Logger LOGGER = Logger.getLogger (KineticCoast.class);
	
	double friction, deltadx, deltady, pointScale;
	boolean multFriction;
	Point movingPoint;
	JComponent comp;
	
	// These variables are used to make sure fractional pixel changes in deltax/deltay add up
	// and are added to the starting point of the 'drift', as just adding the deltas
	// to offsetPoint would round to the nearest pixel each time
	double totalDriftX, totalDriftY;
	Point startingPoint = new Point ();
	
	
	public KineticCoast (final int period, final double friction, final boolean multFriction) {	
		super (period, null);
		
		this.friction = friction;	
		this.multFriction = multFriction;
		addActionListener (new MoveListener ());
		this.setRepeats (true);
	}
	
	
	public void setFriction (final double friction) {
		this.friction = friction;
	}
	
	public void startCoast (final double deltadx, final double deltady, final long deltat, 
			final Point movingPoint, final double pointScale, final JComponent coastComp) {
		
		final double tMultiplier = (double)getDelay() / Math.max (5, deltat);
		this.deltadx = deltadx * tMultiplier;
		this.deltady = deltady * tMultiplier;
		totalDriftX = 0.0;
		totalDriftY = 0.0;
		this.pointScale = pointScale;
		startingPoint.setLocation (movingPoint);
		this.movingPoint = movingPoint;
		comp = coastComp;
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug ("mouse moves dx: "+deltadx+", dy: "+deltady+" in "+deltat+" ms."
					+" = moving dx: "+this.deltadx+", dy: "+this.deltady+" in "+getDelay()+" ms.");
		}
		
		start ();
	}
	
	/**
	 * Extend this class and replace this method if you want to do something apart from repaint
	 * i.e. maybe component should be moved via setLocation (movingPoint) etc
	 */
	public void moveComponent () {
		comp.repaint();
	}
	
	
	class MoveListener implements ActionListener {

		@Override
		public void actionPerformed (final ActionEvent aEvent) {

			// Friction is Fr = pN (p = co-efficient of friction)
			// It appears velocity reduces by a fixed amount each iteration
			// rather than just scaling down as I first thought

			// want to slow down by an amount determined by the 'friction' variable every second
			// so we need to take into account how often this timer function is called
			
			final Timer timer = (Timer)aEvent.getSource();
			final double callsPerSecond = 1000.0 / timer.getDelay();
			//LOGGER.info ("coasting...");
			
			if (multFriction) {
				final double frictionMult = Math.exp (Math.log (friction) / callsPerSecond);
				deltadx *= frictionMult;
				deltady *= frictionMult;
			} else {
				final double dxratio = Math.abs (deltadx) / (Math.abs (deltadx) + Math.abs (deltady));
				final double dyratio = 1.0 - dxratio;
				
				final double angle = Math.atan2 (dyratio, dxratio);
				double ddx = Math.cos (angle);
				double ddy = Math.sin (angle);
				
				final double frictionMult = friction / callsPerSecond;
				
				ddx *= -Math.signum (deltadx) * frictionMult;
				ddy *= -Math.signum (deltady) * frictionMult;
				
				deltadx += ddx;
				deltady += ddy;
				
				//System.err.println ("ddx: "+ddx+", ddy: "+ddy+", deltadx: "+deltadx+", deltady: "+deltady);
			}
			
			totalDriftX += (deltadx / pointScale);
			totalDriftY += (deltady / pointScale);
			
			if (((deltadx * deltadx) + (deltady * deltady)) < 0.5) {
				KineticCoast.this.stop();
			}
			
			movingPoint.setLocation (startingPoint.getX() + totalDriftX,
					startingPoint.getY() + totalDriftY);
			
			((KineticCoast)timer).moveComponent();
		}
	}
}
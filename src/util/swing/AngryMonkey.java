package util.swing;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import util.XMLConstants2;



public class AngryMonkey extends Thread implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7738823114203687130L;
	
	final static Logger LOGGER = Logger.getLogger (AngryMonkey.class);
	
	private static final boolean SERIALIZE = false;
	private static final boolean OUTPUT = true;
	
	private int operationTotal = 0, speed = 0;
	private transient Frame frame;
	private List<Operation> operations = new ArrayList<Operation> ();
	private static boolean running = false;
	
	public AngryMonkey (final int ops, final int speed, final Frame frame) {
		super ();
		operationTotal = ops;
		this.speed = speed;
		this.frame = frame;
	}
			
	@Override
	public void run() {			
		try {
			final Robot robot = new Robot ();
			
			final Point point = new Point (0, 0);
			synchronized (this) {
		        for (int n = 1; n < operationTotal; n++) {
		        	SwingUtilities.convertPointToScreen (point, frame);
		        	point.x += 4 + (Math.random() * (frame.getSize().width - 8));
		        	point.y += 32 + (Math.random() * (frame.getSize().height - 36));
		        	robot.mouseMove (point.x, point.y);
		        	//this.wait (3000);
		        	robot.delay (speed);
		        	int button = 0;
		        	button |= (Math.random () > 0.5) ? InputEvent.BUTTON1_MASK : 0;
		        	button |= (Math.random () > 0.8) ? InputEvent.BUTTON2_MASK : 0;
		        	button |= (Math.random () > 0.95) ? InputEvent.BUTTON3_MASK : 0;
		        	
		        	SwingUtilities.convertPointFromScreen (point, frame);
		        	final Component comp = SwingUtilities.getDeepestComponentAt (frame, point.x, point.y);
		        	final Operation oper = new Operation (n, point.x, point.y, speed, button, comp);
		        	operations.add (oper);
		        	robot.mousePress (button);
		        	robot.mouseRelease (button);
		        	LOGGER.info (oper.toString());
		        	point.setLocation (0, 0);
		        }
			}

		} catch (final AWTException e1) {
			LOGGER.error (e1.toString(), e1);
		} /*catch (InterruptedException e2) {
			LOGGER.error (e2.toString(), e2);
		} */
		
		if (SERIALIZE) {
			try {
				final ObjectOutput out = new ObjectOutputStream (new FileOutputStream ("filename.ser"));
		        out.writeObject (this);
		        out.close();
			} catch (final IOException ioe) {
				LOGGER.error (ioe.toString(), ioe);
			}
		}
		
		if (OUTPUT) {
			try {
				final PrintWriter pWriter = new PrintWriter ("AngryMonkeyFeed.txt", XMLConstants2.UTF8);
				for (final Operation o : operations) {
					pWriter.println (o.toString());
				}
				pWriter.close ();
			} catch (final IOException ioe) {
				LOGGER.error (ioe.toString(), ioe);
			}
		}

	}
	
	static class Operation implements Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final int count, x, y, pause, mouseModifiers;
		private final String compString;
		
		protected Operation (final int count, final int x, final int y, final int pause, final int mouseModifiers, final Component over) {
			this.count = count;
			this.x = x;
			this.y = y;
			this.pause = pause;
			this.mouseModifiers = mouseModifiers;
			compString = over.toString ();
		}
		
		@Override
		public String toString () {
			return "Op "+count+" = x: "+x+", y: "+y+", pause: "+pause+", mod: "+mouseModifiers+", comp: "+compString;
		}
	}
}

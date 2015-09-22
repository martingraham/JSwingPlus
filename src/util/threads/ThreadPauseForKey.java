package util.threads;

import java.awt.Dialog;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;


/**
 * Class that pauses a non-swing thread until a key is pressed
 * Used mainly to buy time when setting up visualvm for monitoring
 * @author cs22
 *
 */

public class ThreadPauseForKey {

	static final Logger LOGGER = Logger.getLogger (ThreadPauseForKey.class);
	
	
	Object lockObj = new Object ();

	
	public void pauseForKeypress () {
		
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater (
				new Runnable () {
					@Override
					public void run() {
						final JDialog jDialog = new JDialog ((Dialog)null, true);		
						jDialog.setTitle ("Press A Key to continue");
						
						jDialog.addKeyListener(
							new KeyAdapter () {
								@Override
								public void keyPressed (final KeyEvent kEvent) {
									synchronized (lockObj) {
										lockObj.notifyAll();
									}
									jDialog.setVisible (false);
									jDialog.dispose ();
								}	
							}
						);
						
						jDialog.setSize (240, 92);
						jDialog.setVisible (true);
					}	
				}
			);
			
			try {
				synchronized (lockObj) {
					lockObj.wait ();
				}
				//Thread.currentThread().wait();
			} catch (final InterruptedException excep) {
				LOGGER.error ("Error", excep);
			}
		}
	}
}

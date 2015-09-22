package util.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.border.Border;


/**
 * Class that makes a modal JDialog with an indeterminate JProgressBar
 * to block user input to a window/frame/applet.
 * If it's a JFrame, the class adds a tint to the frame's glasspane to
 * indicate it is currently unresponsive to input.
 * @author cs22
 *
 */
public class BlockingDialog extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6702702972124059428L;
	static final Color DEFAULTBLOCKTINT = new Color (128, 0, 0, 64);
	 
	protected final JProgressBar jpb; 
    private final Border progBorder = BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder (Color.black, 3), 
			BorderFactory.createEmptyBorder (0, 3, 0, 3)
	);
    ActionListener centreDialogListener;
    PropertyChangeListener disposeDialogListener;
   
	
    public BlockingDialog (final Frame frame) {
    	this (frame, DEFAULTBLOCKTINT);
    }
    
    public BlockingDialog (final Frame frame, final Color blockTint) {
    	super (frame, true);
		jpb = new JProgressBar ();
		jpb.setIndeterminate (true);
		jpb.setStringPainted (true);
		jpb.setString ("please wait");
		jpb.setBorder (progBorder);
		add (jpb);
		setUndecorated (true);
		setLocation (-50, -50);
		
		centreDialogListener = new CentreDialogActionListener ();
		disposeDialogListener = new DisposeDialogListener ();
		
		if (frame instanceof JFrame) {
			final JFrame jFrame = (JFrame)frame;
			final JPanel glassPane = (JPanel)jFrame.getGlassPane();
			glassPane.setVisible (true);
			glassPane.setLayout (new BorderLayout());
			final JPanel tintPanel = new JPanel ();
			tintPanel.setBackground (blockTint);
			glassPane.add (tintPanel, "Center");
		}
    }
    
    
    public void setProgressText (final String text) {
    	jpb.setString (text);
    }
    
    public ActionListener getCentreDialogActionListener () {
    	return centreDialogListener;
    }
    
    public PropertyChangeListener getDisposeDialogListener () {
    	return disposeDialogListener;
    }
    
    
    /**
     * Centres the blocking dialog over the window it's blocking
     * @author cs22
     *
     */
    class CentreDialogActionListener implements ActionListener  {
		@Override
		public void actionPerformed (final ActionEvent aEvent) {
			final BlockingDialog blocker = BlockingDialog.this;
			if (blocker.isVisible()) {
				blocker.pack ();
				final int x = (blocker.getOwner().getWidth() - blocker.getWidth()) / 2;
				final int y = (blocker.getOwner().getHeight() - blocker.getHeight()) / 2;
				blocker.setLocation (new Point (x, y));
			}
		}
	};
	
	
	/**
	 * Disposes of the blocking dialog once a SwingWorker (which has added this PropertyListener) 
	 * fires a DONE status
	 * @author cs22
	 *
	 */
    class DisposeDialogListener implements PropertyChangeListener  {
		public void propertyChange (final PropertyChangeEvent event) {
            if ("state".equals(event.getPropertyName())
                    && SwingWorker.StateValue.DONE == event.getNewValue()) {
            	final BlockingDialog blocker = BlockingDialog.this;
            	
            	if (blocker.getOwner() instanceof JFrame) {
            		final JFrame jFrame = (JFrame)blocker.getOwner();
        			final JPanel glassPane = (JPanel)jFrame.getGlassPane();
        			glassPane.removeAll();
        			glassPane.setVisible (false);
            	}
            	blocker.setLocation (-50, -50);
            	blocker.setVisible (false);
            	blocker.dispose ();
            	
            	
            }
        }
	};
}
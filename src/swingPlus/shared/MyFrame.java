package swingPlus.shared;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JToolBar;

import util.screenshot.JScreenshotBar;

public class MyFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3302002692599202389L;

	public MyFrame () {
		super ();
		myFrameInit ();
	}
	
    public MyFrame (final GraphicsConfiguration gConfig) {
        super (gConfig);
        myFrameInit();
    }

    public MyFrame (final String title) {
        super (title);
        myFrameInit();
    }
    
    public MyFrame (final String title, final GraphicsConfiguration gConfig) {
        super (title, gConfig);
        myFrameInit();
    }
    
    final void myFrameInit () {
		final JToolBar jtb = new JScreenshotBar ();
		this.getContentPane().setLayout (new BorderLayout ());
		this.getContentPane().add (jtb, BorderLayout.NORTH);
		this.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
		this.addWindowListener (new MyFrameWindowAdapter ());
    }
    
    class MyFrameWindowAdapter extends WindowAdapter {
    	@Override
		public void windowClosing (final WindowEvent event) {
    		(MyFrame.this).setVisible (false);
    		(MyFrame.this).dispose ();
    	}
    }
}

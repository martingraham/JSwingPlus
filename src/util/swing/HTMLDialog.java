package util.swing;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import util.Messages;
import util.ui.DragLabelUI;


public class HTMLDialog extends MyDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7632355535989164381L;
	final static Logger LOGGER = Logger.getLogger (HTMLDialog.class);
	
	protected HTMLScrollPane jsp;
	private Point location;

	public HTMLDialog (final Frame frame, final boolean modal, final boolean draggable, final URL htmlURL, final URL cssURL, 
			final String title, final Icon icon, final Point location, final int width) {
		super (frame, modal);
		
		this.location = location;
		
		final JPanel expandPanel = new JPanel (new GridLayout (1, 1));
	    final JLabel jtitle = new JLabel (title, icon, SwingConstants.LEFT);
	    jtitle.setUI (DragLabelUI.getInstance());
	    jtitle.setBorder (BorderFactory.createEmptyBorder (0, 0, 3, 0));
	    jtitle.setHorizontalAlignment (SwingConstants.CENTER);
	    expandPanel.add (jtitle);
	    getUserPanel().add (expandPanel);
	    
		final DragWindowAdapter dwa = new DragWindowAdapter (this);
        jtitle.addMouseListener(dwa);
        jtitle.addMouseMotionListener(dwa);
        
		this.closeButton.setText (Messages.getString (this.getClass(), "closeButtonText"));
	      
		jsp = new HTMLScrollPane ();
		
		getUserPanel().add (jsp);
		this.addComponentListener (new MyAdapter());
		
		/*
        final StyleSheet styleSheet = ((HTMLDocument)jep.getDocument()).getStyleSheet();
        if (cssURL != null) {
	        try {
	        	final InputStream is = DataPrep.getInstance().getInputStream (cssURL);
	            final BufferedReader br = DataPrep.getInstance().getBufferedReader (is);
	            styleSheet.loadRules (br, cssURL);
	            br.close ();
	            System.err.println (styleSheet.toString());
	        }
	        catch (final IOException iox) {
	            logger.debug ("error in reading css file: "+iox.toString());
			}
        }
        */
		jsp.setPreferredSize (new Dimension (width, 240));     
	
		
		final PropertyChangeListener pcl = new PropertyChangeListener () {
            public void propertyChange(final PropertyChangeEvent pcEvent) {
            	final String name = pcEvent.getPropertyName();
        	    LOGGER.debug (name+ " : " + pcEvent.getNewValue());
        	    
            	if ("page".equals (name)) {
            		jsp.getEditorPane().setCaretPosition (0);
            		makeVisible (location);
            	}
            }
    	};
        jsp.getEditorPane().addPropertyChangeListener (pcl);
        
   
		jsp.setHTMLURL (htmlURL);
	}
	
	
	public JEditorPane getHTMLPane () {
		return jsp.getEditorPane();
	}
	
	
	class MyAdapter extends ComponentAdapter {
		
		 /**
	     * Invoked when the component has been made visible.
	     */
		@Override
	    public void componentShown (final ComponentEvent cEvent) {
			final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    		final int dialogHeight = HTMLDialog.this.getHeight();
    		final int scrollPaneHeight = jsp.getHeight();
    		final int diff = dialogHeight - scrollPaneHeight;
    		if (dialogHeight > dim.height - 64) {
    			jsp.setSize (new Dimension (jsp.getWidth(), dim.height - 64 - diff));
    			HTMLDialog.this.setSize (new Dimension (HTMLDialog.this.getWidth(), dim.height - 64));
    		}
		}
	}
}

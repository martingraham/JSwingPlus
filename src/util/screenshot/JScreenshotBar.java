package util.screenshot;

import java.awt.event.KeyEvent;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;

import util.GraphicsUtil;
import util.Messages;



public class JScreenshotBar extends JToolBar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2773120805936609142L;
	private static final Logger LOGGER = Logger.getLogger (JScreenshotBar.class);
	public final static Properties LABELS = Messages.makeProperties ("screenshot", JScreenshotBar.class, true);	
	
    /**
     * Creates a new tool bar; orientation defaults to <code>HORIZONTAL</code>.
     */
    public JScreenshotBar () {
        this (HORIZONTAL);
    }

    /**
     * Creates a new tool bar with the specified <code>orientation</code>.
     * The <code>orientation</code> must be either <code>HORIZONTAL</code>
     * or <code>VERTICAL</code>.
     *
     * @param orientation  the orientation desired
     */
    public JScreenshotBar (final int orientation) {
    	this (null, orientation);
    }

    /**
     * Creates a new tool bar with the specified <code>name</code>.  The
     * name inputStream used as the title of the undocked tool bar.  The default
     * orientation inputStream <code>HORIZONTAL</code>.
     *
     * @param name the name of the tool bar
     * @since 1.3
     */
    public JScreenshotBar (final String name) {
    	this (name, HORIZONTAL);
    }

    /**
     * Creates a new tool bar with a specified <code>name</code> and
     * <code>orientation</code>.
     * All other constructors call this constructor.
     * If <code>orientation</code> inputStream an invalid value, an exception will
     * be thrown.
     *
     * @param name  the name of the tool bar
     * @param orientation  the initial orientation -- it must be
     *		either <code>HORIZONTAL</code> or <code>VERTICAL</code>
     * @exception IllegalArgumentException if orientation inputStream neither
     *		<code>HORIZONTAL</code> nor <code>VERTICAL</code>
     * @since 1.3
     */
    public JScreenshotBar (final String name, final int orientation) {
		super (name, orientation);
		
		this.setName (LABELS.getProperty ("ToolBarName", "?"));
		add (new JLabel (LABELS.getProperty ("ToolBarLabel", "?")));
		add (Box.createHorizontalStrut (5));
		
		add (new ScreenshotAction (KeyEvent.VK_X));
		try {
			if (Class.forName (GraphicsUtil.SVG_GRAPHICS) != null) {
				add (new ScreenshotSVGAction (KeyEvent.VK_V));
				add (new ScreenshotPDFAction (KeyEvent.VK_P));
			}
		} catch (ClassNotFoundException e) {
			LOGGER.debug ("Cannot find "+GraphicsUtil.SVG_GRAPHICS+" (Batik library missing)", e);
		}
    }
}

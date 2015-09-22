package util.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.border.Border;

import org.apache.log4j.Logger;

import util.GraphicsUtil;
import util.Messages;

/**
* Class that maintains and draws a mousetip object.<P>
* @author Martin Graham
* @version 1.1
*/

public class MouseTip extends JLabel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6170047306973720889L;
	
	final static Logger LOGGER = Logger.getLogger (MouseTip.class);
	
	private static final int MOUSEINFOXOFFSET = 24, MOUSEINFOYOFFSET = 12;
    private MouseTipBaseRenderer defaultRenderer, currentRenderer;
    private final Map<Class<?>, MouseTipBaseRenderer> rendererTable;
    private final Dimension noParentDims = new Dimension (120, 60);
    private Object lastObject;
    Timer enterTimer, insideTimer;

	private final static Border DEFAULTBORDER = 
		BorderFactory.createCompoundBorder (
			BorderFactory.createLineBorder (Color.decode (Messages.getString (GraphicsUtil.GRAPHICPROPS, "mouseTextColour")), 1),
			BorderFactory.createEmptyBorder (1, 1, 1, 1)
		);
	/**
	* Constructor.<P>
	* @param tipFontMain font for displaying main line of mousetip text
	* @param tipFontExtra font for diplaying extra lines of mousetip info
	* @param foreground text and border colour
	* @param background mousetip background colour
	*/

	public MouseTip () {

    	super ();
        setDoubleBuffered (true);
        setOpaque (false);

		rendererTable = new HashMap<Class<?>, MouseTipBaseRenderer> ();
		addDefaultRenderer (String.class, new MouseTipBaseRenderer ());
		currentRenderer = defaultRenderer;

		setBorder (DEFAULTBORDER);
		
		final ActionListener timerAction = new ActionListener () {
			@Override
			public void actionPerformed (final ActionEvent aEvent) {
				LOGGER.debug ("TIMER ACTION: "+aEvent.getSource());
				final Timer timer = (Timer)aEvent.getSource ();
				MouseTip.this.setVisible (timer == enterTimer ? true : (timer == insideTimer ? false : MouseTip.this.isVisible()));
			}	
		};
		
        enterTimer = new Timer (750, timerAction);
        enterTimer.setRepeats (false);
        insideTimer = new Timer (12000, timerAction);
        insideTimer.setRepeats (false);
 		//setBorder (BorderFactory.createEtchedBorder (EtchedBorder.RAISED));
	}

    protected void addDefaultRenderer (final Class<?> klass, final MouseTipBaseRenderer renderer) {
        defaultRenderer = renderer;
        addRenderer (klass, renderer);
    }

    public void addRenderer (final Class<?> klass, final MouseTipBaseRenderer renderer) {
        rendererTable.put (klass, renderer);
    }

    public void removeRenderer (final Class<?> klass) {
        rendererTable.remove (klass);
    }

    public MouseTipBaseRenderer getRenderer (final Class<?> klass) {
    	MouseTipBaseRenderer renderer = rendererTable.get (klass);
        if (renderer == null) {
        	final Class<?>[] iFaces = klass.getInterfaces();
        	if (iFaces != null && iFaces.length > 0) {
	        	int iIndex = iFaces.length;
	        	do {
	        		renderer = rendererTable.get (iFaces [--iIndex]);
	        		LOGGER.debug (iFaces[iIndex].toString());
	        	} while (iIndex > 0 && renderer != null);
	        }
        }
        if (renderer == null) {
        	renderer = defaultRenderer;
        }
        return renderer;
    }


	/**
	* Calculates the new mousetip layout and position.<P>
	* @param context graphics context the tip will eventually be drawn to
	* @param text text to be displayed in mouse tip
	* @param panelBounds dimensions of display panel that mouse tip will be drawn to
	* @param newX left hand x position of new mousetip position
	* @param newY top y position of new mousetip position
	*/
    public void calculateNewMouseTip (final Object obj) {
    	
    	if (obj != null) {
        	currentRenderer = getRenderer (obj.getClass());
        	if (getGraphics() != null && obj != lastObject) {
        		setBackground (currentRenderer.getBackground());
        		setText (currentRenderer.format (obj));
        		this.validate();
        		setSize (getPreferredSize());
                enterTimer.restart();
                insideTimer.restart();
			}
		} else {
			enterTimer.stop();
            insideTimer.stop();
			setVisible (false);
		}
    	lastObject = obj;
	}

	@Override
    public void setLocation (final int x, final int y) {

        final Dimension panelBounds = getParent() != null ? getParent().getSize() : noParentDims;
        final Dimension tsize = getSize();
     	final int actualMouseInfoXOffset = (x + MOUSEINFOXOFFSET + tsize.width < panelBounds.width)
			? MOUSEINFOXOFFSET : -MOUSEINFOXOFFSET - tsize.width;
		final int actualMouseInfoYOffset = (y + MOUSEINFOYOFFSET + tsize.height < panelBounds.height)
			? MOUSEINFOYOFFSET : -MOUSEINFOYOFFSET - tsize.height;
		super.setLocation (Math.max (0, x + actualMouseInfoXOffset), Math.max (0, y + actualMouseInfoYOffset));
    }
	
	@Override
	public void paintComponent (final Graphics graphics) {
		graphics.setColor (getBackground());
		graphics.fillRect (0, 0, this.getWidth(), this.getHeight());
		super.paintComponent (graphics);
	}
}
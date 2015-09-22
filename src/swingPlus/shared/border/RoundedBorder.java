package swingPlus.shared.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;

// By niceguy1
// http://forums.sun.com/thread.jspa?forumID=57&threadID=5167255

import javax.swing.border.AbstractBorder;

import util.GraphicsUtil;


public class RoundedBorder extends AbstractBorder {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5632427841460604113L;
    transient final private Color color;
    transient private Stroke stroke;
    
    Path2D path;
    Arc2D arc;
    
    public RoundedBorder (final Color color) {
    	super ();
        this.color = color;
        arc = new Arc2D.Double ();
        path = new Path2D.Double ();
    }
    
    public RoundedBorder (final Color color, final Stroke stroke) {
    	this (color);
    	this.stroke = stroke;
    }
	
	@Override
	public Insets getBorderInsets (final Component comp) {
		return new Insets (1, 4, 1, 4);
	}
	
    /** 
     * Reinitialize the insets parameter with this Border's current Insets. 
     * @param c the component for which this border insets value applies
     * @param insets the object to be reinitialized
     */
    public Insets getBorderInsets (final Component comp, final Insets insets) {
        insets.left = insets.right = 4;
        insets.top = insets.bottom = 1;
        return insets;
    }

	@Override
	public boolean isBorderOpaque() {
		return true;
	}

	@Override
	public void paintBorder (final Component comp, final Graphics graphics, final int x, final int y, 
			final int width, final int height) {
		final Graphics2D g2d = (Graphics2D)graphics;
		final Stroke oldStroke = g2d.getStroke();
		g2d.setStroke (stroke == null ? oldStroke : stroke);
		
		graphics.setColor (color);
		if (height == width) {
			graphics.drawOval (0, 0, width, height);
			g2d.setStroke (oldStroke);
			return;
		}
		
		GraphicsUtil.makeRoundedBox (path, arc, height, width);
		
		g2d.draw (path);
		g2d.setStroke (oldStroke);
	}

}

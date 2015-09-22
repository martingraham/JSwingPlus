package swingPlus.shared.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import sun.swing.SwingUtilities2;

public class OrthogonalTitlesBorder extends TitledBorder {

	final private Point textLoc = new Point(); // copy of same field in TitledBorder
	final private AffineTransform aTrans = new AffineTransform ();
	/**
	 * 
	 */
	private static final long serialVersionUID = 5425983518626186388L;
	
	protected String title2;
	
	
	public OrthogonalTitlesBorder (final Border border) {
		super (border);
	}
	
	public OrthogonalTitlesBorder (final Border border, final String title) {
		super (border, title);
	}
	
	public void setSecondTitle (final String newTitle2) {
		title2 = newTitle2;
	}

	public String getSecondTitle () { return title2; }
	


	/**
     * Returns the insets of the border.
     * @param c the component for which this border insets value applies
     */
    @Override
	public Insets getBorderInsets (final Component comp) {
        return getBorderInsets (comp, new Insets(0, 0, 0, 0));
    }

    /** 
     * Reinitialize the insets parameter with this Border's current Insets. 
     * @param comp the component for which this border insets value applies
     * @param insets the object to be reinitialized
     */
    @Override
	public Insets getBorderInsets (final Component comp, final Insets insets) {
    	Insets borderInsets = super.getBorderInsets (comp, insets);
    	
        int descent = 0;
        int ascent = 16;

        final FontMetrics fontMetrics = comp.getFontMetrics (getFont (comp));

		if (fontMetrics != null) {
	  	   descent = fontMetrics.getDescent();
		   ascent = fontMetrics.getAscent();
		}
		borderInsets.left += ascent + descent;
		
    	return borderInsets;
    }
	
    
    /**
     * I had thought I could blank out the border line using a transparent colour
     * but this actually removes the detail from the swing back buffer, including
     * the parent component detail that's already been painted so I just got a
     * big black space.
     * Instead I've cut'n'pasted the paintBorder routine from TitledBorder and done
     * the necessary twiddling to it.
     * 
     * Paints the border for the specified component with the 
     * specified position and size.
     * @param comp the component for which this border inputStream being painted
     * @param graphics the paint graphics
     * @param x the x position of the painted border
     * @param y the y position of the painted border
     * @param width the width of the painted border
     * @param height the height of the painted border
     */
	@Override
    public void paintBorder (final Component comp, final Graphics graphics, 
    		final int x, final int y, final int width, final int height) {

		final Border border = getBorder();

        if (getTitle() == null || getTitle().equals("")) {
            if (border != null) {
                border.paintBorder(comp, graphics, x, y, width, height);
            }
            return;
        }

        final Graphics2D g2d = (Graphics2D)graphics;
        
        Rectangle grooveRect = new Rectangle(x + EDGE_SPACING, y + EDGE_SPACING,
                                             width - (EDGE_SPACING * 2),
                                             height - (EDGE_SPACING * 2));
        final Font font = graphics.getFont();
        final Color color = graphics.getColor();

        graphics.setFont(getFont(comp));

        final JComponent jComp = (comp instanceof JComponent) ? (JComponent)comp : null;
        final FontMetrics fontMetrics = SwingUtilities2.getFontMetrics(jComp, graphics);
        final int         fontHeight = fontMetrics.getHeight();
        final int         descent = fontMetrics.getDescent();
        final int         ascent = fontMetrics.getAscent();
        int         diff;
        final int         stringWidth = SwingUtilities2.stringWidth(jComp, fontMetrics,
                                                              getTitle());
        Insets      insets;

        if (border == null) {
        	 insets = new Insets(0, 0, 0, 0);
        } else {
        	 insets = border.getBorderInsets(comp);
        }

        final int titlePos = getTitlePosition();
        switch (titlePos) {
            case ABOVE_TOP:
                diff = ascent + descent + (Math.max(EDGE_SPACING,
                                 TEXT_SPACING*2) - EDGE_SPACING);
                grooveRect.y += diff;
                grooveRect.height -= diff;
                textLoc.y = grooveRect.y - (descent + TEXT_SPACING);
                break;
            case TOP:
            case DEFAULT_POSITION:
                diff = Math.max(0, ((ascent/2) + TEXT_SPACING) - EDGE_SPACING);
                grooveRect.y += diff;
                grooveRect.height -= diff;
                textLoc.y = (grooveRect.y - descent) +
                (insets.top + ascent + descent)/2;
                break;
            case BELOW_TOP:
                textLoc.y = grooveRect.y + insets.top + ascent + TEXT_SPACING;
                break;
            case ABOVE_BOTTOM:
                textLoc.y = (grooveRect.y + grooveRect.height) -
                (insets.bottom + descent + TEXT_SPACING);
                break;
            case BOTTOM:
                grooveRect.height -= fontHeight/2;
                textLoc.y = ((grooveRect.y + grooveRect.height) - descent) +
                        ((ascent + descent) - insets.bottom)/2;
                break;
            case BELOW_BOTTOM:
                grooveRect.height -= fontHeight;
                textLoc.y = grooveRect.y + grooveRect.height + ascent +
                        TEXT_SPACING;
                break;
            default:
            	break;
        }

		int justification = getTitleJustification();
		final boolean l2rOriented = (comp.getComponentOrientation() == ComponentOrientation.LEFT_TO_RIGHT);
		if (justification == LEADING || justification == TRAILING || justification == DEFAULT_JUSTIFICATION) {
			justification = (l2rOriented ^ (justification == TRAILING)) ? LEFT : RIGHT;
		}

        switch (justification) {
            case LEFT:
                textLoc.x = grooveRect.x + TEXT_INSET_H + insets.left;
                break;
            case RIGHT:
                textLoc.x = (grooveRect.x + grooveRect.width) -
                        (stringWidth + TEXT_INSET_H + insets.right);
                break;
            case CENTER:
                textLoc.x = grooveRect.x +
                        ((grooveRect.width - stringWidth) / 2);
                break;
            default:
            	break;
        }

        // If title inputStream positioned in middle of border AND its fontsize
        // inputStream greater than the border's thickness, we'll need to paint 
        // the border in sections to leave space for the component's background 
        // to show through the title.
        //
        if (border != null) {
        	final Rectangle saveClip = graphics.getClipBounds();   	
        	final Rectangle2D titleRect1 = fontMetrics.getStringBounds (getTitle(), graphics);
        	titleRect1.setRect (textLoc.x, textLoc.y - ascent, titleRect1.getWidth(), titleRect1.getHeight());

        	final Rectangle2D titleRect2 = getSecondTitle() == null ? 
    				new Rectangle2D.Double (0, 0, 0, 0) : fontMetrics.getStringBounds (getSecondTitle(), graphics);   
    		aTrans.setToIdentity();
    	    aTrans.translate (fontMetrics.getAscent(), height - insets.bottom + 1);
    	    aTrans.quadrantRotate (3);  	    
    	    final Shape rotatedShape = aTrans.createTransformedShape (titleRect2);
            
    	    final GeneralPath path = new GeneralPath (PathIterator.WIND_EVEN_ODD);
            path.append (saveClip, false);
            path.append (rotatedShape, false);
            path.append (titleRect1, false);
           
            ((Graphics2D)graphics).clip (path);
            
            border.paintBorder(comp, graphics, grooveRect.x, grooveRect.y,
                    grooveRect.width, grooveRect.height);
            
        	graphics.setClip (saveClip);
        }
        
        graphics.setColor (getTitleColor());
        graphics.drawString (getTitle(), textLoc.x, textLoc.y);
      
        if (getSecondTitle() != null && !getSecondTitle().isEmpty()) {
        	final AffineTransform tempTrans = g2d.getTransform();
        	g2d.transform (aTrans);
			graphics.drawString (getSecondTitle(), 0, 0);
	        g2d.setTransform (tempTrans);
        }
        
        graphics.setFont(font);
        graphics.setColor(color);
    }
}

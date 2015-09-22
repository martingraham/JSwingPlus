/**
 * http://www.codeguru.com/java/articles/199.shtml
 * Author: Zafir Anjum
 * MG added update to account for html text labels
 */
package util.ui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.text.View;



public class VerticalLabelUI extends BasicLabelUI {
	
	static {
		labelUI = new VerticalLabelUI(false);
	}
	
	protected boolean clockwise;
	protected boolean alwaysVertical;
	
	
	public VerticalLabelUI (final boolean clockwise) {
		this (clockwise, false);
	}
	
	public VerticalLabelUI (final boolean clockwise, final boolean alwaysVertical) {
		super();
		this.clockwise = clockwise;
		this.alwaysVertical = alwaysVertical;
	}
	

    public Dimension getPreferredSize (final JComponent comp) {
    	final Dimension dim = super.getPreferredSize(comp);
    	return new Dimension( dim.height, dim.width );
    }	

    private static Rectangle paintIconR = new Rectangle();
    private static Rectangle paintTextR = new Rectangle();
    private static Rectangle paintViewR = new Rectangle();
    private static Insets paintViewInsets = new Insets(0, 0, 0, 0);

	public void paint (final Graphics graphics, final JComponent comp) {
		
		final JLabel label = (JLabel)comp;
		
		if (label.getWidth() > label.getHeight() && !alwaysVertical) {
			super.paint (graphics, comp);
		}
		else {
			final String text = label.getText();
			final Icon icon = (label.isEnabled()) ? label.getIcon() : label.getDisabledIcon();
	
	        if ((icon == null) && (text == null)) {
	            return;
	        }
	
	        final FontMetrics fontMetrics = graphics.getFontMetrics();
	        paintViewInsets = comp.getInsets(paintViewInsets);
	
	        paintViewR.x = paintViewInsets.left;
	        paintViewR.y = paintViewInsets.top;
	    	
	    	// Use inverted height & width
	        paintViewR.height = comp.getWidth() - (paintViewInsets.left + paintViewInsets.right);
	        paintViewR.width = comp.getHeight() - (paintViewInsets.top + paintViewInsets.bottom);
	
	        paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
	        paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;
	
	        final String clippedText = 
	            layoutCL (label, fontMetrics, text, icon, paintViewR, paintIconR, paintTextR);
	
	        final Graphics2D g2D = (Graphics2D) graphics;
	        final AffineTransform oldTransform = g2D.getTransform();
	    	if( clockwise )
	    	{
		    	g2D.rotate( Math.PI / 2 ); 
	    		g2D.translate( 0, - comp.getWidth() );
	    	}
	    	else
	    	{
		    	g2D.rotate( - Math.PI / 2 ); 
	    		g2D.translate( - comp.getHeight(), 0 );
	    	}
	
	    	if (icon != null) {
	            icon.paintIcon(comp, graphics, paintIconR.x, paintIconR.y);
	        }
	
	        if (text != null) {
	        	final View view = (View) comp.getClientProperty(BasicHTML.propertyKey);
	        	
	    	    if (view == null) {
	    	    	final int textX = paintTextR.x;
	    	    	final int textY = paintTextR.y + fontMetrics.getAscent();
		
		            if (label.isEnabled()) {
		                paintEnabledText(label, graphics, clippedText, textX, textY);
		            }
		            else {
		                paintDisabledText(label, graphics, clippedText, textX, textY);
		            }
	    	    } else {
	    	    	view.paint(graphics, paintTextR);
	    	    }
	        }
	    	
	    	
	    	g2D.setTransform( oldTransform );
		}
    }
}

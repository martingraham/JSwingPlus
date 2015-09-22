package example.histogram.renderers;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

import javax.swing.SwingConstants;

import swingPlus.histogram.AbstractTwoToneBarRenderer;
import swingPlus.histogram.JHistogram;
import util.GraphicsUtil;

public class GradientBarRenderer extends AbstractTwoToneBarRenderer {

	
	public GradientBarRenderer () {
		super ();
		this.setFirstColour (Color.green);
	}
	
	@Override
	public void drawBar (final JHistogram histo, final Graphics graphics, final int x, final int y,
			final int width, final int height, final int dataValue, final double dataValueCount) {
    	graphics.setColor (getBarColour (histo, dataValue));
    	GradientPaint gp;
    	if (histo.getOrientation() == SwingConstants.HORIZONTAL) {
    		gp = new GradientPaint (x, y, getBarColour (histo, dataValue), x, y + height, GraphicsUtil.NULLCOLOUR);
    	} else {
    		gp = new GradientPaint (x, y, getBarColour (histo, dataValue), x + width, y, GraphicsUtil.NULLCOLOUR);
    	}
    	Graphics2D g2D = (Graphics2D)graphics;
    	Paint oldPaint = g2D.getPaint();
    	g2D.setPaint (gp);
    	g2D.fillRect (x, y, width, height);
    	
    	g2D.setPaint (oldPaint);
	}
 
    protected Color getBarColour (final JHistogram histogram, final int dataValue) {
    	return dataValue < histogram.getValue() ? secondColour : firstColour;
    }
    
    public String toString () {
    	return ("Gradient Bar Renderer");
    }
}

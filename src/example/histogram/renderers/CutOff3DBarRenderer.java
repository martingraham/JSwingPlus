package example.histogram.renderers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

import swingPlus.histogram.AbstractTwoToneBarRenderer;
import swingPlus.histogram.JHistogram;

public class CutOff3DBarRenderer extends AbstractTwoToneBarRenderer {

	Polygon top, side;
	
	public CutOff3DBarRenderer () {
		super ();
		top = new Polygon ();
		side = new Polygon ();
		this.setFirstColour (Color.ORANGE);
	}
	
	@Override
	public void drawBar (final JHistogram histo, final Graphics graphics, final int x, final int y,
			final int width, final int height, final int dataValue, final double dataValueCount) {
    	graphics.setColor (getBarColour (histo, dataValue));
    	graphics.fill3DRect (x, y, width, height, true);
    	
    	Graphics2D g2D = (Graphics2D)graphics;
    	
    	graphics.setColor (getBarColour (histo, dataValue).brighter());
    	top.npoints = 0;
    	top.addPoint (x, y);
    	top.addPoint (x + 5, y - 5);
    	top.addPoint (x + 5 + width, y - 5);
    	top.addPoint (x + width, y);
    	g2D.fill(top);
    	
    	graphics.setColor (getBarColour (histo, dataValue).darker());
    	side.npoints = 0;
    	side.addPoint (x + width, y);
    	side.addPoint (x + width + 5, y - 5);
    	side.addPoint (x + width + 5, y + height);
    	side.addPoint (x + width, y + height);
    	g2D.fill(side);
    	
	}
 
    protected Color getBarColour (final JHistogram histogram, final int dataValue) {
    	return dataValue < histogram.getValue() ? secondColour : firstColour;
    }
    
    public String toString () {
    	return ("3D Cut-off Bar Renderer");
    }
}

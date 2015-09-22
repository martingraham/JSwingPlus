package example.histogram.renderers;

import java.awt.Color;
import java.awt.Graphics;

import swingPlus.histogram.AbstractTwoToneBarRenderer;
import swingPlus.histogram.JHistogram;

public class CutOffBarRenderer extends AbstractTwoToneBarRenderer {

	
	@Override
	public void drawBar (final JHistogram histo, final Graphics graphics, final int x, final int y,
			final int width, final int height, final int dataValue, final double dataValueCount) {
    	graphics.setColor (getBarColour (histo, dataValue));
    	graphics.fill3DRect (x, y, width, height, true);
	}
 
    protected Color getBarColour (final JHistogram histogram, final int dataValue) {
    	return dataValue < histogram.getValue() ? secondColour : firstColour;
    }
    
    public String toString () {
    	return ("Cut-off Bar Renderer");
    }
}

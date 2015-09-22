package swingPlus.histogram;

import java.awt.Color;
import java.awt.Graphics;

public abstract class AbstractTwoToneBarRenderer implements BarRenderer {

	
	protected Color firstColour = Color.red;
	protected Color secondColour = Color.gray;
	
	@Override
	abstract public void drawBar (final JHistogram histo, final Graphics graphics, final int x, final int y,
			final int width, final int height, final int dataValue, final double dataValueCount);
	
    abstract protected Color getBarColour (final JHistogram histogram, final int dataValue);
    
	public void setFirstColour (final Color newColour) {
		firstColour = newColour;
	}
	
	public void setSecondColour (final Color newColour) {
		secondColour = newColour;
	}
}

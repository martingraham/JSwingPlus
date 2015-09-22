package swingPlus.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import util.colour.ColourArray;
import util.colour.RGBNonLinear;

public class DefaultGraphEdgeRenderer extends AbstractGraphEdgeRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3309916289376155650L;
	
 	static final private ColourArray SPECTRA = new ColourArray (new RGBNonLinear()) ;
 	static final private Stroke STROKE = new BasicStroke (2.0f);
 	
	public DefaultGraphEdgeRenderer () {
		super ();
		setBorder (null);
		setBackground (Color.blue.brighter());
		setForeground (Color.black);
		//setFont (Font.decode ("Arial-plain-11"));
	}
	
	
    public Color getSpectra (final double value) {
	     return SPECTRA.getColour ((int)((5.0 - value) * 40.0));
    }

	
	@Override
	public void paintComponent (final Graphics graphics) {
		if (obj != null) {
			translateToOrigin (graphics);
			((Graphics2D)graphics).setStroke (STROKE);
			graphics.setColor (Color.blue);
			final int diffx = toX - fromX;
			final int diffy = toY - fromY;
			graphics.drawLine (fromX, fromY, fromX + (diffx / 2), fromY + (diffy / 2));
			graphics.setColor (Color.cyan);
			graphics.drawLine (fromX + (diffx / 2), fromY + (diffy / 2), toX, toY);

		}
	}
}

package example.graph.renderers.edge;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import model.graph.Edge;

import swingPlus.graph.AbstractGraphEdgeRenderer;
import swingPlus.graph.JGraph;
import util.colour.ColourArray;
import util.colour.RGBNonLinear;

public class MultiTreeGraphEdgeRenderer extends AbstractGraphEdgeRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3309916289376155650L;
	
 	static final private ColourArray SPECTRA = new ColourArray (new RGBNonLinear()) ;
 	static {
 		SPECTRA.grey (0.6f);
 	}
 	static final private ColourArray SELECTED_SPECTRA = new ColourArray (new RGBNonLinear()) ;
 	static final private Stroke STROKE = new BasicStroke (1.0f);
 	static final private Stroke SELECTED_STROKE = new BasicStroke (2.0f);
 	protected transient int noOfColours;
 	boolean selected;
 	
	public MultiTreeGraphEdgeRenderer (final int noOfColours) {
		super ();
		//setBorder (null);
		setBackground (Color.blue.brighter());
		setForeground (Color.black);
		this.noOfColours = noOfColours;
		//setFont (Font.decode ("Arial-plain-11"));
	}

	
	public Component getGraphEdgeRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus,
			final int x1, final int y1, final int x2, final int y2) {
		final Component comp = super.getGraphEdgeRendererComponent (graph, value, 
				isSelected, hasFocus, x1, y1, x2, y2);
		selected = isSelected;
		return comp;
	}
	
	
	@Override
	public void paintComponent (final Graphics graphics) {
		if (obj != null) {
			translateToOrigin (graphics);
			((Graphics2D)graphics).setStroke (selected ? SELECTED_STROKE : STROKE);
			final Integer intObj = (Integer)((Edge)obj).getEdgeObject();
			final int colValue = intObj.intValue();
			graphics.setColor (getColourAtIndex (colValue));
			graphics.drawLine (fromX + colValue - 4, fromY + colValue - 4, 
					toX + colValue - 4, toY + colValue - 4);
			translateBack (graphics);
			//graphics.translate ((this.getX() + insets.left), (this.getY() + insets.top));
		}
	}
	
	
	public Color getColourAtIndex (final int index) {
		return (selected ? SELECTED_SPECTRA : SPECTRA).getColour (255 / noOfColours * index);
	}
}

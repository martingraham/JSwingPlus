package example.graph.renderers.edge;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import swingPlus.graph.AbstractGraphEdgeRenderer;
import swingPlus.graph.JGraph;
import util.colour.ColorUtilities;

public class FatEdgeRenderer extends AbstractGraphEdgeRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3309916289376155650L;
	
 	static final private Stroke STROKE = new BasicStroke (3.0f);
 	static final private Color LINE_COLOUR = ColorUtilities.addAlpha (Color.blue, 48);
 	boolean selected;
 	
	public FatEdgeRenderer () {
		super ();
		//setBorder (null);
		setBackground (Color.blue.brighter());
		setForeground (Color.black);
		//setFont (Font.decode ("Arial-plain-11"));
	}

	
	@Override
	public Component getGraphEdgeRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus,
			final int x1, final int y1, final int x2, final int y2) {
		super.getGraphEdgeRendererComponent (graph, value, isSelected, hasFocus, x1, y1, x2, y2);
		selected = isSelected;
		return this;
	}
	
	@Override
	public void paintComponent (final Graphics graphics) {
		if (obj != null) {
			translateToOrigin (graphics);
			((Graphics2D)graphics).setStroke (STROKE);
			graphics.setColor (LINE_COLOUR);
			graphics.drawLine (fromX, fromY, 
					toX, toY);
			translateBack (graphics);
			//graphics.translate ((this.getX() + insets.left), (this.getY() + insets.top));
		}
	}
}
